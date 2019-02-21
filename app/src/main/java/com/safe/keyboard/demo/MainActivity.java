package com.safe.keyboard.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.safe.keyboard.KeyboardEditText;
import com.safe.keyboard.SafeKeyboard;
import com.safe.keyboard.demo.carrier.DialogCarrier;
import com.safe.keyboard.demo.carrier.KeyboardDialog;
import com.safe.keyboard.demo.carrier.ViewCarrier;

public class MainActivity extends AppCompatActivity {

    private SafeKeyboard safeKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KeyboardEditText safeEdit = (KeyboardEditText) findViewById(R.id.safeEditText);
        KeyboardEditText safeEdit2 = (KeyboardEditText) findViewById(R.id.safeEditText2);
        LinearLayout keyboardContainer = (LinearLayout) findViewById(R.id.keyboard_container);

        ViewCarrier viewCarrier = new ViewCarrier.Builder(this)
                            .setContainerLayout(keyboardContainer)
                            .setKeyboardLayout(R.layout.layout_keyboard_view)
                            .setKeyboardViewId(R.id.safe_keyboard_view)
                            .setDelDrawable(getResources().getDrawable(R.drawable.icon_del))
                            .setLowDrawable(getResources().getDrawable(R.drawable.icon_capital_low))
                            .setUpDrawable(getResources().getDrawable(R.drawable.icon_capital_up))
                            .build();

        DialogCarrier dialogCarrier = new DialogCarrier.Builder(this)
                            .setDelDrawable(getResources().getDrawable(R.drawable.icon_del))
                            .setLowDrawable(getResources().getDrawable(R.drawable.icon_capital_low))
                            .setUpDrawable(getResources().getDrawable(R.drawable.icon_capital_up))
                            .setFragmentManager(getSupportFragmentManager())
                            .setKeyboardDialog(new KeyboardDialog())
                            .setKeyboardLayout(R.layout.layout_keyboard_view)
                            .setKeyboardViewId(R.id.safe_keyboard_view)
                            .build();

        safeKeyboard = new SafeKeyboard.Builder(this)
                            .setKeyboardContainer(viewCarrier)
                            .addInputView(safeEdit)
                            .addInputView(safeEdit2)
                            .build();
    }

    // 当点击返回键时, 如果软键盘正在显示, 则隐藏软键盘并是此次返回无效
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (safeKeyboard.isKeyboardShowed()) {
                safeKeyboard.hideKeyboard();
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}
