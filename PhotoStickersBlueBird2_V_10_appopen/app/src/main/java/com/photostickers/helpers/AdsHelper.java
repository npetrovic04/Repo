package com.photostickers.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.photostickers.MyApplication;
import com.photostickers.R;

import java.util.Random;

//Version 11.0

public class AdsHelper {


    private AdsHelperListener adsHelperListener;
    private static AdsHelper instance;
    private AdView adView;
    private AdRequest adRequest;
    private InterstitialAd[] mInterstitialAd;
    private String[] actionNameArray;
    private String[] actionIdArray;
    private int[] actionFireRateArray;
    private int[] countActionFireRateArray;
    private Boolean isBannerSet;
    private Boolean isNativeSet;
    private CountDownTimer countDownTimer;
    private Boolean loaderStart;
    private int countAction;
    private String testDeviceNumber;
    private Boolean interstitialFailed;
    private Activity activity;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private int pom;
    private Boolean loaderDoing;
    private Boolean dialogBoxShow;

    private String TAG = this.getClass().getSimpleName() + "TAG";


    public interface AdsHelperListener {
        void onBannerLoaded(AdView adView);

        void onBannerClicked();

        void onInterstitialLoaded(String actionName);

        void onInterstitialClosed(String actionName);

        void onInterstitialFailed(String actionName);

        void onNativeLoaded(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView);

        void onNativeClicked();

        void onNativeFailedToLoad();

        void onRewardVideoLoaded(String actionName);

        void onRewardVideoCompleted(String actionName);

        void onRewardVideoClosed(String actionName);

        void onRewardVideoAmount(String currency, int amount);

        void onLoadingSplashClose();
    }

    public void setAdsHelperListener(AdsHelperListener adsHelperListener) {
        this.adsHelperListener = adsHelperListener;
    }

    public static AdsHelper getInstance() {
        return instance;
    }

    public AdsHelper(Context context, String testDeviceNumber) {
        if (instance != null) {
            return;
        }

        instance = this;

        MobileAds.initialize(context);

        if (testDeviceNumber == null) {
            adRequest = new AdRequest.Builder()
                    .build();
        } else {
            adRequest = new AdRequest.Builder()
                    /*.addTestDevice(testDeviceNumber)*/
                    .build();
        }

        actionNameArray = context.getResources().getStringArray(R.array.action_name);
        actionIdArray = context.getResources().getStringArray(R.array.action_id);
        actionFireRateArray = context.getResources().getIntArray(R.array.action_fire_rate);
        countActionFireRateArray = new int[actionNameArray.length];
        mInterstitialAd = new InterstitialAd[actionNameArray.length + 1];
        countAction = 0;
        dialogBoxShow = false;
        interstitialFailed = false;
        this.testDeviceNumber = testDeviceNumber;

        for (int i = 0; i < countActionFireRateArray.length; i++) {
            countActionFireRateArray[i] = 1;
        }
    }

    public void clear() {
        instance = null;

        // TODO : VIDETI DA LI OVDE TREBA IZLAZ NA System.exit(0). PERA REKAO DA JE NEKAD ZBOG NECEGA TREBALO...
        //if (!unityVideoLoaded) {
            System.exit(0);
        //}
    }

    public void setAdsOrder(Boolean bannerSet, Boolean nativeSet, Boolean interstitialSet, Activity activity) {
        this.activity = activity;
        this.isBannerSet = bannerSet;
        this.isNativeSet = nativeSet;

        startLoading(activity);

        if (nativeSet) {
            initNative(activity, false);
        } else if (bannerSet) {
            initBanner(activity);
        } else if (interstitialSet) {
            initInterstitialForAction(activity);
        }

    }

