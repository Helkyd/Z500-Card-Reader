package ao.metagest.z500cardreader.utils.tlv;

/**
 * 解包工具
 * Created by xurong on 2017/3/11.
 */

public class DecodePackage {

    private static DecodePackage mDecodePackage = new DecodePackage();

    private DecodePackage() {
    }

    public static DecodePackage getInstance() {
        if (mDecodePackage == null) {
            mDecodePackage = new DecodePackage();
        }
        return mDecodePackage;
    }

    /**
     * 解析数据包
     * STX + MsgType + Length + Value
     * 容器标签+金融请求报文+数据域长度+数据域
     *
     * @param packageData
     * @return
     */
    public MessageBean parseData(String packageData) {
        MessageBean msgBean = new MessageBean();
        try {
            msgBean.STX = packageData.substring(0, 2);
            msgBean.MsgType = packageData.substring(2, 4);
            msgBean.Length = packageData.substring(4, 8);
            msgBean.Value = packageData.substring(8);
            msgBean.tlvList = TLVUtils.builderTLVList(msgBean.Value);
            return msgBean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
