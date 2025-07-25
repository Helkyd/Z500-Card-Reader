package ao.metagest.z500cardreader.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;


/**
 * 输入金额键盘
 * Created by longtao.li on 2016/11/19.
 */
@SuppressLint("AppCompatCustomView")
public class AmountInputView extends TextView {
    //缓存输入的密码
    private StringBuilder inputSB;

    String def = "0.00";

    public AmountInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inputSB = new StringBuilder();
        init();
    }

    private void init(){
        inputSB.append(def);
        setText(def.toString());
    }


    /**
     * 输入一个字符
     * @param text
     */
    public synchronized void addText(String text){
//        //判断是否已包含'.'
//        if(TextUtils.equals(".", text)){
//            boolean b = inputSB.toString().contains(".");
//            if(b)return;
//        }
//        //判断是否已达到最大长度
//        if(inputSB.length() == maxLength){
//            return;
//        }
//
//        if(inputSB.length()==0){
//            //输入第一个数字时
//            inputSB.append("￥");
//            if(TextUtils.equals(".", text)||TextUtils.equals("0", text)){
//                inputSB.append("0.");
//            }else {
//                inputSB.append(text);
//            }
//
//        }else if(inputSB.length()==2){
//            //输入第二个数字时
//            if(!TextUtils.equals(".", text)&&TextUtils.equals("￥0", inputSB.toString())){
//                return;
//            }
//            inputSB.append(text);
//        } else {
//            inputSB.append(text);
//        }
//
//        String temp = inputSB.toString();
//        int posDot = temp.indexOf(".");
//        if (posDot > 0){
//            if (temp.length() - posDot - 1 > 2)
//            {
//                inputSB.delete(posDot + 3, posDot + 4);
//            }
//        }else {
//            //整数最大6位
//            if(inputSB.length()>7){
//                delLast();
//            }
//        }
        if(inputSB.length()>=10){//shall be 13 to support big amount?
            return;
        }
        inputSB.append(text);
        String str = inputSB.toString();

        String cuttedStr = filter(str);

        inputSB.setLength(0);
        inputSB.append(cuttedStr);

        // setText(getResources().getString(R.string.rmb)+inputSB.toString());
        setText(inputSB.toString()); // 20180626
        invalidate();
    }

    @NonNull
    private String filter(String str) {
        String cutedStr = str;
        /* 删除字符串中的dot */
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if ('.' == c) {
                cutedStr = str.substring(0, i) + str.substring(i + 1);
                break;
            }
        }
        /* 删除前面多余的0 */
        int NUM = cutedStr.length();
        int zeroIndex = -1;
        for (int i = 0; i < NUM - 2; i++) {
            char c = cutedStr.charAt(i);
            if (c != '0') {
                zeroIndex = i;
                break;
            }else if(i == NUM - 3){
                zeroIndex = i;
                break;
            }
        }
        if(zeroIndex != -1){
            cutedStr = cutedStr.substring(zeroIndex);
        }
        /* 不足3位补0 */
        if (cutedStr.length() < 3) {
            cutedStr = "0" + cutedStr;
        }
        /* 加上dot，以显示小数点后两位 */
        cutedStr = cutedStr.substring(0, cutedStr.length() - 2)
                + "." + cutedStr.substring(cutedStr.length() - 2);
        return cutedStr;
    }

    /**
     * 删除最后的一个数字
     */
    public void delLast(){
        if( (inputSB.toString()).equals(def) )
            return;
        inputSB.deleteCharAt(inputSB.length()-1);
        String temp = inputSB.toString();
        temp = filter(temp);
        inputSB.setLength(0);
        inputSB.append(temp);
        //setText(getResources().getString(R.string.rmb)+inputSB.toString());
        setText(inputSB.toString()); // 20180626
    }


    /**
     * 清空
     */
    public void clean(){
        inputSB.setLength(0);
        inputSB.append(def);
        //setText(getResources().getString(R.string.rmb)+ String.valueOf(inputSB));
        setText(String.valueOf(inputSB)); // 20180626
    }

    public CharSequence getAmountText() {
//        StringBuilder sb = new StringBuilder();
//        CharSequence text = super.getText();
//        String textStr = text.toString();
//        int dotIndexOf = textStr.lastIndexOf(".");
//        if(!TextUtils.isEmpty(textStr)&&dotIndexOf == textStr.length()-1){
//            delLast();
//        }
//        if(!TextUtils.isEmpty(textStr)){
//            sb.append(inputSB);
//            sb.deleteCharAt(0);
//        }
        return inputSB.toString();
    }
}
