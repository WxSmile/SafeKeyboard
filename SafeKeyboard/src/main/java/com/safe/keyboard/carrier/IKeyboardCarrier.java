package com.safe.keyboard.carrier;

import android.inputmethodservice.Keyboard;
import android.view.View;
import android.view.animation.Animation;

import com.safe.keyboard.KeyboardEditText;

/**
 * Created by wxSmile on 2019/2/21.
 *
 */

public interface IKeyboardCarrier {

    void init();

    void showKeyboard();

    void hideKeyboard();

    boolean isKeyboardShowed();

    void bindEditText(KeyboardEditText editText);

    /** 设置默认的键盘*/
    void setKeyboard(Keyboard keyboard);

    /** 初始化键盘布局*/
    View initKeyboardLayout();

    Animation getShowAnimation();
    Animation getHideAnimation();

    void startShowAnimation();
    void startHideAnimation();

    KeyboardEditText getBindEditText();
}
