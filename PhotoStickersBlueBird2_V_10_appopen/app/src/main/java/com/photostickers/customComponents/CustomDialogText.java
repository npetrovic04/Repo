package com.photostickers.customComponents;


import com.photostickers.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

public class CustomDialogText extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public ImageView yes, no;
    public String text;
    private EditText inputTemp;
    private RadioButton colorPickerBlack, colorPickerWhite;
    public boolean color = false;   // false for black ,  true is for white

    private static GetTextEvent mGetTextEventListener;

    public interface GetTextEvent {

        void onGetText(String text, boolean color);
    }

    public CustomDialogText(Activity a) {
        super(a);
        this.c = a;
        mGetTextEventListener = (GetTextEvent) a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_text);
        yes = findViewById(R.id.btn_yes);
        no = findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        inputTemp = findViewById(R.id.text_input);
        colorPickerBlack = findViewById(R.id.blackTextRadioButton);
        colorPickerWhite = findViewById(R.id.whiteTextRadioButton);
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_yes) {
            if (colorPickerBlack.isChecked()) {
                color = false;
            }
            if (colorPickerWhite.isChecked()) {
                color = true;
            }

            text = inputTemp.getText().toString();
            if (mGetTextEventListener != null && !text.isEmpty()) {
                mGetTextEventListener.onGetText(text, color);
            }
            dismiss();
        } else if (v.getId() == R.id.btn_no) {
            dismiss();
        }

        dismiss();
    }

}
