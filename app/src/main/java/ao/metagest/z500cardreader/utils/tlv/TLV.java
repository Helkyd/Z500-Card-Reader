package ao.metagest.z500cardreader.utils.tlv;

//import test.apidemo.activity.utils.Utils;

import ao.metagest.z500cardreader.utils.Utils;

/**
 * @author Administrator on 2016/9/5.
 */
public class TLV {

    /**
     * 子域Tag标签(hex)
     */
    private String tag;

    /**
     * 子域取值的长度
     */
    private int length;

    /**
     * 子域取值(hex)
     */
    private String value;

    public TLV(String tag, String value) {
        this.tag = tag;
        this.value = value;
        this.length = Utils.hexStr2Bytes(value).length;
    }

    public TLV(String tag, int length, String value) {
        this.length = length;
        this.tag = tag;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "tag=[" + this.tag + "]," + "length=[" + this.length + "]," + "value=[" + this.value + "]";
    }

    /***
     * 将TLV恢复成字符串
     */
    public String recoverToHexStr() {
        return TLVUtils.revertToHexStr(this);
    }

}
