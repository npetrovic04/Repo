package com.photostickers;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.photostickers.adapters.AdapterWithNative;
import com.photostickers.adapters.StickersAdapter;
import com.photostickers.customComponents.CustomDialog;
import com.photostickers.customComponents.CustomDialogOk;
import com.photostickers.customComponents.CustomDialogText;
import com.photostickers.customComponents.StickerTextArea;
import com.photostickers.helpers.AdsHelper;
import com.photostickers.helpers.AndroidFileIO;
import com.photostickers.helpers.AudioHelper;
import com.photostickers.helpers.Constants;
import com.photostickers.helpers.ExifUtil;
import com.photostickers.helpers.FileIO;
import com.photostickers.helpers.ImageHelper;
import com.photostickers.helpers.PreferencesManager;
import com.photostickers.helpers.SavePictureHelper;
import com.photostickers.helpers.SingleMediaScanner;
import com.photostickers.helpers.share.ShareManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class EditorActivity extends MasterActivity implements View.OnClickListener, ShareManager.IShareTaskStatus, StickersAdapter.StickerAdapterInterface, CustomDialog.OnCloseInterface, CustomDialogText.GetTextEvent {
    private RelativeLayout centerContainer;
    private ImageView imagePreview;
    private StickerTextArea stickerTextArea;
    private ImageView flip;
    private ImageView text;
    private ImageView toFront;
    private ImageView trash;
    private RecyclerView stickersGrid;
    private RelativeLayout tutorial;
    private Uri shareUri;
    private String readImagePermission;

    private void findViews() {
        if (ContextCompat.checkSelfPermission(EditorActivity.this, readImagePermission) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (ContextCompat.checkSelfPermission(EditorActivity.this, readImagePermission) == PackageManager.PERMISSION_GRANTED) {
            centerContainer = findViewById(R.id.centerContainer);
            imagePreview = findViewById(R.id.imagePreview);
            stickerTextArea = findViewById(R.id.stickerTextArea);

            if (stickerTextArea != null) {
                stickerTextArea.context = EditorActivity.this;
            }

            ImageView facebook = findViewById(R.id.facebook);
            ImageView twitter = findViewById(R.id.twitter);
            ImageView insta = findViewById(R.id.insta);
            ImageView stickers = findViewById(R.id.stickers);
            flip = findViewById(R.id.flip);
            text = findViewById(R.id.text);
            toFront = findViewById(R.id.toFront);
            ImageView edit = findViewById(R.id.edit);
            trash = findViewById(R.id.trash);
            ImageView save = findViewById(R.id.save);
            stickersGrid = findViewById(R.id.stickersGrid);
            tutorial = findViewById(R.id.tutorial);

            if (facebook != null) {
                facebook.setOnClickListener(this);
            }

            if (twitter != null) {
                twitter.setOnClickListener(this);
            }

            if (insta != null) {
                insta.setOnClickListener(this);
            }

            if (stickers != null) {
                stickers.setOnClickListener(this);
            }

            if (flip != null) {
                flip.setOnClickListener(this);
                flip.setScaleX(0f);
                flip.setScaleY(0f);
                flip.setVisibility(View.INVISIBLE);
            }

            if (text != null) {
                text.setOnClickListener(this);
                text.setScaleX(0f);
                text.setScaleY(0f);
                text.setVisibility(View.INVISIBLE);
            }

            if (toFront != null) {
                toFront.setOnClickListener(this);
                toFront.setScaleX(0f);
                toFront.setScaleY(0f);
                toFront.setVisibility(View.INVISIBLE);
            }

            if (edit != null) {
                edit.setOnClickListener(this);
            }

            if (trash != null) {
                trash.setOnClickListener(this);
            }

            if (save != null) {
                save.setOnClickListener(this);
            }
        }
    }

    Bitmap bmp;
    StickersAdapter adapter;

    private ArrayList<Object> mData = new ArrayList<>();
    ArrayList<Integer> nativePositions = new ArrayList<>();

    GridLayoutManager gridLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readImagePermission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            readImagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        Intent i = getIntent();
        String path = i.getStringExtra("pathToFile");

        bmp = ImageHelper.decodeSampledBitmapFromResource(Uri.parse(path), this, (int) (125 * MasterActivity.scale + 0.5f), (int) (125 * MasterActivity.scale + 0.5f));
        bmp = ExifUtil.rotateBitmap(path, getApplicationContext(), bmp);

        if (bmp != null) {
            if (bmp.getWidth() > bmp.getHeight()) {
                //landscape
                setContentView(R.layout.activity_editor_landscape);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                //portrait
                setContentView(R.layout.activity_editor);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            finish();
        }

        findViews();

        if (ContextCompat.checkSelfPermission(EditorActivity.this, readImagePermission) == PackageManager.PERMISSION_GRANTED) {
            mData.clear();
            mData.addAll(Constants.getInstance().stickers);

            int gridOffset = 0;
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 4);

            if (bmp != null) {
                if (bmp.getWidth() > bmp.getHeight()) {
                    gridLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
                    gridOffset = 1;
                } else {
                    gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
                    gridOffset = 0;
                }
            }

            int startNative = 3 + gridOffset;
            nativePositions.add(startNative);

            for (int j = startNative + 1, counter = 1; j < Constants.getInstance().stickers.size() - 1; j++, counter++) {
                if (counter % (16 + gridOffset) == 0) {
                    nativePositions.add(j);
                }

            }

            for (int j = 0; j < nativePositions.size(); j++) {
                if (nativePositions.get(j) < mData.size()) {
                    mData.add(nativePositions.get(j), new StickersAdapter.EmptyItem());
                }
            }

            if (stickersGrid != null && gridLayoutManager != null) {
                stickersGrid.setLayoutManager(gridLayoutManager);
            }

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (nativePositions.contains(position)) {
                        return gridLayoutManager.getSpanCount();
                    }

                    return 1;
                }
            });

            AdapterWithNative.NativeAdSettings mNativeAdSettings = new AdapterWithNative.NativeAdSettings();
            mNativeAdSettings.setBgdColor(ContextCompat.getColor(this, R.color.nativeStickersBgdColor));
            mNativeAdSettings.setTitleColor(ContextCompat.getColor(this, R.color.nativeStickersTitleColor));
            mNativeAdSettings.setCtaTextColor(ContextCompat.getColor(this, R.color.nativeStickersCtaTextColor));
            mNativeAdSettings.setCtaBgdColor(ContextCompat.getColor(this, R.color.nativeStickersCtaBgdColor));
            mNativeAdSettings.setCtaStrokeColor(ContextCompat.getColor(this, R.color.nativeStickersCtaStrokeColor));

            adapter = new StickersAdapter(EditorActivity.this, mData, mNativeAdSettings, true);
            adapter.setStickerAdapterInterface(this);

            if (stickersGrid != null && adapter != null) {
                stickersGrid.setAdapter(adapter);
            }

            if (PreferencesManager.getInstance(getApplicationContext()).getBooleanValue(PreferencesManager.SHOW_TUTORIAL, true)) {
                if (bmp != null) {
                    if (bmp.getWidth() > bmp.getHeight()) {
                        tutorial.setBackgroundResource(R.drawable.tutorial_land);
                    } else {
                        tutorial.setBackgroundResource(R.drawable.tutorial);
                    }
                    PreferencesManager.getInstance(getApplicationContext()).setBooleanValue(PreferencesManager.SHOW_TUTORIAL, false);
                }
                if (tutorial != null) { // Iako ovo ne bi nikada trebalo da bude null, desavali se crashevi, pa smo ubacili ovu proveru
                    tutorial.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) {
            removenativeAd();
        }
    }

    private void removenativeAd() {
        for (int i = 0; i < nativePositions.size(); i++) {
            if (nativePositions.get(i) < mData.size()) {
                mData.set(nativePositions.get(i), new StickersAdapter.EmptyItem());
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private boolean checkIfNativeAdded() {
        int numberOfNative = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i) instanceof StickersAdapter.NativeAdItem) {
                numberOfNative++;
            }
        }

        return numberOfNative == nativePositions.size();
    }


    @Override
    protected void onFirstInit() {
        if (bmp != null) {
            stickerTextArea.setWidth(stickerTextArea.getWidth());
            stickerTextArea.setHeight(stickerTextArea.getHeight());
            stickerTextArea.setBitmap(bmp);
            imagePreview.setImageBitmap(bmp);
        }
    }

    private boolean clickedOnShare = false;

    @Override
    public void onClick(View v) {
        if (v.getScaleX() > 0f) {
            if (v.getId() == R.id.facebook) {
                if (!clickedOnShare) {
                    clickedOnShare = true;
                    ShareManager.getInstance().initShareManager(EditorActivity.this, getBitmapForShare(), "http://play.google.com/store/apps/details?id=" + getPackageName(), getString(R.string.message), getString(R.string.title));
                    ShareManager.getInstance().shareViaSocialNetworks(ShareManager.getInstance().FACEBOOK);
                }
            } else if (v.getId() == R.id.twitter) {
                if (!clickedOnShare) {
                    clickedOnShare = true;
                    ShareManager.getInstance().initShareManager(EditorActivity.this, getBitmapForShare(), "http://play.google.com/store/apps/details?id=" + getPackageName(), getString(R.string.message), getString(R.string.title));
                    ShareManager.getInstance().shareViaSocialNetworks(ShareManager.getInstance().TWITTER);
                }
            } else if (v.getId() == R.id.insta) {
                if (!clickedOnShare) {
                    clickedOnShare = true;
                    ShareManager.getInstance().initShareManager(EditorActivity.this, getBitmapForShare(), "http://play.google.com/store/apps/details?id=" + getPackageName(), getString(R.string.message), getString(R.string.title));
                    ShareManager.getInstance().shareViaSocialNetworks(ShareManager.getInstance().INSTAGRAM);
                }
            } else if (v.getId() == R.id.stickers) {
                stickersGrid.setVisibility(View.VISIBLE);
                stickersGrid.invalidate();

                if (!animationInProgress) {
                    if (optionsVisible) {
                        flip.setScaleX(0f);
                        flip.setScaleY(0f);
                        text.setScaleX(0f);
                        text.setScaleY(0f);
                        toFront.setScaleX(0f);
                        toFront.setScaleY(0f);
                        optionsVisible = false;
                    }
                }
            } else if (v.getId() == R.id.flip) {
                StickerTextArea.Img image = stickerTextArea.getSelectedImage();
                if (image != null) {
                    image.mMatrix.reset();
                    image.mMatrix.preScale(-1.0f, 1.0f);
                    image.b = Bitmap.createBitmap(image.b.copy(image.b.getConfig(), true), 0, 0, image.b.getWidth(), image.b.getHeight(), image.mMatrix, true);
                    image.drawable = new BitmapDrawable(getResources(), image.b);
                    stickerTextArea.invalidate();
                } else {
                    Info(5);
                }
            } else if (v.getId() == R.id.text) {
                CustomDialogText cdt = new CustomDialogText(this);
                cdt.show();
            } else if (v.getId() == R.id.toFront) {
                StickerTextArea.Img lastImage = stickerTextArea.getSelectedImage();
                if (lastImage != null) {
                    stickerTextArea.images.remove(lastImage);
                    stickerTextArea.images.add(lastImage);
                    stickerTextArea.invalidate();
                } else {
                    Info(6);
                }
            } else if (v.getId() == R.id.edit) {
                if (!animationInProgress) {
                    if (!optionsVisible) {
                        optionsVisible = true;
                        fadeIn();
                    } else {
                        fadeOut();
                        optionsVisible = false;
                    }
                }
            } else if (v.getId() == R.id.trash) {
                if (stickerTextArea.getSelectedImage() != null) {
                    deleteAnimation = (AnimationDrawable) trash.getDrawable();
                    deleteAnimation.stop();
                    deleteAnimation.start();
                    stickerTextArea.removeSelected();
                    stickerTextArea.invalidate();
                    stickerTextArea.postInvalidateDelayed(100); //fixme
                } else {
                    Info(3);
                }
            } else if (v.getId() == R.id.save) {
                checkPermissionAndRun();
            }
        }
    }

    AnimationDrawable deleteAnimation;

    boolean animationInProgress = false;

    ObjectAnimator flipAnimatorX;
    ObjectAnimator flipAnimatorY;
    ObjectAnimator textAnimatorX;
    ObjectAnimator textAnimatorY;

    ObjectAnimator toFrontAnimatorX;
    ObjectAnimator toFrontAnimatorY;

    private void fadeIn() {
        flip.setVisibility(View.VISIBLE);
        text.setVisibility(View.VISIBLE);
        toFront.setVisibility(View.VISIBLE);
        animationInProgress = true;
        flipAnimatorX = ObjectAnimator.ofFloat(flip, "scaleX", 0f, 1f);
        flipAnimatorX.setInterpolator(new OvershootInterpolator());
        flipAnimatorX.setStartDelay(400);
        flipAnimatorY = ObjectAnimator.ofFloat(flip, "scaleY", 0f, 1f);
        flipAnimatorY.setInterpolator(new OvershootInterpolator());
        flipAnimatorY.setStartDelay(400);

        textAnimatorX = ObjectAnimator.ofFloat(text, "scaleX", 0f, 1f);
        textAnimatorX.setInterpolator(new OvershootInterpolator());
        textAnimatorX.setStartDelay(200);
        textAnimatorY = ObjectAnimator.ofFloat(text, "scaleY", 0f, 1f);
        textAnimatorY.setInterpolator(new OvershootInterpolator());
        textAnimatorY.setStartDelay(200);

        toFrontAnimatorX = ObjectAnimator.ofFloat(toFront, "scaleX", 0f, 1f);
        toFrontAnimatorX.setInterpolator(new OvershootInterpolator());
        toFrontAnimatorY = ObjectAnimator.ofFloat(toFront, "scaleY", 0f, 1f);
        toFrontAnimatorY.setInterpolator(new OvershootInterpolator());

        flipAnimatorX.setDuration(500);
        flipAnimatorY.setDuration(500);
        textAnimatorX.setDuration(500);
        textAnimatorY.setDuration(500);
        toFrontAnimatorX.setDuration(500);
        toFrontAnimatorY.setDuration(500);

        flipAnimatorX.start();
        flipAnimatorY.start();
        textAnimatorX.start();
        textAnimatorY.start();
        toFrontAnimatorX.start();
        toFrontAnimatorX.removeListener(animEnd);
        flipAnimatorX.addListener(animEnd);
        toFrontAnimatorY.start();
    }

    Animator.AnimatorListener animEnd = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animationInProgress = false;

            if (!optionsVisible) {
                flip.setVisibility(View.INVISIBLE);
                text.setVisibility(View.INVISIBLE);
                toFront.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            animationInProgress = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private void fadeOut() {
        animationInProgress = true;
        flipAnimatorX.setStartDelay(0);
        flipAnimatorX.removeListener(animEnd);
        flipAnimatorY.setStartDelay(0);
        textAnimatorX.setStartDelay(200);
        textAnimatorY.setStartDelay(200);
        toFrontAnimatorX.setStartDelay(400);
        toFrontAnimatorY.setStartDelay(400);
        flipAnimatorX.reverse();
        flipAnimatorY.reverse();
        textAnimatorX.reverse();
        textAnimatorY.reverse();
        toFrontAnimatorX.reverse();

        toFrontAnimatorX.addListener(animEnd);
        toFrontAnimatorY.reverse();
    }

    boolean optionsVisible = false;

    private Bitmap getBitmapForShare() {
        stickerTextArea.nothingSelected();
        stickerTextArea.invalidate();
        return getViewBitmap(centerContainer);
    }

    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

    @Override
    public void onShareTaskFinished(boolean shareProcessStatus, String shareProcessMessage) {
        if (!shareProcessStatus) {
            Toast.makeText(getApplicationContext(), shareProcessMessage, Toast.LENGTH_SHORT).show();
        }

        if (AdsHelper.getInstance() != null) {
            AdsHelper.getInstance().showInterstitialForAction(this, "Back");
        }

        clickedOnShare = false;
    }

    @Override
    public void onClick(int res) {
        stickerTextArea.addImage(res, 0, (Object) null);
        stickersGrid.setVisibility(View.GONE);
        stickerTextArea.selectLastImage();
        stickerTextArea.invalidate();
    }

    @Override
    public void onBackPressed() {
        if (stickersGrid.getVisibility() == View.VISIBLE) {
            stickersGrid.setVisibility(View.GONE);
        } else {
            CustomDialog cdd = new CustomDialog(this, this);
            cdd.show();
        }
    }

    void Info(int code) {
        CustomDialogOk cdd = null;

        if (code == 3) {
            cdd = new CustomDialogOk(this, getString(R.string.sticker_not_selected));
        } else if (code == 4) {
            cdd = new CustomDialogOk(this, getString(R.string.no_stickers));
        } else if (code == 6) {
            cdd = new CustomDialogOk(this, getString(R.string.sticker_not_selected_for_bringfront));
        } else if (code == 5) {
            cdd = new CustomDialogOk(this, getString(R.string.sticker_not_selected_for_flip));
        }

        if (cdd != null) {
            cdd.show();
        }
    }

    public void checkPermissionAndRun() {
        if (ContextCompat.checkSelfPermission(EditorActivity.this, readImagePermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditorActivity.this, new String[]{readImagePermission}, Constants.REQUEST_EXTERNAL_STORAGE_CODE);
        } else {
            saveAs();
        }
    }

    private void saveAs() {
        shareUri = SavePictureHelper.INSTANCE.saveImage(this, System.currentTimeMillis() + ".png", getString(R.string.app_name), getBitmapForShare(), false);

        if (shareUri != null) {
            Toast.makeText(this, getString(R.string.messageSaved), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClose() {
        if (AdsHelper.getInstance() != null) {
            AdsHelper.getInstance().showInterstitialForAction(this, "Back");
        }

        super.onBackPressed();
    }

    @Override
    public void onGetText(String text, boolean color) {
        stickerTextArea.makeBitmapByText(text, color ? Color.WHITE : Color.BLACK, "Normal");
        stickerTextArea.selectLastImage();
    }

    File imageForShare;

    public class InitTask extends AsyncTask<Void, Void, Boolean> {

        String textForToast = "";

        @Override
        protected Boolean doInBackground(Void... params) {
            System.gc();

            if (bitmap != null) {
                imageForShare = saveImage(false);
                textForToast = getString(R.string.messageSaved);
            }

            return true;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            bitmap = getBitmapForShare();

            super.onPreExecute();
        }

        //Ovde udje nakon izvrsenog zahteva za login
        @Override
        protected void onPostExecute(final Boolean success) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);

            if (!textForToast.equals("")) {
                Toast.makeText(getApplicationContext(), textForToast, Toast.LENGTH_SHORT).show();
            }

            if (AdsHelper.getInstance() != null) {
                AdsHelper.getInstance().showInterstitialForAction(EditorActivity.this, "Back");
            }
        }

        @Override
        protected void onCancelled() {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }

    Bitmap bitmap;

    public File saveImage(boolean forShare) {
        System.gc();
        FileIO files;
        files = new AndroidFileIO(this);
        OutputStream outStream = null;
        String name = "";

        try {
            if (forShare) {
                name = ".share.jpg";
            } else {
                name = System.currentTimeMillis() + ".png";
            }

            outStream = files.writeFile(name);

            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            }

            outStream.flush();
            outStream.close();

            new SingleMediaScanner(EditorActivity.this, files.returnFile(name));

            if (bitmap != null) {
                bitmap.recycle();
            }

            bitmap = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files.returnFile(name);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(EditorActivity.this, readImagePermission) != PackageManager.PERMISSION_GRANTED) {
            finish();
            return;
        }

        if (ShareManager.getInstance() != null) {
            ShareManager.getInstance().onResume();
        }

        if (AudioHelper.getInstance() != null) {
            AudioHelper.getInstance().onStart();
        }

        if (AdsHelper.getInstance() != null) {
            AdsHelper.getInstance().setAdsHelperListener(this);
            AdsHelper.getInstance().initBanner(this);
        }
    }

    @Override
    public void onBannerLoaded(AdView adView) {
        adView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBannerClicked() {
        AdsHelper.getInstance().initBanner(this);
    }

    @Override
    public void onNativeLoaded(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {
        /*
        if (getResources().getBoolean(R.bool.nativeStickers))
        {
            if (!checkIfNativeAdded() && !nativeInProgress)
            {
                nativeInProgress = true;

                new android.os.Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (int i = 0; i < nativePositions.size(); i++)
                        {
                            if (nativePositions.get(i) < mData.size())
                            {
                                mData.set(nativePositions.get(i), new StickersAdapter.NativeAdItem());
                            }
                        }
                        if (adapter != null)
                        {
                            adapter.notifyDataSetChanged();
                        }
                        nativeInProgress = false;

                    }
                }, 250);
            }
        }
        */
    }

    @Override
    public void onNativeClicked() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