    private AdSize getAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public void initBanner(final Activity activity) {
        FrameLayout frameLayout = activity.findViewById(R.id.adView);
        adView = new AdView(activity);
        AdSize adSize = getAdSize(activity);
        adView.setAdUnitId(activity.getString(R.string.banner_id));
        adView.setAdSize(adSize);

        try {
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    adsHelperListener.onBannerLoaded(adView);
                    frameLayout.setVisibility(View.VISIBLE);
                    initActionAfterBanner(activity);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    Log.i(TAG, "Banner failed");
                    initActionAfterBanner(activity);

                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    stopLoader();
                    Log.i(TAG, "Banner opened");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                    Log.i(TAG, "Banner closed");
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    adsHelperListener.onBannerClicked();
                }
            });

            frameLayout.removeAllViews();
            frameLayout.addView(adView);
            adView.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initActionAfterBanner(Activity activity) {
        if (actionNameArray.length > 0) {
            initInterstitialForAction(activity);
        }
    }

    public void initInterstitial(final Activity activity, final String interstitialId, final int actionId) {

        InterstitialAd.load(activity, interstitialId, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                //super.onAdLoaded(interstitialAd);
                if (mInterstitialAd != null) {
                    mInterstitialAd[actionId] = interstitialAd;
                    mInterstitialAd[actionId].setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            if (actionId == 0) {
                                stopLoader();
                                adsHelperListener.onInterstitialClosed("Start");
                                Log.i(TAG, "Interstitial start closed");
                            } else {
                                initInterstitial(activity, interstitialId, actionId);
                                //interstitialAd[actionId].loadAd(new AdRequest.Builder().build());
                                adsHelperListener.onInterstitialClosed(actionNameArray[actionId - 1]);
                                interstitialFailed = false;
                                Log.i(TAG, "Interstitial " + String.valueOf(actionNameArray[actionId - 1]) + " closed");
                            }
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                        }
                    });
                }
                if (countAction != actionNameArray.length + 1) {
                    initActionAfterInterstitial(activity, actionId);
                }

                if (actionId == 0) {
                    adsHelperListener.onInterstitialLoaded("Start");
                    Log.i(TAG, "Start interstitial loaded");
                } else {
                    adsHelperListener.onInterstitialLoaded(actionNameArray[actionId - 1]);
                    Log.i(TAG, actionNameArray[actionId - 1] + " interstitial loaded");
                }

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (mInterstitialAd != null) {
                    mInterstitialAd[actionId] = null;
                }
                if (countAction != actionNameArray.length + 1) {
                    initActionAfterInterstitial(activity, actionId);
                }

                if (actionId == 0) {
                    adsHelperListener.onInterstitialFailed("Start");
                } else {
                    adsHelperListener.onInterstitialFailed(actionNameArray[actionId - 1]);
                    interstitialFailed = true;
                }

            }
        });
    }

    private void initActionAfterInterstitial(Activity activity, int actionId) {
        if (actionId == 0) {
            if (loaderDoing) {
                mInterstitialAd[actionId].show(activity);
            }
            if (isNativeSet) {
                initNative(activity, false);
            } else if (isBannerSet) {
                initBanner(activity);
            } else {
                initInterstitialForAction(activity);
            }
        } else {
            initInterstitialForAction(activity);
        }
    }

    public void initStartInterstitial(Activity activity) {
        if (new Random().nextInt(100) < activity.getResources().getInteger(R.integer.start_frequency))
            initInterstitial(activity, activity.getResources().getString(R.string.start_interstitial_id), 0);
    }

    private void initInterstitialForAction(Activity activity) {
        if (actionNameArray.length == actionIdArray.length) {
            if (countAction < actionNameArray.length) {
                Log.i(TAG, "Loading action " + actionNameArray[countAction]);
                initInterstitial(activity, actionIdArray[countAction], countAction + 1);
                countAction++;
            }
        } else {
            Log.i(TAG, "Number of actions and placements is different");
        }
    }

    public void showInterstitialForAction(Activity activity, String actionName) {
        Boolean actionExist = false;

        if (actionIdArray.length == actionNameArray.length) {
            for (int i = 0; i < actionNameArray.length; i++) {
                if (actionNameArray[i].equals(actionName)) {
                    if (countActionFireRateArray[i] == actionFireRateArray[i]) {
                        fireInterstitial(i);
                        countActionFireRateArray[i] = 1;
                        Log.i(TAG, "Fire " + actionName + " action");
                    } else {
                        Log.i(TAG, "Increment number of clicks for action " + actionName);
                        Log.i(TAG, "Number of clicks is " + String.valueOf(countActionFireRateArray[i]) + " of " + String.valueOf(actionFireRateArray[i]));
                        countActionFireRateArray[i]++;
                    }
                    actionExist = true;
                    Log.i(TAG, "Show action: " + actionNameArray[i]);
                }
            }
            if (!actionExist) {
                Toast.makeText(activity, actionName + " action do not have in ads.xml file", Toast.LENGTH_LONG);
            }
        } else {
            Toast.makeText(activity, "Number of action and number of placement is different. Check do you have every action your own placement", Toast.LENGTH_LONG);
        }
    }

    private void fireInterstitial(int numberOfAction) {
        try {
            mInterstitialAd[numberOfAction + 1].show(activity);
        } catch (Exception e) {
            Log.i(TAG, "Error interstitial action: " + e.toString());
        }
    }

    public void initNative(final Activity activity, final Boolean isOtherScreen) {
        AdLoader.Builder builder = new AdLoader.Builder(activity, activity.getResources().getString(R.string.native_id));

        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onNativeAdLoaded(NativeAd unifiedNativeAd) {
                NativeAdView unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_unified, null);
                //NativeAdView unifiedNativeAdView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.native_ads, null);
                populateUnifiedNativeAdView(unifiedNativeAd, unifiedNativeAdView);
                adsHelperListener.onNativeLoaded(unifiedNativeAd, unifiedNativeAdView);
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError errorCode) {
                Log.i(TAG, "Failed to load native ad: ");
                initActionAfterNative(activity, false, isOtherScreen);
                /*if(testDeviceNumber != null) {
                    errorCodeTranslate(errorCode, "Native");
                }*/
                adsHelperListener.onNativeFailedToLoad();
            }

            @Override
            public void onAdLoaded() {
                Log.i(TAG, "Native ad loaded");
                initActionAfterNative(activity, true, isOtherScreen);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                adsHelperListener.onNativeClicked();
            }
        }).build();

        if (adLoader.isLoading()) {
            // The AdLoader is still loading ads.
            // Expect more adLoaded or onAdFailedToLoad callbacks.
            //Log.i("AdsHelperTAG", "Expect more adLoaded or onAdFailedToLoad callbacks.");
        } else {
            // The AdLoader has finished loading ads.
            //Log.i("AdsHelperTAG", "The AdLoader has finished loading ads.");
        }

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

