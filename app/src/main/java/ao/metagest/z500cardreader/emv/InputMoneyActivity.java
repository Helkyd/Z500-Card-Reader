package ao.metagest.z500cardreader.emv;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.util.Log;

import com.ciontek.hardware.aidl.bean.TransDataV2;
//import com.ctk.sdk.DebugLogUtil;

import ao.metagest.z500cardreader.MainActivity;
import ao.metagest.z500cardreader.utils.BaseActivity;
import ao.metagest.z500cardreader.BaseApp;
import ao.metagest.z500cardreader.R;
import ao.metagest.z500cardreader.utils.MoneyUtils;

import ao.metagest.z500cardreader.view.AmountInputView;
import ao.metagest.z500cardreader.view.NumberKeyboard;

import ao.metagest.z500cardreader.utils.PreferencesUtil;

//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.BaseActivity;
//import test.metagestdemo.activity.R;
//import test.metagestdemo.activity.utils.MoneyUtils;
//import test.metagestdemo.activity.utils.PreferencesUtil;
//import test.metagestdemo.activity.view.AmountInputView;
//import test.metagestdemo.activity.view.NumberKeyboard;

/**
 * 消费输入消费金额
 * When making a purchase, enter the amount spent
 */
public class InputMoneyActivity extends BaseActivity implements View.OnClickListener {
    private AmountInputView amountInputView;
    private AmountInputView amountBackInputView;

    private Boolean inputCashbackFlag = false;
    private String amountAuth;
    private String amountOther;
    private static final String TAG = "InputMoneyActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputmoney);
        initView();
        //BaseApp.newTransFlag = 1;
        BaseApp.Companion.setNewTransFlag(1);

    }

    private void initView() {
        RelativeLayout rl_bottom = findViewById(R.id.rl_bottom);
        amountInputView = findViewById(R.id.amountInputView);
        NumberKeyboard numberKeyboard = findViewById(R.id.keyboard);

        showToast(String.valueOf(R.string.entrymoney));
        rl_bottom.setOnClickListener(this);
        numberKeyboard.setKeyClickCallback(new NumberKeyboard.KeyClickCallback() {
            @Override
            public void onNumClick(int keyNum) {
                amountInputView.addText(keyNum + "");
            }

            @Override
            public void onDelClick() {
                amountInputView.delLast();
            }

            @Override
            public void onCleanClick() {
                amountInputView.clean();
            }
        });

    }

    private void inputCashbackMoney(){
        showToast(getString(R.string.input_cashback_amount));
        inputCashbackFlag = true;
        amountBackInputView = findViewById(R.id.amountInputView);
        NumberKeyboard numberKeyboard1 = findViewById(R.id.keyboard);
        numberKeyboard1.setKeyClickCallback(new NumberKeyboard.KeyClickCallback() {
            @Override
            public void onNumClick(int keyNum) {
                amountBackInputView.addText(keyNum + "");
            }

            @Override
            public void onDelClick() {
                amountBackInputView.delLast();
            }

            @Override
            public void onCleanClick() {
                amountBackInputView.clean();
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_bottom) {
            // 未输入返现金额，提示输入
            //Cashback amount is not entered, prompt for input
            if ((PreferencesUtil.getFunSetTransactionType().equals("09")) && (!inputCashbackFlag)) {
                amountAuth = amountInputView.getAmountText().toString();
                amountInputView.clean();
                inputCashbackMoney();
            } else {
                // 非返现交易或已输入返现金额的返现交易
                //Non-cashback transactions or cashback transactions in which the cashback amount has been entered
                if (!inputCashbackFlag) {
                    amountAuth = amountInputView.getAmountText().toString();
                } else {
                    amountOther = amountBackInputView.getAmountText().toString();
                }
                Log.e(TAG,"amountAuth: "+amountAuth);
                Log.e(TAG,"amountOther: "+amountOther);
                btnPayClick();
            }
        }
    }

    private void btnPayClick() {
        String amount = amountAuth;

        long amountOther = 0;
        long amountAuth;
        if(amount != null) {
            amountAuth = MoneyUtils.stringMoney2LongCent(amount);
        }
        else{
            amountAuth = 1;
            amount = "0.01";
        }
        if(PreferencesUtil.getFunSetTransactionType().equals("09")){ //  cashback
            String amountBack = this.amountOther;
            if(amountBack != null) {
                amountOther = MoneyUtils.stringMoney2LongCent(amountBack);
            }
            else{
                amountOther = 1;
            }
            amount = MoneyUtils.longCent2DoubleMoneyStr(amountOther+amountAuth);
        }

        TransDataV2 TransData = new TransDataV2();
        Log.d(TAG, "btnPayClick amount: " + amount);
        if (amount.equals("0.00") && PreferencesUtil.getFunSetZeroAmtSupport().equals("00")) {
            showToast(String.valueOf(R.string.entrymoney));
        }else {
            TransData.amount = MoneyUtils.stringMoney2LongCent(amount) + "";
            Intent intent = new Intent(this, SwingCardActivity.class);
            intent.putExtra("TransData", TransData);
            intent.putExtra("amount", TransData.amount);//string - has point '.'
            intent.putExtra("amountAuth", amountAuth + amountOther);//long - has not point '.'
            intent.putExtra("amountOther", amountOther);//long - has not point '.'
            Log.d(TAG,"goto SwingCardActivity");
            //BaseActivity.Companion.openActivity(intent, true);
            BaseActivity.Companion.openActivity(this, intent, true);
        }
    }

}

