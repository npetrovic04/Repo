package com.photostickers.customComponents;

import com.photostickers.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class CustomDialogMoreApps extends Dialog implements android.view.View.OnClickListener {

    public Activity c;

    public Dialog d;

    public ImageView yes, no;

    public CustomDialogMoreApps(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_more_apps);
        yes = findViewById(R.id.btn_yes);
        no = findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_yes) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://search?q=pub:" + v.getContext().getString(R.string.moreAppsNalog)));
                v.getContext().startActivity(intent);
            } catch (ActivityNotFoundException ignored) {

            }
        } else if (v.getId() == R.id.btn_no) {
            dismiss();
        }

        dismiss();
    }

}
