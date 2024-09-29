package com.photostickers.customComponents;


import com.photostickers.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class CustomDialog extends Dialog implements android.view.View.OnClickListener {


    public Activity c;

    public Dialog d;

    public ImageView yes, no;

    private OnCloseInterface mInterface;

    public CustomDialog(Activity a, OnCloseInterface mI) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.mInterface = mI;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_exit);
        yes = findViewById(R.id.btn_yes);
        no = findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_yes) {
            if (this.mInterface != null) {
                this.mInterface.onClose();
            }
        } else if (v.getId() == R.id.btn_no) {
            dismiss();
        }

        dismiss();
    }

    public interface OnCloseInterface {

        void onClose();
    }
}