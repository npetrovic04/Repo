package com.photostickers.helpers;

import com.photostickers.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by petarljubic on 6/6/2016.
 */
public class CameraHelper
{

    private Activity mActivity;
    private Uri uri;

    public CameraHelper(Activity acti)
    {
        mActivity = acti;
    }
    public File output = null;

    /*** VEZANO ZA GALERIJU I SLIKANJE **/

    public static final String BITMAP_STORAGE_KEY = "viewbitmap";
    public static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    public Bitmap mImageBitmap;

    /*** VEZANO ZA GALERIJU I SLIKANJE **/

    public void dispatchTakePictureIntent(int actionCode)
    {
        if (isExternalStorageWritable() && mActivity != null)
        {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = getImageFileUri();
            i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            List<ResolveInfo> resInfoList = mActivity.getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList)
            {
                if(uri != null){
                    String packageName = resolveInfo.activityInfo.packageName;
                    mActivity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            mActivity.startActivityForResult(i, actionCode);
        }
    }

    public void handleBigCameraPhoto()
    {
        if (output != null && mActivity != null)
        {
            galleryAddPic();
            String[] mPaths = new String[1];
            mPaths[0] = output.getAbsolutePath();

            MediaScannerConnection.scanFile(mActivity, mPaths, null, new MediaScannerConnection.OnScanCompletedListener()
            {
                public void onScanCompleted(String path, Uri uri)
                {

                }
            });
        }
    }

    private Uri getImageFileUri()
    {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), mActivity.getString(R.string.app_name));

//        if (!output.exists())
//        {
//            if (!output.mkdirs())
//            {
//                return null;
//            }
//        }

        output = new File(folder, "IMAGE_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpeg");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDir = mActivity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();


//        if (!output.exists())
//        {
//            try
//            {
//                output.createNewFile();
//            }
//            catch (IOException ignored)
//            {
//
//            }
//        }

        //        return FileProvider.getUriForFile(mActivity, mActivity.getApplicationContext().getPackageName() + ".provider", output);
        return FileProvider.getUriForFile(mActivity.getApplicationContext(),
                mActivity.getApplicationContext().getPackageName() + ".provider",
                image);
    }

    public Uri getUri(){
        return uri;
    }

    private boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void galleryAddPic()
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(output);
        mediaScanIntent.setData(contentUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }

    private static final class Client implements MediaScannerConnection.MediaScannerConnectionClient
    {
        private final String path;
        private final String mimeType;
        MediaScannerConnection connection;

        public Client(String path, String mimeType)
        {
            this.path = path;
            this.mimeType = mimeType;
        }

        @Override
        public void onMediaScannerConnected()
        {
            connection.scanFile(path, mimeType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri)
        {
            connection.disconnect();
        }
    }
}
