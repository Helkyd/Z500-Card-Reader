package ao.metagest.z500cardreader.utils.tlv;

//import test.metagestdemo.activity.utils.Utils;
import ao.metagest.z500cardreader.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TLV工具类
 *
 * @author Administrator on 2016/9/5.
 */
public class TLVUtils {

    private final static String TAG = "TLVUtils";

    /**
     * 将16进制字符串转换为TLV对象列表
     */
    public static List<TLV> builderTLVList(String hexString) {
        List<TLV> list = new ArrayList<>();

        int position = 0;

        while (position != hexString.length()) {
            String tag = getTag(hexString, position);
            if (tag.equals("00")) {
                break;
            }

            position += tag.length();

            LPosition l_position = getLengthAndPosition(hexString, position);

            int _vl = l_position.get_vL();

            position = l_position.get_position();

            String _value = hexString.substring(position, position + _vl * 2);

            position = position + _value.length();

            list.add(new TLV(tag, _vl, _value));
        }
        return list;
    }

    /**
     * 将16进制字符串转换为TLV对象列表
     */
    public static List<TLV> builderTLVList(byte[] hexByte) {
        String hexString = Utils.bytes2HexString(hexByte);
        return builderTLVList(hexString);
    }

    /**
     * 将16进制字符串转换为TLV对象MAP
     */
    public static Map<String, TLV> builderTLVMap(String hexString) {

        // TLV文档连接 http://wenku.baidu.com/view/b31b26a13186bceb18e8bb53.html?re=view&qq-pf-to=pcqq.c2c
        Map<String, TLV> map = new HashMap<>();

        int position = 0;
        while (position < hexString.length()) {
            String tag = getTag(hexString, position).toUpperCase();
            if (tag.equals("00")) {
                break;
            }
            position += tag.length();

            LPosition l_position = getLengthAndPosition(hexString, position);

            int _vl = l_position.get_vL();
            position = l_position.get_position();
            String _value = hexString.substring(position, position + _vl * 2);
            position = position + _value.length();
            System.out.println("TLV-builderTLVMap" + tag + ": " + _value);

            if (map.get(tag) == null) {
                map.put(tag, new TLV(tag, _vl, _value));
            }
        }

        return map;
    }

    /**
     * 将字节数组转换为TLV对象MAP
     *
     * @param hexByte byte数据格式的TLV数据
     * @return TLV数据Map
     */
    public static Map<String, TLV> builderTLVMap(byte[] hexByte) {
        String hexString = Utils.bytes2HexString(hexByte);
        return builderTLVMap(hexString);
    }

    /**
     * 返回最后的Value的长度
     */
    private static LPosition getLengthAndPosition(String hexString, int pos) {
        int position = pos;
        String hexLength;

        String firstByteString = hexString.substring(position, position + 2);
        int i = Integer.parseInt(firstByteString, 16);

        // Length域的编码比较简单,最多有四个字节, 
        // 如果第一个字节的最高位b8为0, b7~b1的值就是value域的长度. 
        // 如果b8为1, b7~b1的值指示了下面有几个子字节. 下面子字节的值就是value域的长度.
        if (((i >> 7) & 1) == 0) {
            hexLength = hexString.substring(position, position + 2);
            position = position + 2;
        } else {
            // 当最左侧的bit位为1的时候，取得后7bit的值，
            int _L_Len = i & 127; // 127的二进制 0111 1111
            position = position + 2;
            hexLength = hexString.substring(position, position + _L_Len * 2);
            // position表示第一个字节，后面的表示有多少个字节来表示后面的Value值
            position = position + _L_Len * 2;
        }

        return new LPosition(Integer.parseInt(hexLength, 16), position);
    }

    /**
     * 取得子域Tag标签，Tag标签不仅包含1个字节、2个字节，还包含3个字节。
     */
    public static String getTag(String hexString, int position) {
        try {
            String firstByte = hexString.substring(position, position + 2);
            int i = Integer.parseInt(firstByte, 16);
            String secondByte = hexString.substring(position + 2, position + 4);
            int j = Integer.parseInt(secondByte, 16);
            // b5~b1如果全为1，则说明这个tag下面还有一个子字节，PBOC/EMV里的tag最多占两个字节
            if ((i & 0x1F) == 0x1F) {
                if ((j & 0x80) == 0x80)//除Tag标签首字节外，tag中其他字节最高位为：1-表示后续还有字节；0-表示为最后一个字节。
                {
                    return hexString.substring(position, position + 6);//3Bytes的tag
                } else {
                    return hexString.substring(position, position + 4);//2Bytes的tag
                }
            } else {
                return hexString.substring(position, position + 2);
            }
        } catch (Exception e) {
            return "";
        }
    }

    /** 将TVL对象反转成16进制字符串 */
    public static String revertTVL2HexStr(TLV tlv) {
        StringBuilder sb = new StringBuilder();
        sb.append(tlv.getTag());
        int length = tlv.getLength();
//        if (length > 127) {
//            byte[] bytes = NumberUtil.intToByte4(length);
//            int first = -1;
//            for (int i = 0; i < bytes.length; i++) {
//                if (bytes[i] != 0) {
//                    first = i;
//                    break;
//                }
//            }
//            bytes = Arrays.copyOfRange(bytes, first, bytes.length);
//            int l = (bytes.length | 0x80);
//            sb.append(Integer.toHexString(l));
//            sb.append(Integer.toHexString(length));
//        } else {
//            sb.append(Integer.toHexString(length));
//        }
        sb.append(TLVValueLengthToHexString(length));
        sb.append(tlv.getValue());
        return sb.toString().toUpperCase();
    }

    /** 将TLV数据反转成字节数组 */
    public static byte[] revertTVL2Bytes(TLV tlv) {
        String hex = revertTVL2HexStr(tlv);
        return Utils.hexStr2Bytes(hex);
    }

    /**
     * 将TLV中数据长度转化成16进制字符串
     *
     * @param length
     * @return
     */
    public static String TLVValueLengthToHexString(int length) {
        if (length < 0) {
            throw new RuntimeException("不符要求的长度");
        }
        String hexLengthStr = String.format("%02x", length);
        if (length <= 0x7f) {
            return hexLengthStr;
        } else if (length <= 0xff) {
            return "81" + hexLengthStr;
        } else if (length <= 0xffff) {
            return "82" + hexLengthStr;
        } else if (length <= 0xffffff) {
            return "83" + hexLengthStr;
        } else {
            return "TLV 长度最多4个字节";
        }
    }


    /***
     * 将TLV转换成16进制字符串
     */
    public static String revertToHexStr(TLV tlv) {
        StringBuilder sb = new StringBuilder();
        sb.append(tlv.getTag());
        sb.append(TLVValueLengthToHexString(tlv.getLength()));
        sb.append(tlv.getValue());
        return sb.toString();
    }

}