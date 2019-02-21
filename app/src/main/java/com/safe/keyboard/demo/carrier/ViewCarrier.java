package com.safe.keyboard.demo.carrier;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import com.safe.keyboard.demo.R;
import com.safe.keyboard.carrier.AbsKeyboardCarrier;

/**
 * Created by wxSmile on 2019/2/21.
 *
 *
 */

public class ViewCarrier extends AbsKeyboardCarrier {

    private boolean onAnimation = false;

    private Handler hEndHandler = new Handler(Looper.getMainLooper());
    private Handler sEndHandler = new Handler(Looper.getMainLooper());

    private ViewCarrier(Builder builder) {
        super(builder);
        initKeyboardLayout();
        ViewGroup containerLayout = builder.containerLayout;
        containerLayout.addView(keyboardLayout);
        init();
    }

    private void initAnimation() {
        TranslateAnimation showAnimation = getShowAnimation();
        TranslateAnimation hideAnimation = getHideAnimation();

        showAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onAnimation = true;
                // 在这里设置可见, 会出现第一次显示键盘时直接闪现出来, 没有动画效果, 后面正常
                // keyboardLayout.setVisibility(View.VISIBLE);
                // 动画持续时间 SHOW_TIME 结束后, 不管什么操作, 都需要执行, 把 isShowStart 值设为 false; 否则
                // 如果 onAnimationEnd 因为某些原因没有执行, 会影响下一次使用
                sEndHandler.removeCallbacks(showEnd);
                sEndHandler.postDelayed(showEnd, SHOW_TIME);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onAnimation = false;
                keyboardLayout.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onAnimation = true;
                // 动画持续时间 HIDE_TIME 结束后, 不管什么操作, 都需要执行, 把 isHideStart 值设为 false; 否则
                // 如果 onAnimationEnd 因为某些原因没有执行, 会影响下一次使用
                hEndHandler.removeCallbacks(hideEnd);
                hEndHandler.postDelayed(hideEnd, HIDE_TIME);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onAnimation = false;
                if (keyboardLayout.getVisibility() != View.GONE) {
                    keyboardLayout.setVisibility(View.GONE);
                }
                keyboardLayout.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private final Runnable hideEnd = new Runnable() {
        @Override
        public void run() {
            onAnimation = false;
            if (keyboardLayout.getVisibility() != View.GONE) {
                keyboardLayout.setVisibility(View.GONE);
            }
        }
    };

    private final Runnable showEnd = new Runnable() {
        @Override
        public void run() {
            onAnimation = false;
            // 在迅速点击不同输入框时, 造成自定义软键盘和系统软件盘不停的切换, 偶尔会出现停在使用系统键盘的输入框时, 没有隐藏
            // 自定义软键盘的情况, 为了杜绝这个现象, 加上下面这段代码
            if (!mEditText.isFocused()) {
                hideKeyboard();
            }
        }
    };

    public static class Builder extends Creator<Builder> {

        private ViewGroup containerLayout;

        public Builder(Context mContext) {
            super(mContext);
        }

        public Builder setContainerLayout(ViewGroup containerLayout) {
            this.containerLayout = containerLayout;
            return this;
        }

        public ViewCarrier build() {
            if (containerLayout == null) throw new IllegalArgumentException("need a container");
            if (keyboardLayout == 0) throw new IllegalArgumentException("need keyboard layout");
            if (keyboardViewId == 0) throw new IllegalArgumentException("need keyboardView id");

            return new ViewCarrier(this);
        }
    }

    @Override
    public void init() {

        initAnimation();

        keyboardLayout.setVisibility(View.GONE);

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
        keyboardView.setKeyboard(defaultKeyboard);
        keyboardLayout.setVisibility(View.VISIBLE);
        keyboardLayout.clearAnimation();
        keyboardLayout.startAnimation(getShowAnimation());
    }

    @Override
    public void hideKeyboard() {
        keyboardLayout.clearAnimation();
        keyboardLayout.startAnimation(getHideAnimation());
    }

    @Override
    public boolean isKeyboardShowed() {
        boolean visible = keyboardLayout.getVisibility() == View.VISIBLE;
        return visible && !onAnimation;
    }
}
