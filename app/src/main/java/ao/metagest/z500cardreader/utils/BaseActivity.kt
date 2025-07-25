package ao.metagest.z500cardreader.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private val dlgHandler = Handler()
    companion object {
        /**
         * Opens a new activity with optional extras
         * @param context The context to start from (usually 'this' from Activities)
         * @param target The activity class to open
         * @param extras Optional bundle of extras
         * @param clearTask If true, clears the back stack
         */
        /*
        fun openActivity(
            context: Context,
            target: Class<*>,
            extras: Bundle? = null,
            clearTask: Boolean = false
        ) {
            val intent = Intent(context, target).apply {
                extras?.let { putExtras(it) }
                if (clearTask) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
            context.startActivity(intent)
        }

         */
        // Original version
        fun openActivity(context: Context, target: Class<*>, extras: Bundle? = null, clearTask: Boolean = false) {
            val intent = Intent(context, target).apply {
                extras?.let { putExtras(it) }
                if (clearTask) {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
            context.startActivity(intent)
        }

        // New version for prepared Intents
        fun openActivity(context: Context, intent: Intent, clearTask: Boolean = false) {
            if (clearTask) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
        // Overload for simple case without extras
        fun openActivity(context: Context, target: Class<*>) {
            openActivity(context, target, null)
        }

    }

    /** 显示toast  */
    protected fun showToast(msg: String?) {
        showToastOnUI(msg)
    }

    /** 显示toast  */
    protected fun showToast(context: Context, resId: Int) {
        showToastOnUI(context.getString(resId))
    }

    /** 在UI线程显示toast  */
    private fun showToastOnUI(msg: String?) {
        runOnUiThread(object : Runnable {
            override fun run() {
                Toast.makeText(this@BaseActivity, msg, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** 取消LoadDlg  */
    protected fun dismissLoadDlg() {
        runOnUiThread(object : Runnable {
            override fun run() {
//                if (loadDialog != null && loadDialog.isShowing()) {
//                    loadDialog.dismiss();
//                }
                dlgHandler.removeCallbacksAndMessages(null)
            }
        })
    }
}