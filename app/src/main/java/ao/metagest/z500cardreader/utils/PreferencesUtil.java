package ao.metagest.z500cardreader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;


import com.ciontek.hardware.aidl.AidlConstantsV2;
import com.ciontek.hardware.aidl.emv.EMVOptV2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import android.util.Log;

import ao.metagest.z500cardreader.BaseApp;
//import test.metagestdemo.MyApplication;
//import test.metagestdemo.activity.bean.LastTransResultBean;

import ao.metagest.z500cardreader.InitActivity;
import ao.metagest.z500cardreader.MainActivity;
import ao.metagest.z500cardreader.bean.LastTransResultBean;


/**
 * Preference文件
 */
public final class  PreferencesUtil {
    private static final String TAG = "PreferencesUtil";

    private static final String FILE_NAME = "emvAuth_pref";

    //IP地址和端口号
    private static final String KEY_SERVER_IP = "key_server_ip";
    private static final String KEY_SERVER_PORT = "key_server_port";
    //功能设置相关缓存
    private static final String KEY_TRANSACTION_TYPE = "key_transaction_type";//交易类型选择
    private static final String KEY_FORCE_ACCEPTANCE = "key_force_acceptance";//强制接受
    private static final String KEY_ACCOUNT_TYPE = "key_account_type";//账户类型选择
    private static final String KEY_DATA_COLLECT = "key_data_collect";//数据采集选择
    private static final String KEY_FORCE_ONLINE = "key_force_online";//强制联机选择
    private static final String KEY_PRINT_SET = "key_print_set";//打印设置
    private static final String KEY_ACCUMULATED_COUNT = "key_accumulated_count";//累计交易金额
    private static final String KEY_ACCUMULATED_AMOUNT = "key_accumulated_amount";//累计交易金额
    private static final String KEY_MERCHANT_NAME = "key_merchant_name";//商户名称
    private static final String KEY_LAST_TRANS_RESULT = "key_last_trans_result";//上次交易结果
    private static final String KEY_ZERO_AMT_SUPPORT = "key_zero_amt_support"; //0金额是否支持

    // 20180607
    private static final String KEY_GETDATA_PIN = "key_getdata_pin";//输PIN获取PIN重试次数
    private static final String KEY_BYPASS_PIN = "key_bypass_pin";//Bypass PIN
    private static final String KEY_BYPASS_ALL = "key_bypass_all";// Bypass all
    private static final String KEY_TERMINAL_TYPE = "key_terminal_type";//终端类型
    private static final String KEY_IFD_SN = "key_interface_device_sn"; // 终端序列号 9F1E
    private static final String KEY_TERMINAL_CAPACITY = "key_terminal_capacity"; // 终端能力 9F33
    private static final String KEY_ADD_TERMINAL_CAPACITY = "key_add_terminal_capacity"; // 附加终端能力 9F40
    private static final String KEY_SUPPORT_PSE = "key_support_pse"; // 支持PSE选择
    private static final String KEY_USE_TERM_AIP = "key_use_term_aip"; //支持终端AIP风险管理
    private static final String KEY_SUPPORT_ADVICE = "key_support_advice"; //支持通知
    private static final String KEY_SUPPORT_SM = "key_support_sm"; // 支持SM算法
    private static final String KEY_SUPPORT_MULTI_LANGUAGE = "key_support_multi_language"; //支持多语言
    private static final String KEY_LANGUAGE_SELECT_METHOD = "key_language_select_method"; //支持语言选择方法
    private static final String KEY_SUPPORT_EXCEPTFILE = "key_support_exceptfile"; // 支持黑名单
    private static final String KEY_SUPPORT_ACCOUNT_SELECT = "key_support_account_select"; // 支持账户选择
    private static final String KEY_SUPPORT_QUICS = "key_support_quics"; // 支持账户选择
    private static final String KEY_CLSS_TTQ = "key_clss_ttq"; //非接终端交易属性 TTQ
    private static final String KEY_SUPPORT_READLOG = "key_support_readlog"; // 支持读卡片日志
    private static final String KEY_AUTOTEST_INTERVAL = "key_autotest_interval"; // 交易间隔时间
    private static final String KEY_SUPPORT_CLSS = "key_support_clss"; // 支持非接

