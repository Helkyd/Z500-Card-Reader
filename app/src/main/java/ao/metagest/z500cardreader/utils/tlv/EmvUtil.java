package ao.metagest.z500cardreader.utils.tlv;


import com.ciontek.hardware.aidl.bean.AidV2;
import com.ciontek.hardware.aidl.bean.CapkV2;
import com.ciontek.hardware.aidl.bean.EmvTermParamV2;
import com.ciontek.hardware.aidl.bean.RevocListV2;
import com.ciontek.hardware.aidl.emv.EMVOptV2;

//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.utils.Log;
//import test.metagestdemo.activity.utils.Utils;

import ao.metagest.z500cardreader.BaseApp;
import ao.metagest.z500cardreader.utils.Utils;
import android.util.Log;
        
import java.util.HashMap;
import java.util.Map;

public final class EmvUtil {

    public static final String COUNTRY_CHINA = "China";
    public static final String COUNTRY_RUSSIA = "Russia";
    public static final String COUNTRY_INDIA = "India";

    private EmvUtil() {
    }

    /**
     * Get configuration by country
     */
    public static Map<String, String> getConfig(String country) {
        Map<String, String> map = new HashMap<>();
        map.put("countryCode", "0156");     // country code(国家代码)
        map.put("capability", "E0F8C8");    // capability(终端性能)
        map.put("5F2A", "0156");            // transaction currency code(交易货币代码)
        map.put("5F36", "00");              // transaction currency code exponent(交易货币代码指数)
        switch (country) {
            case COUNTRY_RUSSIA:
                map.put("countryCode", "0643");
                map.put("capability", "E0F8C8");
                map.put("5F2A", "0643");
                map.put("5F36", "00");
                break;
            case COUNTRY_INDIA:
                map.put("countryCode", "0356");
                map.put("capability", "E0F8C8");
                map.put("5F2A", "0356");
                map.put("5F36", "02");
                break;
        }
        return map;
    }


