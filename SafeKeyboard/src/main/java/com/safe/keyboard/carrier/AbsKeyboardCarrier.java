package com.safe.keyboard.carrier;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import com.safe.keyboard.KeyboardEditText;
import com.safe.keyboard.KeyboardEx;
import com.safe.keyboard.KeyboardUtil;
import com.safe.keyboard.OnKeyboardActionListenerImpl;
import com.safe.keyboard.R;
import com.safe.keyboard.SafeKeyboardView;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by wxSmile on 2019/2/22.
 *
 */

public abstract class AbsKeyboardCarrier implements IKeyboardCarrier {

    private static final String TAG = "KeyboardCarrier";

    protected Context mContext;
    protected View keyboardLayout;
    private int keyboardLayoutRes;
    private int keyboardViewId;
    protected SafeKeyboardView keyboardView;
    protected Keyboard defaultKeyboard;
    protected KeyboardEditText mEditText;
    private TextView preViewTextView;

    private TranslateAnimation showAnimation;
    private TranslateAnimation hideAnimation;

    public static final long HIDE_TIME = 300;
    public static final long SHOW_TIME = 300;

    private KeyboardEx keyboardNumber;//数字键盘
    private KeyboardEx keyboardLetter;//字母键盘
    private KeyboardEx keyboardSymbol;//符号键盘

    private Drawable delDrawable;
    private Drawable lowDrawable;
    private Drawable upDrawable;

    private static boolean isCapes = false;
    private int keyboardType = 1;

    public AbsKeyboardCarrier(Creator builder) {
        this.mContext = builder.mContext;
        this.keyboardLayoutRes = builder.keyboardLayout;
        this.keyboardViewId = builder.keyboardViewId;
        this.delDrawable = builder.delDrawable;
        this.lowDrawable = builder.lowDrawable;
        this.upDrawable = builder.upDrawable;

        initKeyboard();
        initAnimation();
    }

    private void initKeyboard() {
        keyboardNumber = new KeyboardEx(mContext, R.xml.keyboard_num);            //实例化数字键盘
        keyboardLetter = new KeyboardEx(mContext, R.xml.keyboard_letter);         //实例化字母键盘
        keyboardSymbol = new KeyboardEx(mContext, R.xml.keyboard_symbol);         //实例化符号键盘

        //符号键盘与字母键盘共用一个KeyBoardView
        setKeyboard(keyboardLetter);
    }

