package ao.metagest.z500cardreader.emv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import com.ctk.sdk.Log;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;


import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import ao.metagest.z500cardreader.BaseApp;
import ao.metagest.z500cardreader.utils.BaseActivityOLD;
import ao.metagest.z500cardreader.R;
import ao.metagest.z500cardreader.bean.LastTransResultBean;
import ao.metagest.z500cardreader.db.dao.DBSessionManager;
import ao.metagest.z500cardreader.db.entiry.EMVBatchUploadData;
import ao.metagest.z500cardreader.db.entiry.EMVTransferLog;
import ao.metagest.z500cardreader.utils.CollectionUtil;
import ao.metagest.z500cardreader.utils.Constants;
import ao.metagest.z500cardreader.utils.PreferencesUtil;
import ao.metagest.z500cardreader.utils.Utils;
import ao.metagest.z500cardreader.utils.tlv.TLV;
import ao.metagest.z500cardreader.utils.tlv.TLVUtils;

//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.BaseActivity;
//import test.metagestdemo.activity.R;
//import test.metagestdemo.activity.bean.LastTransResultBean;
//import test.metagestdemo.activity.db.dao.DBSessionManager;
//import test.metagestdemo.activity.db.entiry.EMVBatchUploadData;
//import test.metagestdemo.activity.db.entiry.EMVTransferLog;
//import test.metagestdemo.activity.utils.CollectionUtil;
//import test.metagestdemo.activity.utils.Constants;
//import test.metagestdemo.activity.utils.PreferencesUtil;
//import test.metagestdemo.activity.utils.Utils;
//import test.metagestdemo.activity.utils.tlv.TLV;
//import test.metagestdemo.activity.utils.tlv.TLVUtils;

public class TransactionResultActivity extends BaseActivityOLD {
    private static final String TAG = "TransactResultActivity";

    public static final String EXTRA_TRANS_RESULT = "extra_trans_result";

    private TextView emvResult;
    private TextView tvr;
    private TextView tsi;
    private TextView tvrbits;
    private TextView tsibits;
    private TextView sr;
    private Button confirm;
    private Handler handler = new Handler();
    private EMVTransResult transResult;
    private CountDownLatch latch = new CountDownLatch(2);
    private TextView offlineBallance;
    private TextView ballanceTxt;
    public static int flag_batch=0;

