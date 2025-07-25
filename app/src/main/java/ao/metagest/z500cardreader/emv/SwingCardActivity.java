package ao.metagest.z500cardreader.emv;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciontek.hardware.aidl.AidlConstantsV2;
import com.ciontek.hardware.aidl.bean.CardInfoV2;
import com.ciontek.hardware.aidl.bean.EMVCandidateV2;
import com.ciontek.hardware.aidl.emv.EMVListenerV2;
import com.ciontek.hardware.aidl.emv.EMVOptV2;
import com.ciontek.hardware.aidl.readcard.CheckCardCallbackV2;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


import pos.paylib.posPayKernel;
import ao.metagest.z500cardreader.BaseApp;
import ao.metagest.z500cardreader.MainActivity;
import ao.metagest.z500cardreader.utils.BaseActivity;
import ao.metagest.z500cardreader.R;
import ao.metagest.z500cardreader.constants.UpdateDataConstant;
import ao.metagest.z500cardreader.BuildConfig;
import ao.metagest.z500cardreader.pinkeypad.PinpadCustomLayoutActivity;
import ao.metagest.z500cardreader.utils.MoneyUtils;
import ao.metagest.z500cardreader.utils.PreferencesUtil;
import ao.metagest.z500cardreader.utils.Utils;
//import ao.metagest.z500cardreader.util.TLV;
import ao.metagest.z500cardreader.utils.tlv.TLVUtils;
import ao.metagest.z500cardreader.utils.tlv.TLV;
import ao.metagest.z500cardreader.utils.db.BlackListDaoUtil;
import ao.metagest.z500cardreader.view.CheckReferralSelect;


//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.BaseActivity;
//import test.metagestdemo.activity.BuildConfig;
//import test.metagestdemo.activity.R;
//import test.metagestdemo.activity.constants.UpdateDataConstant;
//import test.metagestdemo.activity.pinkeypad.PinpadCustomLayoutActivity;
//import test.metagestdemo.activity.utils.MoneyUtils;
//import test.metagestdemo.activity.utils.PreferencesUtil;
//import test.metagestdemo.activity.utils.Utils;
//import test.metagestdemo.activity.utils.db.BlackListDaoUtil;
//import test.metagestdemo.activity.utils.tlv.TLV;
//import test.metagestdemo.activity.utils.tlv.TLVUtils;
//import test.metagestdemo.activity.view.CheckReferralSelect;

/**
 * 消费模块的刷卡页面
 * When consuming, swing card
 */
public class SwingCardActivity extends BaseActivity {
    private static final String TAG = "SwingCardActivity";

    private RelativeLayout back_rel;
    private TextView receivables_value;

    private com.ciontek.hardware.aidl.bean.TransDataV2 TransData;

    private String keyRandom;
    private int cardType;
    private String cardNo;
    private String track2;
    private String amount;
    private String mCardNo;
    private ProgressDialog mLoadDialog;
    private int pinPadType = 1;
    private byte[] pinBlock;
    private int pinType;//same as mpinType
    private EMVOptV2 mEmvOpt= BaseApp.Companion.getEmvOpt(); // MyApplication.app.emvOpt;

    private CardInfoV2 mCardInfo;
    private int mPinType;
    private int mRemainTime;
    private int onlineResCode;
    private int errorCode;
    private boolean isOnlineReq = false;
    private boolean checkCardSuc = false;
    public static int downGrade = 0;
    public static int fallback = 0;
    public static int mag = 0;
    public static int flag_advice;

    private String DispMessage ="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置该Activity屏幕独占，需要放在setContentView前面
        posPayKernel.screenMonopoly(getWindow());
        setContentView(R.layout.activity_swingcard);
        if(BuildConfig.FLAVOR.contains("CM30")){
            ImageView swipcardView = findViewById(R.id.swip_card_img);
            swipcardView.setVisibility(ImageView.INVISIBLE);
        }
        initData();