//        if (nativeAd.getBody() == null) {
//            adView.getBodyView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getBodyView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
//        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

        VideoController vc = nativeAd.getMediaContent().getVideoController();

        if (vc.hasVideoContent()) {
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    Log.i(TAG, "Video status: Video playback has ended.");
                    super.onVideoEnd();
                }
            });
        } else {
            Log.i(TAG, "Video status: Ad does not contain a video asset.");
        }
    }

    private void initActionAfterNative(Activity activity, Boolean isLoaded, Boolean isOtherScreen) {
        if (!isOtherScreen) {
            if (isLoaded) {
                if (actionNameArray.length > 0) {
                    initInterstitialForAction(activity);
                }
            } else {
                if (isBannerSet) {
                    initBanner(activity);
                }
            }
        }
    }

    public void startLoading(Activity activity) {
        Log.i(TAG, "startLoading");
        try {
            showLoading(activity);
            loaderStart = true;
            loaderDoing = true;
        } catch (Exception e) {
            Log.i(TAG, "Error loader");
        }

        pom = 0;

        countDownTimer = new CountDownTimer(10000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(pom++);
            }

            @Override
            public void onFinish() {
                if (MyApplication.Companion.getInstance().appOpenManager.isAdAvailable()) {
                    MyApplication.Companion.getInstance().appOpenManager.showAdIfAvailable(new AppOpenManager.AppOpenInterface() {
                        @Override
                        public void appOpenDismissed() {
                            if (relativeLayout != null) {
                                progressBar.setProgress(100);
                                relativeLayout.setOnClickListener(null);
                                relativeLayout.setOnTouchListener(null);
                                relativeLayout.setVisibility(View.GONE);
                                loaderDoing = false;
                                if (!dialogBoxShow) {
                                    adsHelperListener.onLoadingSplashClose();
                                    dialogBoxShow = true;
                                }
                                if (isNativeSet) {
                                    initNative(activity, false);
                                } else if (isBannerSet) {
                                    initBanner(activity);
                                } else {
                                    initInterstitialForAction(activity);
                                }
                                //   adsHelperListener.onInterstitialFailed("Start");
                            }
                        }
                    });
                } else {
                    if (relativeLayout != null) {
                        progressBar.setProgress(100);
                        relativeLayout.setOnClickListener(null);
                        relativeLayout.setOnTouchListener(null);
                        relativeLayout.setVisibility(View.GONE);
                        loaderDoing = false;
                        if (!dialogBoxShow) {
                            adsHelperListener.onLoadingSplashClose();
                            dialogBoxShow = true;
                        }
                        adsHelperListener.onInterstitialFailed("Start");
                        if (isNativeSet) {
                            initNative(activity, false);
                        } else if (isBannerSet) {
                            initBanner(activity);
                        } else {
                            initInterstitialForAction(activity);
                        }
                    }
                }
            }
        }.start();
    }

    private void stopLoader() {
        if (loaderStart && relativeLayout != null) {
            countDownTimer.cancel();
            progressBar.setProgress(100);
            relativeLayout.setOnClickListener(null);
            relativeLayout.setOnTouchListener(null);
            relativeLayout.setVisibility(View.GONE);
            loaderDoing = false;
            if (!dialogBoxShow) {
                adsHelperListener.onLoadingSplashClose();
                dialogBoxShow = true;
            }
        }
    }

    private void showLoading(Activity activity) {
        relativeLayout = new RelativeLayout(activity);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.setOnClickListener(null);
        relativeLayout.setOnTouchListener(null);

        ImageView imageView = new ImageView(activity);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnClickListener(null);
        imageView.setOnTouchListener(null);

        progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams progresBarParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progresBarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        progresBarParams.setMargins(0, 0, 0, 50);
        progressBar.setLayoutParams(progresBarParams);
        progressBar.setPadding(30, 0, 30, 0);
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setProgressDrawable(activity.getResources().getDrawable(R.drawable.loading_style_horizontal));
        progressBar.setOnClickListener(null);
        progressBar.setOnTouchListener(null);

        relativeLayout.addView(imageView);
        relativeLayout.addView(progressBar);

        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        viewGroup.addView(relativeLayout);

        if (activity != null && activity.getResources().getIdentifier("loading_splash", "drawable", activity.getPackageName()) != 0) {
            imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.loading_splash));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imageView.setBackgroundColor(activity.getColor(R.color.loader_screen_color));
            } else {
                imageView.setBackgroundColor(activity.getResources().getColor(R.color.loader_screen_color));
            }
        }
    }

}
