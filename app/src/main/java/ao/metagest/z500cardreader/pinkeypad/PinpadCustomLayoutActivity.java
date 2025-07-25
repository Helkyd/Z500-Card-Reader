package ao.metagest.z500cardreader.pinkeypad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ciontek.hardware.aidl.pinpad.IInputPinCallback;

import java.lang.reflect.Method;

import pos.paylib.data.keySystem;
import pos.paylib.data.keyType;

//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.BuildConfig;
//import test.metagestdemo.activity.R;
//import test.metagestdemo.activity.utils.Log;
//import test.metagestdemo.activity.utils.Utils;
//import com.ctk.sdk.ByteUtil;

import ao.metagest.z500cardreader.util.ByteUtil;
import ao.metagest.z500cardreader.BaseApp;
import ao.metagest.z500cardreader.MainActivity;
import ao.metagest.z500cardreader.R;
import ao.metagest.z500cardreader.utils.Utils;
import ao.metagest.z500cardreader.BuildConfig;

public class PinpadCustomLayoutActivity extends Activity {
    private static final String TAG = "PinpadCustLayoutAct";

    private static final byte KEYCODE_DIGIT = 0;
    private static final byte KEYCODE_CONFIRM = 13;
    private static final byte KEYCODE_CLEAR = -2;
    public static final byte KEYCODE_CANCEL = 14;

    public static final byte KEYCODE_TIMEOUT = -5;

    //online- pinblock keypad input status return code
    public static final int INPUT_ACTION_DONE = 0;
    public static final int INPUT_ACTION_BACK = -1;
    public static final int INPUT_ACTION_NOFORCUS = -2;
    public static final int INPUT_ACTION_NOINPUT = -3;
    public static final int INPUT_ACTION_CANCEL = -4;
    public static final int INPUT_ACTION_TIMEOUT = -5;
    public static final int INPUT_ACTION_NO_INITIAL = -6;
    public static final int INPUT_ACTION_BYPINPASS = -7;
    public static final int INPUT_ACTION_PIN_ILLEGAL = -8;


    private View mPinContainer;
    private View mKeyboard;
    private Button mKeyboard_0;
    private Button mKeyboard_1;
    private Button mKeyboard_2;
    private Button mKeyboard_3;
    private Button mKeyboard_4;
    private Button mKeyboard_5;
    private Button mKeyboard_6;
    private Button mKeyboard_7;
    private Button mKeyboard_8;
    private Button mKeyboard_9;
    private Button mKeyboard_cancel;
    private Button mKeyboard_clear;
    private Button mKeyboard_confirm;
    private TextView mTextPin, mTextPrompt;

    private WorkHandler mWorkHandler;
    private HandlerThread mWorkThread;
    private boolean mThreadFinished = false;

    private static final int MESSAGE_SHOW_KEYPAD = 1;
    private static final int MESSAGE_CLOSE_KEYPAD = 2;
    private static final int MESSAGE_INFO_UPDATE = 3;
    private static final int MESSAGE_PIN_INPUT_CLEAR = 4;
    private static final int MESSAGE_PIN_INPUT_DIGIT = 5;
    private static final int MESSAGE_SELECT_APP = 6;
    private static  int TransType = 0;
    private static  int disableTab = 0;

    private int pinPadType; //0 =softkeypad,  8 =physical keyboard
    private int pinType = -1;
    private int LcdSize = 0;
    private String cardNo;


