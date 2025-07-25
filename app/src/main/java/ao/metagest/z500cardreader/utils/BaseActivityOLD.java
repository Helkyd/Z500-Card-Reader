package ao.metagest.z500cardreader.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import ao.metagest.z500cardreader.R;

/**
 * Function ： 基类Activity
 */
public abstract class BaseActivityOLD extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    private Handler dlgHandler = new Handler();

    /** 显示LoadDlg */
    protected void showLoadDlg() {
        showLoadDlg(getString(R.string.base_loadding));
    }

    /** 显示LoadDlg */
    protected void showLoadDlg(final String msg) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (loadDialog == null) {
//                    loadDialog = new LoadDialog(BaseActivity.this, msg);
//                }
//                if (!loadDialog.isShowing()) {
//                    loadDialog.show();
//                }
//            }
//        });
    }

    /** 显示倒计时LoadDlg */
    protected void showCountdownLoadDlg(String msg, int count) {
        showLoadDlg(String.format(Locale.getDefault(), "%s(%d)", msg, count));
        updateCountdownLoadDlgMsg(msg, count);
    }

    /** 更新倒计时LoadDlg文本 */
    private void updateCountdownLoadDlgMsg(final String msg, final int count) {
        if (count == 0) {
            dismissLoadDlg();
        } else {
            dlgHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    loadDialog.changeText(String.format(Locale.getDefault(), "%s(%d)", msg, count));
                    updateCountdownLoadDlgMsg(msg, count - 1);
                }
            }, 1000);
        }
    }

    /** 取消LoadDlg */
    protected void dismissLoadDlg() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (loadDialog != null && loadDialog.isShowing()) {
//                    loadDialog.dismiss();
//                }
                dlgHandler.removeCallbacksAndMessages(null);
            }
        });
    }

    /** 更新LoadDlg消息 */
    protected void changeLoadDlgMsg(final String newMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (loadDialog != null) {
//                    loadDialog.changeText(newMsg);
//                }
            }
        });
    }

    /** 显示toast */
    protected void showToast(String msg) {
        showToastOnUI(msg);
    }

    /** 显示toast */
    protected void showToast(int resId) {
        showToastOnUI(getString(resId));
    }

    /** 在UI线程显示toast */
    private void showToastOnUI(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivityOLD.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 跳转页面 */
    protected void openActivity(Class<? extends Activity> cls) {
        openActivity(new Intent(this, cls), false);
    }

    /** 跳转页面 */
    protected void openActivity(Class<? extends Activity> cls, boolean finishSelf) {
        openActivity(new Intent(this, cls), finishSelf);
    }

    /** 跳转页面 */
    protected void openActivity(Intent intent) {
        openActivity(intent, false);
    }

    /** 跳转页面 */
    protected void openActivity(Intent intent, boolean finishSelf) {
        startActivity(intent);
        if (finishSelf) {
            finish();
        }
    }

}
