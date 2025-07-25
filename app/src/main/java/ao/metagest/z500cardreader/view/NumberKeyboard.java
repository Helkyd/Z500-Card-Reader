
package ao.metagest.z500cardreader.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

//import test.metagestdemo.activity.R;
import ao.metagest.z500cardreader.R;


/**
 * 九宫格数字键盘
 *
 * @author longtao.li
 */
public class NumberKeyboard extends ViewGroup {

    private int mLineHeight;
    private int mKeyWidth;
    private int hSpacing = 0;  // 子View之间的横向间隔
    private int vSpacing = 0;  // 子View之间的纵向间隔

    private KeyClickCallback mKeyClickCallback;

    public void setKeyClickCallback(KeyClickCallback callback) {
        this.mKeyClickCallback = callback;
    }

    public NumberKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    public NumberKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberKeyboard(Context context) {
        super(context);
        init();
    }

    private void init() {

        DisplayMetrics dm = new DisplayMetrics();
        Activity activity = (Activity) getContext();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        int screenWidth = dm.widthPixels;

        mKeyWidth = screenWidth * 240 / 720;
        mLineHeight = screenHeight * 130 / 1184;
        // textSize= mKeyWidth / 5;
        int textSize = 18;
        hSpacing = 0;
        vSpacing = 0;
        // 生成键盘的每个key
        for (int i = 1; i <= 12; i++) {
            View child;
            TextView tv = (TextView) inflate(getContext(), R.layout.num_key, null);
            tv.setTextSize(textSize);
            child = tv;
            if (i == 10) {
                child = inflate(getContext(), R.layout.num_key_del, null);
                View del = child.findViewById(R.id.del);
                int delW = mKeyWidth * 50 / 240;
                int delH = mLineHeight * 50 / 140;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(delW, delH);
                del.setLayoutParams(params);
            } else if (i == 12) {
                child = inflate(getContext(), R.layout.num_key_clean, null);
                View clean = child.findViewById(R.id.clean);
                int delW = mKeyWidth * 40 / 240;
                int delH = mLineHeight * 40 / 140;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(delW, delH);
                clean.setLayoutParams(params);

            } else if (i == 11) {
                tv.setText("0");
            } else {
                tv.setText(String.valueOf(i));
            }
            LayoutParams params = new LayoutParams(mKeyWidth, mLineHeight);
            child.setLayoutParams(params);
            addView(child);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 得到ViewGroup的初始宽高
        int width = mKeyWidth * 3 + hSpacing * 2;
        int height = MeasureSpec.getSize(heightMeasureSpec)
                + getPaddingBottom() + getPaddingTop();

        final int count = getChildCount();
        int line_height = 0;
        // 获取第一个子View的起始点位置
        int yPos = getPaddingTop();
        // 计算每一个子View的尺寸,并算出ViewGroup的高度
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = child.getLayoutParams();
                //算出子View宽的MeasureSpec值
                int wSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                //算出子View高的MeasureSpec值
                int hSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                //让子View记住自己宽高的MeasureSpec值,子View的
                //onMeasure(int widthMeasureSpec,int heightMeasureSpec)
                //函数传入的就是这里算出来的这两个值
                child.measure(wSpec, hSpec);
                //设置完MeasureSpec值后调用View.getMeasuredWidth()函数算出View的宽度
                line_height = Math.max(line_height, child.getMeasuredHeight() + vSpacing);

                //每行3个(每行满3个子view就换行)
                int newline = i % 3;
                if (newline == 0 && i != 0) {
                    //初始坐标的x偏移值+子View宽度>ViewGroup宽度 就换行
                    yPos += mLineHeight;    //坐标y偏移值再加上本行的行高也就是换行
                }
                //算出下一个子View的起始点x偏移值
            }
        }
        this.mLineHeight = line_height;

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            //对高度期望值没有限制
            height = yPos + mLineHeight;

        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            //达不到指定高度则缩小高度
            if (yPos + mLineHeight < height) {
                height = yPos + mLineHeight;
            }
        } else {
            height = yPos + mLineHeight;
        }
        //设置ViewGroup宽高值
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int xPos = getPaddingLeft();
        int yPos = getPaddingTop();
        //设置每一个子View的位置,左上角xy坐标与右下角xy坐标确定View的位置
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childW = child.getMeasuredWidth();
                final int childH = child.getMeasuredHeight();
                //每行3个(每行满3个子view就换行)
                int newline = i % 3;
                if (newline == 0 && i != 0) {
                    xPos = getPaddingLeft();
                    yPos += mLineHeight;
                }
                child.layout(xPos, yPos, xPos + childW, yPos + childH);
                xPos += childW + hSpacing;

            }
        }

    }

    int position = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int currentX = (int) event.getX();
        int currentY = (int) event.getY();
        View child;
        if (position != -1 && position != pointToPosition(currentX, currentY)) {
            child = getChildAt(position);
            child.setBackgroundColor(getResources().getColor(R.color.white));
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                position = pointToPosition(currentX, currentY);
                child = getChildAt(position);
                if (child == null) {
                    return true;
                }
                child.setBackgroundColor(getResources().getColor(R.color.white));

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                position = pointToPosition(currentX, currentY);
                child = getChildAt(position);
                if (child == null) {
                    return true;
                }

                child.setBackgroundColor(getResources().getColor(R.color.white));
                if (mKeyClickCallback != null) {
                    if ((position + 1) == 10) {
                        mKeyClickCallback.onDelClick();
                    } else if ((position + 1) == 11) {
                        mKeyClickCallback.onNumClick(0);
                    } else if ((position + 1) == 12) {
                        mKeyClickCallback.onCleanClick();
                    } else {
                        mKeyClickCallback.onNumClick((position + 1));
                    }

                }
                break;
        }
        return true;
    }

    /**
     * 根据坐标，判断当前item所属的位置，即编号
     */
    public int pointToPosition(int x, int y) {
        Rect frame = new Rect();
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public interface KeyClickCallback {

        /**
         * 数字按键被点击
         */
        void onNumClick(int keyNum);

        /**
         * 退格按键
         */
        void onDelClick();

        /**
         * 清空按键
         */
        void onCleanClick();
    }

}
