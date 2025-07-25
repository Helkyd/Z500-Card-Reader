package ao.metagest.z500cardreader.constants;

public class UpdateDataConstant {
    //需更新数据类型
    //The type of data to be updated
    public static final String UPDATE_PREPROCESS = "update_start_preprocess";
    public static final String UPDATE_PUB_KEY = "update_pub_key";
    public static final String UPDATE_AID = "update_aid";
    public static final String UPDATE_TERM_PARAM = "update_term_param";
    public static final String UPDATE_BLACK_LIST = "update_black_list";
    public static final String UPDATE_RECYCLE_PUBKEY = "update_recycle_pubkey";

    //从后台获取数据发送的报文  格式如下：command，dataType，keyword
    //Get the data sent packet from the background  ,the format is:command，dataType，keyword
    public static final String MESSAGE_UPDATE_PREPROCESS = "02C00000,80,DF8106";
    public static final String MESSAGE_UPDATE_PUB_KEY = "02C20000,82,9F06";
    public static final String MESSAGE_UPDATE_AID = "02C30000,83,9F06";
    public static final String MESSAGE_UPDATE_TERM_PARAM = "02C40000,84,";
   // public static final String MESSAGE_UPDATE_TERM_PARAM = "02C4 002EDF810401019F3501229F3303E0F8C89F40056000F020019F1A0208409F1E08123456789A031806089F2103120000,84,";
    public static final String MESSAGE_UPDATE_BLACK_LIST = "02C50000,85,5A";
    public static final String MESSAGE_UPDATE_RECYCLE_PUBKEY = "02C60000,86,9F06";

    public static int mRemainTime = 0;
}
