package com.safe.keyboard.demo.carrier;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Created by wxSmile on 2019/2/22.
 *
 */

public class KeyboardDialog extends DialogFragment {

    private DialogCarrier dialogCarrier;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        iniAnimation();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setInnerBackgroundTransparent();
        if (dialogCarrier == null) {
            throw new IllegalArgumentException("dialog carrier is null");
        }
        return dialogCarrier.initKeyboardLayout();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dialogCarrier.init();
        dialogCarrier.startShowAnimation();
    }

    @Override
    public void onStart() {
        super.onStart();
        setOuterBackgroundTransparent();
    }


    /** 对话框内部的背景设为透明 */
    private void setInnerBackgroundTransparent() {
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /** 对话框外部的背景设为透明 */
    private void setOuterBackgroundTransparent() {
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.0f;
            attributes.gravity = Gravity.BOTTOM;
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //点击Dialog外部区域(包括被Dialog内容覆盖的区域)可以顺利响应点击事件(如果可点击)
            attributes.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            window.setAttributes(attributes);
        }
    }

    public void showKeyboard(FragmentManager fragmentManager) {
        if (isAdded()) {
            fragmentManager.beginTransaction().remove(this).commit();
        }

        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(this, "Keyboard");
            transaction.commitAllowingStateLoss();
            transaction.show(this);
        }
    }

    public void hideKeyboard() {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.isShowing()) {
            dialogCarrier.startHideAnimation();
        }
    }

    private void iniAnimation() {
        TranslateAnimation showAnimation = dialogCarrier.getShowAnimation();
        TranslateAnimation hideAnimation = dialogCarrier.getHideAnimation();

        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismissAllowingStateLoss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void carrier(DialogCarrier dialogCarrier) {
        this.dialogCarrier = dialogCarrier;
    }
}
