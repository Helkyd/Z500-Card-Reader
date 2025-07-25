package ao.metagest.z500cardreader

//Z500 PayLib

import android.app.Application
import com.ciontek.hardware.aidl.emv.EMVOptV2
import com.ciontek.hardware.aidl.ped.PedOpt
import com.ciontek.hardware.aidl.pinpad.PinpadOpt
import com.ciontek.hardware.aidl.print.PrinterOpt
import com.ciontek.hardware.aidl.readcard.ReadCardOptV2
import com.ciontek.hardware.aidl.sysCard.SysCardOpt
import com.ciontek.hardware.aidl.system.SysBaseOpt
import com.ciontek.hardware.aidl.tax.TaxOpt


class BaseApp : Application() {


    //@JvmField
    //var basicOpt: SysBaseOpt.Stub.Proxy = TODO("initialize me")

    companion object{

        lateinit var app: BaseApp
            public set  // Keep setter private if needed

        public var newTransFlag: Int = 0
        public var autoTest: Boolean = false
        /**
         * 获取基础操作模块
         * Gets the basic action module
         */
        var basicOpt: SysBaseOpt? = null

        /**
         * 获取读卡模块
         * Gets the module that reads the card
         */
        var readCardOpt: ReadCardOptV2? = null

        /**
         * 获取PinPad操作模块
         * Get the pinpad operation module
         */
        var pinPadOpt: PinpadOpt? = null

        /**
         * 获取Ped操作模块
         * Get the ped operation module
         */
        var pedOpt: PedOpt? = null

        /**
         * 获取Printer操作模块
         * The module that gets control of the printer
         */
        var printerOpt: PrinterOpt? = null

        /**
         * 获取Tax操作模块
         * Get the module for tax control
         */
        var taxOpt: TaxOpt? = null

        /**
         * 获取EMV操作模块
         * Gets the module that EMV operates on
         */
        var emvOpt: EMVOptV2? = null


        /**
         * 获取系统卡模块
         * The module that gets the card used by the system
         */
        var syscardOpt: SysCardOpt? = null

        //private val mPosPayKernel: posPayKernel? = null

    }
    override fun onCreate() {
        super.onCreate()
        app = this
    }
}