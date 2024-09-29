package com.photostickers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gallery.PickConfig;
import com.gallery.adapter.ThumbPhotoAdapter;
import com.gallery.model.NativeSettings;
import com.gallery.model.Photo;
import com.gallery.views.CustomPickPhotoView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.photostickers.helpers.AdsHelper;
import com.photostickers.helpers.AudioHelper;

public class GalleryActivity extends AppCompatActivity implements ThumbPhotoAdapter.IItemsListener, CustomPickPhotoView.IProgressSetter, AdsHelper.AdsHelperListener {
    private CustomPickPhotoView gridGallery;

    private void findViews() {
        gridGallery = findViewById(R.id.gridGallery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        findViews();

        if (gridGallery != null) {
            gridGallery.setProgressSetter(this);
            gridGallery.setup(CustomPickPhotoView.TYPE.GALLERY_WITH_IMAGES, null, 0, 4, PickConfig.MODE_SINGLE_PICK, 1, true, -1, -1, 0, 7, 2);
            NativeSettings mNativeSettings = new NativeSettings(this);
            mNativeSettings.setNativeBgdColor(ContextCompat.getColor(this, R.color.nativeGalleryBgdColor));
            mNativeSettings.setNativeTitleColor(ContextCompat.getColor(this, R.color.nativeGalleryTitleColor));
            mNativeSettings.setNativeCtaTextColor(ContextCompat.getColor(this, R.color.nativeGalleryCtaTextColor));
            mNativeSettings.setNativeCtaBgdColor(ContextCompat.getColor(this, R.color.nativeGalleryCtaBgdColor));
            //mNativeSettings.setNativeCtaStroke(getResources().getBoolean(R.bool.nativeGalleryCtaStroke));
            mNativeSettings.setNativeCtaStrokeColor(ContextCompat.getColor(this, R.color.nativeGalleryCtaStrokeColor));
            //mNativeSettings.setNativeCtaRadius(getResources().getBoolean(R.bool.nativeGalleryCtaRadius));
            gridGallery.setNativeSettings(mNativeSettings);
        }
    }

    boolean clicked = false;

    @Override
    public void itemClick(Object o) {

    }

    @Override
    public void itemSelected(Object o) {

    }

    @Override
    public void itemDeselected(Object o) {

    }

    @Override
    public void itemSingleClicked(Object s) {
        if (!clicked) {
            clicked = true;
            Intent returnIntent = new Intent();
//            returnIntent.putExtra("result",s);
            if (s instanceof Photo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    returnIntent.setData(((Photo) s).getUri());
                else
                    returnIntent.setData(Uri.parse(((Photo) s).getPath()));
            }
            setResult(Activity.RESULT_OK, returnIntent);
            finish();

        }
    }

    @Override
    public void itemNumOverflow(Object s) {

    }

    @Override
    public void itemMaxReached(Object s) {

    }

    @Override
    public void allSelected() {

    }

    @Override
    public void allSelectedFromGallery() {

    }

    @Override
    public void allSelectedFromResources() {

    }

    @Override
    public void dataReady() {

    }

    @Override
    public void startProgressBar() {

    }

    @Override
    public void stopProgressBar() {

    }

    @Override
    public void onResume() {
        super.onResume();

        if (AdsHelper.getInstance() != null) {
            AudioHelper.getInstance().onStart();
            AdsHelper.getInstance().setAdsHelperListener(this);
            AdsHelper.getInstance().initBanner(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gridGallery != null) {
            /*
            if (!isFinishing() && CMSMain.shouldRemoveNativeAd())
            {
                gridGallery.refreshNativeAds(false, null);
            }
            else if (isFinishing())
            {
                gridGallery.stop();
            }
            */
            if (isFinishing()) {
                gridGallery.stop();
            }
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
    public void onInterstitialLoaded(String actionName) {

    }

    @Override
    public void onInterstitialClosed(String actionName) {

    }

    @Override
    public void onInterstitialFailed(String actionName) {

    }

    @Override
    public void onNativeLoaded(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {
        /*
        if (actionId.equalsIgnoreCase(getString(R.string.cms_native)))
        {
            if (getResources().getBoolean(R.bool.nativGallery))
            {
                gridGallery.refreshNativeAds(true, actionId);
            }
        }
        */
    }

    @Override
    public void onNativeClicked() {
        AdsHelper.getInstance().initNative(this, false);
    }

    @Override
    public void onNativeFailedToLoad() {
        AdsHelper.getInstance().initBanner(this);
    }

    @Override
    public void onRewardVideoLoaded(String actionName) {

    }

    @Override
    public void onRewardVideoCompleted(String actionName) {

    }

    @Override
    public void onRewardVideoClosed(String actionName) {

    }

    @Override
    public void onRewardVideoAmount(String currency, int amount) {

    }

    @Override
    public void onLoadingSplashClose() {

    }
}
