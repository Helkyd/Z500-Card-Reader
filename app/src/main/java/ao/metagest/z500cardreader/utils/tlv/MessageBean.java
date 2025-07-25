package ao.metagest.z500cardreader.utils.tlv;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据域遵循BER-TLV 规则，数值由报文类型决定，
 */
public class MessageBean {
    //报文头
    public String STX;
    //报文类型
    public String MsgType;
    //数据长度
    public String Length;
    //数据域
    public String Value;
    //解析数据域后得到的tlv数据列表
    public List<TLV> tlvList = new ArrayList<>();

    @Override
    public String toString() {
        return "MessageBean{" +
                "STX='" + STX + '\'' +
                ", MsgType='" + MsgType + '\'' +
                ", Length='" + Length + '\'' +
                ", Value='" + Value + '\'' +
                ", tlvList=" + tlvList +
                '}';
    }
}
