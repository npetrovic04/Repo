package com.photostickers;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.photostickers.customComponents.CustomDialogMoreApps;
import com.photostickers.helpers.AdsHelper;
import com.photostickers.helpers.AudioHelper;
import com.photostickers.helpers.CameraHelper;
import com.photostickers.helpers.Constants;

public class MainActivity extends MasterActivity implements View.OnClickListener {
    private ImageView buttonGallery;
    private ImageView buttonCamera;
    private ImageView buttonMoreApps;
    private  String readImagePermission;

    private void findViews() {
        buttonGallery = findViewById(R.id.button_gallery);
        buttonCamera = findViewById(R.id.button_camera);
        buttonMoreApps = findViewById(R.id.button_more_apps);
    }

    CameraHelper cameraHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readImagePermission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            readImagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        cameraHelper = new CameraHelper(this);
        findViews();
        Constants.getInstance().listRaw("rage_", getApplicationContext());

        TextView privacyPolicyTextView = findViewById(R.id.privacyPolicyTextView);
        privacyPolicyTextView.setPaintFlags(privacyPolicyTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privacyPolicyTextView.setText(getString(R.string.privacyPolicyText));
        privacyPolicyTextView.setTextColor(ContextCompat.getColor(this, R.color.privacyPolicyTextColor));
        privacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.privacyPolicyUrl);
                if (url.length() > 0) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    } catch (Exception ignore) {
                    }
                }
            }
        });

        buttonGallery.setOnClickListener(this);
        buttonCamera.setOnClickListener(this);
        buttonMoreApps.setOnClickListener(this);

        if (AdsHelper.getInstance() == null) {
            new AdsHelper(this, null);
            //new AdsHelper(this, "22EAD0A67299D7286D61FEA96A1FC532"); //J4
            AdsHelper.getInstance().setAdsOrder(true, true, true, this);
        }
        AdsHelper.getInstance().setAdsHelperListener(this);

        new AudioHelper(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_gallery) {
            clickedOnGallery = true;
            checkPermissionAndRun();
        } else if (v.getId() == R.id.button_camera) {
            clickedOnGallery = false;
            checkPermissionAndRun();
        } else if (v.getId() == R.id.button_more_apps) {
            CustomDialogMoreApps moreApps = new CustomDialogMoreApps(MainActivity.this);
            moreApps.show();
        }
    }

    public boolean clickedOnGallery;

    public void checkPermissionAndRun() {
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this, readImagePermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{readImagePermission}, Constants.REQUEST_EXTERNAL_STORAGE_CODE);
            } else {
                if (clickedOnGallery) {
                    openGallery();
                } else {
                    openCamera();
                }
            }
        }
    }

    public void openCamera() {
        clickedOnGallery = false;
        cameraHelper.dispatchTakePictureIntent(1313);
    }

    public void openGallery() {
        Intent i = new Intent(this, GalleryActivity.class);
        startActivityForResult(i, 1);
        clickedOnGallery = false;
    }

    Class<?> activityToStart;

    public void openEditActivity(Class<?> activity, String pathToFile) {
        Intent i = new Intent(this, activity);
        i.putExtra("pathToFile", pathToFile);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == Constants.REQUEST_EXTERNAL_STORAGE_CODE) {
                    if (clickedOnGallery) {
                        openGallery();
                    } else {
                        openCamera();
                    }
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (requestCode == Constants.REQUEST_EXTERNAL_STORAGE_CODE) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, readImagePermission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.permission_denied));
                        builder.setMessage(getString(R.string.permission_storage_gallery));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{readImagePermission}, Constants.REQUEST_EXTERNAL_STORAGE_CODE);
                            }
                        });
                        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.permission_denied));

                        builder.setMessage(getString(R.string.permission_storage_gallery_settings));

                        builder.setPositiveButton(getString(R.string.goToSettings), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, Constants.REQUEST_EXTERNAL_STORAGE_CODE);
                            }
                        });

                        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }
                }
            }
        }
    }

    public boolean isCameraOn = false;
    String path;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_EXTERNAL_STORAGE_CODE) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, readImagePermission) == PackageManager.PERMISSION_GRANTED) {
                if (clickedOnGallery) {
                    openGallery();
                } else {
                    openCamera();
                }
            }
        }

        if (requestCode == 1313) {
            isCameraOn = false;

            if (resultCode == RESULT_OK) {
                cameraHelper.handleBigCameraPhoto();

                if (cameraHelper.output != null) {
                    path = cameraHelper.getUri().toString();
                    activityToStart = EditorActivity.class;
                    openEditActivity(activityToStart, path);
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (cameraHelper.output != null) {
                    cameraHelper.output.delete();
                }
            }
        }

        if (requestCode == 1) //Galerija
        {
            if (resultCode == RESULT_OK) {
                Uri chosenImageUri = data.getData();
                if (chosenImageUri != null) {
                    path = chosenImageUri.getPath();
                    activityToStart = EditorActivity.class;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        openEditActivity(activityToStart, chosenImageUri.toString());
                    } else {
                        openEditActivity(activityToStart, path);
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CameraHelper.BITMAP_STORAGE_KEY, cameraHelper.mImageBitmap);
        outState.putBoolean(CameraHelper.IMAGEVIEW_VISIBILITY_STORAGE_KEY, (cameraHelper.mImageBitmap != null));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cameraHelper.mImageBitmap = savedInstanceState.getParcelable(CameraHelper.BITMAP_STORAGE_KEY);
    }

    @Override
    public void onBackPressed() {
        AudioHelper.getInstance().onStop();

        if (AdsHelper.getInstance() != null) {
            AdsHelper.getInstance().clear();
        }

        super.onBackPressed();
    }

    @Override
    protected void onFirstInit() {
        ObjectAnimator galleryAnimX = ObjectAnimator.ofFloat(buttonGallery, "scaleX", 0f, 1f);
        ObjectAnimator galleryAnimY = ObjectAnimator.ofFloat(buttonGallery, "scaleY", 0f, 1f);
        ObjectAnimator cameraAnimX = ObjectAnimator.ofFloat(buttonCamera, "scaleX", 0f, 1f);
        ObjectAnimator cameraAnimY = ObjectAnimator.ofFloat(buttonCamera, "scaleY", 0f, 1f);
        ObjectAnimator appsAnimX = ObjectAnimator.ofFloat(buttonMoreApps, "scaleX", 0f, 1f);
        ObjectAnimator appsAnimY = ObjectAnimator.ofFloat(buttonMoreApps, "scaleY", 0f, 1f);

        AnimatorSet as = new AnimatorSet();
        as.playTogether(galleryAnimX, galleryAnimY, cameraAnimX, cameraAnimY, appsAnimX, appsAnimY);
        as.setInterpolator(new OvershootInterpolator());
        as.setDuration(600);
        as.start();
    }

    @Override
    public void onBannerLoaded(AdView adView) {
        adView.setVisibility(View.VISIBLE);
        findViewById(R.id.native_background).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBannerClicked() {
        AdsHelper.getInstance().initBanner(this);
    }

    @Override
    public void onNativeLoaded(NativeAd unifiedNativeAd, NativeAdView unifiedNativeAdView) {
        findViewById(R.id.native_background).setVisibility(View.VISIBLE);
        FrameLayout frameLayout = findViewById(R.id.native_holder);
        frameLayout.removeAllViews();
        frameLayout.addView(unifiedNativeAdView);
    }

    @Override
    public void onNativeClicked() {
        AdsHelper.getInstance().initNative(this, false);
        findViewById(R.id.native_background).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onInterstitialClosed(String actionName) {
        Log.v("UNITY_ADS_TEST", "!!! onInterstitialClosed za akciju: " + actionName);
        if (actionName.equalsIgnoreCase("Exit")) {
            finish();
        }
    }

    @Override
    public void onLoadingSplashClose() {
        AudioHelper.getInstance().onStart();
    }
}
