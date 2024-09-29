package com.photostickers;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.photostickers.helpers.AdsHelper;

public class MasterActivity extends AppCompatActivity implements AdsHelper.AdsHelperListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScale();
        firstInit = true;
    }

    boolean firstInit;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (firstInit) {
            onFirstInit();
            firstInit = false;
        }
    }

    protected void onFirstInit() {

    }

    public static float scale = 0;

    public void setScale() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        double inches = Math.sqrt((metrics.widthPixels * metrics.widthPixels) + (metrics.heightPixels * metrics.heightPixels)) / metrics.densityDpi;
        scale = metrics.density;

        if (inches > 9) {
            scale *= 2;
        } else if (inches > 6) {
            scale *= 1.5;
        }
    }

    @Override
    public void onBannerLoaded(AdView adView) {

    }

    @Override
    public void onBannerClicked() {

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

    }

    @Override
    public void onNativeClicked() {

    }

    @Override
    public void onNativeFailedToLoad() {

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

    @Override
    public void onResume() {
        super.onResume();

        if (AdsHelper.getInstance() != null) {
            AdsHelper.getInstance().setAdsHelperListener(this);
        }
    }
}