    public static void start(Context context, String transResult) {
        Intent starter = new Intent(context, TransactionResultActivity.class);
        starter.putExtra(EXTRA_TRANS_RESULT, transResult);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_result_layout);
        initView();
        initData();
    }

    private void initView() {
        emvResult = findViewById(R.id.emv_result);
        tvr = findViewById(R.id.tvr);
        tsi = findViewById(R.id.tsi);
        tvrbits = findViewById(R.id.tvrbits);
        tsibits = findViewById(R.id.tsibits);
        sr = findViewById(R.id.sr);
        offlineBallance = findViewById(R.id.ballance);
        ballanceTxt = findViewById(R.id.ballanceTxt);
        confirm = findViewById(R.id.confirm);
        confirm.setEnabled(true);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        try {
            if(PreferencesUtil.getFunSetSupportClss().equals("01")) {
                BaseApp.Companion.getReadCardOpt().cardOff(4);
                BaseApp.Companion.getReadCardOpt().cardOff(2);
            }else {
                BaseApp.Companion.getReadCardOpt().cardOff(2);
            }
            Log.e(TAG, "-------- Cardoff");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        transResult = (EMVTransResult) getIntent().getSerializableExtra(EXTRA_TRANS_RESULT);
        Log.e(TAG, "transResult =  " + transResult);
        String str = Utils.empty2Dash(transResult.getDepicter());
        Log.e(TAG, "str =  " + str);

        if(str.equals("Online Approve")){
            emvResult.setText("Online Approve");
        }
        else if(str.equals("Online Decline")){
            emvResult.setText("Online Decline");
        }
        else if(str.equals("Online Request")){
            emvResult.setText("Online Request");
        }
        else if(str.equals("Offline Approve")){
            emvResult.setText("Offline Approve");
        }
        else if(str.equals("Offline Decline")){
            emvResult.setText("Offline Decline");
        }
        else if(str.equals("USE OTHER INTERFACE")){
            emvResult.setText("USE OTHER INTERFACE");
        }
        else if(str.equals("Transaction Terminate")){
            emvResult.setText("Transaction Terminate");
        }
        else if(str.equals("NOT ACCEPTED")){
            emvResult.setText("NOT ACCEPTED");
        }
        else if(str.equals("Online Failed")){
            emvResult.setText("Online Failed");
        } else {
            emvResult.setText(Utils.empty2Dash(transResult.getDepicter()));
        }

        String[] tags = {"95", "9B", "DF51", "9F5D"};
        Map<String, TLV> tlvMap = readKernelData(tags);
        String tag95 = "";
        String tag9B = "";
        String tagDF51 = "";
        String tag9F5D = "";
        byte[] btmp = new byte[5];
        String stmp = "";
        if (!tlvMap.isEmpty()) {
            if(tlvMap.containsKey("95")) {
                tag95 = tlvMap.get("95").getValue();
            }
            if(tlvMap.containsKey("9B")) {
                tag9B = tlvMap.get("9B").getValue();
            }
            if(tlvMap.containsKey("DF51")) {
                tagDF51 = tlvMap.get("DF51").getValue();
            }
            if(tlvMap.containsKey("9F5D")) {
                tagDF51 = tlvMap.get("9F5D").getValue();
            }
            tvr.setText(Utils.empty2Dash(tag95));
            tsi.setText(Utils.empty2Dash(tag9B));
            sr.setText(Utils.empty2Dash(tagDF51));
            String data = getTransAmount(Utils.empty2Dash(tag9F5D));
            if(data.equals("--")) {
                offlineBallance.setVisibility(View.INVISIBLE);
                ballanceTxt.setVisibility(View.INVISIBLE);
            }else {
                offlineBallance.setText(getTransAmount(Utils.empty2Dash(tag9F5D)));
            }

            if(tag95.length()==10){
                btmp = Utils.hexStr2Bytes(tag95);
                stmp = Utils.getEigthBitsStringFromByte(btmp[0]&0xFF) + " " + Utils.getEigthBitsStringFromByte(btmp[1]&0xFF) + " " + Utils.getEigthBitsStringFromByte(btmp[2]&0xFF) + " "
                        + Utils.getEigthBitsStringFromByte(btmp[3]&0xFF) + " " + Utils.getEigthBitsStringFromByte(btmp[4]&0xFF);
                tvrbits.setText(Utils.empty2Dash(stmp));
            }

            if(tag9B.length()==4){
                btmp = Utils.hexStr2Bytes(tag9B);
                stmp = Utils.getEigthBitsStringFromByte(btmp[0]&0xFF) + " " + Utils.getEigthBitsStringFromByte(btmp[1]&0xFF);
                tsibits.setText(Utils.empty2Dash(stmp));
            }

            //保存上次交易结果
            LastTransResultBean result = new LastTransResultBean(transResult.getDepicter(), tag95, tag9B, tagDF51);
            PreferencesUtil.setLastTransactionResult(result);
        }
        updateDBTransferLog();
        updateDBBatchUploadData();

    }

    private String getTransAmount(String dbAmount) {
        try {
            Log.e(TAG, "dbAmount =  " + dbAmount);
            if(Objects.equals(dbAmount, "--"))
                return dbAmount;
            float value = Float.parseFloat(dbAmount);
            return String.format(Locale.getDefault(), "%.2f", value / 100);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "--";
    }

    /**
     * 更新交易记录到本地数据库
     * Update transaction records to the local database
     */
    private void updateDBTransferLog() {
        //联机/脱机批准时才保存交易日志,非交易批准不保存交易记录
        if (EMVTransResult.ONLINE_APPROVE != transResult
                && EMVTransResult.OFFLINE_APPROVE != transResult) {
            enableConfirm();
            return;
        }
        Log.e(TAG, "--------updateDBTransferLog");
        String[] tags = {"9A", "9F21", "5F2A", "9F02", "9F4E", "9F36", "5A"};
        Map<String, TLV> tlvMap = readKernelData(tags);
        if (tlvMap.isEmpty()) {
            return;
        }
        String cardnum ="00";
        if (tlvMap.get("5A") != null) { //更新累计交易金额
            cardnum = tlvMap.get("5A").getValue();
        }

        if (tlvMap.get("9F02") != null) { //更新累计交易金额
            String amount = tlvMap.get("9F02").getValue();
            if (!TextUtils.isEmpty(amount) && TextUtils.isDigitsOnly(amount)) {
                PreferencesUtil.addAccumulatedAmount(Long.valueOf(amount), cardnum);
            }
        }
        //更新交易记录
        EMVTransferLog entity = new EMVTransferLog();
        fillTagEntityByTlvMap(tags, tlvMap, entity);
        Log.e(TAG, "update db transfer log：" + entity);
        DBSessionManager.getDaoSession().getEMVTransferLogDao().rx().insert(entity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<EMVTransferLog>() {
                    @Override
                    public void call(EMVTransferLog transLog) {
                        Log.e(TAG, "update db transfer log success.");
                        enableConfirm();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "update db transfer log failed：" + t.getMessage());
                        showToast("update transfer log error：" + t.getMessage());
                    }
                });
    }

    /** 更新批上送数据到本地数据库 */
    private void updateDBBatchUploadData() {
        if (EMVTransResult.ONLINE_APPROVE == transResult
                || EMVTransResult.OFFLINE_APPROVE == transResult) {
            flag_batch = 1;
        }
        else{
            flag_batch = 0;
        }
        if (!"01".equals(PreferencesUtil.getFunSetDataCollect())) {//非批数据采集模式时不保存批数据记录
            Log.e(TAG, "--------updateDBBatUploadData skipped,data_collect=" + PreferencesUtil.getFunSetDataCollect());
            enableConfirm();
            return;
        }
        Log.e(TAG, "--------updateDBBatUploadData");

        String[] tags = {"82","84", "9F36", "9F07", "9F27", "8E", "9F34", "9F1E", "9F0D",
                "9F0E", "9F0F", "9F10","9F11", "9F33", "9F35", "95", "9B", "9F26", "9F37",
                "9F01", "9F02", "9F03","9F09","9F0B", "5F25", "5F24","5F20", "50", "5A", "5F34", "5F28","5F2D", "9F12", "9F15", "9F16",
                "9F1A", "9F1C", "57", "81", "5F2A", "9A", "9F21", "9C", "9F24", "9F19", "9F06","9F39", "8A"};
        Map<String, TLV> tlvMap = readKernelData(tags);
        if (tlvMap.isEmpty()) {
            return;
        }
        EMVBatchUploadData entity = new EMVBatchUploadData();
        fillTagEntityByTlvMap(tags, tlvMap, entity);

        Log.e(TAG, "update db bat upload data：" + entity);
        DBSessionManager.getDaoSession().getEMVBatchUploadDataDao().rx().insert(entity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<EMVBatchUploadData>() {
                    @Override
                    public void call(EMVBatchUploadData transLog) {
                        Log.e(TAG, "update db bat upload data success.");
                        enableConfirm();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "update db bat upload data failed：" + t.getMessage());
                        showToast("update db bat upload data error：" + t.getMessage());
                    }
                });
    }

    /** emv读内核数据 */
    private Map<String, TLV> readKernelData(String[] tags) {
        try {
            byte[] outBuf = new byte[2048];
            int len = BaseApp.Companion.getEmvOpt().readKernelData(tags, outBuf);
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

    /** 根据Tag设置Entity个字段的值 */
    private void fillTagEntityByTlvMap(String[] tags, Map<String, TLV> map, Object entity) {
        try {
            if (CollectionUtil.isEmpty(tags) || CollectionUtil.isEmpty(map)) {
                return;
            }
            Class<?> cls = entity.getClass();
            for (String tag : tags) {//设置各个tag字段的值
                Log.d(TAG, "fillTagEntityByTlvMap tag: " + tag.toUpperCase());
                Field field = null;
                try {
                    field = cls.getDeclaredField("tag" + tag.toUpperCase());
                } catch (NoSuchFieldException e) {
                    field = null;
                }
                Log.d(TAG, "fillTagEntityByTlvMap field: " + field);
                if (field == null) {
                    continue;
                }
                field.setAccessible(false);
                if (map.get(tag) != null) {
                    if(map.get(tag).getLength() == 0){
                        continue;
                    }
                    Log.d(TAG, "fillTagEntityByTlvMap field setAccessible ");
                    field.setAccessible(true);
                    field.set(entity, map.get(tag).getValue());
                }
            }

            //设置timeStamp字段的值
            Field field = cls.getDeclaredField("timeStamp");
            if (field != null) {
                field.setAccessible(true);
                field.set(entity, new Date());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使能倒计时关闭页面按钮
     * Enable a button that can be used to count down the closing of the page
     */
    private void enableConfirm() {
        latch.countDown();
        if (latch.getCount() == 0) {
            confirm.setEnabled(true);
            //startCountdown(15);
            if (BaseApp.Companion.getAutoTest()) {
                startCountdown(Integer.valueOf(PreferencesUtil.getFunSetAutoTestInterval()).intValue()); // 20180613 自动测试时间间隔
            }
            else{
                startCountdown(10);
            }
        }
    }

    /**
     * 倒计时关闭页面
     * Countdown to closing the page
     */
    private void startCountdown(final int count) {

        confirm.setText(String.format(Locale.getDefault(), "%s(%d)", "ok", count));

        if (count == 0) {
            finish();
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startCountdown(count - 1);
                }
            }, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_AUTO_TEST));
        super.onDestroy();
    }
}
