package ao.metagest.z500cardreader.Callback

//import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2
import com.ciontek.hardware.aidl.pinpad.IInputPinCallback
import com.ciontek.hardware.aidl.pinpad.PinpadOpt
open class PinPadCallback : IInputPinCallback.Stub(){

    override fun onInputResult(p0: Int, p1: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onKeyPress(p0: Byte) {
        TODO("Not yet implemented")
    }
}

open class PinpadOptCallback : PinpadOpt.Stub(){
    override fun SetCustomLayout(p0: IntArray?): Int {
        TODO("Not yet implemented")
    }

    override fun GetPinKey(p0: ByteArray?): Int {
        TODO("Not yet implemented")
    }

    override fun ServicesCallContactEmvPinblock(
        p0: Int,
        p1: Int,
        p2: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun ServicesPinInputResultPost(p0: Int, p1: ByteArray?): Int {
        TODO("Not yet implemented")
    }

    override fun ServiceSetInputPinCallback(
        p0: Int,
        p1: IInputPinCallback?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun ServiceGetPinBlock(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: ByteArray?,
        p4: ByteArray?,
        p5: ByteArray?,
        p6: Int,
        p7: Int,
        p8: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun ServiceGetDukptPinBlock(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: ByteArray?,
        p4: ByteArray?,
        p5: ByteArray?,
        p6: Int,
        p7: Int,
        p8: Int
    ): Int {
        TODO("Not yet implemented")
    }

}