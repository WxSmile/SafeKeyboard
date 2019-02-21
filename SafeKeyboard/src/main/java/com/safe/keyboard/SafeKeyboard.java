package com.safe.keyboard;

import android.content.Context;

import com.safe.keyboard.carrier.IKeyboardCarrier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wxSmile on 2019/2/21.
 *
 */

public class SafeKeyboard {

    private Context mContext;

    private IKeyboardCarrier mKeyboardContainer;
    private List<KeyboardEditText> inputs;

    private SafeKeyboard(Builder builder) {
        mContext = builder.mContext;
        mKeyboardContainer = builder.keyboardContainer;
        inputs = builder.inputs;
        initEditText();
    }

    private void initEditText() {
        for (KeyboardEditText editText : inputs) {
            editText.setOnTouchListener(editText);
            editText.setOnFocusChangeListener(editText);
            editText.bindKeyboard(this);
        }
    }

    public IKeyboardCarrier getCarrier() {
        return mKeyboardContainer;
    }

    public static class Builder {
        private Context mContext;
        private IKeyboardCarrier keyboardContainer;
        private List<KeyboardEditText> inputs;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder setKeyboardContainer(IKeyboardCarrier keyboardContainer) {
            this.keyboardContainer = keyboardContainer;
            return this;
        }

        public Builder addInputView(KeyboardEditText inputView) {
            if (inputView != null) {
                if (inputs == null) {
                    inputs = new ArrayList<>();
                }
                if (!inputs.contains(inputView)) {
                    inputs.add(inputView);
                }
            }
            return this;
        }

        public SafeKeyboard build() {
            if (keyboardContainer == null) {
                throw new IllegalArgumentException("please set IKeyboardCarrier");
            }

            if (inputs == null) {
                throw new IllegalArgumentException("please set a editText view");
            }

            return new SafeKeyboard(this);
        }
    }

    public void showKeyboard() {
        mKeyboardContainer.showKeyboard();
    }

    public void hideKeyboard() {
        mKeyboardContainer.hideKeyboard();
    }

    public boolean isKeyboardShowed() {
        return mKeyboardContainer.isKeyboardShowed();
    }

}