        back_rel = findViewById(R.id.back_rel);
        back_rel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SwingCardActivity.this.handleBackClick();
                    }
                }
        );

        receivables_value = findViewById(R.id.receivables_value);
        amount = getIntent().getStringExtra("amount");//string - has point '.'
        String currencySymbol = " ";
        if(PreferencesUtil.getCurrencyCode().equals("0156")){
            currencySymbol = "￥ ";
        }
        else if(PreferencesUtil.getCurrencyCode().equals("0840")){
            currencySymbol = "$ ";
        }
        String amount1 =  getString(R.string.auth_amount) + currencySymbol + MoneyUtils.longCent2DoubleMoneyStr(MoneyUtils.stringMoney2LongCent(amount)/100);
        if(PreferencesUtil.getFunSetTransactionType().equals("09")){
            amount1 = amount1 + "\n" + getString(R.string.cash_back_amount) + currencySymbol + MoneyUtils.longCent2DoubleMoneyStr(getIntent().getLongExtra("amountOther", 0));
        }
        receivables_value.setText(amount1);

        mCardInfo = new CardInfoV2();
        try {
            BaseApp.Companion.getEmvOpt().abortTransactProcess();
            if (initTransData()) {//init ok
                swingCard();
            } else {
                //REVER showToast(getString(R.string.init_emv_fail));
                Log.e("dd-",getString(R.string.init_emv_fail));
                finish();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onBackPressed() { // 取消检卡 cancel check card
        handleBackClick();
        finish();
    }

    private boolean initTransData() {
        try {
            //int Emv Process
            int code = Objects.requireNonNull(BaseApp.Companion.getEmvOpt()).initEmvProcess();
            if (code != 0) {
                Log.e(TAG, "initEmvProcess errorCode:" + code);
                return false;
            }

            //Set EP flag
            Bundle bundle = new Bundle();
            bundle.putBoolean("supportEP", true);
            BaseApp.Companion.getEmvOpt().setTermParamEx(bundle);

            //set tlv list
            long count = PreferencesUtil.getAccumulatedCount();
            String[] tags = {"9C", "9F41", "9F02", "81"};
            String[] values = {
                    PreferencesUtil.getFunSetTransactionType(),
                    String.format(Locale.getDefault(), "%012d", count),
                    String.format(Locale.getDefault(), "%012d", getIntent().getLongExtra("amountAuth", 0)),
                    String.format(Locale.getDefault(), "%08x", getIntent().getLongExtra("amountAuth", 0)),
            };
            BaseApp.Companion.getEmvOpt().setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, values);

            if (PreferencesUtil.getFunSetTransactionType().equals("09")) {
                String[] tags1 = {"9F03", "9F04"};
                String[] values1 = {
                        String.format(Locale.getDefault(), "%012d", getIntent().getLongExtra("amountOther", 0)),
                        String.format(Locale.getDefault(), "%08x", getIntent().getLongExtra("amountOther", 0)),
                };
                BaseApp.Companion.getEmvOpt().setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags1, values1);
            }

            ++count;
            if (count > 99999999) {
                count = 0;// 清0,set to 0
            }
            PreferencesUtil.setAccumulatedCount(count);

            //MC and AE TLV list setting
            PPS_MChip1();
            BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_AE, "9F6D", "C8"); //Amex Contactless Reader Capabilities - Tag ‘9F6D’,  Value 'C8'
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void PPS_MChip1()
    {
        Log.e(TAG, "PPS_MChip1");
        byte[] out = new byte[256];
        try {
            Log.e(TAG,"PPS_MChip1");
            //not present
            String[] tag_not_present = {
                    "5F57",
                    "DF8104",
                    "DF8105",
                    "9F5C",
                    "DF8130",
                    "DF812D",
                    "DF8110",
                    "DF8112",
                    "FF8102",
                    "FF8103",
                    "DF8127"
            };
            BaseApp.Companion.getEmvOpt().setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_SET_TAG_NOT_PRESENT, tag_not_present, null);
            //empty
            String[] tag_empty = {
                    "9F01",
                    "9F16",
                    "DF8108",
                    "DF60",
                    "DF8109",
                    "DF62",
                    "DF810A",
                    "DF63",
                    "DF810D",
                    "9F4E",
                    "9F7E",
                    "9F33",
                    "9F1C"
            };
            String[] value_empty = {"","","","","","","","","","","","",""};
            BaseApp.Companion.getEmvOpt().setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_SET_TAG_EMPTY, tag_empty, value_empty);

            String tags_1[]={
                    "9F09",
                    "9F40",
                    "DF8117",
                    "DF8118",
                    "DF8119",
                    "DF811A",
                    "9F1E",
                    "DF810C",
                    "9F6D",
                    "DF811C",
                    "DF811E",
                    "DF812C",
                    "9F1D"
            };
            String values_1[]={
                    "0002", //"9F09"
                    "0000000000", //9F40
                    "00",//DF8117
                    "60",//DF8118
                    "08", //DF8119
                    "9F6A04",//DF811A
                    "1122334455667788",//9F1E
                    "02",//DF810C
                    "0001",//9F6D
                    "0000",//DF811C
                    "10",//DF811E
                    "00",//DF812C
                    "6CFF000000000000"//9F1D
            };

            BaseApp.Companion.getEmvOpt().setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tags_1, values_1);
            String tags[] = {
                    "DF811D",
                    "9F15",
                    "DF8123",
                    "DF8124",
                    "DF8125",
                    "DF811F",
                    "DF8120",
                    "DF8121",
                    "DF8122",
                    "9F1A",
                    "DF811B",
                    "DF8126"
            };
            String values[] = {
                    "00",//DF811D
                    "0001",//9F15
                    "000000010000",//DF8123
                    "000000030000",//DF8124
                    "000000050000",//DF8125
                    "08",//DF811F
                    "0000000000",//DF8120
                    "0000000000",//DF8121
                    "0000000000",//DF8122
                    "0056",//9F1A
                    "20",//DF811B
                    "00000050000"//DF8126
            };
            BaseApp.Companion.getEmvOpt().setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tags, values);
            //BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS,"DF8126","00000050000");


            if(PreferencesUtil.getFunSetTransactionType().equals("01")){
                BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, "9F35", "14");
            }else if(PreferencesUtil.getFunSetTransactionType().equals("17")){
                BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, "9F35", "11");
            }else if(PreferencesUtil.getFunSetTransactionType().equals("00") ||
                    PreferencesUtil.getFunSetTransactionType().equals("09") ||
                    PreferencesUtil.getFunSetTransactionType().equals("20")){
                BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, "9F35", "22");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷卡
     */
    private void swingCard() {
        try {
            mLoadDialog = new ProgressDialog(this);
            mLoadDialog.setMessage(getString(R.string.handling));
            mLoadDialog.setCancelable(false);
            mLoadDialog.setCanceledOnTouchOutside(true);
            mLoadDialog.show();

            int allType = 7;
            if(PreferencesUtil.getFunSetSupportClss().equals("01")) {
                allType = AidlConstantsV2.CardType.MAGNETIC.getValue() | AidlConstantsV2.CardType.IC.getValue() | AidlConstantsV2.CardType.NFC.getValue();//   测试qUICS 开启NFC
            }else {
                allType = AidlConstantsV2.CardType.MAGNETIC.getValue() | AidlConstantsV2.CardType.IC.getValue(); // 20180627 close NFC
            }
            checkCardSuc = false;
            BaseApp.Companion.getReadCardOpt().checkCard(allType, mCheckCardCallback, 60);//time out
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int EMV_APP_SELECT = 1;
    private static final int EMV_FINAL_APP_SELECT = 2;
    private static final int EMV_CONFIRM_CARD_NO = 3;
    private static final int EMV_CERT_VERIFY = 4;
    private static final int EMV_SHOW_PIN_PAD = 5;
    private static final int EMV_ONLINE_PROCESS = 6;
    private static final int EMV_SIGNATURE = 7;
    private static final int MAG_SELECT_EXECUTE_METHOD =8;
    private static final int MAG_CONFIRM_CARD_NO =9;
    private static final int EMV_TRANS_SUCCESS = 888;
    private static final int EMV_TRANS_SUCCESS_OFFLINE_APPROVE = 8881;
    private static final int EMV_TRANS_FAIL = 999;
    private static final int EMV_TRANS_FAIL_OFFLINE_DECLINE = 9992;

    private static final int PIN_CLICK_NUMBER = 50;
    private static final int PIN_CLICK_PIN = 51;
    private static final int PIN_CLICK_CONFIRM = 52;
    private static final int PIN_CLICK_CANCEL = 53;
    private static final int PIN_ERROR = 54;

    private static final int EMV_TRANS_FLOW_PROCESSING = 60;
    private static final int EMV_ONLINE_RESULT = 61;
    private static final int EMV_TRANS_ONERROR = 62;
    private static final int PIN_INIT = 63;
    private static final int MAG_RESULT= 64;
    private int mSelectIndex;
    private AlertDialog mAppSelectDialog;
    private AlertDialog mUserAuthDialog;
    private AlertDialog mSelectReadCardMethodDialog;

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EMV_FINAL_APP_SELECT:
                    importFinalAppSelectStatus(0);
                    break;
                case EMV_APP_SELECT:
                    String[] candiNames = (String[]) msg.obj;
                    mAppSelectDialog = new AlertDialog.Builder(SwingCardActivity.this)
                            .setTitle(R.string.emv_app_select)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            importAppSelect(-1);
                                        }
                                    }
                            )
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            importAppSelect(mSelectIndex);
                                        }
                                    }
                            )
                            .setSingleChoiceItems(candiNames, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mSelectIndex = which;
                                            Log.e(TAG, "singleChoiceItems which:" + which);
                                        }
                                    }
                            ).create();
                    mAppSelectDialog.show();
                    break;
                case EMV_CONFIRM_CARD_NO:
                    importCardNoStatus(0);
                    break;
                case EMV_CERT_VERIFY:
                    String[] obj = (String[]) msg.obj;
                    if (onEMVListener != null) {
                        onEMVListener.onCertVerify(Integer.parseInt(obj[0]), obj[1]);
                    }
                    break;
                case EMV_SHOW_PIN_PAD:
                    boolean pinpass = false;
                    if(pinpass)
                    {
                        try {
                            BaseApp.Companion.getEmvOpt().importPinInputStatus(0, 2);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }else {
                        if (onEMVListener != null) {
                            onEMVListener.onRequestShowPinPad(mPinType, mRemainTime);
                        }
                    }
                    break;
                case EMV_ONLINE_PROCESS:
                    if (onEMVListener != null) {
                        onEMVListener.onProcessEnd();
                    }
                    break;
                case EMV_SIGNATURE:
                    importSignatureStatus(0);
                    break;
                case MAG_SELECT_EXECUTE_METHOD:
                    String PanNumber = (String) msg.obj;
                    String[] cardName = {"Fallback to Magnetic ","Chip card"};
                    mSelectReadCardMethodDialog = new AlertDialog.Builder(SwingCardActivity.this)
                            .setTitle(R.string.fallback_or_not)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //processing
                                            if(mSelectIndex ==0){
                                                UiProcessing(1000);
                                                downGrade = 0;
                                                startTransResultActivity(EMVTransResult.ONLINE_APPROVE);
                                            }else if(mSelectIndex ==1){
                                                //go back to chip card
                                                startOperateCardAgain();
                                            }
                                        }
                                    }
                            ).setSingleChoiceItems(cardName, 0, new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mSelectIndex = which;
                                    Log.e(TAG, "singleChoiceItems which:" + which);
                                }
                            }).create();
                    mSelectReadCardMethodDialog.show();
                    break;
                case MAG_CONFIRM_CARD_NO:
                    String Pan = (String) msg.obj;
                    mSelectReadCardMethodDialog = new AlertDialog.Builder(SwingCardActivity.this)
                            .setTitle(R.string.confirm_card_number)
                            .setMessage(Pan)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //go back to chip card
                                            startTransResultActivity(EMVTransResult.TRANSACTION_TERMINATED);
                                        }
                                    }
                            )
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //processing
                                            UiProcessing(1000);
                                            startTransResultActivity(EMVTransResult.ONLINE_APPROVE);
                                        }
                                    }
                            ).create();
                    mSelectReadCardMethodDialog.show();
                    break;
                case PIN_CLICK_NUMBER:
                    break;
                case PIN_CLICK_PIN:
                    break;
                case PIN_CLICK_CONFIRM:
                    break;
                case PIN_CLICK_CANCEL:
                    showToast("user cancel");
                    break;
                case PIN_ERROR:
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    break;
                case EMV_TRANS_FAIL:
                    resetUI();
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    break;
                case EMV_TRANS_SUCCESS:
                    resetUI();
                    break;
                case EMV_TRANS_SUCCESS_OFFLINE_APPROVE:
                    if (onEMVListener != null) {
                        onEMVListener.offlineApproval();
                    }
                    break;
                case EMV_TRANS_FAIL_OFFLINE_DECLINE:
                    EMVTransResult kernelResult = EMVTransResult.OFFLINE_DECLINE;
                    startTransResultActivity(kernelResult);
                    break;
                case EMV_TRANS_FLOW_PROCESSING:
                    if (mLoadDialog != null) {
                        mLoadDialog.setMessage(getString(R.string.handling));
                    }
                    break;
                case EMV_ONLINE_RESULT:
                    if (onEMVListener != null) {
                        onEMVListener.onTransResultByKernelRetCode(onlineResCode);
                    }
                    break;
                case EMV_TRANS_ONERROR:
                    if (onEMVListener != null && checkCardSuc) {
                        onEMVListener.onError(errorCode);
                    } else {
                        SetonError();
                    }
                    break;
                case PIN_INIT:
                    mPinType = 0;
                    break;
                case MAG_RESULT:
                    break;
            }
        }
    };

    private void resetUI() {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
              SwingCardActivity.this.dismissAppSelectDialog();
            }
            }
        );
    }


    private void SetonError() {
        Log.e(TAG, "--------emv onError,errorCode:" + errorCode);
        EMVTransResult transResult = EMVTransResult.TRANSACTION_TERMINATED;
        if (errorCode == -4000) {
            transResult = EMVTransResult.OFFLINE_DECLINE;
        } else if (errorCode == -4110) {
            transResult = EMVTransResult.NOT_ACCEPTED;
        }else if(errorCode == -4003){
            transResult = EMVTransResult.SEE_PHONE;
        }
        startTransResultActivity(transResult);
    }

    private void dismissAppSelectDialog() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              if (mAppSelectDialog != null) {
                                  try {
                                      mAppSelectDialog.dismiss();
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }
                                  mAppSelectDialog = null;
                              }
                          }
                      }
        );
    }



    private void importPinInputStatus(int inputResult, int pinType) {
        Log.e(TAG, "importPinInputStatus:" + inputResult);
        try {
            BaseApp.Companion.getEmvOpt().importPinInputStatus(pinType, inputResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importSignatureStatus(int status) {
        Log.e(TAG, "importSignatureStatus status:" + status);
        try {
            BaseApp.Companion.getEmvOpt().importSignatureStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importCardNoStatus(int status) {
        Log.e(TAG, "importCardNoStatus status:" + status);
        try {
            BaseApp.Companion.getEmvOpt().importCardNoStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void importFinalAppSelectStatus(int status) {
        try {
            Log.e(TAG, "importFinalAppSelectStatus status:" + status);
            BaseApp.Companion.getEmvOpt().importAppFinalSelectStatus(status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void importAppSelect(int selectIndex) {
        Log.e(TAG, "importAppSelect selectIndex:" + selectIndex);
        try {
            BaseApp.Companion.getEmvOpt().importAppSelect(selectIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getCandidateNames(List<EMVCandidateV2> candiList) {
        if (candiList == null || candiList.size() == 0)
            return new String[0];
        String[] result = new String[candiList.size()];
        for (int i = 0; i < candiList.size(); i++) {
            EMVCandidateV2 candi = candiList.get(i);
            String name = candi.appName;
            name = TextUtils.isEmpty(name) ? candi.appLabel : name;
            name = TextUtils.isEmpty(name) ? candi.appPreName : name;
            name = TextUtils.isEmpty(name) ? "" : name;
            result[i] = name;
            Log.e(TAG, "EMVCandidateV2: " + name);
        }
        return result;
    }


    private final EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {
        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            Log.e(TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            String[] candidateNames = getCandidateNames(appNameList);
            mHandler.obtainMessage(EMV_APP_SELECT, candidateNames).sendToTarget();
        }

        @Override
        public void onAppFinalSelect(String tag9F06value) throws RemoteException {
            Log.e(TAG, "onAppFinalSelect tag9F06value:" + tag9F06value);
            if ("01".equals(PreferencesUtil.getFunSetSupportSM()))
                BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "DF69", "01");
            mHandler.obtainMessage(EMV_FINAL_APP_SELECT, tag9F06value).sendToTarget();
        }

        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            Log.e(TAG, "onConfirmCardNo cardNo:" + cardNo);
            mCardInfo.cardNo = cardNo;
            mLoadDialog.dismiss();
            importBlackListData(mCardInfo.cardNo);
            if (PreferencesUtil.getFunSetSupportClss().equals("01")) {
                showToast(getString(R.string.remove_card));
            } else {
                showToast(getString(R.string.read_card_success));
            }

            String accuPanAmount = PreferencesUtil.getAccumulatedAmount();
            String[] value = accuPanAmount.split("=");
            String accPan = value[0];
            String accuAmount;
            if(cardNo.equals(accPan)){
                accuAmount = value[1];
            }else{
                accuAmount = "000000000000";
            }
            Log.e(TAG, "Set DF70 = " + accuAmount);
            BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "DF70", accuAmount);
            //mHandler.obtainMessage(EMV_SIGNATURE).sendToTarget();
            checkCardSuc = true;
            //检卡成功开始交易处理
            //Card is successfully acquired and transaction processing begins
            transaction(mCardInfo);
        }

        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            Log.e(TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
            mPinType = pinType;
            UpdateDataConstant.mRemainTime = remainTime;
            mHandler.obtainMessage(EMV_SHOW_PIN_PAD).sendToTarget();
        }

        @Override
        public void onRequestSignature() throws RemoteException {
            Log.e(TAG, "onRequestSignature");
            mHandler.obtainMessage(EMV_SIGNATURE).sendToTarget();
        }

        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
            Log.e(TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
            mHandler.obtainMessage(EMV_CERT_VERIFY, new String[]{String.valueOf(certType), certInfo}).sendToTarget();
        }

        @Override
        public void onOnlineProc() throws RemoteException {
            Log.e(TAG, "onOnlineProcess");
            isOnlineReq = true;
            mHandler.obtainMessage(EMV_ONLINE_PROCESS).sendToTarget();
        }

        @Override
        public void onCardDataExchangeComplete() throws RemoteException {
            Log.e(TAG, "onCardDataExchangeComplete");
            mLoadDialog.dismiss();
        }

        @Override
        public void onTransResult(int code, String desc) throws RemoteException {
            Log.e(TAG, "onTransResult code:" + code + " desc:" + desc);
            Log.e(TAG, "***************************************************************");
            Log.e(TAG, "****************************End Process************************");
            Log.e(TAG, "***************************************************************");

            byte[] _9f27 = new byte[8];
            byte[] _df69 = new byte[8];
            byte[] _df817b = new byte[16];
            byte[] _df817a = new byte[16];
            byte[] _5f2d = new byte[16];

            BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "9F27", _9f27);
            BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "DF69", _df69);
            BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "DF817B", _df817b);
            BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "DF817A", _df817a);
            BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "5F2D", _5f2d);

            Log.e(TAG, "9F27:" + Utils.byte2HexStr(_9f27) + " DF69: " + Utils.byte2HexStr(_df69) + " DF817B: " + Utils.byte2HexStr(_df817b)
                    + " DF817A: " + Utils.byte2HexStr(_df817a) + " 5F2D: " + Utils.byte2HexStr(_5f2d));

            mLoadDialog.dismiss();
            BaseApp.Companion.setNewTransFlag(0);
//            new Thread(new Runnable() {
//                int isInslot = -1;
//                int checktick =0;
//                @Override
//                public void run() {
//                    while(true){
//                        try {
//                            if(checktick >=5){
//                                break;
//                            }
//                            if(MyApplication.app.newTransFlag == 1){
//                                break;
//                            }
//                            isInslot = MyApplication.app.syscardOpt.sysIccCheck((byte) 0);
//                            Log.d(TAG,"isInslot: "+ isInslot);
//                            if(isInslot ==0){
//                                checktick = 0;
//                                MyApplication.app.basicOpt.SpBeep();
//                                Thread.sleep(200);
//                            }else{
//                                checktick++;
//                            }
//                        }catch (RemoteException e){
//                            e.printStackTrace();
//                        }catch (InterruptedException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
            if (isOnlineReq) {
                isOnlineReq = false;
                onlineResCode = code;
                mHandler.obtainMessage(EMV_ONLINE_RESULT).sendToTarget();
                return;
            }

            if (code < 0) {
                errorCode = code;
                mHandler.obtainMessage(EMV_TRANS_ONERROR).sendToTarget();
            } else if (code == 1) {
                mHandler.obtainMessage(EMV_TRANS_SUCCESS_OFFLINE_APPROVE).sendToTarget();
            } else if (code == 2) {
                mHandler.obtainMessage(EMV_TRANS_FAIL_OFFLINE_DECLINE).sendToTarget();
            }else{
                mHandler.obtainMessage(EMV_TRANS_ONERROR).sendToTarget();
            }
        }

        @Override
        public void onConfirmationCodeVerified() throws RemoteException {
            BaseApp.Companion.getReadCardOpt().cardOff(mCardInfo.cardType);
            runOnUiThread(() -> new AlertDialog.Builder(SwingCardActivity.this)
                    .setTitle("See Phone")
                    .setMessage("execute See Phone flow")
                    .setPositiveButton("OK", (dia, which) -> {
                                dia.dismiss();
                                try {
                                    BaseApp.Companion.getEmvOpt().initEmvProcess();
                                    swingCard();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    ).show()
            );
        }

        @Override
        public void onRequestDataExchange(String s) throws RemoteException {
            Log.e(TAG, "onRequestDataExchange,cardNo:" + s);
            BaseApp.Companion.getEmvOpt().importDataExchangeStatus(0);
        }

        @Override
        public void onTermRiskManagement() throws RemoteException {
            Log.e(TAG, "onTermRiskManagement");
            BaseApp.Companion.getEmvOpt().importTermRiskManagementStatus(0);
        }

        @Override
        public void onPreFirstGenAC() throws RemoteException {
            Log.e(TAG, "onPreFirstGenAC");
            BaseApp.Companion.getEmvOpt().importPreFirstGenACStatus(0);
        }

        @Override
        public void onDataStorageProc(String[] strings, String[] strings1) throws RemoteException {
            Log.e(TAG, "onDataStorageProc,");
            String[] tags = new String[0];
            String[] values = new String[0];
            BaseApp.Companion.getEmvOpt().importDataStorage(tags, values);
        }

    } ;

    private void transactProcess() {
        Log.e(TAG, "transactProcess");
        try {
            Bundle bundle = new Bundle();
            bundle.putString("amount", String.format(Locale.getDefault(), "%012d", getIntent().getLongExtra("amountAuth", 0)));
            bundle.putString("transType", PreferencesUtil.getFunSetTransactionType());
            if (mCardInfo.cardType == AidlConstantsV2.CardType.NFC.getValue()) {
                bundle.putInt("flowType", AidlConstantsV2.EMV.FlowType.TYPE_NFC_SPEEDUP);
            } else {
                bundle.putInt("flowType", AidlConstantsV2.EMV.FlowType.TYPE_EMV_STANDARD);
            }
            bundle.putInt("cardType", mCardInfo.cardType);
            bundle.putString("cashbackAmount", String.format(Locale.getDefault(), "%012d", getIntent().getLongExtra("amountOther", 0)));
            bundle.putInt("emvAuthLevel", 1);
            bundle.putBoolean("preProcessCompleted", false);
            bundle.putInt("cardPollTimeout", 60);
            BaseApp.Companion.getEmvOpt().transactProcessEx(bundle, mEMVListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 服务代码（SC） 第一位
     * 如果卡片仅含有磁条，不含有芯片，那么服务码仅可以是1或5开头。
     * 如果卡片含有芯片，那么服务码仅可以是2或6开头。
     *
     * Service code（SC） first
     * If the card has only magnetic strips and no chips, the service code can start with a 1 or a 5
     * If the card contains a chip, then the service code can only begin with a 2 or 6
     *
     * @param track2 磁道二的数据  Data for the second track
     */
    static boolean isChipCard(String track2) {
        if (TextUtils.isEmpty(track2)) {
            return false;
        }
        int index = track2.indexOf("=");
        if (index < 0) {
            return false;
        }
        String num = track2.substring(index + 5, index + 6);
        Log.e("lxy","num:"+num);
        return "2".equals(num) || "6".equals(num);
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2.Stub() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            mag = 1;
            Log.e(TAG, "findMagCard:" + bundle);
            mLoadDialog.dismiss();
            if (bundle != null) {
                mCardInfo.track2 = bundle.getString("TRACK2");
            }
            mCardInfo.cardType = AidlConstantsV2.CardType.MAGNETIC.getValue();
            String TRACK1 = bundle.getString("TRACK1");//: 磁道1数据,Data for the first track
            Log.e(TAG, TRACK1);

            mCardInfo.cardType = AidlConstantsV2.CardType.MAGNETIC.getValue();
            String TRACK2 = bundle.getString("TRACK2");//: 磁道2数据,Data for the second track
            Log.e(TAG, TRACK2);
            if(downGrade == 1) {
                if (!TextUtils.isEmpty(TRACK2)) {
                    int index = TRACK2.indexOf("=");
                    if (index != -1) {
                        mCardNo = TRACK2.substring(1, index);
                    }
                }
                if (!TextUtils.isEmpty(mCardNo)) {
                    downGrade = 0;
                    mHandler.obtainMessage(MAG_SELECT_EXECUTE_METHOD, mCardNo).sendToTarget();
                }
            }else{
                Log.e(TAG,"entry downgrade =0 process");
                if(isChipCard(TRACK2)){
                    DispMessage = "Chip card, Please Insert Card";
                    startOperateCardAgain();
                    return ;
                }
                if (!TextUtils.isEmpty(TRACK2)) {
                    int index = TRACK2.indexOf("=");
                    if (index != -1) {
                        mCardNo = TRACK2.substring(1, index);
                    }
                }
                if (!TextUtils.isEmpty(mCardNo)) {
                    mHandler.obtainMessage(MAG_CONFIRM_CARD_NO, mCardNo).sendToTarget();
                }
                downGrade = 0;

            }
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            Log.e(TAG, "findICCard:" + atr);
            mag = 0;
            mLoadDialog.dismiss();
            mHandler.obtainMessage(EMV_TRANS_FLOW_PROCESSING).sendToTarget();
            mCardInfo.cardType = AidlConstantsV2.CardType.IC.getValue();
            transactProcess();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            Log.e(TAG, "findRFCard:" + uuid);
            mag = 0;
            mLoadDialog.dismiss();
            mHandler.obtainMessage(EMV_TRANS_FLOW_PROCESSING).sendToTarget();
            mCardInfo.cardType = AidlConstantsV2.CardType.NFC.getValue();
            transactProcess();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            mLoadDialog.dismiss();
            Log.e(TAG, "错误码:" + code + ",错误信息:" + message);//the error code and message
            if(code == -2820 || code == -2823){
                downGrade = 1;
                DispMessage += "Chip card power on failed, fallback";
                startOperateCardAgain();
                //startTransResultActivity(EMVTransResult.SWIPE_CARD);
            }else if(code == -2002){
                //multiple card for NFC
            }
            finish();
            checkCardFail(code);
        }

        @Override
        public void findICCardEx(Bundle bundle) throws RemoteException {

        }

        @Override
        public void findRFCardEx(Bundle bundle) throws RemoteException {

        }

        @Override
        public void onErrorEx(Bundle bundle) throws RemoteException {

        }

    };

    private void startOperateCardAgain(){
        int ret =0;
        dismissLoadDlg();
        Intent intent = new Intent(this,TransAgainActivity.class);
        intent.putExtra(TransAgainActivity.CardMethod,cardType);
        intent.putExtra(TransAgainActivity.CardOperateResult,DispMessage);
        BaseActivity.Companion.openActivity(this,intent,true);
    }

    private void checkCardFail(int code) {
        if ((code > -4000 || code < -5000) && (code != -900)) { // 不是EMV L2错误，读卡错误，重新读卡 // -900 no record 20180718
            if(code == -2002){
                showToast("mutile card, please remove the card and try again");
                try {
                    BaseApp.Companion.getBasicOpt().SpBeep();
                    Thread.sleep(1000 * 2);
                }catch (RemoteException e){
                    e.printStackTrace();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                BaseActivity.Companion.openActivity(this,InputMoneyActivity.class);
            }else if(BaseApp.Companion.getAutoTest()) { // 自动测试，重新检卡 20180620
                Log.e(TAG, "检卡失败，自动测试重新检卡");//auto test,restart
                BaseActivity.Companion.openActivity(this,InputMoneyActivity.class);
            }
        } else { // 交易结束，提示交易结果
                 // The transaction is over, and the result of the transaction is indicated
            EMVTransResult transResult = EMVTransResult.TRANSACTION_TERMINATED;
            if (code == -4000) {
                transResult = EMVTransResult.OFFLINE_DECLINE;
            } else if (code == -4110) {
                transResult = EMVTransResult.NOT_ACCEPTED;
            }
            startTransResultActivity(transResult);
        }
    }

    private void importBlackListData(String cardNo) {
        // EMV 检测到黑名单 不需要直接取消交易，改为内核置位 20180704
        //EMV  Detection in the blacklist, do not need to directly cancel the transaction, instead the payment kernel for record
        BlackListDaoUtil blackListDaoUtil = new BlackListDaoUtil();
        if ("01".equals(PreferencesUtil.getFunSetSupportExceptFile())) {
            byte[] out = new byte[64];
            try {
                int len = BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "5F34", out);
                if (len > 0) {
                    byte[] bytesOut = Arrays.copyOf(out, len);
                    Map<String, TLV> map = TLVUtils.builderTLVMap(bytesOut);
                    String cardSerialNo = Objects.requireNonNull(map.get("5F34")).getValue();
                    if (blackListDaoUtil.isBlackList(cardNo, cardSerialNo)) {//判断
                        byte[] outBuf = new byte[10];
                        len = BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "95", outBuf);
                        Log.e(TAG, "get Exception file TVR: " + Utils.bytes2HexString(outBuf) + "  " + outBuf[0] + "  " + outBuf[1]);
                        if ((len >= 0) && (outBuf[0] == 0x95 || outBuf[0] == -107) && (outBuf[1] == 0x05)) {
                            outBuf[2] |= 0x10;
                            String tlv = Utils.bytes2HexString(outBuf);
                            if (!TextUtils.isEmpty(tlv) && tlv.length() > 4) {
                                BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "95", tlv.substring(4));
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 调用交易处理
     * Invoking transaction processing
     */
    private void transaction(CardInfoV2 cardInfo) {
        try {
            jump2InputPassword(cardInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到输入密码页面
     * Go to the password input screen
     * @param cardInfo
     */
    private void jump2InputPassword(CardInfoV2 cardInfo) {
        try {
            BaseApp.Companion.getEmvOpt().importCardNoStatus(0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //此处不做卡号合法性判断, add by kobi
/*        if (TextUtils.isEmpty(cardInfo.cardNo)) {
            showToast(getString(R.string.trans_info));
            finish();
            Log.e(TAG, "Card NO Error");
            startTransResultActivity(EMVTransResult.TRANSACTION_TERMINATED);
            return;
        }*/

        Log.d(TAG,"jump2InputPassword");
        TransData = getIntent().getParcelableExtra("TransData");
        cardType = cardInfo.cardType;
        cardNo = cardInfo.cardNo;
        track2 = cardInfo.track2;

        //mEmvOpt = MyApplication.app.emvOpt;
    }

    /**
     * 点击返回按钮
     * Click the Back button
     */
    private void handleBackClick() {
        Log.d(TAG, "handleBackClick");
        try {
            BaseApp.Companion.getReadCardOpt().cancelCheckCard();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        finish();
    }


    private static OnEMVListener onEMVListener;

    public static void setOnEMVListener(OnEMVListener onEMVListener1) {
        onEMVListener = onEMVListener1;
    }

    interface OnEMVListener {

        void onRequestShowPinPad(int pinType, int remainTime);

        void onProcessEnd();

        void onError(int errorCode);

        void offlineApproval();

        void onTransResultByKernelRetCode(int code);

        void onCertVerify(int certType, String certInfo);

    }

    private void initData() {
        SwingCardActivity.setOnEMVListener(new OnEMVListener() {
            @Override
            public void onRequestShowPinPad(int pinType, int remainTime) {
                mRemainTime = UpdateDataConstant.mRemainTime;
                Log.e(TAG, "emv requestShowPinPad, pinType:" + pinType+ ",remainTime: " + mRemainTime);
                if (mRemainTime == 2) {
                    // 脱机PIN重输密码
                    // offline PIN,Please Enter PIN Again
                    showToast(getString(R.string.re_input_pin));
                    pinType = 1;
                } else if (mRemainTime == 1) {
                    showToast(getString(R.string.input_pin_last_time));
                    pinType = 1;
                } else {
                    showToast(getString(R.string.input_pin_or_skip));
                }

                //调用密码键盘  pinType 0:联机PIN  1:脱机PIN
                //Call pinpad pinType  0:online PIN        1: offline PIN
                initEMVKeyboard(pinType);
            }

            @Override
            public void onProcessEnd() {
                Log.e(TAG, "--------emv onProcessEnd--1");
                    if (cardType == AidlConstantsV2.CardType.MAGNETIC.getValue()) {
                        msSendAuthorisationRequestMsg();
                    } else {
                        Log.e(TAG, "--------emv onProcessEnd--12");
                        emvSendAuthorisationRequestMsg(0);
                    }
                Log.e(TAG, "--------emv onProcessEnd--2");
            }

            @Override
            public void onError(int errorCode) {
                String[] tag = new String[]{"DF817C"}; // 20190621
                String Advice;
                int len = 0;
                flag_advice = 0;
                Log.e(TAG, "--------emv onError,errorCode:" + errorCode);
                EMVTransResult transResult = EMVTransResult.TRANSACTION_TERMINATED;
                if (errorCode == -4000) {
                    transResult = EMVTransResult.OFFLINE_DECLINE;
                }
                else if(errorCode == -4110){
                    transResult = EMVTransResult.NOT_ACCEPTED;
                }

                Map<String, TLV> tlvMap = readKernelData(tag);
                if ((errorCode == -4000) && ((!tlvMap.isEmpty() && (PreferencesUtil.getFunSetSupportAdvice().equals("01"))) || (PreferencesUtil.getFunForceAcceptance().equals("01")))) {
                    if (tlvMap.containsKey("DF817C") || (PreferencesUtil.getFunForceAcceptance().equals("01"))) {
                        Advice = tlvMap.get("DF817C").getValue();
                        len = tlvMap.get("DF817C").getLength();
                        if (Advice.equals("01") || PreferencesUtil.getFunForceAcceptance().equals("01")){
                            if (PreferencesUtil.getFunForceAcceptance().equals("01")) {
                                transResult = EMVTransResult.OFFLINE_APPROVE;
                            }
                            if (PreferencesUtil.getFunSetDataCollect().equals("00")) {
                                emvSendAdviceMsg(transResult);
                            }else if (PreferencesUtil.getFunSetDataCollect().equals("01")){
                                flag_advice = 1;
                            }
                        }
                    }
                }

                startTransResultActivity(transResult);
                Log.d(TAG, "-RunOrStop----onPause");
            }

            @Override
            public void offlineApproval() {
                startTransResultActivity(EMVTransResult.OFFLINE_APPROVE);
            }

            @Override
            public void onTransResultByKernelRetCode(int code) {
                Log.d(TAG,"onTransResultByKernelRetCode code: " + code);
                if ("00".equals(PreferencesUtil.getFunSetDataCollect()) && !sendFinanceOnlineTransResult) {
                    sendFinanceOnlineTransResult = true;
                    if (isOnSuccess) {
                        sendFinanceOnlineTransResult(code, mTransResult, mOnlineResult);
                    } else {
                        sendFinanceOnlineTransResult2(code);
                    }
                } else if ("01".equals(PreferencesUtil.getFunSetDataCollect()) && !sendAuthorOnlineTransResult) {
                    sendAuthorOnlineTransResult = true;
                    if (isOnSuccess) {
                        sendAuthorOnlineTransResult(code, mTransResult, mOnlineResult);
                    } else {
                        sendAuthorOnlineTransResult2(code);
                    }
                }
            }

            @Override
            public void onCertVerify(int certType, String certInfo) {
                String[] tagValue = new String[]{String.valueOf(certType), certInfo};
                String userIDType = "";
                if(tagValue[0].equals("536911872")) {
                    userIDType = getString(R.string.id_card);
                }else if(tagValue[0].equals("536911873")) {
                    userIDType = getString(R.string.officer_card);
                }else if(tagValue[0].equals("536911874")) {
                    userIDType = getString(R.string.visa);
                }else if(tagValue[0].equals("536911875")) {
                    userIDType = getString(R.string.entry_permit);
                }else if(tagValue[0].equals("536911876")) {
                    userIDType = getString(R.string.temp_id_card);
                }else{
                    userIDType = getString(R.string.other);
                }

                String idTypeStr = getString(R.string.check_id_card) + "\n"
                        + userIDType + ": " + certInfo; // 20180726
                androidx.appcompat.app.AlertDialog  mUserAuthDialog = new androidx.appcompat.app.AlertDialog.Builder(SwingCardActivity.this)
                        .setTitle(idTypeStr)
                        .setNegativeButton(R.string.Auth_Error, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        importCertStatus(1);
                                    }
                                }
                        )
                        .setPositiveButton(R.string.Auth_OK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        importCertStatus(0);
                                    }
                                }
                        ).create();
                mUserAuthDialog.show();
            }
        });
    }

    /** 显示密码键盘 */
    private void initEMVKeyboard(int pPinType) {
        pinType = pPinType;
        Intent intent = new Intent(this, PinpadCustomLayoutActivity.class);
        intent.putExtra("pinPadType", pinPadType);
        intent.putExtra("pinType", pinType);
        intent.putExtra("cardNo", cardNo);
        //openActivity(intent, true);
        startActivity(intent);
    }

    /**
     * 从卡号的倒数第二位开始截取  得到PAN
     * Intercept to the penultimate card number, get PAN
     */
    private byte[] getPanBlock(String cardNumber) {
        int length = cardNumber.length();
        String panStr;
        if (length >= 13) {
            panStr = cardNumber.substring(length - 13, length - 1);
        } else {
            panStr = cardNumber.substring(0, length - 1);
        }
        try {
            return panStr.getBytes("US-ASCII");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void importCertStatus(int status) {
        Log.e(TAG, "importCertStatus status:" + status);
        try {
            BaseApp.Companion.getEmvOpt().importCertStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送金融请求报文
     * Send the finance request message
     */
    private void emvSendFinanceRequestMsg(final int step) {
        try {  //根据tags,需要取出的tag集合
            Log.e(TAG, "emvSendFinanceRequestMsg-------->>> emvProcessStep1");

            String[] tag1 = {"50","57","9F09","5F20","9F0B","9F34","84","9F11","5F2D","82", "9F12","9f36", "9f07", "9f26",
                    "9f27", "9F1E", "9F0D",
                    "9F0E", "9F0F", "9F10", "9F33", "9F35", "95", "9B", "9F37", "9F01", "9F02",
                    "9F03", "5F25", "5F24", "5A", "5F34", "5F28", "9F15", "9F16", "9F39",
                    "9F1A", "9F1C", "81", "5F2A", "9A", "9F21", "9C", "9F41", "9F24","9F19","9F25"
            };


            byte[] outBuf11 = new byte[2048];
            int len11 = BaseApp.Companion.getEmvOpt().getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tag1, outBuf11);
            Map<String, TLV> tlvMap1 = TLVUtils.builderTLVMap(Utils.byte2HexStr(Arrays.copyOf(outBuf11, len11)));
            String Advice2 =  tlvMap1.get("57").getValue();
            Log.e(TAG, "--------tag57 Advice2：" + Advice2);

            byte[] _57 = new byte[128];
            BaseApp.Companion.getEmvOpt().getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "9F27", _57);
            Log.e(TAG, "tag57:" + Utils.byte2HexStr(_57));


            byte[] outBuf1 = new byte[2048];
            int len1 = mEmvOpt.readKernelData(tag1, outBuf1);
            Log.e(TAG, "outBuf1：" + Utils.bytes2HexString(outBuf1));
            if (len1 < 0) {
                Log.e(TAG, "--------emvSendFinanceRequestMsg EMV读取内核数据出错：" + len1);
                finish();
                return;
            }

            String[] tag2= {"8E"};
            byte[] outBuf2 = new byte[1024];
            int len2 = mEmvOpt.readKernelData(tag2, outBuf2);
            if (len2 < 0) {
                Log.e(TAG, "--------emvSendFinanceRequestMsg EMV读取内核数据8E出错：" + len2);
                finish();
                return;
            }

            byte[] outBuf = new byte[len1 + len2];
            System.arraycopy(outBuf1,0,outBuf,0,len1);
            System.arraycopy(outBuf2,0,outBuf,len1,len2);

            String[] tags = new String[tag1.length + tag2.length];
            System.arraycopy(tag1,0,tags,0,tag1.length);
            System.arraycopy(tag2,0,tags,tag1.length,tag2.length);
            int len = len1 + len2;


            //去掉取出来空值的tag
            //Remove the content of the tag with a null value
            String outString = Utils.bytes2HexString(outBuf);
            outBuf = Utils.hexStr2Bytes(outString);

            byte[] sendBuf = Arrays.copyOf(outBuf, len);//tlv数据

            //构建pinBlock tlv数据
            if(pinType == 0) { // 联机PIN 则上送PINBlock
                byte[] pinBlockTlv = TLVUtils.revertTVL2Bytes(new TLV("99", Utils.byte2HexStr(pinBlock))); // "99" for BCTC host
                sendBuf = Utils.concatByteArray(sendBuf, pinBlockTlv);//tlv数据+pinBlock
            }

            //测试数据
            //test data
            byte[] data = Utils.hexStr2Bytes("0201001889063132333435368A023030910A1A1B1C1D1E1F2A2B3030");
            len  = 28;

            isOnSuccess = true;
            flag_advice = 0;
            Log.e(TAG, "--------emvSendFinanceRequestMsg send data to POSP success");

            EMVTransResult transResult = getTransResultByFinanceRequestRsp(data, len);
            if (cardType != AidlConstantsV2.CardType.IC.getValue()) {//非接卡
                startTransResultActivity(transResult);//跳到结果页面   go to result
            } else if (step == 0) {
                //接触卡金融请求第一次应答,将POSP应答数据导入内核
                // the first financial request for the contact card,Writing posp data to the kernel
                byte[] scriptResult = new byte[80];
                byte[] responseData = new byte[len];
                System.arraycopy(data, 4, responseData, 0, len - 4);
                int onlineResult = 0;
                if(transResult == EMVTransResult.ONLINE_FAILED) {
                    onlineResult = 2;
                }
                else {
                    onlineResult = EMVTransResult.ONLINE_APPROVE == transResult ? 0 : 1;
                }
                Log.e(TAG, "emvSendFinanceRequestMsg: importResponseData onlineResult " + onlineResult);

                mTransResult = transResult;
                mOnlineResult = onlineResult;
                int ret = importResponseData(responseData, onlineResult);
                sendFinanceOnlineTransResult = false;
                if (ret <= 0) {
                    sendFinanceOnlineTransResult = true;
                    sendFinanceOnlineTransResult(ret, mTransResult, mOnlineResult);
                }
                //sendOnlineTransResult(len, mTransResult, mOnlineResult);
            } else if (step == 1) {
                //接触卡金融请求第二次应答
                // the second financial request for the contact card
                if (EMVTransResult.ONLINE_APPROVE == transResult) {
                    emvSendFinanceConfirmMsg(transResult);//直接发送融确认报文
                } else {
                    startTransResultActivity(transResult);//跳到结果页面
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EMVTransResult mTransResult;
    private int mOnlineResult;
    private boolean isOnSuccess;

    private int importResponseData(byte[] responseData, int onlineResult) throws RemoteException {
        List<TLV> tlvList = TLVUtils.builderTLVList(responseData);
        List<String> tagList = new ArrayList<>();
        List<String> valList = new ArrayList<>();
        for (TLV tlv : tlvList) {
            tagList.add(tlv.getTag());
            valList.add(tlv.getValue());
        }

        byte[] out = new byte[1024];
        String[] tags = tagList.toArray(new String[tagList.size()]);
        String[] values = valList.toArray(new String[valList.size()]);
        int len = BaseApp.Companion.getEmvOpt().importOnlineProcStatus(onlineResult, tags, values, out);
        if (len < 0) {
            Log.e(TAG, "importOnlineProcessStatus error,code:" + len);
        } else {
            byte[] bytes = Arrays.copyOf(out, len);
            String hexStr = Utils.bytes2HexString(bytes);
            Log.e(TAG, "importOnlineProcessStatus outData:" + hexStr);
        }

        return len;
    }

    private boolean sendFinanceOnlineTransResult;
    private boolean sendAuthorOnlineTransResult;

    private void sendFinanceOnlineTransResult(int code, EMVTransResult transResult, int onlineResult) {
        Log.e(TAG, "sendFinanceOnlineTransResult code= " + code + "transResult = " + transResult + "onlineResult=" + onlineResult);
        if (code == 0) {
            code = 1;  //联机批准
        }
        EMVTransResult kernelResult = getTransResultByKernelRetCode(code);
        if (EMVTransResult.ONLINE_APPROVE == kernelResult) {
            if (onlineResult == 2) {
                if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                    kernelResult = EMVTransResult.OFFLINE_APPROVE;
                } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                    kernelResult = EMVTransResult.OFFLINE_DECLINE;
                }
                emvSendReversalMsg(kernelResult); // 20180720 联机失败 冲正报文
            }
            emvSendFinanceConfirmMsg(kernelResult);
        } else if (EMVTransResult.ONLINE_REQUEST == kernelResult) {
            emvSendFinanceRequestMsg(1);
        } else if ((EMVTransResult.ONLINE_DECLINE == kernelResult) && (EMVTransResult.ONLINE_APPROVE == transResult || EMVTransResult.ONLINE_DECLINE == transResult)) {
            emvSendReversalMsg(kernelResult);
            String[] tag = new String[]{"95","DF817C"}; // 20190621
            String Advice;
            int len2 = 0;

            Map<String, TLV> tlvMap = readKernelData(tag);
            if ((!tlvMap.isEmpty() && (PreferencesUtil.getFunSetSupportAdvice().equals("01"))) || (PreferencesUtil.getFunForceAcceptance().equals("01"))) {
                if (tlvMap.containsKey("DF817C") || (PreferencesUtil.getFunForceAcceptance().equals("01"))) {
                    Advice = tlvMap.get("DF817C").getValue();
                    len2 = tlvMap.get("DF817C").getLength();
                    if (Advice.equals("01") || (PreferencesUtil.getFunForceAcceptance().equals("01"))){
                        if (PreferencesUtil.getFunForceAcceptance().equals("01")) {
                            kernelResult = EMVTransResult.OFFLINE_APPROVE;
                        }
                        if (PreferencesUtil.getFunSetDataCollect().equals("00")) {
                            emvSendAdviceMsg(kernelResult);
                        }else if (PreferencesUtil.getFunSetDataCollect().equals("01")){
                            flag_advice = 1;
                        }
                    }
                }
            }
            startTransResultActivity(kernelResult);
        } else {
            if (onlineResult == 2) {
                if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                    kernelResult = EMVTransResult.OFFLINE_APPROVE;
                } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                    kernelResult = EMVTransResult.OFFLINE_DECLINE;
                }
                emvSendReversalMsg(kernelResult); // 20180720 联机失败 冲正报文
            }
            startTransResultActivity(kernelResult);
        }
    }

    private void sendFinanceOnlineTransResult2(int code) {
        if (code == 0) {
            code = 1;  //联机失败，批准。
        }
        EMVTransResult kernelResult = getTransResultByKernelRetCode(code);
        if (EMVTransResult.ONLINE_APPROVE == kernelResult) {
            if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                kernelResult = EMVTransResult.OFFLINE_APPROVE;
            } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                kernelResult = EMVTransResult.OFFLINE_DECLINE;
            }
            emvSendReversalMsg(kernelResult); // 20180720 联机失败 冲正报文
            emvSendFinanceConfirmMsg(kernelResult);
        } else if (EMVTransResult.ONLINE_REQUEST == kernelResult) {
            emvSendFinanceRequestMsg(1);
        } else {
            if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                kernelResult = EMVTransResult.OFFLINE_APPROVE;
            } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                kernelResult = EMVTransResult.OFFLINE_DECLINE;
            }
            emvSendReversalMsg(kernelResult); // 20180720 联机失败 冲正报文
            startTransResultActivity(kernelResult);
        }
    }

    private void sendAuthorOnlineTransResult2(int code) {
        EMVTransResult kernelResult = getTransResultByKernelRetCode(code);
        if (EMVTransResult.ONLINE_APPROVE == kernelResult) {
            if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                kernelResult = EMVTransResult.OFFLINE_APPROVE;
            } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                kernelResult = EMVTransResult.OFFLINE_DECLINE;
            }
            startTransResultActivity(kernelResult);
        } else if (EMVTransResult.ONLINE_REQUEST == kernelResult) {
            emvSendAuthorisationRequestMsg(1);
        } else {
            if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                kernelResult = EMVTransResult.OFFLINE_APPROVE;
            } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                kernelResult = EMVTransResult.OFFLINE_DECLINE;
            }
            //emvSendReversalMsg(kernelResult); 联机失败 冲正报文
            startTransResultActivity(kernelResult);
        }
    }


    private void sendAuthorOnlineTransResult(int code, EMVTransResult transResult, int onlineResult) {
        if (code == 0) {
            code = 1;  //联机批准
        }

        EMVTransResult kernelResult = getTransResultByKernelRetCode(code);
        Log.e(TAG,"transResult: "+ transResult.toString() + " ;kernelResult: "+ kernelResult.toString());
        if (EMVTransResult.ONLINE_APPROVE == kernelResult) {
            if (onlineResult == 2) {
                if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                    kernelResult = EMVTransResult.OFFLINE_APPROVE;
                } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                    kernelResult = EMVTransResult.OFFLINE_DECLINE;
                }
            }
            startTransResultActivity(kernelResult);
        } else if (EMVTransResult.ONLINE_REQUEST == kernelResult) {
            emvSendAuthorisationRequestMsg(1);
        } else if ((EMVTransResult.ONLINE_DECLINE == kernelResult) && (EMVTransResult.ONLINE_APPROVE == transResult)) {
            String[] tag = new String[]{"DF817C"};
            String Advice;
            int len2 = 0;

            Map<String, TLV> tlvMap = readKernelData(tag);
            if ((!tlvMap.isEmpty() && (PreferencesUtil.getFunSetSupportAdvice().equals("01"))) || (PreferencesUtil.getFunForceAcceptance().equals("01"))) {
                if (tlvMap.containsKey("DF817C") || (PreferencesUtil.getFunForceAcceptance().equals("01"))) {
                    Advice = tlvMap.get("DF817C").getValue();
                    len2 = tlvMap.get("DF817C").getLength();
                    if (Advice.equals("01") || (PreferencesUtil.getFunForceAcceptance().equals("01"))){
                        if (PreferencesUtil.getFunForceAcceptance().equals("01")) {
                            kernelResult = EMVTransResult.OFFLINE_APPROVE;
                        }
                        if (PreferencesUtil.getFunSetDataCollect().equals("00")) {
                            emvSendAdviceMsg(kernelResult);
                        }else if (PreferencesUtil.getFunSetDataCollect().equals("01")){
                            flag_advice = 1;
                        }
                    }
                }
            }
            //startTransResultActivity(kernelResult);
        } else {
            if (onlineResult == 2) {
                if (kernelResult == EMVTransResult.ONLINE_APPROVE) {
                    kernelResult = EMVTransResult.OFFLINE_APPROVE;
                } else if (kernelResult == EMVTransResult.ONLINE_DECLINE) {
                    kernelResult = EMVTransResult.OFFLINE_DECLINE;
                }
            }
            //startTransResultActivity(kernelResult);
        }
    }

    /** 发送授权请求报文 */  // 不发冲正报文
    private void emvSendAuthorisationRequestMsg(final int step) {
        try {  //根据tags,需要取出的tag集合
            Log.e(TAG, "emvSendAuthorisationRequestMsg-------->>>> emvProcessStep1");

            String[] tag1 = {"8A","82","84", "9F36", "9F07", "9F27", "8E", "9F34", "9F1E", "9F0D",
                    "9F0E", "9F0F", "9F10","9F11", "9F33", "9F35", "95", "9B", "9F26", "9F37",
                    "9F02", "9F03","9F09","9F0B", "5F25", "5F24","5F20", "50", "5A", "5F34", "5F28","5F2D", "9F12", "9F15", "9F16",
                    "9F1A", "9F1C", "57", "81", "5F2A", "9A", "9F21", "9C", "9F24", "9F19", "9F06", "9F01", "9F39"};

            byte[] outBuf11 = new byte[2048];
            int len11 = BaseApp.Companion.getEmvOpt().getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tag1, outBuf11);
            Map<String, TLV> tlvMap1 = TLVUtils.builderTLVMap(Utils.byte2HexStr(Arrays.copyOf(outBuf11, len11)));
            String Advice2 =  tlvMap1.get("57").getValue();
            Log.e(TAG, "--------tag57 Advice2：" + Advice2);


            byte[] outBuf1 = new byte[2048];
            int len1 = mEmvOpt.readKernelData(tag1, outBuf1);
            if (len1 < 0) {
                Log.e(TAG, "--------emvSendAuthorisationRequestMsg EMV读取内核数据出错：" + len1);
                finish();
                return;
            }

            String[] tag2= {"8E"};
            byte[] outBuf2 = new byte[1024];
            int len2 = mEmvOpt.readKernelData(tag2, outBuf2);
            if (len2 < 0) {
                Log.e(TAG, "--------emvSendFinanceRequestMsg EMV读取内核数据8E出错：" + len2);
                finish();
                return;
            }

            byte[] outBuf = new byte[len1 + len2];
            System.arraycopy(outBuf1,0,outBuf,0,len1);
            System.arraycopy(outBuf2,0,outBuf,len1,len2);

            String[] tags = new String[tag1.length + tag2.length];
            System.arraycopy(tag1,0,tags,0,tag1.length);
            System.arraycopy(tag2,0,tags,tag1.length,tag2.length);
            int len = len1 + len2;

            //去掉取出来空值的tag
            String outString = Utils.bytes2HexString(outBuf);
            outBuf = Utils.hexStr2Bytes(outString);

            byte[] sendBuf = Arrays.copyOf(outBuf, len);//tlv数据

            //构建pinBlock tlv数据
            if(pinType == 0) { // 联机PIN 则上送PINBlock 20180607
                // byte[] pinBlockTlv = TLVUtils.revertTVL2Bytes(new TLV("DF50", Utils.byte2HexStr(pinBlock)));
                byte[] pinBlockTlv = TLVUtils.revertTVL2Bytes(new TLV("99", Utils.byte2HexStr(pinBlock))); // "99" for BCTC host
                sendBuf = Utils.concatByteArray(sendBuf, pinBlockTlv);//tlv数据+pinBlock
            }

            byte[] msgHead = {0x02, 0x42, (byte) (sendBuf.length / 256), (byte) (sendBuf.length % 256)};//消息头
            sendBuf = Utils.concatByteArray(msgHead, sendBuf);//消息头+tlv数据+pinBlock
            //测试数据
            byte[] data = Utils.hexStr2Bytes("0201001889063132333435368A023030910A00000000000000000000");
            len  = 28;

            isOnSuccess = true;
            Log.e(TAG, "--------emvSendAuthorisationRequestMsg send data to POSP success");
            try {
                EMVTransResult transResult = getTransResultByFinanceRequestRsp(data, len);
                if (cardType == AidlConstantsV2.CardType.MAGNETIC.getValue()) {//磁条卡
                    startTransResultActivity(transResult);//跳到结果页面
                } else if (step == 0) {//接触卡授权请求第一次应答,将POSP应答数据导入内核
                    byte[] scriptResult = new byte[80];
                    byte[] responseData = new byte[len];
                    System.arraycopy(data, 4, responseData, 0, len - 4);
                    int onlineResult = 0;
                    if(transResult == EMVTransResult.ONLINE_FAILED) {
                        onlineResult = 2;
                    }
                    else {
                        onlineResult = EMVTransResult.ONLINE_APPROVE == transResult ? 0 : 1;
                    }
                    Log.e(TAG, "emvSendAuthorisationRequestMsg: importResponseData onlineResult " + onlineResult +"transResult: "+transResult);
                    //int ret = mEmvOpt.importResponseData(onlineResult, responseData, len - 4, scriptResult);
                    mTransResult = transResult;
                    mOnlineResult = onlineResult;
                    int ret = importResponseData(responseData, onlineResult);
                    sendAuthorOnlineTransResult = false;
                    Log.d(TAG,"importResponseData: "+ ret);
                    if (ret < 0) {
                        sendAuthorOnlineTransResult = true;
                        sendAuthorOnlineTransResult(ret, mTransResult, mOnlineResult);
                    }
                    //sendAuthorOnlineTransResult(transResult, onlineResult, ret);
                } else if (step == 1) {//接触卡金融请求第二次应答
                    if (EMVTransResult.ONLINE_APPROVE == transResult) {
                        startTransResultActivity(transResult);//跳到结果页面
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 发送金融请求报文(磁条卡) */
    private void msSendFinanceRequestMsg() {
        try {  //根据tags,需要取出的tag集合
            Log.e(TAG, "msSendFinanceRequestMsg--------> emvProcessStep1");
            String[] tags = {"9F02"};
            byte[] outBuf = new byte[30];
            int len = mEmvOpt.readKernelData(tags, outBuf);
            if (len < 0) {
                Log.e(TAG, "--------msSendFinanceRequestMsg EMV读取内核数据出错：" + len);
                finish();
                return;
            }

            //去掉取出来空值的tag
            String outString = Utils.bytes2HexString(outBuf);
            Log.d("aaaaa1", "outString: " + outString);
            for(int j = 0; j < tags.length; j++){
                String tempTL;
                tempTL = tags[j].toUpperCase()+"00";
                Log.d("aaaaa1", "tempTL: " + tempTL);
                if(outString.contains(tempTL)){
                    outString = outString.replaceAll(tempTL, "");
                    len -= (tempTL.length()/2);
                }
                Log.d("aaaaa1", "outString: " + outString);
            }
            outString = outString.substring(0, len*2) + "9F390180"; // 降级
            len += 4;

            int lent2 = track2.length();
            if(lent2%2 == 1){
                track2 = track2 + "F";
                lent2 += 1;
            }
            outString = outString.substring(0, len*2) + "DF8107" + TLVUtils.TLVValueLengthToHexString(lent2/2) + track2; // DF8107 二磁道数据
            len = len + 4 + lent2/2;

            outBuf = Utils.hexStr2Bytes(outString);

            byte[] sendBuf = Arrays.copyOf(outBuf, len);//tlv数据

            //构建pinBlock tlv数据
            if(pinType == 0) { // 联机PIN 则上送PINBlock 20180607
                byte[] pinBlockTlv = TLVUtils.revertTVL2Bytes(new TLV("99", Utils.byte2HexStr(pinBlock))); // "99" for BCTC host
                sendBuf = Utils.concatByteArray(sendBuf, pinBlockTlv);//tlv数据+pinBlock
            }

            byte[] msgHead = {0x02, 0x41, (byte) (sendBuf.length / 256), (byte) (sendBuf.length % 256)};//消息头
            sendBuf = Utils.concatByteArray(msgHead, sendBuf);//消息头+tlv数据+pinBlock

            //测试数据
            byte[] data = Utils.hexStr2Bytes("0201001889063132333435368A023030910A1A1B1C1D1E1F2A2B3030");
            len  = 28;

            Log.e(TAG, "--------msSendFinanceRequestMsg send data to POSP success");
            try {
                EMVTransResult transResult = getTransResultByFinanceRequestRsp(data, len);
                if (cardType != AidlConstantsV2.CardType.IC.getValue()) {//非接卡 和磁条卡
                    startTransResultActivity(transResult);//跳到结果页面
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 发送授权请求报文(磁条卡) */
    private void msSendAuthorisationRequestMsg() {
        try {  //根据tags,需要取出的tag集合
            Log.e(TAG, "msSendAuthorisationRequestMsg-------->> emvProcessStep1");
            String[] tags = {"9F02"};
            byte[] outBuf = new byte[30];
            int len = mEmvOpt.readKernelData(tags, outBuf);
            if (len < 0) {
                Log.e(TAG, "--------msSendAuthorisationRequestMsg EMV读取内核数据出错：" + len);
                finish();
                return;
            }

            //去掉取出来空值的tag
            String outString = Utils.bytes2HexString(outBuf);
            Log.d("aaaaa1", "outString: " + outString);
            for(int j = 0; j < tags.length; j++){
                String tempTL;
                tempTL = tags[j].toUpperCase()+"00";
                Log.d("aaaaa1", "tempTL: " + tempTL);
                if(outString.contains(tempTL)){
                    outString = outString.replaceAll(tempTL, "");
                    len -= (tempTL.length()/2);
                }
                Log.d("aaaaa1", "outString: " + outString);
            }
            outString = outString.substring(0, len*2) + "9F390180"; // 降级
            len += 4;

            int lent2 = track2.length();
            if(lent2%2 == 1){
                track2 = track2 + "F";
                lent2 += 1;
            }
            outString = outString.substring(0, len*2) + "DF8107" + TLVUtils.TLVValueLengthToHexString(lent2/2) + track2; // DF8107 二磁道数据
            len = len + 4 + lent2/2;

            outBuf = Utils.hexStr2Bytes(outString);

            byte[] sendBuf = Arrays.copyOf(outBuf, len);//tlv数据

            //构建pinBlock tlv数据
            if(pinType == 0) { // 联机PIN 则上送PINBlock 20180607
                byte[] pinBlockTlv = TLVUtils.revertTVL2Bytes(new TLV("99", Utils.byte2HexStr(pinBlock))); // "99" for BCTC host
                sendBuf = Utils.concatByteArray(sendBuf, pinBlockTlv);//tlv数据+pinBlock
            }

            byte[] msgHead = {0x02, 0x42, (byte) (sendBuf.length / 256), (byte) (sendBuf.length % 256)};//消息头
            sendBuf = Utils.concatByteArray(msgHead, sendBuf);//消息头+tlv数据+pinBlock

            //测试数据
            byte[] data = Utils.hexStr2Bytes("0201001889063132333435368A023030910A1A1B1C1D1E1F2A2B3030");
            len  = 28;

            Log.e(TAG, "--------msSendAuthorisationRequestMsg send data to POSP success");
            try {
                EMVTransResult transResult = getTransResultByFinanceRequestRsp(data, len);
                if (cardType != AidlConstantsV2.CardType.IC.getValue()) {//非接卡 和磁条卡
                    startTransResultActivity(transResult);//跳到结果页面
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送金融确认报文
     */
    private void emvSendFinanceConfirmMsg(final EMVTransResult step1TransResult) {
        startTransResultActivity(step1TransResult);
    }

    /**
     * 发送通知报文
     */
    private void emvSendAdviceMsg(final EMVTransResult step1TransResult) {

    }

    /**
     * 发送冲正报文
     */
    private void emvSendReversalMsg(final EMVTransResult step1TransResult) {

    }


    /**
     * 上送电子凭条和签名
     */
    public static void emvSendReceiptMsg(int uploadSign) {

    }

    /** emv读内核数据 */
    private static Map<String, TLV> readKernelData(String[] tags) {
        try {
            byte[] outBuf = new byte[2048];
            int len = BaseApp.Companion.getEmvOpt().getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, outBuf);
            if (len < 0) {
                Log.e(TAG, "----updateDB----EMV读取内核数据出错：" + len);
                return new HashMap<>();
            }
            return TLVUtils.builderTLVMap(Utils.byte2HexStr(Arrays.copyOf(outBuf, len)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }


    /**取EMV内核TLV数据，将tag替换为后台指定的tag**/
    private byte[] replaceTagOfEMVKernelData(String tagOriginal, String tagLast){
        try {
            String[] tags = new String[1];
            tags[0] = tagOriginal;
            byte[] outBuf = new byte[256];
            byte[] tlv = {0x0};
            int len = mEmvOpt.readKernelData(tags, outBuf);
            Log.e(TAG, "--------replaceTagOfEMVKernelData tag： " + tagOriginal + ", length: " + len +  ", value: " + Utils.byte2HexStr(outBuf));
            if (len < 0) {
                Log.e(TAG, "--------replaceTagOfEMVKernelData EMV读取内核数据出错：" + tagOriginal + "  " + len);
                return null;
            }
            else if(outBuf[tagOriginal.length()/2] == 0){ // length = 0
                Log.e(TAG, "--------replaceTagOfEMVKernelData tag： " + tagOriginal + ", length is 0.");
                return null;
            }

            Map<String, TLV> tlvMap1 = TLVUtils.builderTLVMap(Utils.byte2HexStr(Arrays.copyOf(outBuf, len)));

            tlv = TLVUtils.revertTVL2Bytes(new TLV(tagLast, tlvMap1.get(tagOriginal).getValue()));
            Log.e(TAG, "--------replaceTagOfEMVKernelData TLV：" + Utils.byte2HexStr(tlv));
            return tlv;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    /** 根据POSP后台应答数据获取交易结果 */
    private EMVTransResult getTransResultByFinanceRequestRsp(byte[] data, int len) {
        String hexStr = Utils.byte2HexStr(Arrays.copyOfRange(data, 4, len));
        Map<String, TLV> tlvMap = TLVUtils.builderTLVMap(hexStr);
//        Log.e(TAG, "--------getTransResultByFinanceRequestRsp 8A " + tlvMap.get("8A").getValue());
        if (!tlvMap.containsKey("8A") || tlvMap.get("8A") == null) {
            Log.e(TAG, "--------getTransResultByFinanceRequestRsp TRANSACTION_TERMINATED");
            return EMVTransResult.TRANSACTION_TERMINATED;
        }
        Log.e(TAG, "getTransResultByFinanceRequestRsp: len: " + tlvMap.get("8A").getLength() + " value: "+ tlvMap.get("8A").getValue() );
        if(2 != tlvMap.get("8A").getLength()){
            return EMVTransResult.ONLINE_FAILED;
        }

        if("3031".equals(tlvMap.get("8A").getValue())){  // referral 20180716
            //showReferralSelect("Call your bank");
            String[] tags = {"5A"}; // call your bank 显示卡号 20180725
            byte[] outBuf = new byte[30];
            String pan = "";
            try {
                int len1 = mEmvOpt.readKernelData(tags, outBuf);
                if (len1 < 0) {
                    Log.e(TAG, "--------getTransResultByFinanceRequestRsp EMV读取内核数据出错：" + len1);
                    finish();
                }
                pan = Utils.bytes2HexString(outBuf); // 5A084761739001010010
                pan = pan.substring(4, (len1-2)*2+1);
                pan = "卡号: " + pan;
                Log.e(TAG, "--------getTransResultByFinanceRequestRsp pan：" + pan);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            CheckReferralSelect mCheckReferral = new CheckReferralSelect(this, "请联系发卡行", pan);
            if(mCheckReferral.getResult() == 1){
                return EMVTransResult.ONLINE_APPROVE;
            }
            else{
                return EMVTransResult.ONLINE_DECLINE;
            }
        }
        else if ("3030".equals(tlvMap.get("8A").getValue())) {
            return EMVTransResult.ONLINE_APPROVE;
        }
        return EMVTransResult.ONLINE_DECLINE;
    }


    /** 根据EMV内核错误码获取交易结果 */
    private EMVTransResult getTransResultByKernelRetCode(int errCode) {
        Log.e(TAG, "--------getTransResultByKernelRetCode errcode = " + errCode);
        if (errCode == 1 || errCode == 5) {
            return EMVTransResult.ONLINE_APPROVE;
        }
        if (errCode == 2) {
            return EMVTransResult.ONLINE_REQUEST;
        }
        if (errCode == -4000 || errCode == 6) {
            return EMVTransResult.ONLINE_DECLINE;
        }
        if (errCode == -4001) {
            return EMVTransResult.USE_OTHER_INTERFACE;
        }
        if (errCode == -4110) {
            return EMVTransResult.NOT_ACCEPTED;
        }
        Log.e(TAG, "--------getTransResultByKernelRetCode TRANSACTION_TERMINATED");
        return EMVTransResult.TRANSACTION_TERMINATED;
    }

    private int checkSignatureProcess(EMVTransResult transResult){ // 20180713
        String[] tags = {"9F34"};
        byte[] outBuf = new byte[10]; // 9F 34 03 1E xx 00
        try {
            if(PreferencesUtil.getFunSetSendReceipt().equals("00")) { // 20180724 实验室提议 送凭条才开启签名使能
                return 0;
            }

            int len = mEmvOpt.readKernelData(tags, outBuf);
            Log.e(TAG, "--------readKernelData 9F34: " + Utils.bytes2HexString(outBuf));
            if((len > 0) && ((outBuf[3] == 0x1E) || (outBuf[3] == 0x03) || (outBuf[3] == 0x05)) && (outBuf[5] == 0x00)){
                Log.e(TAG, "--------HandWriteActivity");
                return 1;
            }
            else{
                return 0; // no signature
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void startTransResultActivity(EMVTransResult transResult) {
        int ret = 0;
        dismissLoadDlg();
        ret = checkSignatureProcess(transResult); // 20180713
        Log.e(TAG,"transResult: " + transResult.toString());

        if(ret == 0){
            if(PreferencesUtil.getFunSetSendReceipt().equals("01")) {
                emvSendReceiptMsg(ret); // 20180720
            }
            Intent intent = new Intent(this, TransactionResultActivity.class);
            intent.putExtra(TransactionResultActivity.EXTRA_TRANS_RESULT, transResult);
            BaseActivity.Companion.openActivity(this,intent, true);
        }
    }


    private void pinOnError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(pinType, 3);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** 获取PinBock */
    private byte[] getPinBlockTLVBytes() {
        TLV tlv = new TLV("DF50", Utils.byte2HexStr(pinBlock));
        return TLVUtils.revertTVL2Bytes(tlv);
    }

    /**
     * 解析55域数据
     */
    private String F55Data(byte[] na) {
        String icData = Utils.Bcd2String(na).toUpperCase();
        Map<String, TLV> tags = TLVUtils.builderTLVMap(icData);

        // 交易类型
        //the type of transaction
        TLV tlv_9C = tags.get("9C");
        if (tlv_9C != null) {
            Log.d(TAG, "9C：" + tlv_9C.getValue());
        }

        // 密码密文
        TLV tlv_df02 = tags.get("DF02");
        if (tlv_df02 != null) {
            Log.d(TAG, "DF02：" + tlv_df02.getValue());
        }
        // 主账号序号（针对IC卡）
        String PANSubNo = "000";
        TLV tlv_5f34 = tags.get("5F34");
        if (tlv_5f34 != null) {
            try {
                String value = tlv_5f34.getValue();
                PANSubNo = String.format(Locale.getDefault(), "%03d", Integer.parseInt(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "5F34：" + PANSubNo);

        // AID
        TLV tlv_9f06 = tags.get("9F06");
        if (tlv_9f06 != null) {
            String value = tlv_9f06.getValue();
            Log.d(TAG, "9F06：" + value);
        }
        // 应用标签
        TLV tlv_ff30 = tags.get("FF30");
        if (tlv_ff30 != null) {
            Log.d(TAG, "FF30：" + tlv_ff30.getValue());
        }
        // 应用首选名称
        TLV tlv_ff31 = tags.get("FF31");
        if (tlv_ff31 != null) {
            Log.d(TAG, "FF31：" + tlv_ff31.getValue());
        }
        // TVR
        TLV tlv_95 = tags.get("95");
        if (tlv_95 != null) {
            if (cardType == AidlConstantsV2.CardType.NFC.getValue()) {
                Log.d(TAG, "95：0000000000");
            } else {
                Log.d(TAG, "95：" + tlv_95.getValue());
            }
        }
        // TSI
        TLV tlv_9b = tags.get("9B");
        if (tlv_9b != null) {
            Log.d(TAG, "9B：" + tlv_9b.getValue());
        }
        // ATC
        TLV tlv_9f36 = tags.get("9F36");
        if (tlv_9f36 != null) {
            Log.d(TAG, "9F36：" + tlv_9f36.getValue());
        }
        // TC/ARQC
        TLV tlv_9f26 = tags.get("9F26");
        if (tlv_9f26 != null) {
            Log.d(TAG, "9F26：" + tlv_9f26.getValue());
        }
        // 脚本处理结果
        TLV tlv_df31 = tags.get("DF31");
        if (tlv_df31 != null) {
            Log.d(TAG, "DF31：" + tlv_df31.getValue());
        }
        // 密文信息数据
        TLV tlv_9f27 = tags.get("9F27");
        if (tlv_9f27 != null) {
            Log.d(TAG, "9F27：" + tlv_9f27.getValue());
        }

        // 二磁
        String track2 = null;
        TLV tlv_57 = tags.get("57");
        if (tlv_57 != null) {
            int idxF = tlv_57.getValue().indexOf("F");
            if (idxF > 0) {
                track2 = tlv_57.getValue().substring(0, idxF);
                Log.d(TAG, "57：" + track2);
            } else {
                track2 = tlv_57.getValue();
                Log.d(TAG, "57：" + track2);
            }

        } else {
            Log.e(TAG, "CARD_TYPE_NFC: no Track2");
        }

        // PAN
        TLV tlv_5a = tags.get("5A");
        if (tlv_5a != null && tlv_5a.getValue().length() >= 2 && !tlv_5a.getValue().substring(0, 2).equals("00")) {
            String value = tlv_5a.getValue();
            char charAt = value.charAt(value.length() - 1);
            if ((charAt >= 65 && charAt <= 90) || (charAt >= 97 && charAt <= 122)) {
                Log.d(TAG, "5A：" + value.substring(0, value.length() - 1));
            } else {
                Log.d(TAG, "5A：" + value);
            }
            Log.d(TAG, "卡号(IC):" + value);
        } else if (track2 != null) {
            if (track2.length() >= 16) {
                int idx = track2.indexOf("D");
                Log.d(TAG, "卡号(QP):" + track2.substring(0, idx));
            }
        }
        // 卡有效期
        String validity = "";
        TLV tlv_5f24 = tags.get("5F24");
        if (tlv_5f24 != null) {
            validity = tlv_5f24.getValue();
            if (validity.equals("000000")) {
                int idx = track2.indexOf("D");
                validity = track2.substring(idx + 1, idx + 5);
            }
        } else {
            if (track2 != null) {
                if (track2.length() >= 16) {
                    int idx = track2.indexOf("D");
                    validity = track2.substring(idx + 1, idx + 5);
                }
            }
        }
        if (validity.length() >= 4) {
            Log.d(TAG, "卡有效期:" + validity.substring(0, 4));
        }

        // 55 域
        String F55 = "";
        if (tags.get("9F26") != null) {
            F55 += "9F2608" + tags.get("9F26").getValue();
        }
        if (tags.get("9F27") != null) {
            F55 += "9F2701" + tags.get("9F27").getValue();
        }
        if (tags.get("9F10") != null) {
            F55 += "9F10" + String.format("%02X", tags.get("9F10").getLength()) + tags.get("9F10").getValue();
        }
        if (tags.get("9F37") != null) {
            F55 += "9F3704" + tags.get("9F37").getValue();
        }
        if (tags.get("9F36") != null) {
            F55 += "9F3602" + tags.get("9F36").getValue();
        }
        if (tags.get("95") != null) {
            if (cardType == AidlConstantsV2.CardType.NFC.getValue()) {
                F55 += "9505" + "0000000000";
            } else {
                F55 += "9505" + tags.get("95").getValue();
            }
        }
        if (tags.get("9A") != null) {
            F55 += "9A03" + tags.get("9A").getValue();
        }
        if (tags.get("9C") != null) {
            F55 += "9C01" + tags.get("9C").getValue();
        } else {
            F55 += "9C0100";
        }
        if (tags.get("9F02") != null) {
            F55 += "9F0206" + tags.get("9F02").getValue();
        }
        if (tags.get("5F2A") != null) {
            F55 += "5F2A02" + tags.get("5F2A").getValue();
        } else {
            F55 += "5F2A020156";
        }
        if (tags.get("82") != null) {
            F55 += "8202" + tags.get("82").getValue();
        }
        if (tags.get("9F1A") != null) {
            F55 += "9F1A02" + tags.get("9F1A").getValue();
        } else {
            F55 += "9F1A020156";
        }
        if (tags.get("9F03") != null) {
            F55 += "9F0306" + tags.get("9F03").getValue();
        }
        if (tags.get("9F33") != null) {
            F55 += "9F3303" + tags.get("9F33").getValue();
        } else {
            F55 += "9F330360F800";
        }

        if (tags.get("9F35") != null) {
            F55 += "9F3501" + tags.get("9F35").getValue();
        } else {
            F55 += "9F350121";
        }
        if (tags.get("9F1E") != null) {
            F55 += "9F1E08" + tags.get("9F1E").getValue();
        } else {
            F55 += "9F1E083730303030303034";
        }
        if (tags.get("84") != null) {
            F55 += "8408" + tags.get("84").getValue();
        } else {
            if (tags.get("9F06") != null) {
                F55 += "8408" + tags.get("9F06").getValue();
            } else {
                if (tags.get("4F") != null) {
                    F55 += "8408" + tags.get("4F").getValue();
                } else {
                    F55 += "8408A000000333010102";
                }
            }
        }
        if (tags.get("9F09") != null) {
            F55 += "9F0902" + tags.get("9F09").getValue();
        } else {
            F55 += "9F09020030";
        }
        if (tags.get("9F41") != null) {
            String str = String.format(Locale.getDefault(), "%02d", tags.get("9F41").getLength());
            F55 += "9F41" + str + tags.get("9F41").getValue();
        } else {
            //TODO 输入起始批次号
            F55 += "9F4103" + String.format(Locale.getDefault(), "%06d", Integer.parseInt("000001"));
        }
        return F55;
    }

    private void UiProcessing(int delayTimeout){
        mLoadDialog = new ProgressDialog(this);
        mLoadDialog.setMessage(getString(R.string.handling));
        mLoadDialog.setCancelable(false);
        mLoadDialog.setCanceledOnTouchOutside(false);
        mLoadDialog.show();
        try {
            Thread.sleep(delayTimeout);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