    private static final String KEY_TM_TIME = "key_tm_date"; // 交易时间
    private static final String KEY_TM_DATE = "key_tm_time"; // 交易日期

    private static final String KEY_CURRENCY_CODE = "key_currency_code"; // 货币代码
    private static final String KEY_CURRENCY_EXP = "key_currency_exp"; // 货币指数

    private static final String KEY_COUNTRY_CODE = "key_country_code"; // 国家代码
    private static final String KEY_SEND_RECEIPT = "key_send_receipt"; // 上送凭条

    private static final String KEY_CLSS_CAPA = "key_clss_capa"; // 9F6D
    private static final String KEY_EN_CLSS_CAPA = "key_en_clss_capa"; // 9F6E
    private static final String KEY_UN_RANGE = "key_un_range"; // DF8170
    private static final String KEY_SET_CONFIG = "key_set_config"; // set config
    private static final String KEY_GO_ONLINE = "key_go_online"; //  DF8169
    private static final String KEY_DELAYED_AUTH = "key_delayed_auth";
    private static final String KEY_DRL_LIST = "key_drl_list";
    private static final String KEY_SET_PROFILE = "key_set_profile";
    private static final String KEY_SET_AUTO_AMT = "key_set_auto_amt";
    private static final String KEY_SET_LAST_AMT = "key_set_last_amt";
    private static final String KEY_SET_AUTO_INPUT_PIN = "key_set_auto_input_pin";
    private static final String KEY_SET_KEY_REVOC = "key_set_key_revoc";
    private static final String KEY_SET_KEY_CHECKSUM = "key_set_key_checksum";

    private static final String KEY_APP_TYPE = "key_app_type";

    private static final String KEY_SET_CONFIG_INDEX = "key_set_config_index";

    public  static EMVOptV2 mEmvOpt;

    private PreferencesUtil() {
        throw new AssertionError("create instance of PreferencesUtil is forbidden");
    }

