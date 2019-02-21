package com.safe.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import java.lang.reflect.Field;
import java.util.List;

import static com.safe.keyboard.KeyboardUtil.dp2px;

/**
 * Created by wxSmile on 2019/2/21.
 *
 */

public class SafeKeyboardView extends KeyboardView implements View.OnTouchListener {

    private static final String TAG = "SafeKeyboardView";

    private Context mContext;
    private boolean isCap;
    private Drawable delDrawable;
    private Drawable lowDrawable;
    private Drawable upDrawable;

    /**
     * 按键的宽高至少是图标宽高的倍数
     */
    private static final int ICON2KEY = 2;
    private Keyboard mKeyboard;

    public SafeKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        this.mContext = context;
    }

    public SafeKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        this.mContext = context;
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        this.mKeyboard = keyboard;
        int resId = ((KeyboardEx)keyboard).getXmlLayoutResId();
        ViewGroup parent = (ViewGroup) getParent();
        if (resId == R.xml.keyboard_letter || resId == R.xml.keyboard_symbol) {
            parent.setPadding(0, dp2px(getContext(),10), 0, dp2px(getContext(), 4));
        }else {
            parent.setPadding(0, 0, 0, 0);
        }
        super.setKeyboard(keyboard);
    }

    private CharSequence adjustCase(CharSequence label) {
        if (mKeyboard.isShifted() && label != null && label.length() < 3
                            && Character.isLowerCase(label.charAt(0))) {
            label = label.toString().toUpperCase();
        }
        return label;
    }


    private void init() {
        this.isCap = false;
        this.delDrawable = null;
        this.lowDrawable = null;
        this.upDrawable = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            KeyboardEx keyboard = (KeyboardEx) getKeyboard();
            int resId = keyboard.getXmlLayoutResId();
            List<Keyboard.Key> keys = keyboard.getKeys();
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == -5 || key.codes[0] == -2 || key.codes[0] == 100860 || key.codes[0] == -1) {
                    drawSpecialKey(canvas, key, resId);
                }else if (resId == R.xml.keyboard_letter || resId == R.xml.keyboard_symbol) {
                    drawKeyBackground(R.drawable.key_normal_bg, canvas, key);
                    drawTextAndIcon(canvas, key, null);
                }else {
                    drawKeyBackground(R.drawable.key_num_bg, canvas, key);
                    drawTextAndIcon(canvas, key, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawSpecialKey(Canvas canvas, Keyboard.Key key, int keyBoardResId) {
        if (key.codes[0] == -5) {//del
            int backgroundId;
            if (keyBoardResId == R.xml.keyboard_num) {
                backgroundId = R.drawable.key_special_num_bg;
            }else {
                backgroundId = R.drawable.key_special_normal_bg;
            }
            drawKeyBackground(backgroundId, canvas, key);
            drawTextAndIcon(canvas, key, delDrawable);
        } else if (key.codes[0] == -2 || key.codes[0] == 100860) { ///ABC
            int backgroundId;
            if (keyBoardResId == R.xml.keyboard_num) {
                backgroundId = R.drawable.key_special_num_bg;
            }else {
                backgroundId = R.drawable.key_special_normal_bg;
            }
            drawKeyBackground(backgroundId, canvas, key);
            drawTextAndIcon(canvas, key, null);
        } else if (key.codes[0] == -1) {
            if (isCap) {
                drawKeyBackground(R.drawable.key_special_capital_up_bg, canvas, key);
                drawTextAndIcon(canvas, key, upDrawable);
            } else {
                drawKeyBackground(R.drawable.key_special_capital_low_bg, canvas, key);
                drawTextAndIcon(canvas, key, lowDrawable);
            }
        }
    }

    private void drawKeyBackground(int id, Canvas canvas, Keyboard.Key key) {
        Drawable drawable = mContext.getResources().getDrawable(id);
        int[] state = key.getCurrentDrawableState();
        if (key.codes[0] != 0) {
            drawable.setState(state);
        }
        drawable.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
        drawable.draw(canvas);
    }

    private void drawTextAndIcon(Canvas canvas, Keyboard.Key key, @Nullable Drawable drawable) {
        try {
            Rect bounds = new Rect();
            Paint paint = new Paint();
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);

            String label = key.label == null ? null : adjustCase(key.label).toString();
            if (key.label != null) {
                Field field;

                if (label.length() > 1 && key.codes.length < 2) {
                    int labelTextSize = 0;
                    try {
                        field = KeyboardView.class.getDeclaredField(getContext().getString(R.string.mLabelTextSize));
                        field.setAccessible(true);
                        labelTextSize = (int) field.get(this);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }


                    paint.setTextSize(labelTextSize);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    int keyTextSize = 0;
                    try {
                        field = KeyboardView.class.getDeclaredField(getContext().getString(R.string.mKeyTextSize));
                        field.setAccessible(true);
                        keyTextSize = (int) field.get(this);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    paint.setTextSize(keyTextSize);
                    paint.setTypeface(Typeface.DEFAULT);
                }

                paint.getTextBounds(key.label.toString(), 0, key.label.toString().length(), bounds);
                canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                        (key.y + key.height / 2) + bounds.height() / 2, paint);
            }
            if (drawable == null) return;
            // 约定: 最终图标的宽度和高度都需要在按键的宽度和高度的二分之一以内
            // 如果: 图标的实际宽度和高度都在按键的宽度和高度的二分之一以内, 那就不需要变换, 否则就需要等比例缩小
            int iconSizeWidth, iconSizeHeight;
            key.icon = drawable;
            int iconH = px2dip(mContext, key.icon.getIntrinsicHeight());
            int iconW = px2dip(mContext, key.icon.getIntrinsicWidth());
            if (key.width >= (ICON2KEY * iconW) && key.height >= (ICON2KEY * iconH)) {
                //图标的实际宽度和高度都在按键的宽度和高度的二分之一以内, 不需要缩放, 因为图片已经够小或者按键够大
                setIconSize(canvas, key, iconW, iconH);
            } else {
                //图标的实际宽度和高度至少有一个不在按键的宽度或高度的二分之一以内, 需要等比例缩放, 因为此时图标的宽或者高已经超过按键的二分之一
                //需要把超过的那个值设置为按键的二分之一, 另一个等比例缩放
                //不管图标大小是多少, 都以宽度width为标准, 把图标的宽度缩放到和按键一样大, 并同比例缩放高度
                double multi = 1.0 * iconW / key.width;
                int tempIconH = (int) (iconH / multi);
                if (tempIconH <= key.height) {
                    //宽度相等时, 图标的高度小于等于按键的高度, 按照现在的宽度和高度设置图标的最终宽度和高度
                    iconSizeHeight = tempIconH / ICON2KEY;
                    iconSizeWidth = key.width / ICON2KEY;
                } else {
                    //宽度相等时, 图标的高度大于按键的高度, 这时按键放不下图标, 需要重新按照高度缩放
                    double mul = 1.0 * iconH / key.height;
                    int tempIconW = (int) (iconW / mul);
                    iconSizeHeight = key.height / ICON2KEY;
                    iconSizeWidth = tempIconW / ICON2KEY;
                }
                setIconSize(canvas, key, iconSizeWidth, iconSizeHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setIconSize(Canvas canvas, Keyboard.Key key, int iconSizeWidth, int iconSizeHeight) {
        iconSizeWidth *= 1.5;
        iconSizeHeight *= 1.5;
        int left = key.x + (key.width - iconSizeWidth) / 2;
        int top = key.y + (key.height - iconSizeHeight) / 2;
        int right = key.x + (key.width + iconSizeWidth) / 2;
        int bottom = key.y + (key.height + iconSizeHeight) / 2;
        key.icon.setBounds(left, top, right, bottom);
        key.icon.draw(canvas);
        key.icon = null;
    }

    public void setCap(boolean cap) {
        isCap = cap;
    }

    public void setDelDrawable(Drawable delDrawable) {
        this.delDrawable = delDrawable;
    }

    public void setLowDrawable(Drawable lowDrawable) {
        this.lowDrawable = lowDrawable;
    }

    public void setUpDrawable(Drawable upDrawable) {
        this.upDrawable = upDrawable;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //onTouchListener
        return event.getAction() == MotionEvent.ACTION_MOVE;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