    /**
     * Initialize AIDs and RIDs
     */
    public static void initAidAndRid() {
        try {
            EMVOptV2 emvOptV2 = BaseApp.Companion.getEmvOpt(); // MyApplication.app.emvOpt;
            // Normal AIDs
            AidV2 aid = EmvUtil.hexStr2Aid("9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF21061000000000005F2A020643");
            emvOptV2.addAid(aid);


            // Normal RIDs
            CapkV2 capkV2 = EmvUtil.hexStr2Rid("9F0605A0000003339F220104DF05083230323531323331DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5");
            emvOptV2.addCapk(capkV2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set EMV terminal parameters
     */
    public static void setTerminalParam(Map<String, String> map) {
        try {
            EMVOptV2 emvOptV2 = BaseApp.Companion.getEmvOpt(); //MyApplication.app.emvOpt;

            EmvTermParamV2 emvTermParam = new EmvTermParamV2();
            emvTermParam.countryCode = map.get("countryCode");
            emvTermParam.capability = map.get("capability");
            int result = emvOptV2.setTerminalParam(emvTermParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AidV2 hexStr2Aid(String hexStr) {
        AidV2 aidV2 = new AidV2();


        Map<String, TLV> map = TLVUtils.builderTLVMap(hexStr);
        Log.e("TAG", "hexStr2Aid : 9F06");
        TLV tlv = map.get("9F06");
        if (tlv != null) {
            aidV2.aid = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF01");
        tlv = map.get("DF01");
        if (tlv != null) {
            aidV2.selFlag = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F09");
        tlv = map.get("9F09");
        if (tlv != null) {
            aidV2.version = Utils.hexStr2Bytes(tlv.getValue());
        }
        Log.e("TAG", "hexStr2Aid : DF11");
        tlv = map.get("DF11");
        if (tlv != null) {
            aidV2.TACDefault = Utils.hexStr2Bytes(tlv.getValue());
        }
        Log.e("TAG", "hexStr2Aid : DF12");
        tlv = map.get("DF12");
        if (tlv != null) {
            aidV2.TACOnline = Utils.hexStr2Bytes(tlv.getValue());
        }
        Log.e("TAG", "hexStr2Aid : DF13");
        tlv = map.get("DF13");
        if (tlv != null) {
            aidV2.TACDenial = Utils.hexStr2Bytes(tlv.getValue());
        }
        Log.e("TAG", "hexStr2Aid : DF15");
        tlv = map.get("DF15");
        if (tlv != null) {
            aidV2.threshold = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF16");
        tlv = map.get("DF16");
        if (tlv != null) {
            aidV2.maxTargetPer = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF17");
        tlv = map.get("DF17");
        if (tlv != null) {
            aidV2.targetPer = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF14");
        tlv = map.get("DF14");
        if (tlv != null) {
            aidV2.dDOL = Utils.hexStr2Bytes(tlv.getValue());
        }
        Log.e("TAG", "hexStr2Aid : 9F1B");
        tlv = map.get("9F1B");
        if (tlv != null) {
            aidV2.floorLimit = Utils.hexStr2Bytes(tlv.getValue());
        }
        Log.e("TAG", "hexStr2Aid : 9F7B");
        tlv = map.get("9F7B");
        if (tlv != null) {
            aidV2.termOfflineFloorLmt = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF19");
        tlv = map.get("DF19");
        if (tlv != null) {
            aidV2.termClssOfflineFloorLmt = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF20");
        tlv = map.get("DF20");
        if (tlv != null) {
            aidV2.termClssLmt = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF21");
        tlv = map.get("DF21");
        if (tlv != null) {
            aidV2.cvmLmt = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F01");
        tlv = map.get("9F01");
        if (tlv != null) {
            aidV2.AcquierId = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F4E");
        tlv = map.get("9F4E");
        if (tlv != null) {
            aidV2.merchName = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F15");
        tlv = map.get("9F15");
        if (tlv != null) {
            aidV2.merchCateCode = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F16");
        tlv = map.get("9F16");
        if (tlv != null) {
            aidV2.merchId = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F3C");
        tlv = map.get("9F3C");
        if (tlv != null) {
            aidV2.referCurrCode = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : 9F3D");
        tlv = map.get("9F3D");
        if (tlv != null) {
            aidV2.referCurrExp = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DFC108");
        tlv = map.get("DFC108");
        if (tlv != null) {
            aidV2.clsStatusCheck = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DFC109");
        tlv = map.get("DFC109");
        if (tlv != null) {
            aidV2.zeroCheck = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF8101");
        tlv = map.get("DF8101");
        if (tlv != null) {
            aidV2.referCurrCon = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DF8102");
        tlv = map.get("DF8102");
        if (tlv != null) {
            aidV2.tDOL = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DFC10A");
        tlv = map.get("DFC10A");
        if (tlv != null) {
            aidV2.kernelType = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DFC10B");
        tlv = map.get("DFC10B");
        if (tlv != null) {
            aidV2.paramType = Utils.hexStr2Byte(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DFC10C");
        tlv = map.get("DFC10C");
        if (tlv != null) {
            aidV2.kernelID = Utils.hexStr2Bytes(tlv.getValue());
        }

        Log.e("TAG", "hexStr2Aid : DFC10D");
        tlv = map.get("DFC10D");
        if (tlv != null) {
            aidV2.extSelectSupFlg = Utils.hexStr2Byte(tlv.getValue());
        }
        return aidV2;
    }

    public static CapkV2 hexStr2Rid(String hexStr) {
        CapkV2 capkV2 = new CapkV2();
        Map<String, TLV> map = TLVUtils.builderTLVMap(hexStr);
        TLV tlv = map.get("9F06");
        if (tlv != null) {
            capkV2.rid = Utils.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F22");
        if (tlv != null) {
            capkV2.index = Utils.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF06");
        if (tlv != null) {
            capkV2.hashInd = Utils.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF07");
        if (tlv != null) {
            capkV2.arithInd = Utils.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF02");
        if (tlv != null) {
            capkV2.modul = Utils.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF04");
        if (tlv != null) {
            capkV2.exponent = Utils.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF05");
        if (tlv != null) {
            capkV2.expDate = Utils.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF03");
        if (tlv != null) {
            capkV2.checkSum = Utils.hexStr2Bytes(tlv.getValue());
        }
        return capkV2;
    }

    public static RevocListV2 hexStr2RevocList(String hexStr) {
        RevocListV2 revocListV2 = new RevocListV2();
        Map<String, TLV> map = TLVUtils.builderTLVMap(hexStr);
        TLV tlv = map.get("9F06");
        if (tlv != null) {
            revocListV2.rid = Utils.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("8F");
        if (tlv != null) {
            revocListV2.index = Utils.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF8105");
        if (tlv != null) {
            revocListV2.sn = Utils.hexStr2Bytes(tlv.getValue());
        }
        return revocListV2;
    }


}