    /** 保存POSP IP */
    public static void setServerIP(String ip) {

        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SERVER_IP, ip).apply();
    }

    /** 获取POSP IP */
    public static String getServerIP() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SERVER_IP, "10.0.0.128");
    }

    /** 保存POSP 端口 */
    public static void setServerPort(String port) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SERVER_PORT, port).apply();
    }

    /** 获取POSP 端口 */
    public static String getServerPort() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SERVER_PORT, "60000");
    }

    /**
     * 功能设置-设置交易类型
     *
     * @param value 00:物品/服务 01:现金 09:返现  FF:查询/转账/管理/存款/支付
     */
    public static void setFunSetTransactionType(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_TRANSACTION_TYPE, value).apply();
    }

    /**
     * 功能设置- 获取交易类型
     *
     * @return 00:物品/服务 01:现金 09:返现  FF:查询/转账/管理/存款/支付
     */
    public static String getFunSetTransactionType() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_TRANSACTION_TYPE, "00");
    }

    /**
     * 功能设置-设置强制接受
     *
     * @param value 00:关闭强制接受 01:开启强制接受
     */
    public static void setFunForceAcceptance(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_FORCE_ACCEPTANCE, value).apply();
    }

    /**
     * 功能设置- 获取强制接受
     *
     * @return 00:关闭强制接受 01:开启强制接受
     */
    public static String getFunForceAcceptance() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_FORCE_ACCEPTANCE, "00");
    }

    /**
     * 功能设置- 设置账户类型
     *
     * @param value 00:默认 10:储蓄 20:借贷记  30:信用卡
     */
    public static void setFunSetAccountType(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        Log.e(TAG, "setFunSetAccountType: " + value);
        pref.edit().putString(KEY_ACCOUNT_TYPE, value).apply();
    }

    /**
     * 功能设置-获取账户类型
     *
     * @return 00:默认 10:储蓄 20:借贷记  30:信用卡
     */
    public static String getFunSetAccountType() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_ACCOUNT_TYPE, "00");
    }

    /**
     * 功能设置-设置数据采集
     *
     * @param value 00:联机数据 01:批数据
     */
    public static void setFunSetDataCollect(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_DATA_COLLECT, value).apply();
    }

    /**
     * 功能设置-获取数据采集
     *
     * @return 00:联机数据 01:批数据
     */
    public static String getFunSetDataCollect() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_DATA_COLLECT, "00");
    }

    /**
     * 功能设置-设置强制联机
     *
     * @param value 00:关闭强制联机 01:开启强制联机
     */
    public static void setFunSetForceOnline(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_FORCE_ONLINE, value).apply();
    }

    /**
     * 功能设置-获取强制联机
     *
     * @return 00:关闭强制联机 01:开启强制联机
     */
    public static String getFunSetForceOnline() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_FORCE_ONLINE, "00");
    }
    /**
     * 功能设置-获取0金额是否支持
     *
     * @return 00:不支持 01:支持
     */
    public static String getFunSetZeroAmtSupport(){
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        return pref.getString(KEY_ZERO_AMT_SUPPORT,"00");
    }
    /**
     * 功能设置-设置0金额是否支持
     *
     * @return 00:不支持 01:支持
     */
    public static void setFunZeroAmtSupport(String value){
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        pref.edit().putString(KEY_ZERO_AMT_SUPPORT,value).apply();
    }
    // 20180607
    /**
     * 功能设置-设置开启输PIN前获取PIN重试次数
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetGetDataPIN(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_GETDATA_PIN, value).apply();
    }

    /**
     * 功能设置-获取开启输PIN前获取PIN重试次数
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetGetDataPIN() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_GETDATA_PIN, "01");
    }

    /**
     * 功能设置-设置是否开启输PIN bypass
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetBypassPIN(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_BYPASS_PIN, value).apply();
    }

    /**
     * 功能设置-获取是否开启输PIN bypass
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetBypassPIN() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_BYPASS_PIN, "01");
    }

    /**
     * 功能设置-设置是否开启输PIN bypass all
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetBypassPinAll(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_BYPASS_ALL, value).apply();
    }

    /**
     * 功能设置-获取是否开启输PIN bypass all
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetBypassPinAll() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_BYPASS_ALL, "01");
    }

    /**
     * 功能设置-设置是否开启支持PSE选择
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportPSE(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_PSE, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持PSE选择
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportPSE() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_PSE, "01");
    }

    /**
     * 功能设置-设置是否开启支持终端AIP风险管理
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetUseTermAIP(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_USE_TERM_AIP, value).apply();
    }

    /**
     * 功能设置-获取是否支持终端AIP风险管理
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetUseTermAIP() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_USE_TERM_AIP, "01");
    }

    /**
     * 功能设置-设置是否开启支持通知
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportAdvice(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_ADVICE, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持通知
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportAdvice() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_ADVICE, "00");
    }

    /**
     * 功能设置-设置是否开启支持非接
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportClss(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_CLSS, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持通知非接
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportClss() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_CLSS, "01");
    }

    /**
     * 功能设置-设置是否开启支持SM算法
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportSM(String value) {
        Log.e("setFunSetSupportSM", value);
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_SM, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持SM算法
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportSM() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_SM, "00");
    }

    /**
     * 功能设置-设置是否开启支持多语言
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportMultiLanguage(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_MULTI_LANGUAGE, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持多语言
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportMultiLanguage() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_MULTI_LANGUAGE, "00");
    }

    /**
     * 功能设置-设置是否开启支持语言选择方法
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetLanguageSelectMethod(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_LANGUAGE_SELECT_METHOD, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持语言选择方法
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetLanguageSelectMethod() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_LANGUAGE_SELECT_METHOD, "00");
    }

    /**
     * 功能设置-设置是否开启支持黑名单
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportExceptFile(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_EXCEPTFILE, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持黑名单
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportExceptFile() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_EXCEPTFILE, "01");
    }

    /**
     * 功能设置-设置是否开启支持账户选择
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportAccountSelect(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_ACCOUNT_SELECT, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持账户选择
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportAccountSelect() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_ACCOUNT_SELECT, "01");
    }

    /**
     * 功能设置-设置是否支持quics
     *
     * @param value 00:不支持 01:支持
     */
    public static void setFunSetSupportQuics(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_QUICS, value).apply();
    }

    /**
     * 功能设置-获取是否支持quics
     *
     * @return 00:不支持 01:支持
     */
    public static String getFunSetSupportQuics() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_QUICS, "01");
    }

    /**
     * 功能设置-设置是否开启支持读卡片日志
     *
     * @param value 00:关闭 01:开启
     */
    public static void setFunSetSupportReadLog(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SUPPORT_READLOG, value).apply();
    }

    /**
     * 功能设置-获取是否开启支持读卡片日志
     *
     * @return 00:关闭 01:开启
     */
    public static String getFunSetSupportReadLog() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SUPPORT_READLOG, "01");
    }

    /**
     * 功能设置-设置自动测试间隔时间
     *
     * @param value
     */
    public static void setFunSetAutoTestInterval(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_AUTOTEST_INTERVAL, value).apply();
    }

    /**
     * 功能设置- 获取自动测试间隔时间
     *
     * @return
     */
    public static String getFunSetAutoTestInterval() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_AUTOTEST_INTERVAL, "15");
    }

    /**
     * 功能设置-设置自动测试金额
     *
     * @param value
     */
    public static void setFunSetAutoTestAmount(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_AUTO_AMT, value).apply();
    }

    /**
     * 功能设置- 获取自动测试金额
     *
     * @return
     */
    public static String getFunSetAutoTestAmount() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_AUTO_AMT, "1500");
    }

    /**
     * 功能设置-设置支持回收公钥
     *
     * @param value
     */
    public static void setFunSetKeyRevoc(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_KEY_REVOC, value).apply();
    }

    /**
     * 功能设置- 获取支持回收公钥
     *
     * @return
     */
    public static String getFunSetKeyRevoc() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_KEY_REVOC, "00");
    }


    /**
     * 功能设置-设置上笔交易金额
     *
     * @param value
     */
    public static void setFunSetLastAmount(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_LAST_AMT, value).apply();
    }

    /**
     * 功能设置- 获取上笔交易金额
     *
     * @return
     */
    public static String getFunSetLastAmount() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_LAST_AMT, "15.00");
    }

    /**
     * 功能设置-设置自动输入PIN
     *
     * @param value
     */
    public static void setFunSetAutoPin(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_AUTO_INPUT_PIN, value).apply();
    }

    /**
     * 功能设置- 获取自动输入PIN
     *
     * @return
     */
    public static String getFunSetAutoPin() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_AUTO_INPUT_PIN, "00");
    }


    /**
     * 功能设置-设置自动上送凭条
     *
     * @param value
     */
    public static void setFunSetSendReceipt(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SEND_RECEIPT, value).apply();
    }

    /**
     * 功能设置- 获取自动上送凭条
     *
     * @return
     */
    public static String getFunSetSendReceipt() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SEND_RECEIPT, "00");
    }

    /**
     * 功能设置-设置终端类型
     *
     * @param value (1x for Financial Institution)   11: attended online only  12: attended Offline with online capability 13: attended offline only 14: unattended online only 15: unattended Offline with online capability 16: unattended offline only
     *                (2x for Merchant )              21: attended online only  22: attended Offline with online capability 23: attended offline only 24: unattended online only 25: unattended Offline with online capability 26: unattended offline only
     */
    public static void setFunSetTerminalType(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_TERMINAL_TYPE, value).apply();
    }

    /**
     * 功能设置- 获取终端类型
     *
     * @return (1x for Financial Institution)   11: attended online only  12: attended Offline with online capability 13: attended offline only 14: unattended online only 15: unattended Offline with online capability 16: unattended offline only
     *          (2x for Merchant )               21: attended online only  22: attended Offline with online capability 23: attended offline only 24: unattended online only 25: unattended Offline with online capability 26: unattended offline only
     */
    public static String getFunSetTerminalType() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_TERMINAL_TYPE, "22");
    }

    public static void setFunSetAppType(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_APP_TYPE, value).apply();
    }

    /**
     * 功能设置- 获取终端类型
     *
     * @return  00 - EMV  01 - AE
     */
    public static String getFunSetAppType() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_APP_TYPE, "01");
    }


    /** 设置终端序列号 */
    public static void setIfdSn(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_IFD_SN, name).apply();
    }

    /** 获取终端序列号 */
    public static String getIfdSn() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_IFD_SN, "5465726D696E616C"); // "Terminal"
    }

    /** 设置终端序货币代码 */
    public static void setCurrencyCode(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_CURRENCY_CODE, name).apply();
    }

    /** 获取终端货币代码 */
    public static String getCurrencyCode() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_CURRENCY_CODE, "0840");
    }

    /** 设置终端序货币指数 */
    public static void setCurrencyExp(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_CURRENCY_EXP, name).apply();
    }

    /** 获取终端货币指数 */
    public static String getCurrencyExp() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_CURRENCY_EXP, "02");
    }

    /** 设置终端国家代码 */
    public static void setCountryCode(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_COUNTRY_CODE, name).apply();
    }

    /** 获取终端国家代码 */
    public static String getCountryCode() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_COUNTRY_CODE, "0840");
    }

    /** 设置终端能力 */
    public static void setTerminalCapacity(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_TERMINAL_CAPACITY, name).apply();
    }

    /** 获取终端能力 */
    public static String getTerminalCapacity() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_TERMINAL_CAPACITY, "E0F8C8");
    }



    /** 获取交易时间 */
    public static String getTransTime() {
        //SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        //Result SystemTime = MyApplication.app.emvAuthOpt.sysGetTime();

        /*
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
        */
        String systemTime = "";
        try {
           /* systemTime = MyApplication.app.emvAuthOpt.sysGetTime();*/
            byte[] outData = new byte[64];
            //int len = MyApplication.app.emvOpt.sysGetTime(outData);
            int len = BaseApp.Companion.getBasicOpt().sysGetTime(outData);
            //int len = BaseApp.app.basicOpt.sysGetTime(outData);
            if (len > 0) {
                return Utils.byte2HexStr(Arrays.copyOf(outData, len));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


//        byte[] outBuf = new byte[20];
//        String[] tags = new String[]{"9A", "9F21"};
//        mEmvOpt = MyApplication.app.emvOpt;
//        String data = "";
//        try {
//            mEmvOpt.readKernelData(tags, outBuf);
//            data = Utils.byte2HexStr(outBuf);
//
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        if(data.length() < 22){
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//            Date date = new Date(System.currentTimeMillis());
//            String time = simpleDateFormat.format(date);
//            return time;
//        }
//        String date = data.substring(4, 10);
//        String time = data.substring(16, 22);
//        if(outBuf[2] < 0x50){
//            time = "20" + date + time;
//        }else{
//            time = "19" + date + time;
//        }

        return systemTime;

    }

    /** 设置交易日期 */
    public static void setTransDate(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String date = pref.getString(KEY_TM_DATE, "180101");
        try {
            /*mEmvOpt.writeKernelData("9A03"+ date);*/
            //MyApplication.app.emvOpt.setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, "9A", date);
            //MyApplication.app.emvOpt.setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL,"9A",date);
            BaseApp.Companion.getEmvOpt().setTlv(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL,"9A",date);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //pref.edit().putString(KEY_TM_DATE, name).apply();
    }

    /** 获取交易日期 */
    public static String getTransDate() {
        //SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);


        byte[] outBuf = new byte[10];
        String[] tags = new String[]{"9A"};
        mEmvOpt = BaseApp.Companion.getEmvOpt(); // BaseApp.emvOpt; // MyApplication.app.emvOpt;
        try {
            mEmvOpt.readKernelData(tags, outBuf);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Utils.byte2HexStr(outBuf).substring(2, 5);
        //return pref.getString(KEY_TM_DATE, "180101");
    }

    /** 设置附加终端能力 */
    public static void setAddTerminalCapacity(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_ADD_TERMINAL_CAPACITY, name).apply();
    }

    /** 获取附加终端能力 */
    public static String getAddTerminalCapacity() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_ADD_TERMINAL_CAPACITY, "F000F0A001");
    }

    /** 设置TTQ */
    public static void setClssTTQ(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_CLSS_TTQ, name).apply();
    }

    /** 获取TTQ */
    public static String getClssTTQ() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_CLSS_TTQ, "26000080");
    }

    /** 设置累计交易次数 */
    public static void setAccumulatedCount(long count) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putLong(KEY_ACCUMULATED_COUNT, count).apply();
    }

    /** 获取累计交易次数 */
    public static long getAccumulatedCount() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getLong(KEY_ACCUMULATED_COUNT, 0);
    }

    /** 设置累计交易金额（分） */
    public static void addAccumulatedAmount(long amount, String cardnum) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String saccuPanAmount = pref.getString(KEY_ACCUMULATED_AMOUNT, "0000000000000000=000000000000");
        String[] value = saccuPanAmount.split("=");
        String saccpan;
        String saccuAmount;
        if(value.length == 2) {
            saccpan = value[0];
            saccuAmount = value[1];
        }else{
            saccpan = "0000000000000000";
            saccuAmount = "000000000000";
        }

        long laccuAmount = Long.valueOf(saccuAmount).longValue();
        laccuAmount = laccuAmount + amount;
        String accuAmount = String.format(Locale.getDefault(), "%012d", laccuAmount);
        String sAmount = String.format(Locale.getDefault(), "%012d", amount);
        String accuPanAmount;
        if(cardnum.equals(saccpan)) {
            accuPanAmount = cardnum + "=" + accuAmount;
        }else{
            accuPanAmount = cardnum + "=" + sAmount;
        }
        pref.edit().putString(KEY_ACCUMULATED_AMOUNT, accuPanAmount).apply();

    }

    /** 获取累计交易金额（分） */
    public static String getAccumulatedAmount() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_ACCUMULATED_AMOUNT, "0000000000000000=000000000000");
    }

    /** 清除累计交易金额 */
    public static void clearAccumulatedAmount() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        //pref.edit().putLong(KEY_ACCUMULATED_AMOUNT, 0).apply();
        pref.edit().putString(KEY_ACCUMULATED_AMOUNT, "0000000000000000=000000000000").apply();
    }

    /** 设置终端商户名 */
    public static void setMerchantName(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_MERCHANT_NAME, name).apply();
    }

    /** 获取终端商户名 */
    public static String getMerchantName() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_MERCHANT_NAME, "bctest");
    }

    /** 设置上次交易结果 */
    public static void setLastTransactionResult(LastTransResultBean lastResult) {
        String str = object2String(lastResult);
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_LAST_TRANS_RESULT, str).apply();
    }

    /** 获取上次交易结果 */
    public static LastTransResultBean getLastTransactionResult() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String base64Str = pref.getString(KEY_LAST_TRANS_RESULT, "");
        return string2Object(base64Str);
    }


    /** 设置终端非接交易属性 *AE* */
    public static void setClssCapa(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_CLSS_CAPA, name).apply();
    }

    /** 获取终端非接交易属性 *AE* */
    public static String getClssCapa() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_CLSS_CAPA, "C0");
    }

    /** 设置终端增强非接交易属性 *AE* */
    public static void setEnClssCapa(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_EN_CLSS_CAPA, name).apply();
    }

    /** 获取终端增强非接交易属性 *AE* */
    public static String getEnClssCapa() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_EN_CLSS_CAPA, "D8E00000");
    }

    /** 设置随机数范围 *AE* */
    public static void setUNRange(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_UN_RANGE, name).apply();
    }

    /** 获取随机数范围 *AE* */
    public static String getUnRange() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_UN_RANGE, "60");
    }

    /** 设置CheckSum *AE* */
    public static void setFunSetChecksum(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_KEY_CHECKSUM, name).apply();
    }

    /** 获取CheckSum *AE* */
    public static String getFunSetChecksum() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_KEY_CHECKSUM, "--");
    }

    /** 设置测试配置 *AE* */
    public static void setFunSetConfig(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_CONFIG, name).apply();
    }

    /** 获取测试配置 *AE* */
    public static String getFunSetConfig() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_CONFIG, "00");
    }

    /** 设置测试Profile *AE* */
    public static void setFunSetProfile(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_SET_PROFILE, name).apply();
    }

    /** 获取测试Profile *AE* */
    public static String getFunSetProfile() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_SET_PROFILE, "00");
    }

    /** 设置联机条件 *AE* */
    public static void setFunSetGoOnline(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_GO_ONLINE, name).apply();
    }

    /** 获取联机条件 *AE* */
    public static String getFunSetGoOnline() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_GO_ONLINE, "01");
    }

    /** 设置延迟联机 *AE* */
    public static void setFunSetDelayedAuth(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_DELAYED_AUTH, name).apply();
    }

    /** 获取延迟联机 *AE* */
    public static String getFunSetDelayedAuth() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_DELAYED_AUTH, "00");
    }

    /** 设置DRL */
    public static void setFunSetDrlList(String name) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_DRL_LIST, name).apply();
    }

    /** 获取DRL */
    public static String getFunSetDrlList() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_DRL_LIST, "00"); // ID + CLSS Limit + Floor Limit + CVM Limit
    }



    /** 将可序列化对象转换成Base64字符串 */
    private static <T extends Serializable> String object2String(T obj) {
        // 创建字节输出流
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            // 创建对象输出流,封装字节流
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            // 将对象写入字节流
            oos.writeObject(obj);
            // 将字节流编码成base64的字符串
            return new String(Base64.encode(bos.toByteArray(), 0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(bos);
            IOUtil.close(oos);
        }
        return null;
    }

    /** 将Base64字符串转换成可序列化对象 */
    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T string2Object(String base64Str) {
        if (TextUtils.isEmpty(base64Str)) {
            return null;
        }
        // 读取字节
        byte[] bytes = Base64.decode(base64Str.getBytes(), 0);
        // 封装到字节读取流
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            // 封装到对象读取流
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            // 读取对象
            return (T) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(ois);
            IOUtil.close(bis);
        }
        return null;
    }


    public static String getFunSetAccountTypeIndex() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString("selectIndex", "0");
    }

    public static void setFunSetAccountTypeIndex(String value) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString("selectIndex",value).apply();

    }

    /**
     * 保存应用选的config信息
     * 0 : config1
     * 1 : config2
     */
    public static int getFunSetConfigIndex() {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_SET_CONFIG_INDEX, 0);
    }

    public static void setFunSetConfigIndex(int configIndex) {
        SharedPreferences pref = BaseApp.Companion.getApp().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_SET_CONFIG_INDEX, configIndex).apply();
    }
}
