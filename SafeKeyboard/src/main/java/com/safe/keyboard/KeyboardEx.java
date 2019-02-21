package com.safe.keyboard;


import android.content.Context;
import android.inputmethodservice.Keyboard;

/**
 * Created by wxSmile on 2019/2/19.
 *
 */

public class KeyboardEx extends Keyboard {

    private int mXmlLayoutResId;

    public KeyboardEx(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
        this.mXmlLayoutResId = xmlLayoutResId;
    }

    public int getXmlLayoutResId() {
        return mXmlLayoutResId;
    }
}