    //update mTextPin
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {

                case MESSAGE_SHOW_KEYPAD:
                    mPinContainer.setVisibility(View.VISIBLE);
                    mTextPrompt.setText("Please enter PIN:");
                    break;

                case MESSAGE_CLOSE_KEYPAD:
                    mTextPin.setText("");
                    mPinContainer.setVisibility(View.INVISIBLE);
                    break;
                case MESSAGE_PIN_INPUT_CLEAR:
                    int len = mTextPin.getText().toString().length();
                    if(len <=1){
                        mTextPin.setText("");
                    }else{
                        String str = mTextPin.getText().toString().substring(0,len -1);
                        mTextPin.setText(str);
                    }
                    break;
                case MESSAGE_PIN_INPUT_DIGIT:
                    Log.d(TAG, "-CustomizeKeypad----MESSAGE_PIN_INPUT_DIGIT ****");
                    mTextPin.setText(mTextPin.getText().toString() + "*");
                    break;

                default:
                    break;
            }
        }
    };

    private int LcdSize()
    {
        int LcdSize = 0;
        try {
            Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("getInt", String.class, int.class);
            int lcdSize = (int) get.invoke(c, "ro.build.lcd_size", -1);
            if (lcdSize == 55) {
                Log.d("debug", "lcdSize: " + lcdSize);
                LcdSize = 55;
            } else
            if (lcdSize == 40) {//CS10F
                Log.d("debug", "lcdSize: " + lcdSize);
                LcdSize = 40;
            } else {
                Log.e("debug", "illegal lcdSize: " + lcdSize);
                LcdSize = 0;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return LcdSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableFunctionLaunch(true);
        LcdSize = LcdSize();
        pinPadType = getIntent().getIntExtra("pinPadType", 0);
        pinType = getIntent().getIntExtra("pinType", 0);
        cardNo = getIntent().getStringExtra("cardNo");

        setContentView(R.layout.activity_pinpad_custom_layout);

        mPinContainer = findViewById(R.id.keypad);
        mKeyboard = findViewById(R.id.keyboard_view);
        if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40))
        {
            mKeyboard.setVisibility(View.GONE);//use hardware keyboard for PIN input
        }
        else
        {
            mKeyboard_1 = findViewById(R.id.keyboard_1);
            mKeyboard_2 = findViewById(R.id.keyboard_2);
            mKeyboard_3 = findViewById(R.id.keyboard_3);
            mKeyboard_4 = findViewById(R.id.keyboard_4);
            mKeyboard_5 = findViewById(R.id.keyboard_5);
            mKeyboard_6 = findViewById(R.id.keyboard_6);
            mKeyboard_7 = findViewById(R.id.keyboard_7);
            mKeyboard_8 = findViewById(R.id.keyboard_8);
            mKeyboard_9 = findViewById(R.id.keyboard_9);
            mKeyboard_0 = findViewById(R.id.keyboard_0);
            mKeyboard_cancel = findViewById(R.id.keyboard_cancel);
            mKeyboard_clear = findViewById(R.id.keyboard_clear);
            mKeyboard_confirm = findViewById(R.id.keyboard_confirm);
        }

        mTextPin = findViewById(R.id.textPin);
        mTextPrompt = findViewById(R.id.textPrompt);

        mWorkThread = new HandlerThread("sdk_pinblock_thread");
        mWorkThread.start();
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());
        Intent intent = getIntent();

        pinType = intent.getIntExtra("pinType", 0);
        Log.d(TAG, "-pinType----> "+ pinType);
        if(pinType ==10){
            startPinpadTest();
        } else
        if(pinType == 1){
            startOFFLINE_PinblockTest();
        }
        else
        {
            //startONLINEPIN_MKSKPinblockTest();   //MKSK
            startONLINEPIN_DUKPTPinblockTest();  //DUKPT
        }
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "-RunOrStop----onResume");
        super.onResume();

    }
    @Override
    protected void onPause() {
        Log.d(TAG, "-RunOrStop----onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mThreadFinished = true;

        mWorkHandler.removeCallbacksAndMessages(null);
        mWorkThread.quitSafely();
        Log.d(TAG, "-RunOrStop----onPause");
        disableFunctionLaunch(false);
        super.onDestroy();
    }

    private void startPinpadTest(){
        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_TEST_PINPAD);
    }
    private void startOFFLINE_PinblockTest() {
        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_TEST_OFFLINEPIN);
    }

    private void startONLINEPIN_MKSKPinblockTest() {
        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_TEST_ONLINEPIN_MKSKTDESPINBLOCK);
    }

    private void startONLINEPIN_DUKPTPinblockTest() {
        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_TEST_ONLINEPIN_DUKPTTDESPINBLOCK);
    }



    private PinpadLayoutEntity getPinpadLayoutEntity() {
        if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40))
        {
            return null;
        }
        else {
            PinpadLayoutEntity pinpadLayout = new PinpadLayoutEntity();

            int[] location = new int[2];
            Rect r;
            mKeyboard_1.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_1.getWidth() + r.left;
            r.bottom = mKeyboard_1.getHeight() + r.top;
            pinpadLayout.setKey1(r);

            mKeyboard_2.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_2.getWidth() + r.left;
            r.bottom = mKeyboard_2.getHeight() + r.top;
            pinpadLayout.setKey2(r);

            mKeyboard_3.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_3.getWidth() + r.left;
            r.bottom = mKeyboard_3.getHeight() + r.top;
            pinpadLayout.setKey3(r);

            mKeyboard_4.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_4.getWidth() + r.left;
            r.bottom = mKeyboard_4.getHeight() + r.top;
            pinpadLayout.setKey4(r);

            mKeyboard_5.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_5.getWidth() + r.left;
            r.bottom = mKeyboard_5.getHeight() + r.top;
            pinpadLayout.setKey5(r);

            mKeyboard_6.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_6.getWidth() + r.left;
            r.bottom = mKeyboard_6.getHeight() + r.top;
            pinpadLayout.setKey6(r);

            mKeyboard_7.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_7.getWidth() + r.left;
            r.bottom = mKeyboard_7.getHeight() + r.top;
            pinpadLayout.setKey7(r);

            mKeyboard_8.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_8.getWidth() + r.left;
            r.bottom = mKeyboard_8.getHeight() + r.top;
            pinpadLayout.setKey8(r);

            mKeyboard_9.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_9.getWidth() + r.left;
            r.bottom = mKeyboard_9.getHeight() + r.top;
            pinpadLayout.setKey9(r);

            mKeyboard_0.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_0.getWidth() + r.left;
            r.bottom = mKeyboard_0.getHeight() + r.top;
            pinpadLayout.setKey10(r);

            mKeyboard_cancel.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_cancel.getWidth() + r.left;
            r.bottom = mKeyboard_cancel.getHeight() + r.top;
            pinpadLayout.setKeyCancel(r);

            mKeyboard_clear.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_clear.getWidth() + r.left;
            r.bottom = mKeyboard_clear.getHeight() + r.top;
            pinpadLayout.setKeyClear(r);

            mKeyboard_confirm.getLocationOnScreen(location);
            r = new Rect();
            r.left = location[0];
            r.top = location[1];
            r.right = mKeyboard_confirm.getWidth() + r.left;
            r.bottom = mKeyboard_confirm.getHeight() + r.top;
            pinpadLayout.setKeyConfirm(r);

            return pinpadLayout;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "-onBackPressed----onBackPressed");

        if(disableTab == 0)
            super.onBackPressed();
    }

    public byte[] setPinpadLayout(PinpadLayoutEntity var1) {
        Log.d(TAG, "setPinpadLayout ");
        try {
            if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40))
            {

            }
            else
            {
                if (var1 == null) {
                    return null;
                } else {
                    int[] var2 = new int[60];
                    byte var3 = 0;
                    int var8 = var3 + 1;
                    var2[var3] = var1.getKey1().left;
                    var2[var8++] = var1.getKey1().top;
                    var2[var8++] = var1.getKey1().right;
                    var2[var8++] = var1.getKey1().bottom;
                    var2[var8++] = var1.getKey2().left;
                    var2[var8++] = var1.getKey2().top;
                    var2[var8++] = var1.getKey2().right;
                    var2[var8++] = var1.getKey2().bottom;
                    var2[var8++] = var1.getKey3().left;
                    var2[var8++] = var1.getKey3().top;
                    var2[var8++] = var1.getKey3().right;
                    var2[var8++] = var1.getKey3().bottom;
                    var2[var8++] = var1.getKeyCancel().left;
                    var2[var8++] = var1.getKeyCancel().top;
                    var2[var8++] = var1.getKeyCancel().right;
                    var2[var8++] = var1.getKeyCancel().bottom;
                    var2[var8++] = var1.getKey4().left;
                    var2[var8++] = var1.getKey4().top;
                    var2[var8++] = var1.getKey4().right;
                    var2[var8++] = var1.getKey4().bottom;
                    var2[var8++] = var1.getKey5().left;
                    var2[var8++] = var1.getKey5().top;
                    var2[var8++] = var1.getKey5().right;
                    var2[var8++] = var1.getKey5().bottom;
                    var2[var8++] = var1.getKey6().left;
                    var2[var8++] = var1.getKey6().top;
                    var2[var8++] = var1.getKey6().right;
                    var2[var8++] = var1.getKey6().bottom;
                    var2[var8++] = var1.getKeyClear().left;
                    var2[var8++] = var1.getKeyClear().top;
                    var2[var8++] = var1.getKeyClear().right;
                    var2[var8++] = var1.getKeyClear().bottom;
                    var2[var8++] = var1.getKey7().left;
                    var2[var8++] = var1.getKey7().top;
                    var2[var8++] = var1.getKey7().right;
                    var2[var8++] = var1.getKey7().bottom;
                    var2[var8++] = var1.getKey8().left;
                    var2[var8++] = var1.getKey8().top;
                    var2[var8++] = var1.getKey8().right;
                    var2[var8++] = var1.getKey8().bottom;
                    var2[var8++] = var1.getKey9().left;
                    var2[var8++] = var1.getKey9().top;
                    var2[var8++] = var1.getKey9().right;
                    var2[var8++] = var1.getKey9().bottom;
                    var2[var8++] = -1;
                    var2[var8++] = -1;
                    var2[var8++] = -1;
                    var2[var8++] = -1;
                    var2[var8++] = var1.getKey10().left;
                    var2[var8++] = var1.getKey10().top;
                    var2[var8++] = var1.getKey10().right;
                    var2[var8++] = var1.getKey10().bottom;
                    var2[var8++] = -1;
                    var2[var8++] = -1;
                    var2[var8++] = -1;
                    var2[var8++] = -1; //var2[55]--> random keypad == 1
                    var2[var8++] = var1.getKeyConfirm().left;
                    var2[var8++] = var1.getKeyConfirm().top;
                    var2[var8++] = var1.getKeyConfirm().right;
                    var2[var8++] = var1.getKeyConfirm().bottom;

                    String result = " ";
                    for (int i = 0; i < var2.length; i++) {
                        result += " " + var2[i];

                    }

                    int retCode = BaseApp.Companion.getPinPadOpt().SetCustomLayout(var2);
                    if (retCode != 0) {
                        return null;
                    } else {
                        byte[] digits = new byte[10];
                        retCode = BaseApp.Companion.getPinPadOpt().GetPinKey(digits);
                        if (retCode != 0) {
                            return null;
                        } else {
                            return digits;
                        }
                    }
                }
            }
        } catch (RemoteException var7) {
            var7.printStackTrace();
        }
        return null;
    }

    public void DisplayPinpadLayout(final boolean isOnline, //unused
                                    int numberAttemptsRemaining) {
        Log.e(TAG, "onCardHolderInputPin(); isPinOnline: " + isOnline +
                ", numberAttemptsRemaining: " + numberAttemptsRemaining);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Pin container is showed");
                mHandler.sendEmptyMessage(MESSAGE_SHOW_KEYPAD);
                if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40))
                {

                }
                else
                {
                    //get positions of custom pin keys
                    final PinpadLayoutEntity pinpadLayout = getPinpadLayoutEntity();

                    //The UI of PIN KEYPAD is implemented in the application layer.
                    // The function of obtaining the online/offline PIN in the EMV Kernel calls the CreateKeyPad function of the service layer.
                    // The CreateKeyPad function parses the pressed keyboard value and combines it into PIN value, and the application layer draws “ * ”
                    final byte[] number = setPinpadLayout(pinpadLayout);
                    if (number != null) {
                        Log.d(TAG, "numbers : " + number.length);
                        mKeyboard_1.setText(String.valueOf(number[1]));
                        mKeyboard_2.setText(String.valueOf(number[2]));
                        mKeyboard_3.setText(String.valueOf(number[3]));
                        mKeyboard_4.setText(String.valueOf(number[4]));
                        mKeyboard_5.setText(String.valueOf(number[5]));
                        mKeyboard_6.setText(String.valueOf(number[6]));
                        mKeyboard_7.setText(String.valueOf(number[7]));
                        mKeyboard_8.setText(String.valueOf(number[8]));
                        mKeyboard_9.setText(String.valueOf(number[9]));
                        mKeyboard_0.setText(String.valueOf(number[0]));
                    } else {
                        mHandler.sendEmptyMessage(MESSAGE_CLOSE_KEYPAD);
                        //todo: cancel pin screen
                        Log.e(TAG, "failure creating custom layout");
                    }
                }
            }
        }, 50);

    }
    private IInputPinCallback mIInputPinCallback = new IInputPinCallback.Stub() {
        @Override
        public void onInputResult(int result, byte[] pinBlock) throws RemoteException {
            Log.e(TAG,"onInputResult result "+result+" PinBlock " + (pinBlock != null ? ByteUtil.bytearrayToHexString(pinBlock, pinBlock.length) : ""));
            mHandler.sendEmptyMessage(MESSAGE_CLOSE_KEYPAD);
            String dispmsg = (result == 0)?"entry pin success":"entry pin failed";
            finish(); //White screen, need to destroy the current activity
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), dispmsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        @Override
        public void onKeyPress(byte key) throws RemoteException {
            Log.e(TAG,"onKeyPress");
            switch (key) {
                case KEYCODE_CONFIRM:
                    Log.d(TAG, "Confirm");
                    mHandler.sendEmptyMessage(MESSAGE_CLOSE_KEYPAD);
                    onConfirm();
                    finish();
                    break;
                case KEYCODE_CANCEL:
                    Log.d(TAG, "Cancel");
                    mHandler.sendEmptyMessage(MESSAGE_CLOSE_KEYPAD);
                    onCanncel();
                    finish();
                    break;
                case KEYCODE_CLEAR:
                    Log.d(TAG, "Clear");
                    mHandler.sendEmptyMessage(MESSAGE_PIN_INPUT_CLEAR);
                    break;
                case KEYCODE_DIGIT:
                    Log.d(TAG, "CustomizeKeypad digit");
                    mHandler.sendEmptyMessage(MESSAGE_PIN_INPUT_DIGIT);
                    break;
                case KEYCODE_TIMEOUT:
                    Log.d(TAG,"timeout");
                    mHandler.sendEmptyMessage(MESSAGE_CLOSE_KEYPAD);
                    onCanncel();
                    finish();

            }
        }
    };
    private void onCanncel(){
        // online pin
        if (0 == pinType) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onCancel online pin");
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(0,1);//1 -PIN cancle
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        // offline pin
        if (1 == pinType) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onCancel offline pin");
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(1,1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    private void onError(){
        // online pin
        if (0 == pinType) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onError online pin");
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(0, 3);//3 -PINPAD breakdown
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        // offline pin
        if (1 == pinType) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onError offline pin");
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(1, 3);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void onConfirm() {
        // online pin
        if (0 == pinType) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onConfirm online pin");
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(0, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        // offline pin
        if (1 == pinType) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onConfirm offline pin");
                    BaseApp.Companion.getEmvOpt().importPinInputStatus(1, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private class WorkHandler extends Handler {

        public static final int MSG_TEST_ONLINEPIN_MKSKTDESPINBLOCK = 1;
        public static final int MSG_TEST_ONLINEPIN_DUKPTTDESPINBLOCK = 2;
        public static final int MSG_TEST_OFFLINEPIN = 3;

        public static final int MSG_TEST_PINPAD = 4;

        public WorkHandler(Looper looper) {
            super(looper);
        }

        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            int ret;
            Log.e(TAG, "vpos*****************WorkHandler EmvGetPinBlock--what= "+what);

            switch (what) {
                case MSG_TEST_PINPAD:
 //                   pinPadType = 10; //soft keyboard and just for pinpad test, independent of EMV process
//                    pinPadType = 8; //soft keyboard and just for pinpad test, independent of EMV process
                    if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40))
                        pinPadType = 2; //cs10F
                    else
                        pinPadType = 0; //cs10F softpinpad
                    try {
                        DisplayPinpadLayout(true,3);
                        BaseApp.Companion.getPinPadOpt().ServiceSetInputPinCallback(60,mIInputPinCallback);
                        ret = BaseApp.Companion.getPinPadOpt().ServicesCallContactEmvPinblock(pinPadType,0,12);
                        if(ret < 0){
                            onError();
                        }
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                    break;
                case MSG_TEST_OFFLINEPIN:
                    ///pinPadType = 0; //soft keyboard

                    if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40)) {
                        pinPadType = 2;
                    }
                    else {
                        pinPadType = 0;//soft keyboard
                    }
                    try {
                        DisplayPinpadLayout(true,3);
                        BaseApp.Companion.getPinPadOpt().ServiceSetInputPinCallback(60,mIInputPinCallback);
                        ret = BaseApp.Companion.getPinPadOpt().ServicesCallContactEmvPinblock(pinPadType,0,12);
                        Log.e(TAG, "BaseApp.Companion.getPinPadOpt().ServicesCallContactEmvPinblock ret"+ret);
                        if(ret < 0){
                            onError();
                        }
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                    break;

                case MSG_TEST_ONLINEPIN_MKSKTDESPINBLOCK:
                    int pinkey_n = 0;
                    Log.e("TAG", "vpos*****************WorkHandler EmvGetPinBlock--1");
                    Log.e(TAG, "cardNo:"+cardNo);
                    if(cardNo == null)
                            cardNo = "2200240699301061";
                    byte[] card_no = Utils.string2ByteArray(cardNo);
                    byte[] mode = new byte[]{1};
                    byte[] pin_block = new byte[8];
                    Log.e(TAG, "EmvGetPinBlock --00 pan=" +card_no[0]);

                    if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40)) {
                        pinPadType = 2;
                    }
                    else {
                        pinPadType = 0;//soft keyboard
                    }

                    try {
                        DisplayPinpadLayout(false,3);
                        BaseApp.Companion.getPinPadOpt().ServiceSetInputPinCallback(60,mIInputPinCallback);

                        BaseApp.Companion.getPinPadOpt().ServiceGetPinBlock(keySystem.MS_DES.getkeySystem(),
                                pinPadType,pinkey_n,card_no,mode,pin_block,0,12,60);
                        Log.d(TAG,"mksk pinblock: "+ Utils.Bcd2String(pin_block) + "card_no: "+ Utils.Bcd2String(card_no));
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                    disableFunctionLaunch(false);
                    Log.e(TAG, "EmvGetPinBlock --01");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), show, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case MSG_TEST_ONLINEPIN_DUKPTTDESPINBLOCK:
                    int dukptpinkey_n = 2;
                    Log.e(TAG, "vpos*****************WorkHandler EmvGetDukptPinblock--1");
                    byte[] dukptpin_block = new byte[8];
                    byte[] dukptOutKsn = new byte[10];
                    Log.d(TAG, "EmvGet--00");

                    if(cardNo == null)
                        cardNo = "2200240699301061";
                    byte[] dukptcard_no = Utils.string2ByteArray(cardNo);
                    if((BuildConfig.FLAVOR.equals("CS10F")) && (LcdSize==40))
                        pinPadType = 2;
                    else
                        pinPadType = 0;

                    disableFunctionLaunch(false);
                    Log.d(TAG, "EmvGetDukptPinblock --01");
                    try {
                        DisplayPinpadLayout(false,3);
                        BaseApp.Companion.getPinPadOpt().ServiceSetInputPinCallback(60,mIInputPinCallback);
                        BaseApp.Companion.getPinPadOpt().ServiceGetDukptPinBlock(keySystem.DUKPT.getkeySystem(),
                                pinPadType, dukptpinkey_n,  dukptcard_no,dukptpin_block,dukptOutKsn,0,12 ,60);
                        Log.d(TAG,"dukpt pinblock: "+ Utils.Bcd2String(dukptpin_block)+"; KSN: "+ Utils.Bcd2String(dukptOutKsn));
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), showdukpt, Toast.LENGTH_SHORT).show();
                        }
                    });

                    break;

                default:
                    disableFunctionLaunch(false);
                    break;
            }
        }
    }

    private void disableFunctionLaunch(boolean state) {
        if (state) {
            disableTab = 1;
            Settings.System.putInt(getApplicationContext().getContentResolver(), "is_autotest_test_keys", 1);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });

        } else {
            disableTab = 0;
            Settings.System.putInt(getApplicationContext().getContentResolver(), "is_autotest_test_keys", 0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    }


}