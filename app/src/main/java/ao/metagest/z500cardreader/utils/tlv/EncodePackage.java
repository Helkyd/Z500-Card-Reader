package ao.metagest.z500cardreader.utils.tlv;

/**
 * 组包工具
 * Created by xurong on 2017/3/11.
 */

public class EncodePackage {

    private MessageBean msgBean;

    private static EncodePackage mEncodePackage = new EncodePackage();

    private EncodePackage() {
    }

    public static EncodePackage getInstance() {
        if (mEncodePackage == null) {
            mEncodePackage = new EncodePackage();
        }
        return mEncodePackage;
    }

}
