package com.safe.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.safe.keyboard.carrier.IKeyboardCarrier;

import static com.safe.keyboard.KeyboardUtil.hideSystemKeyBoard;

/**
 * Created by wxSmile on 2019/2/21.
 *
 */

public class KeyboardEditText extends android.support.v7.widget.AppCompatEditText
                    implements View.OnTouchListener, View.OnFocusChangeListener {

    Handler mHandler;

    private static final int MSG_SHOW_KEYBOARD = 1;
    private static final int MSG_HIDE_KEYBOARD = 2;
    private static final long SHOW_DELAY = 200;
    private static final long DELAY_TIME = 100;

    private SafeKeyboard safeKeyboard;
    private IKeyboardCarrier keyboardCarrier;
    private long lastTouchTime;

    public KeyboardEditText(Context context) {
        super(context);
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindKeyboard(SafeKeyboard safeKeyboard) {
        this.safeKeyboard = safeKeyboard;
        this.keyboardCarrier = safeKeyboard.getCarrier();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        keyboardCarrier.bindEditText(this);

        //onTouchListener
        if (event.getAction() == MotionEvent.ACTION_UP) {
            hideSystemKeyBoard((KeyboardEditText) v);

            if (!isKeyboardShowed()) {
                mHandler.removeMessages(MSG_SHOW_KEYBOARD);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SHOW_KEYBOARD), SHOW_DELAY);
            }
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //onFocusListener
        boolean result = isValidTouch();
        if (!hasFocus) {
            if (result) {
                KeyboardEditText editText = keyboardCarrier.getBindEditText();
                /* 多个KeyboardEditText切换焦点时，不隐藏安全键盘*/
                if (isKeyboardShowed() && editText == this) {
                    hideKeyboard();
                }
            } else {
                hideKeyboard();
            }
        } else {
            hideSystemKeyBoard((KeyboardEditText) v);
            if (result) {
                if (!isKeyboardShowed()) {
                    mHandler.removeMessages(MSG_SHOW_KEYBOARD);
                    mHandler.sendMessageDelayed(
                                    mHandler.obtainMessage(MSG_SHOW_KEYBOARD),
                                    SHOW_DELAY);
                }
            } else {
                mHandler.removeMessages(MSG_SHOW_KEYBOARD);
                mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_SHOW_KEYBOARD),
                                SHOW_DELAY + DELAY_TIME);
            }
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_SHOW_KEYBOARD:
                            showKeyboard();
                            break;
                    }
                }
            };
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    private void closing() {
        removeMessage();
    }

    private void removeMessage() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_SHOW_KEYBOARD);
            mHandler.removeMessages(MSG_HIDE_KEYBOARD);
        }
    }

    private void showKeyboard() {
        safeKeyboard.showKeyboard();
    }

    private void hideKeyboard() {
        safeKeyboard.hideKeyboard();
    }

    private boolean isKeyboardShowed() {
        return safeKeyboard.isKeyboardShowed();
    }

    private boolean isValidTouch() {
        long thisTouchTime = SystemClock.elapsedRealtime();
        if (thisTouchTime - lastTouchTime > 500) {
            lastTouchTime = thisTouchTime;
            return true;
        }
        lastTouchTime = thisTouchTime;
        return false;
    }
}
