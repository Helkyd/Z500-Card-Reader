package ao.metagest.z500cardreader


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import ao.metagest.z500cardreader.databinding.ActivityInitBinding
import pos.paylib.posPayKernel

class InitActivity : AppCompatActivity() {

    private val binding : ActivityInitBinding by viewBinding()
    //private var mSMPayKernel: SunmiPayKernel? = null
    private var mPosPayKernel: posPayKernel? = null
    public var isConnect: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)


        mPosPayKernel = posPayKernel.getInstance()
        mPosPayKernel!!.initPaySDK(this,object : posPayKernel.ConnectCallback {
            override fun onDisconnectPaySDK() {

                isConnect = false;
                BaseApp.emvOpt = null;
                BaseApp.basicOpt = null;
                BaseApp.pinPadOpt = null;
                BaseApp.readCardOpt = null;
                BaseApp.pedOpt = null;
                BaseApp.taxOpt = null;
                BaseApp.printerOpt = null;
                BaseApp.syscardOpt = null;

                Log.e("dd--", "DISCONNECT SDK INIT SUCCESSFUL")
            }
            override fun onConnectPaySDK() {
                try {

                    isConnect = true;
                    BaseApp.emvOpt = mPosPayKernel?.mEmvOpt;
                    BaseApp.basicOpt = mPosPayKernel?.mBasicOpt;
                    BaseApp.pinPadOpt = mPosPayKernel?.mPinpadOpt;
                    BaseApp.readCardOpt = mPosPayKernel?.mReadcardOpt;
                    BaseApp.pedOpt = mPosPayKernel?.mPedOpt;
                    BaseApp.taxOpt = mPosPayKernel?.mTaxOpt;
                    BaseApp.printerOpt = mPosPayKernel?.mPrintOpt;
                    BaseApp.syscardOpt = mPosPayKernel?.mSysCardOpt;

                    Log.e("dd--", "SDK INIT SUCCESSFUL")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })


        binding.button.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }

    }


}