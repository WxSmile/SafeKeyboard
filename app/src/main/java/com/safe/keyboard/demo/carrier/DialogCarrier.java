package com.safe.keyboard.demo.carrier;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.safe.keyboard.demo.R;
import com.safe.keyboard.carrier.AbsKeyboardCarrier;

/**
 * Created by wxSmile on 2019/2/22.
 *
 */

public class DialogCarrier extends AbsKeyboardCarrier {

    private KeyboardDialog keyboardDialog;
    private FragmentManager fragmentManager;
    private static final long SHOW_TIME = 300;

    public DialogCarrier(Builder builder) {
        super(builder);
        this.keyboardDialog = builder.keyboardDialog;
        this.fragmentManager = builder.fragmentManager;
        keyboardDialog.carrier(this);
    }

    @Override
    public void init() {
        TextView keyboardTip = (TextView) keyboardLayout.findViewById(R.id.keyboardTip);
        keyboardTip.getPaint().setFakeBoldText(true);
        TextView done = (TextView) keyboardLayout.findViewById(R.id.tv_keyboardDone);
        done.getPaint().setFakeBoldText(true);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isKeyboardShowed()) {
                    hideKeyboard();
                }
            }
        });
    }

    @Override
    public void showKeyboard() {
        keyboardDialog.showKeyboard(fragmentManager);
    }

    @Override
    public void hideKeyboard() {
        keyboardDialog.hideKeyboard();

    }

    @Override
    public boolean isKeyboardShowed() {
        return keyboardDialog != null && keyboardDialog.getDialog() != null
                            && keyboardDialog.getDialog().isShowing();
    }

    public static class Builder extends Creator<Builder> {

        private KeyboardDialog keyboardDialog;

        private FragmentManager fragmentManager;

        public Builder(Context mContext) {
            super(mContext);
        }

        public Builder setKeyboardDialog(KeyboardDialog keyboardDialog) {
            this.keyboardDialog = keyboardDialog;
            return this;
        }

        public Builder setFragmentManager(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            return this;
        }

        public DialogCarrier build() {
            return new DialogCarrier(this);
        }
    }
}
