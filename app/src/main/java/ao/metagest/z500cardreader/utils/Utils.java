package ao.metagest.z500cardreader.utils;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
    private final static String TAG = Utils.class.getName();

    static final char[] HEX = "0123456789ABCDEF".toCharArray();


    /**
     * 判断是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        if (sz == 0) {
            return false;
        }
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i)) && (str.charAt(i) != '.')) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字节数组转换成十六进制字符串
     */
    public static String byte2HexStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes == null || bytes.length == 0) {
            return sb.toString();
        }
        for (byte b : bytes) {
            String s = Integer.toHexString(b & 0xFF);
            if (s.length() < 2) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 十六进制字符串转换成bytes
     *
     * @param hexStr
     * @return
     */
    public static byte[] hexStr2Bytes(String hexStr) {
        if (TextUtils.isEmpty(hexStr)) {
            return new byte[0];
        }
        int l = hexStr.length();
        if (l % 2 != 0) {
            StringBuilder sb = new StringBuilder(hexStr);
            sb.insert(hexStr.length(), '0');
            hexStr = sb.toString();
        }
        byte[] b = new byte[hexStr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexStr.charAt(j++);
            char c1 = hexStr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    public static byte hexStr2Byte(String hexStr) {
        return (byte) Integer.parseInt(hexStr, 16);
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    /**
     * 将一个8/16字节数组转成128二进制数组
     */
    public static boolean[] getBinaryFromByte(byte[] b) {
        boolean[] binary = new boolean[b.length * 8 + 1];
        String str = "";
        for (int i = 0; i < b.length; i++) {
            str += getEigthBitsStringFromByte(b[i]);
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.substring(i, i + 1).equalsIgnoreCase("1")) {
                binary[i + 1] = true;
            } else {
                binary[i + 1] = false;
            }
        }
        return binary;
    }

    public static String getEigthBitsStringFromByte(int b) {
        // if this is a positive number its bits number will be less
        // than 8
        // so we have to fill it to be a 8 digit binary string
        // b=b+100000000(2^8=256) then only get the lower 8 digit
        b |= 256; // mark the 9th digit as 1 to make sure the string
        // has at
        // least 8 digits
        String str = Integer.toBinaryString(b);
        int len = str.length();
        return str.substring(len - 8, len);
    }

    /**
     * 将BCD码转成int
     */
    public static int bcdToint(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            int h = ((b[i] & 0xF0) >> 4) + 48;
            sb.append((char) h);
            int l = (b[i] & 0x0F) + 48;
            sb.append((char) l);
        }
        return Integer.parseInt(sb.toString());
    }

    /**
     * 判断字符串是否是乱码
     *
     * @param strName 字符串
     * @return 是否是乱码
     */
    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|t*|r*|n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字符是否是中文
     *
     * @param c 字符
     * @return 是否是中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * Decodes a TYPE_BCD-encoded number as a String.
     *
     * @param buf The byte buffer containing the TYPE_BCD data.
     */
    public static String Bcd2String(byte[] buf) throws IndexOutOfBoundsException {
        int length = buf.length;
        char[] digits = new char[length * 2];
        int start = 0;

        for (int i = 0; i < length; i++) {
            digits[start++] = (char) ((((buf[i] & 0x00f0) >> 4) > 9) ?
                    ((buf[i] & 0x00f0) >> 4) + 55 : ((buf[i] & 0x00f0) >> 4) + 48);
            digits[start++] = (char) (((buf[i] & 0x000f) > 9) ?
                    (buf[i] & 0x000f) + 55 : (buf[i] & 0x000f) + 48);
        }

        return new String(digits);
    }

    /**
     * 字节数组转换成int类型，从高字节到底字节
     *
     * @param bytes
     * @return
     */
    public static int bytes2Int(byte[] bytes) {
        int len = bytes.length;
        if (len > 4) throw new RuntimeException("字节数组长度不对");
        int rec = 0;
        for (int i = 0; i < len; i++) {
            int leftOff = 8 * (len - 1 - i);
            rec += ((bytes[i] & 0xFF) << leftOff);
        }
        return rec;
    }

    /**
     * 将String转成BCD码
     *
     * @param s
     * @return
     */
    public static byte[] StrToBCDBytes(String s) {
        if (s.length() % 2 != 0) {
            s = "0" + s;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        char[] cs = s.toCharArray();
        for (int i = 0; i < cs.length; i += 2) {
            int high = cs[i] - 48;
            int low = cs[i + 1] - 48;
            baos.write(high << 4 | low);
        }

        return baos.toByteArray();
    }

    /**
     * 将一个128二进制数组转成16字节数组
     *
     * @param binary
     * @return
     */
    public static byte[] getByteFromBinary(boolean[] binary) {
        int num = (binary.length - 1) / 8;
        if (((binary.length - 1) % 8) != 0) {
            num = num + 1;
        }

        byte[] b = new byte[num];
        String s = "";
        for (int i = 1; i < binary.length; i++) {
            if (binary[i]) {
                s += "1";
            } else {
                s += "0";
            }
        }

        String tmpstr;
        int j = 0;
        for (int i = 0; i < s.length(); i = i + 8) {
            tmpstr = s.substring(i, i + 8);
            b[j] = getByteFromEigthBitsString(tmpstr);
            j = j + 1;
        }

        return b;
    }

    public static byte getByteFromEigthBitsString(String str) {
        // if(str.length()!=8)
        // throw new Exception("It's not a 8 length string");
        byte b;
        // check if it's a minus number
        if (str.substring(0, 1).equals("1")) {
            // get lower 7 digits original code
            str = "0" + str.substring(1);
            b = Byte.valueOf(str, 2);
            // then recover the 8th digit as 1 equal to plus
            // 1000000
            b |= 128;
        } else {
            b = Byte.valueOf(str, 2);
        }
        return b;
    }

    private static String PREFIX = "\\u";

    /**
     * Native to ascii string. It's same as execut native2ascii.exe.
     *
     * @param str
     *            native string
     * @return ascii string
     */
    public static String native2Ascii(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            sb.append(char2Ascii(chars[i]));
        }
        return sb.toString();
    }

    /**
     * Native character to ascii string.
     *
     * @param c
     *            native character
     * @return ascii string
     */
    private static String char2Ascii(char c) {
        if (c > 255) {
            StringBuilder sb = new StringBuilder();
            sb.append(PREFIX);
            int code = (c >> 8);
            String tmp = Integer.toHexString(code);
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
            code = (c & 0xFF);
            tmp = Integer.toHexString(code);
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
            return sb.toString();
        } else {
            return Character.toString(c);
        }
    }

    /**
     * Ascii to native string. It's same as execut native2ascii.exe -reverse.
     *
     * @param str
     *            ascii string
     * @return native string
     */
    public static String ascii2Native(String str) {
        StringBuilder sb = new StringBuilder();
        int begin = 0;
        int index = str.indexOf(PREFIX);
        while (index != -1) {
            sb.append(str.substring(begin, index));
            sb.append(ascii2Char(str.substring(index, index + 6)));
            begin = index + 6;
            index = str.indexOf(PREFIX, begin);
        }
        sb.append(str.substring(begin));
        return sb.toString();
    }

    /**
     * Ascii to native character.
     *
     * @param str
     *            ascii string
     * @return native character
     */
    private static char ascii2Char(String str) {
        if (str.length() != 6) {
            throw new IllegalArgumentException(
                    "Ascii string of a native character must be 6 character.");
        }
        if (!PREFIX.equals(str.substring(0, 2))) {
            throw new IllegalArgumentException(
                    "Ascii string of a native character must start with \"\\u\".");
        }
        String tmp = str.substring(2, 4);
        int code = Integer.parseInt(tmp, 16) << 8;
        tmp = str.substring(4, 6);
        code += Integer.parseInt(tmp, 16);
        return (char) code;
    }

    /** 将16进制字符串转换成Ascii字符串 */
    public static String hexStr2AsciiStr(String hexStr) {
        byte[] bytes = hexStr2Bytes(hexStr);
        return bytes2AsciiStr(bytes);
    }

    /** 将字节数组转换成Ascii字符串 */
    public static String bytes2AsciiStr(byte[] bytes) {
        try {
            return new String(bytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** ASCII字符串转换成字节数组 */
    public static byte[] asciiStr2Bytes(String ascii) {
        byte[] dat = null;
        try {
            dat = ascii.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dat;
    }

    /** ASCII字符串转换成16进制字符串 */
    public static String asciiStr2HexStr(String ascii) {
        byte[] dat = asciiStr2Bytes(ascii);
        return byte2HexStr(dat);
    }

    /** 合并字节数组 */
    public static byte[] concatByteArray(List<byte[]> byteList) {
        if (CollectionUtil.isEmpty(byteList)) {
            return new byte[0];
        }
        int totalLen = 0;
        for (byte[] b : byteList) {
            if (b == null) {
                continue;
            }
            totalLen += b.length;
        }
        byte[] result = new byte[totalLen];
        int index = 0;
        for (byte[] b : byteList) {
            System.arraycopy(b, 0, result, index, b.length);
            index += b.length;
        }
        return result;
    }

    /** 合并字节数组 */
    public static byte[] concatByteArray(byte[]... byteList) {
        if (CollectionUtil.isEmpty(byteList)) {
            return new byte[0];
        }
        List<byte[]> list = new ArrayList<>(byteList.length);
        Collections.addAll(list, byteList);
        return concatByteArray(list);
    }

    /** 将空串转换成两个连字符 */
    public static String empty2Dash(String src) {
        if (TextUtils.isEmpty(src)) {
            return "--";
        }
        return src;
    }

    /**
     * Convert byte[] to hex string.将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytes2HexString(byte... src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 16进制字符串转byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexString2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return "".getBytes();
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (char2Byte(hexChars[pos]) << 4 | char2Byte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    public static int char2Byte(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }


    public static byte[] string2ByteArray(String str) {
        byte[] b = new byte[str.length()];
        for (int i = 0 ; i < str.length(); i++)
            b[i] = (byte) (char2Byte(str.charAt(i))+48);
        return b;
    }

//    public static void main(String[] args) {
//        String s = "12345";
//        System.out.println("%d:" + (char2Byte('1') + 48));
//    }

}