    @Override
    public View initKeyboardLayout() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        keyboardLayout = inflater.inflate(keyboardLayoutRes, null, false);
        keyboardView = (SafeKeyboardView) keyboardLayout.findViewById(keyboardViewId);
        initKeyboardView();
        return keyboardLayout;
    }

    public void initKeyboardView() {
        if (keyboardView == null) return;

        keyboardView.setDelDrawable(delDrawable);
        keyboardView.setLowDrawable(lowDrawable);
        keyboardView.setUpDrawable(upDrawable);

        //给键盘View设置键盘
        keyboardView.setKeyboard(keyboardLetter);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(listener);
        keyboardView.setOnTouchListener(keyboardView);
        preViewTextView = getPreViewTextView();
    }


    private KeyboardView.OnKeyboardActionListener listener = new OnKeyboardActionListenerImpl() {

        @Override
        public void onPress(int primaryCode) {
            if (keyboardType == 3) {
                keyboardView.setPreviewEnabled(false);
            } else {
                keyboardView.setPreviewEnabled(true);
                if (primaryCode == -1 || primaryCode == -5 || primaryCode == 32 || primaryCode == -2
                                    || primaryCode == 100860 || primaryCode == -35) {
                    keyboardView.setPreviewEnabled(false);
                } else {
                    int paddingLeft = KeyboardUtil.dp2px(mContext, 11);
                    int paddingRight = KeyboardUtil.dp2px(mContext, 11);
                    int paddingTop = preViewTextView.getPaddingTop();
                    int paddingBottom = preViewTextView.getPaddingBottom();
                    if (primaryCode == 113 || primaryCode == 33 || primaryCode == 39) {//q ! '
                        if (preViewTextView != null) {
                            preViewTextView.setBackgroundResource(R.drawable.key_preview_left_bg);
                            preViewTextView.setPadding(KeyboardUtil.dp2px(mContext, 9), paddingTop, KeyboardUtil.dp2px(mContext, 9), paddingBottom);
                        }
                    }else if (primaryCode == 112 || primaryCode == 41 || primaryCode == 183) {//p ) ·
                        if (preViewTextView != null) {
                            preViewTextView.setBackgroundResource(R.drawable.key_preview_right_bg);
                            preViewTextView.setPadding(KeyboardUtil.dp2px(mContext, 10), paddingTop, KeyboardUtil.dp2px(mContext, 10), paddingBottom);
                        }
                    }else {
                        if (preViewTextView != null) {
                            preViewTextView.setBackgroundResource(R.drawable.key_preview_normal_bg);
                            preViewTextView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                        }
                    }

                    keyboardView.setPreviewEnabled(true);
                }
            }
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            try {
                Editable editable = mEditText.getText();
                int start = mEditText.getSelectionStart();
                int end = mEditText.getSelectionEnd();
                if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                    // 隐藏键盘
                    hideKeyboard();
                } else if (primaryCode == Keyboard.KEYCODE_DELETE || primaryCode == -35) {

                    // 回退键,删除字符
                    if (editable != null && editable.length() > 0) {
                        if (start == end) { //光标开始和结束位置相同, 即没有选中内容
                            editable.delete(start - 1, start);
                        } else { //光标开始和结束位置不同, 即选中EditText中的内容
                            editable.delete(start, end);
                        }
                    }
                } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                    // 大小写切换
                    changeKeyboardLetterCase();
                    // 重新setKeyboard, 进而系统重新加载, 键盘内容才会变化(切换大小写)
                    keyboardType = 1;
                    switchKeyboard();
                } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                    // 数字与字母键盘互换
                    if (keyboardType == 3) { //当前为数字键盘
                        keyboardType = 1;
                    } else {        //当前不是数字键盘
                        keyboardType = 3;
                    }
                    switchKeyboard();
                } else if (primaryCode == 100860) {
                    // 字母与符号切换
                    if (keyboardType == 2) { //当前是符号键盘
                        keyboardType = 1;
                    } else {        //当前不是符号键盘, 那么切换到符号键盘
                        keyboardType = 2;
                    }
                    switchKeyboard();
                } else {
                    // 输入键盘值
                    // editable.insert(start, Character.toString((char) primaryCode));
                    editable.replace(start, end, Character.toString((char) primaryCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void switchKeyboard() {
        switch (keyboardType) {
            case 1:
                keyboardView.setKeyboard(keyboardLetter);
                break;
            case 2:
                keyboardView.setKeyboard(keyboardSymbol);
                break;
            case 3:
                keyboardView.setKeyboard(keyboardNumber);
                break;
            default:
                Log.e(TAG, "ERROR keyboard type");
                break;
        }
    }

    private void changeKeyboardLetterCase() {
        List<Keyboard.Key> keyList = keyboardLetter.getKeys();
        if (isCapes) {
            for (Keyboard.Key key : keyList) {
                if (key.label != null && isUpCaseLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] += 32;
                }
            }
        } else {
            for (Keyboard.Key key : keyList) {
                if (key.label != null && isLowCaseLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] -= 32;
                }
            }
        }
        isCapes = !isCapes;
        keyboardView.setCap(isCapes);
    }

    private boolean isLowCaseLetter(String str) {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        return letters.contains(str);
    }

    private boolean isUpCaseLetter(String str) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return letters.contains(str);
    }

    public TextView getPreViewTextView() {
        TextView preViewTextView = null;
        try {
            Field field = KeyboardView.class.getDeclaredField("mPreviewText");
            field.setAccessible(true);
            preViewTextView = (TextView) field.get(keyboardView);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return preViewTextView;
    }

    private void initAnimation() {
        showAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF
                            , 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        hideAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF
                            , 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        showAnimation.setDuration(SHOW_TIME);
        hideAnimation.setDuration(HIDE_TIME);
    }

    @Override
    public TranslateAnimation getShowAnimation() {
        return showAnimation;
    }

    @Override
    public TranslateAnimation getHideAnimation() {
        return hideAnimation;
    }

    @Override
    public void startShowAnimation() {
        if (keyboardLayout == null) return;

        keyboardLayout.clearAnimation();
        keyboardLayout.startAnimation(showAnimation);
    }

    @Override
    public void startHideAnimation() {
        if (keyboardLayout == null) return;

        keyboardLayout.clearAnimation();
        keyboardLayout.startAnimation(hideAnimation);
    }

    @Override
    public KeyboardEditText getBindEditText() {
        return mEditText;
    }

    @Override
    public void bindEditText(KeyboardEditText editText) {
        this.mEditText = editText;
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        this.defaultKeyboard = keyboard;
    }

    public static class Creator<T extends Creator> {

        public Context mContext;
        public int keyboardLayout;
        public int keyboardViewId;
        private Drawable delDrawable;
        private Drawable lowDrawable;
        private Drawable upDrawable;

        public Creator(Context mContext) {
            this.mContext = mContext;
        }

        public T setKeyboardLayout(@LayoutRes int keyboardLayout) {
            this.keyboardLayout = keyboardLayout;
            return (T) this;
        }

        public T setKeyboardViewId(@IdRes int keyboardViewId) {
            this.keyboardViewId = keyboardViewId;
            return (T) this;
        }

        public T setDelDrawable(Drawable delDrawable) {
            this.delDrawable = delDrawable;
            return (T) this;
        }

        public T setLowDrawable(Drawable lowDrawable) {
            this.lowDrawable = lowDrawable;
            return (T) this;
        }

        public T setUpDrawable(Drawable upDrawable) {
            this.upDrawable = upDrawable;
            return (T) this;
        }
    }
}
