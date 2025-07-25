package ao.metagest.z500cardreader.emv;


//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.R;
import ao.metagest.z500cardreader.BaseApp;
import ao.metagest.z500cardreader.MainActivity;
import ao.metagest.z500cardreader.R;

/** EMV交易结果 */
public enum EMVTransResult {
    /** 联机批准 */
    ONLINE_APPROVE(BaseApp.Companion.getApp().getString(R.string.online_approve)),
    /** 联机拒绝 */
    ONLINE_DECLINE(BaseApp.Companion.getApp().getString(R.string.online_decline)),
    /** 继续执行联机操作 */
    ONLINE_REQUEST(BaseApp.Companion.getApp().getString(R.string.online_request)),
    /** 脱机批准 */
    OFFLINE_APPROVE(BaseApp.Companion.getApp().getString(R.string.offline_approve)),
    /** 脱机拒绝 */
    OFFLINE_DECLINE(BaseApp.Companion.getApp().getString(R.string.offline_decline)),
    /** 请使用其他界面 */
    USE_OTHER_INTERFACE(BaseApp.Companion.getApp().getString(R.string.another_interface)),
    /** 交易终止 */
    TRANSACTION_TERMINATED(BaseApp.Companion.getApp().getString(R.string.transaction_terminate)),
    /** 交易终止(服务不接受) */
    NOT_ACCEPTED(BaseApp.Companion.getApp().getString(R.string.terminal_service_not_accepted)),
    /** 此卡为芯片卡,不可降级交易 */
    READ_CARD_FALLBACK(BaseApp.Companion.getApp().getString(R.string.use_ic_card)),
    /** 此卡为芯片卡,不可降级交易 */
    SWIPE_CARD(BaseApp.Companion.getApp().getString(R.string.swipe_card)),
    /** 联机失败 */
    ONLINE_FAILED(BaseApp.Companion.getApp().getString(R.string.online_failed)),

    /** 看手机 */
    SEE_PHONE(BaseApp.Companion.getApp().getString(R.string.see_phone));

    EMVTransResult(String depicter) {
        this.depicter = depicter;
    }

    public String getDepicter() {
        return depicter;
    }

    private String depicter;
}
