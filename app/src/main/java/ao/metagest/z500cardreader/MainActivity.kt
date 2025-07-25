package ao.metagest.z500cardreader

//import EmvUtil

//Z500 PayLIB

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ao.metagest.z500cardreader.Callback.CheckCardCallback
import ao.metagest.z500cardreader.Callback.EMVCallback
import ao.metagest.z500cardreader.Callback.PinPadCallback
import ao.metagest.z500cardreader.databinding.ActivityMainBinding
import ao.metagest.z500cardreader.emv.InputMoneyActivity
import ao.metagest.z500cardreader.util.ApduResp
import ao.metagest.z500cardreader.util.ApduSend
import ao.metagest.z500cardreader.util.ByteUtil
import ao.metagest.z500cardreader.util.TLV
import ao.metagest.z500cardreader.util.TLVUtil
import ao.metagest.z500cardreader.utils.BaseActivity
import com.ciontek.hardware.aidl.AidlConstantsV2
import com.ciontek.hardware.aidl.bean.EMVCandidateV2
import com.ciontek.hardware.aidl.bean.EMVTransDataV2
import com.ciontek.hardware.aidl.emv.EMVOptV2
import com.ciontek.hardware.aidl.ped.PedOpt
import com.ciontek.hardware.aidl.pinpad.PinpadOpt
import com.ciontek.hardware.aidl.readcard.CheckCardCallbackV2
import com.ciontek.hardware.aidl.readcard.ReadCardOptV2
import com.ciontek.hardware.aidl.sysCard.SysCardOpt
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    
    private val TAG: String = "SwingCardActivity"
    private val binding : ActivityMainBinding by viewBinding()

    //private var readCardOpt: ReadCardOptV2 = BaseApp.readCardOpt!!
    private var readCardOpt: ReadCardOptV2 = BaseApp.readCardOpt!!

    //private var emvOpt: EMVOptV2 = BaseApp.emvOpt!!
    private var emvOpt: EMVOptV2 = BaseApp.emvOpt!!

    //private var pinPadOpt: PinPadOptV2 = BaseApp.pinPadOpt!!
    private var pinPadOpt: PinpadOpt = BaseApp.pinPadOpt!!

    var pedOpt: PedOpt? = BaseApp.pedOpt!!

    var syscardOpt: SysCardOpt? = BaseApp.syscardOpt!!

    //FOR UI
    private val cardType = MutableLiveData<String>()
    private val result = MutableLiveData<String>()

    private val amount = "40000"
    private var mCardNo: String = ""
    private var mCardType = 0
    private var mPinType: Int? = null
    private var mCertInfo: String = ""

    public var strInfo = "";
    private val ATR: ByteArray = ByteArray(40)
    private val vcc_mode: Byte = 1




    override fun onStart() {
        super.onStart()

        //EmvUtil().init()
        Log.e(TAG,"Inicia o EMV....")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //show card type
        cardType.observe(this, Observer {
            binding.txtType.text = it
        })

        //show result
        result.observe(this, Observer {
            binding.txtResult.text = it
        })

        binding.btnTransacao.setOnClickListener {
            Log.e("dd--","Button TRANSACAO TESTE clicked")
            //BaseActivityOLD.openActivity(InputMoneyActivity::class.java)
            BaseActivity.openActivity(this@MainActivity, InputMoneyActivity::class.java)

        }

        binding.btnICC.setOnClickListener {
            Log.e("dd--","Button ICC TESTE clicked")
            startTestIcc(0)
        }
        binding.btnIC.setOnClickListener {
            checkCard(AidlConstantsV2.CardType.IC.value)
        }

        binding.btnMSC.setOnClickListener {
            checkCard(AidlConstantsV2.CardType.MAGNETIC.value)
        }

        binding.btnNFC.setOnClickListener {
            checkCard(AidlConstantsV2.CardType.NFC.value)
        }

        binding.btnAll.setOnClickListener {
            val cardType: Int =
                AidlConstantsV2.CardType.MAGNETIC.value or AidlConstantsV2.CardType.NFC.value or
                        AidlConstantsV2.CardType.IC.value
            checkCard(cardType)
        }


    }

    //Z500
    fun startTestIcc(slot: Byte) {
        var ret = 1
        var dataIn: ByteArray? = ByteArray(512)

        if (slot.toInt() == 0) {
            try {
                ret = BaseApp.syscardOpt?.sysIccCheck(slot)!! // MyApplication.app.syscardOpt.sysIccCheck(slot)
                if (ret != 0) {
                    runOnUiThread(object : Runnable {
                        override fun run() {
                            //tv_msg.setText("CPU Check Failed")
                            Log.e("dd--","CPU CHECK FAILED")
                        }
                    })

                    return
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        try {
            ret = BaseApp.syscardOpt?.sysIccOpen(slot, vcc_mode, ATR)!! //MyApplication.app.syscardOpt.sysIccOpen(slot, vcc_mode, ATR)
            if (ret != 0) {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //tv_msg.setText("Open Failed")
                        Log.e("dd--","OPEN FAILED")
                    }
                })
                //Log.e(TAG, "IccOpen failed!")
                Log.e("dd--","ICCCC OPEN FAILED")
                return
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }


        var atrString = ""
        for (i in ATR?.indices!!) {
            atrString += Integer.toHexString(ATR[i].toInt().toString().toInt())
                .replace("f".toRegex(), "")
        }
        Log.d("dd--", "atrString = " + ByteUtil.bytearrayToHexString(ATR, ATR.size))



        val cmd: ByteArray? = ByteArray(4)
        var lc: Short = 0
        var le: Short = 0

        if (slot.toInt() == 0) {
            cmd!![0] = 0x00.toByte() //0-3 cmd
            cmd[1] = 0xa4.toByte()
            cmd[2] = 0x04
            cmd[3] = 0x00
            lc = 0x05
            le = 0x00

            dataIn!![0] = 0x49.toByte()
            dataIn[1] = 0x47.toByte()
            dataIn[2] = 0x54.toByte()
            dataIn[3] = 0x50.toByte()
            dataIn[4] = 0x43.toByte()
        } else {
            cmd!![0] = 0x00 //0-3 cmd
            cmd[1] = 0x84.toByte()
            cmd[2] = 0x00
            cmd[3] = 0x00
            lc = 0x00
            le = 0x08
            val sendmsg = ""
            dataIn = sendmsg.toByteArray()
            Log.e("liuhao Icc  ", "PSAM *******")
        }

        val mApduSend: ApduSend = ApduSend(cmd, lc, dataIn, le)
        var mApduResp: ApduResp? = null
        val resp = ByteArray(516)

        try {
            ret = BaseApp.syscardOpt?.sysIccCommand(slot, mApduSend.bytes, resp)!!
            if (0 == ret) {
                mApduResp = ApduResp(resp)
                strInfo =
                    (ByteUtil.bytearrayToHexString(mApduResp.dataOut, mApduResp.lenOut.toInt()) + "SWA:"
                            + ByteUtil.byteToHexString(mApduResp.sWA) + " SWB:" + ByteUtil.byteToHexString(
                        mApduResp.sWB
                    ))
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //tv_msg.setText(strInfo)
                        Log.e("dd--","SYSICC COMMAND $strInfo")
                        cardType.value = "Type: IC"
                        result.value = "Result: " + ByteUtil.bytearrayToHexString(ATR, ATR.size)
                        Log.e("dd--", "Type:IC")


                    }
                })
            } else {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        //tv_msg.setText("Command Failed")
                        Log.e("dd-","command Failed...")
                    }
                })
                Log.e("dd-", "Icc_Command failed!")

            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        try {
            BaseApp.syscardOpt?.sysIccClose(slot)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

        try {
            Thread.sleep(200)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    private fun checkCard(cardType: Int) {
        try {
            emvOpt.abortTransactProcess()
            emvOpt.initEmvProcess()

            readCardOpt.checkCard(cardType, mCheckCardCallback, 60)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mCheckCardCallback: CheckCardCallbackV2 = object : CheckCardCallback() {
        @SuppressLint("SetTextI18n")
        override fun findICCard(atr: String) {
            super.findICCard(atr)

            runOnUiThread {
                cardType.value = "Type: IC"
                result.value = "Result: $atr"
                Log.e("dd--", "Type:IC")
                Log.e("dd--", "ID: $atr")


                mCardType = AidlConstantsV2.CardType.IC.value
                transactProcess()
            }
        }

        @SuppressLint("SetTextI18n")
        override fun findMagCard(info: Bundle) {
            super.findMagCard(info)

            val track1 = info.getString("TRACK1")
            val track2 = info.getString("TRACK2")
            val track3 = info.getString("TRACK3")

            runOnUiThread {

                cardType.value = "Type: Magnetic"
                result.value =
                    "Result:\n Track 1: $track1 \nTrack 2: $track2 \nTrack 3: $track3 \n"
                Log.e("dd--", "Type:Magnetic")
                Log.e("dd--", "ID: ${result.value}")


                mCardType = AidlConstantsV2.CardType.MAGNETIC.value
            }
        }

        @SuppressLint("SetTextI18n")
        override fun findRFCard(uuid: String) {
            super.findRFCard(uuid)

            runOnUiThread {

                cardType.value = "Type: NFC"
                result.value = "Result:\n UUID: $uuid"
                Log.e("dd--", "Type:NFC")
                Log.e("dd--", "ID: $uuid")

                mCardType = AidlConstantsV2.CardType.NFC.value
                transactProcess()

            }
        }

        override fun onError(code: Int, message: String) {
            super.onError(code, message)
            val error = "onError:$message -- $code"
            println("Error : $error")
        }
    }

    private fun transactProcess() {
        Log.e("dd--", "transactProcess")
        try {
            val emvTransData = EMVTransDataV2()
            emvTransData.amount = amount //in cent (9F02)
            emvTransData.flowType = 1 //1 Standard Flow, 2 Simple Flow, 3 QPass
            emvTransData.cardType = mCardType
            emvOpt.transactProcess(emvTransData, mEMVCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mEMVCallback = object : EMVCallback(){

        override fun onWaitAppSelect(p0: MutableList<EMVCandidateV2>?, p1: Boolean) {
            super.onWaitAppSelect(p0, p1)

            Log.e("dd--", "onWaitAppSelect isFirstSelect:$p1")

            //Debit Card might have 2 AID
            //Priority 1 should be 'Debit <MS/VISA>'
            //Priority 2 should be 'ATM'
            p0?.forEach {
                Log.e("dd--", "EMVCandidate:$it")
            }
            //default take 1 priority
            emvOpt.importAppSelect(0)
        }

        override fun onAppFinalSelect(p0: String?) {
            super.onAppFinalSelect(p0)

            Log.e("dd--", "onAppFinalSelect value:$p0")


            val tags = arrayOf("5F2A", "5F36", "9F33", "9F66")
            val value = arrayOf("0458", "00", "E0F8C8", "B6C0C080")
            emvOpt.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, value)


            if (p0 != null && p0.isNotEmpty()){
                val isVisa = p0.startsWith("A000000003")
                val isMaster =
                    (p0.startsWith("A000000004") || p0.startsWith("A000000005"))

                if (isVisa){
                    // VISA(PayWave)
                    Log.e("dd--", "detect VISA card")
                }else if(isMaster){

                    // MasterCard(PayPass)
                    Log.e("dd--", "detect MasterCard card")
                    val tagsPayPass = arrayOf(
                        "DF8117", "DF8118", "DF8119", "DF811B", "DF811D",
                        "DF811E", "DF811F", "DF8120", "DF8121", "DF8122",
                        "DF8123", "DF8124", "DF8125", "DF812C"
                    )
                    val valuesPayPass = arrayOf(
                        "E0", "F8", "F8", "30", "02",
                        "00", "E8", "F45084800C", "0000000000", "F45084800C",
                        "000000000000", "999999999999", "999999999999", "00"
                    )
                    emvOpt.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tagsPayPass, valuesPayPass)

                    //Reader CVM Required Limit (Malaysia => RM250)
                    emvOpt.setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS,"DF8126","000000025000")

                }

            }
            emvOpt.importAppFinalSelectStatus(0)
        }

        override fun onConfirmCardNo(p0: String?) {
            super.onConfirmCardNo(p0)
            Log.e("dd--", "onConfirmCardNo cardNo:$p0")
            mCardNo = p0!!
            emvOpt.importCardNoStatus(0)
        }

        override fun onRequestShowPinPad(p0: Int, p1: Int) {
            super.onRequestShowPinPad(p0, p1)
            Log.e("dd--", "onRequestShowPinPad pinType:$p0 remainTime:$p1")
            // 0 - online pin, 1 - offline pin
            mPinType = p0
            //initPidPad()
            Log.e("dd--","INIT PID PAD not ready....")
        }

        override fun onCertVerify(p0: Int, p1: String?) {
            super.onCertVerify(p0, p1)
            Log.e("dd--", "onCertVerify certType:$p0 certInfo:$p1")
            mCertInfo = p1.toString()
            emvOpt.importCertStatus(p0)
        }

        override fun onOnlineProc() {
            super.onOnlineProc()
            Log.e("dd--", "onOnlineProc")
            try{

                if(mCardType != AidlConstantsV2.CardType.MAGNETIC.value){
                    getTlvData()
                }
                importOnlineProcessStatus(0)

            }catch (e:Exception){
                e.printStackTrace()
                importOnlineProcessStatus(-1)
            }

        }

        override fun onTransResult(p0: Int, p1: String?) {
            super.onTransResult(p0, p1)
            //Code = 0 (Success)
            Log.e("dd--", "onTransResult code:$p0 desc:$p1")
        }
        
    }


    private fun initPidPad(){
        Log.e("dd--", "initPinPad")
        try {
            val pinPadConfig = PinPadConfig()
            pinPadConfig.pinPadType = 0
            pinPadConfig.pinType = mPinType!!
            pinPadConfig.isOrderNumKey = true
            val panBytes = mCardNo.substring(mCardNo.length - 13, mCardNo.length - 1)
                .toByteArray(StandardCharsets.US_ASCII)
            pinPadConfig.pan = panBytes
            pinPadConfig.timeout = 60 * 1000 // input password timeout
            pinPadConfig.pinKeyIndex = 12 // pik index (0-19)
            pinPadConfig.maxInput = 12
            pinPadConfig.minInput = 4
            pinPadConfig.keySystem = 0 // 0 - MkSk 1 - DuKpt
            pinPadConfig.algorithmType = 0 // 0 - 3DES 1 - SM4
            //pinPadOpt.initPinPad(pinPadConfig, mPinPadCallback)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private val mPinPadCallback = object: PinPadCallback(){
        override fun onInputResult(p0: Int, p1: ByteArray?) {
            Log.e("dd--","POR FAZERO onInputResult")
        }

    }


    private fun importPinInputStatus(inputResult: Int) {
        Log.e("dd--", "importPinInputStatus:$inputResult")
        try {
            emvOpt.importPinInputStatus(mPinType!!, inputResult)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTlvData() {
        try {
            val tagList = arrayOf(
                "DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26",
                "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F03", "9F33", "9F10", "9F37", "9C",
                "9A", "9F02", "5F2A", "5F36", "82", "9F34", "9F35", "9F1E", "84", "4F", "9F09", "9F41",
                "9F63", "5F20", "9F12", "50"
            )
            //Only Mastercard have this extra tag
            val payPassTags = arrayOf(
                "DF811E",
                "DF812C",
                "DF8118",
                "DF8119",
                "DF811F",
                "DF8117",
                "DF8124",
                "DF8125",
                "9F6D",
                "DF811B",
                "9F53",
                "DF810C",
                "9F1D",
                "DF8130",
                "DF812D",
                "DF811C",
                "DF811D",
                "9F7C"
            )
            val outData = ByteArray(2048)
            val map: MutableMap<String, TLV> = HashMap()
            var len = emvOpt.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData)
            if (len > 0) {
                val hexStr = ByteUtil.bytes2HexStr(outData.copyOf(len))
                map.putAll(TLVUtil.hexStrToTLVMap(hexStr))
            }
            len = emvOpt.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData)
            if (len > 0) {
                val hexStr = ByteUtil.bytes2HexStr(outData.copyOf(len))
                map.putAll(TLVUtil.hexStrToTLVMap(hexStr))
            }

            // https://emvlab.org/emvtags/all/ refer this as TLV data
            // Eg: 5F24 -> Expire date
            // Eg: 5F20 -> Card holder
            var temp = ""
            val set: Set<String> = map.keys
            set.forEach {
                val tlv = map[it]
                temp += if (tlv != null) {
                    "$it : ${tlv.value} \n"
                } else {
                    "$it : \n"
                }
            }
            Log.e("dd--", "TLV: $temp")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun importOnlineProcessStatus(status: Int) {
        Log.e("dd--", "importOnlineProcessStatus status:$status")
        try {
            val tags = arrayOf("71", "72", "91", "8A", "89")
            val values = arrayOf("", "", "", "", "")
            val out = ByteArray(1024)
            val len = emvOpt.importOnlineProcStatus(status, tags, values, out)
            if (len < 0) {
                Log.e("dd--", "importOnlineProcessStatus error,code:$len")
            } else {
                val bytes = out.copyOf(len)
                val hexStr = ByteUtil.bytes2HexStr(bytes)
                Log.e("dd--", "importOnlineProcessStatus outData:$hexStr")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        readCardOpt.cancelCheckCard()
    }



}
class PinPadConfig : Parcelable {
    var pinPadType: Int = 0 // PinPad type. 0:Default Pinpad 1:Self-defined Pinpad
    var pinType: Int = 0 // Pin type(0: Online PIN,1:Offline PIN)
    var isOrderNumKey: Boolean = false // true: Normal Pinpad; false: Random Pinpad
    var pan: ByteArray? = null // Ascii format to byte. eg. "123456".getbytes("us ascii")
    var pinKeyIndex: Int = 0 // Pin Key Index
    var maxInput: Int = 6 // Maximum password input(max 12 numbers)
    var minInput: Int = 0 // Minimum password input
    var timeout: Int = 60000 // Time out/millisecond
    var isSupportbypass: Boolean = true // support bypasspin?
    var pinblockFormat: Int = 0 // pinblock format
    var algorithmType: Int = 0 // Encrypted Pin algorithm type 0-3DES(returns 8 bytes), 1-SM4(returns 16 bytes)
    var keySystem: Int = 0 // The key system to which Pik currently belongs is 0-SEC_MKSK, 1-SEC_DUKPT

    constructor()

    protected constructor(parcel: Parcel) {
        pinPadType = parcel.readInt()
        pinType = parcel.readInt()
        isOrderNumKey = parcel.readByte() != 0.toByte()
        pan = parcel.createByteArray()
        pinKeyIndex = parcel.readInt()
        maxInput = parcel.readInt()
        minInput = parcel.readInt()
        timeout = parcel.readInt()
        isSupportbypass = parcel.readByte() != 0.toByte()
        pinblockFormat = parcel.readInt()
        algorithmType = parcel.readInt()
        keySystem = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(pinPadType)
        parcel.writeInt(pinType)
        parcel.writeByte(if (isOrderNumKey) 1 else 0)
        parcel.writeByteArray(pan)
        parcel.writeInt(pinKeyIndex)
        parcel.writeInt(maxInput)
        parcel.writeInt(minInput)
        parcel.writeInt(timeout)
        parcel.writeByte(if (isSupportbypass) 1 else 0)
        parcel.writeInt(pinblockFormat)
        parcel.writeInt(algorithmType)
        parcel.writeInt(keySystem)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PinPadConfig> {
        override fun createFromParcel(parcel: Parcel): PinPadConfig {
            return PinPadConfig(parcel)
        }

        override fun newArray(size: Int): Array<PinPadConfig?> {
            return arrayOfNulls(size)
        }
    }
}