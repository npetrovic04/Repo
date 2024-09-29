package com.photostickers.customComponents;


import com.photostickers.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialogOk extends Dialog implements android.view.View.OnClickListener {


    public Activity c;
    public Dialog d;
    public ImageView yes;
    private TextView info_text;
    private String message;

    public CustomDialogOk(Activity a, String m) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        message = m;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_ok);

        info_text = this.findViewById(R.id.tekst);
        info_text.setText(message);

        yes = findViewById(R.id.btn_ok);
        yes.setOnClickListener(this);
    }

    public void setText(String s) {
        info_text.setText(s);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            dismiss();
        } else {
            dismiss();
        }
    }
}