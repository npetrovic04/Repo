package com.photostickers.helpers.share;


import com.photostickers.R;
import com.photostickers.helpers.SavePictureHelper;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.text.Html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marko on 3/19/2016.
 */
public class ShareManager
{

    //
    private final int ACTIVITY_NOT_FOUND_EXCEPTION = -5;

    private String SHARE_ERROR_APP_NOT_INSTALLED_SUFIX = " app is not installed, please install it first and try again";

    private String SHARE_ERROR_COMMON = "Error during share process, please try again with another share app";

    private String SHARE_LOADING = "Loading...Please wait";

    private String SHARE_DIALOG_TITLE = "Share via:";

    private String SHARE_CANCELLED = "Share cancelled";

    private String SHARE_SUCCESSFULLY = "Successfully shared!";

    private String SHARE_EXTERNAL_PERMISION_ERROR = "Hey developer, external storage permission is needed!!!";

    public final String FACEBOOK = "com.facebook.katana";

    public final String INSTAGRAM = "com.instagram.android";

    public final String TWITTER = "com.twitter.android";

    public final String GOOGLE_PLUS = "com.google.android.apps.plus";

    public final String GMAIL = "com.google.android.gm";

    public final String WHATSUP = "com.whatsapp";

    public final String VIBER = "com.viber.voip";

    public final String FB_MESSANGER = "com.facebook.orca";

    public String MMS = "com.android.mms";

    private final String[] MMS_ALTERNATIVES = new String[]{"com.sonyericsson.conversations", "com.motorola.blur.conversations", "com.htc.sense.mms"};

    public final String GOOGLE_HANGOUTS = "com.google.android.talk";

    public final String SKYPE = "com.skype.raider";

    public final String SYSTEM_SHARE = "system_share";

    public final String SHARABLE_IMAGE_NAME = "sharable_image.png";

    public final String SHARABLE_SOUND = "sharable_sound.wav";

    public static final int SHARE_INTENT_REQUEST_CODE = 21323;

    private Activity activity;

    private Bitmap bitmapToShare;

    private String link, message, title, errorMessage, errorMessageSufix, shareDialogTitle, cancelMessage, successfullySharedMessage;

    private Uri audioFileURI;

    private String shareError;

    private ProgressDialog loading;

    private ShareTask shareTask;

    private boolean sharingTaskCompleted, externalStoragePermissionNeeded;

    private ShareStatus currentShareStatus;

    private static ShareManager shareInstance = new ShareManager();

    public static ShareManager getInstance()
    {
        return shareInstance;
    }

    private ShareManager()
    {

    }


    private IShareTaskStatus shareStatusListener;

    public interface IShareTaskStatus
    {

        void onShareTaskFinished(boolean shareProcessStatus, String shareProcessMessage);
    }


    public void initShareManager(Activity activity, Uri audioFileURI, String link, String message, String title)
    {
        this.audioFileURI = audioFileURI;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {//provera da li je developer ukljucio external permision storage
            init(activity, link, message, title);
        }
        else
        {
            shareStatusListener = (IShareTaskStatus) activity;
            currentShareStatus = new ShareStatus(false, SHARE_EXTERNAL_PERMISION_ERROR); //ovo je poruka za developera, ne prevodi se
            sharingTaskCompleted = true;
            externalStoragePermissionNeeded = true;
            informActivity();

        }

    }

    public void initShareManager(Activity activity, Bitmap bitmapToShare, String link, String message, String title)
    {
        this.bitmapToShare = bitmapToShare;
        init(activity, link, message, title);
    }

    private void init(Activity activity, String link, String message, String title)
    {
        this.activity = activity;
        this.link = link;
        this.message = message;
        this.title = title;

        this.errorMessageSufix = SHARE_ERROR_APP_NOT_INSTALLED_SUFIX;
        this.shareDialogTitle = SHARE_DIALOG_TITLE;
        this.errorMessage = SHARE_ERROR_COMMON;
        this.cancelMessage = SHARE_CANCELLED;
        this.successfullySharedMessage = SHARE_SUCCESSFULLY;

        (loading = new ProgressDialog(this.activity)).setMessage(SHARE_LOADING);
        loading.setCanceledOnTouchOutside(false);

        shareStatusListener = (IShareTaskStatus) activity;
    }


    //initialiazer
    public void initShareManager(Activity context, Uri audioFileURI, String link, String message, String title, String loadingDialogText, String shareDialogTitle, String successfullySharedMessage, String errorMessage, String errorMessageSufix, String cancelMessage)
    {
        initShareManager(context, audioFileURI, link, message, title);
        setCustomMessages(loadingDialogText, shareDialogTitle, successfullySharedMessage, errorMessage, errorMessageSufix, cancelMessage);
    }

    public void initShareManager(Activity context, Bitmap bitmapToShare, String link, String message, String title, String loadingDialogText, String shareDialogTitle, String successfullySharedMessage, String errorMessage, String errorMessageSufix, String cancelMessage)
    {
        initShareManager(context, bitmapToShare, link, message, title);
        setCustomMessages(loadingDialogText, shareDialogTitle, successfullySharedMessage, errorMessage, errorMessageSufix, cancelMessage);
    }

    private void setCustomMessages(String loadingDialogText, String shareDialogTitle, String successfullySharedMessage, String errorMessage, String errorMessageSufix, String cancelMessage)
    {
        if (errorMessageSufix != null && !errorMessageSufix.equalsIgnoreCase(""))
        {
            this.errorMessageSufix = errorMessageSufix;
        }

        if (shareDialogTitle != null && !shareDialogTitle.equalsIgnoreCase(""))
        {
            this.shareDialogTitle = shareDialogTitle;
        }

        if (loadingDialogText != null || !loadingDialogText.equalsIgnoreCase(""))
        {
            if (this.loading != null)
            {
                loading.setMessage(loadingDialogText);
            }
        }

        if (errorMessage != null || !errorMessage.equalsIgnoreCase(""))
        {
            this.errorMessage = errorMessage;
        }

        if (cancelMessage != null || !cancelMessage.equalsIgnoreCase(""))
        {
            this.cancelMessage = cancelMessage;
        }

        if (successfullySharedMessage != null || !successfullySharedMessage.equalsIgnoreCase(""))
        {
            this.successfullySharedMessage = successfullySharedMessage;
        }
    }


    class ShareStatus
    {

        private String shareTaskMessage;

        private boolean shareTaskStatus;

        public ShareStatus(boolean shareTaskStatus, String shareTaskMessage)
        {
            this.shareTaskMessage = shareTaskMessage;
            this.shareTaskStatus = shareTaskStatus;
        }

    }

    //main methods

    public void onResume()
    {

        informActivity();

    }


    private void informActivity()
    {
        if (sharingTaskCompleted)
        {
            sharingTaskCompleted = false;

            if (shareStatusListener != null && currentShareStatus != null)
            {
                shareStatusListener.onShareTaskFinished(currentShareStatus.shareTaskStatus, currentShareStatus.shareTaskMessage);
                currentShareStatus = null;
            }
        }
    }


    public void shareViaSocialNetworks(String... socialNetworks)
    {

        if (externalStoragePermissionNeeded)
        {
            currentShareStatus = new ShareStatus(false, SHARE_EXTERNAL_PERMISION_ERROR); //ovo je poruka za developera, ne prevodi se
            sharingTaskCompleted = true;
            externalStoragePermissionNeeded = true;
            informActivity();
            return;
        }

        if (socialNetworks != null && socialNetworks.length == 1)
        {//single networks share

            shareViaSingleSocialNetwork(socialNetworks[0]);

        }
        else
        {//sistemski share sa samo nabrojanim mrezama

            shareTask = new ShareTask(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                shareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, socialNetworks);
            }
            else
            {
                shareTask.execute(socialNetworks);
            }

        }

    }

    public void shareWithExclusion(String... socialNetworksToExclude)
    {

        if (externalStoragePermissionNeeded)
        {
            currentShareStatus = new ShareStatus(false, SHARE_EXTERNAL_PERMISION_ERROR); //ovo je poruka za developera, ne prevodi se
            sharingTaskCompleted = true;
            externalStoragePermissionNeeded = true;
            informActivity();
            return;
        }

        //sistemski share sa filtriranjem
        shareTask = new ShareTask(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            shareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, socialNetworksToExclude);
        }
        else
        {
            shareTask.execute(socialNetworksToExclude);
        }

    }

    private void shareViaSingleSocialNetwork(String socialNetwork)
    {

        ApplicationInfo applicationInfo = getInstalledAppInfo(activity, socialNetwork);

        if (socialNetwork.equalsIgnoreCase(MMS) && applicationInfo == null)
        {//ako je developer izabrao MMS, proveri da li je Sony ili motorola, jer imaju razlicite app za MMS (ne koriste klasican com.android.mms)
            for (String mmsApp : MMS_ALTERNATIVES)
            {
                applicationInfo = getInstalledAppInfo(activity, mmsApp);
                if (applicationInfo != null)
                {
                    socialNetwork = applicationInfo.packageName;
                    MMS = applicationInfo.packageName;
                    break;
                }
            }
        }

        if (applicationInfo != null) //ukoliko je MMS(nema packagename)
        {//ako postoji, onda je isntalirana app

            //start share task
            shareTask = new ShareTask(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                shareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, socialNetwork);
            }
            else
            {
                shareTask.execute(socialNetwork);
            }

        }
        else
        {

            if (applicationInfo != null && !applicationInfo.name.equalsIgnoreCase(""))
            {
                errorMessage = applicationInfo.name + " " + this.errorMessageSufix;
            }
            else
            {
                if (socialNetwork.equalsIgnoreCase(FACEBOOK))
                {
                    errorMessage = "Facebook " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(TWITTER))
                {
                    errorMessage = "Twitter " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(INSTAGRAM))
                {
                    errorMessage = "Instagram " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(VIBER))
                {
                    errorMessage = "Viber " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(GOOGLE_PLUS))
                {
                    errorMessage = "Google+ " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(GMAIL))
                {
                    errorMessage = "Gmail " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(WHATSUP))
                {
                    errorMessage = "Whatsapp " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(FB_MESSANGER))
                {
                    errorMessage = "FB Messenger " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(SKYPE))
                {
                    errorMessage = "Skype " + this.errorMessageSufix;
                }
                else if (socialNetwork.equalsIgnoreCase(MMS))
                {
                    errorMessage = "MMS " + this.errorMessageSufix;
                }
                else
                {
                    errorMessage = "This " + this.errorMessageSufix;
                }
            }

            //Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();

            shareStatusListener.onShareTaskFinished(false, errorMessage);

        }

        /*}*/

    }

    public void cancelShare()
    {
        if (shareTask != null)
        {
            if (!shareTask.isCancelled())
            {
                shareTask.cancel(true);
            }
        }

    }


    Intent buildSingleNetworkShareIntent(Activity activity, String socialNetwork, Uri sharableUri, String link, String message, String title) throws Exception
    {

        ApplicationInfo info = getInstalledAppInfo(activity, socialNetwork);

        /*kod S2 ovde treba permisija, razmisli da li da ide u ovom lsucaju prebudzivanje za sisteme >= 5.0 da ide ovako, a za manje samo da sibne text*/

        Intent targetedShare = new Intent(Intent.ACTION_SEND);
        targetedShare.setType(bitmapToShare != null ? "image/*" : "audio/*");

        //za trazenje permisija za 6.0 WRITE_EXTERNAL_STORAGE: http://stackoverflow.com/a/33149822/1485837
        //sharovanje preko SD kartice
        /*if(socialNetwork.equalsIgnoreCase(MMS))
        {



            File file = new File(activity.getExternalFilesDir(null), SHARABLE_IMAGE_NAME);
            FileOutputStream out;
            out = new FileOutputStream(file);
            bitmapToShare.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            targetedShare.putExtra("sms_body", "user_location");
            targetedShare.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            targetedShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            targetedShare.setPackage(info.packageName);

            return targetedShare;

        }*/

        if (sharableUri != null && !socialNetwork.equalsIgnoreCase("com.sonyericsson.conversations")) //zeza kod sonijeve app
        {
            //DODAJ SAMO MREZAMA KOJE MOGU DA SHERUJU SLIKU
            activity.grantUriPermission(info.packageName, sharableUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (socialNetwork.equalsIgnoreCase(MMS))
            {
                if (audioFileURI == null) //ukoliko share-uje sliku ide preko content provider-a, u suprotnom je audio share
                {
                    sharableUri = Uri.parse("content://" + activity.getPackageName() + ".provider" + "/" + SHARABLE_IMAGE_NAME);
                }
            }

            targetedShare.putExtra(Intent.EXTRA_STREAM, sharableUri);

        }

        //ovde treba da se prilagodi mrezama
        if (!info.packageName.equalsIgnoreCase(FACEBOOK))
        {
            //

            if (link != null && !link.equalsIgnoreCase("") && message != null && !message.equalsIgnoreCase(""))
            {//oba dostupna
                targetedShare.putExtra(Intent.EXTRA_TEXT, message + "\n" + link);
            }
            else
            {
                if (link != null && !link.equalsIgnoreCase(""))
                {//znaci da je link dostupan, on ima priorotet u odnosu na message
                    targetedShare.putExtra(Intent.EXTRA_TEXT, link);
                }
                else
                {
                    targetedShare.putExtra(Intent.EXTRA_TEXT, message);
                }
            }

            if (title != null && !title.equalsIgnoreCase(""))
            {
                targetedShare.putExtra(Intent.EXTRA_SUBJECT, title);
            }

            targetedShare.putExtra(Intent.EXTRA_TEXT, message + "\n" + link);
        }

        //obradi posebno GMAIL

        targetedShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        targetedShare.setPackage(info.packageName);
        /*if(socialNetwork.equalsIgnoreCase(MMS))
        {
            targetedShare.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageMms");
        }*/
        //targetedShare.setClassName(info.packageName, info.className);

        return targetedShare;

    }


    Intent getSystemShareIntent(Activity activity, Uri sharableUri, String link, String message, String title, String shareDialogTitle, boolean excludeSomeNetworks, String... socialNetworks) throws Exception
    {

        Intent chooserIntent;

        List<Intent> targetedShareIntents = new ArrayList<Intent>();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(bitmapToShare != null ? "image/*" : "audio/*");

        List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(share, 0);

        Intent targetedShare;

        boolean currentNetworkFound = false;

        if (!resInfo.isEmpty())
        {

            for (ResolveInfo info : resInfo)
            {
                if (info.activityInfo.packageName.equals(activity.getPackageName()))
                {
                    continue;
                }
                if (socialNetworks != null && socialNetworks.length > 0)
                {//hocemo samo odredjene mreze da ukljucimo/iskljucimo

                    for (String network : socialNetworks)
                    {

                        currentNetworkFound = false;
                        if (excludeSomeNetworks)
                        {
                            if (info.activityInfo.packageName.equalsIgnoreCase(network))
                            {
                                currentNetworkFound = true;
                                break;
                            }

                        }
                        else
                        {//just some networks
                            if (info.activityInfo.packageName.equalsIgnoreCase(network))
                            {
                                targetedShare = buildSingleNetworkShareIntent(activity, network, sharableUri, link, message, title);
                                targetedShare.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                                targetedShareIntents.add(targetedShare);
                            }
                        }

                    }

                    //provera za iskljucene mreze
                    if (excludeSomeNetworks && !currentNetworkFound)
                    {//dodaj u listu smao ako nisi nasao neku iz liste iskljucenih mreza
                        targetedShare = buildSingleNetworkShareIntent(activity, info.activityInfo.packageName, sharableUri, link, message, title);
                        targetedShare.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                        targetedShareIntents.add(targetedShare);

                    }

                }
                else
                {//all networks
                    targetedShare = buildSingleNetworkShareIntent(activity, info.activityInfo.packageName, sharableUri, link, message, title);
                    targetedShare.setClassName(info.activityInfo.packageName, info.activityInfo.name);
                    targetedShareIntents.add(targetedShare);
                }

            }

        }
        else
        {//vratio praznu listu, nesto ne valja, prijavi gresku
            throw new Exception("Prazna lista activity-ja za slanje podataka");
        }

        chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), shareDialogTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));

        return chooserIntent;

    }


    //share task
    class ShareTask extends AsyncTask<String, Void, Integer>
    {


        boolean excludeFromList = false;

        public ShareTask(boolean excludeFromList)
        {
            this.excludeFromList = excludeFromList;
        }

        @Override
        protected void onPreExecute()
        {

            if (activity != null && !activity.isFinishing() && loading != null && !loading.isShowing())
            {
                loading.show();
            }

            sharingTaskCompleted = false;

        }

        @Override
        protected Integer doInBackground(String... socialNetworks)
        {

            int result = 0;

            if (socialNetworks.length == 1)
            {//single networks share

                try
                {

                    Uri sharableUri = null;

                    if (bitmapToShare != null)
                    {//share0uje ili bitmpapu ili audio file
                        sharableUri = getUriFromSavedBitmap(activity, bitmapToShare);
                    }
                    else if (audioFileURI != null)
                    {
                        //sharableUri = getUriFromSavedFile(activity, audioFile);
                        sharableUri = audioFileURI;
                    }

                    if (sharableUri != null)
                    {
                        activity.startActivityForResult(buildSingleNetworkShareIntent(activity, socialNetworks[0], sharableUri, link, message, title), SHARE_INTENT_REQUEST_CODE);

                        result = 1;
                    }
                    else
                    {
                        result = 0;
                        errorMessage = "Ne moze da napravi bitmapu iz nekog razloga, proveri ovo";
                    }
                }
                catch (ActivityNotFoundException e)
                {
                    //ne postoji ova app
                    result = ACTIVITY_NOT_FOUND_EXCEPTION;
                    if (e != null && e.getMessage() != null && !e.getMessage().equalsIgnoreCase(""))
                    {
                        errorMessage = e.getMessage() + "Ne postoji activity koji moze da handle-uje ovaj share";
                    }
                }
                catch (Exception e)
                {
                    if (e != null && e.getMessage() != null && !e.getMessage().equalsIgnoreCase(""))
                    {
                        if (e != null && e.getMessage() != null && !e.getMessage().equalsIgnoreCase(""))
                        {

                            if (e.getMessage().startsWith("Attempt to invoke virtual method 'android.content.res.XmlResourceParser"))
                            {
                                errorMessage = Html.fromHtml("<big>Proveri da li si dodao odgovarajuce <b>providere u manifest</b> <br>i <b>fajl res/xml/file_paths.xml </b></big>").toString();
                            }
                            else
                            {
                                errorMessage = e.getMessage();
                            }
                        }
                    }
                    result = 0;
                }

            }
            else
            {//sistem share

                try
                {
                    //Uri sharableUri = getUriFromSavedBitmap(activity, bitmapToShare);

                    Uri sharableUri = null;

                    if (bitmapToShare != null)
                    {
                        sharableUri = getUriFromSavedBitmap(activity, bitmapToShare);
                    }
                    else if (audioFileURI != null)
                    {
                        sharableUri = audioFileURI;
                    }

                    activity.startActivityForResult(getSystemShareIntent(activity, sharableUri, link, message, title, shareDialogTitle, excludeFromList, socialNetworks), SHARE_INTENT_REQUEST_CODE);
                    result = 1;
                }
                catch (Exception e)
                {
                    if (e != null && e.getMessage() != null && !e.getMessage().equalsIgnoreCase(""))
                    {
                        errorMessage = e.getMessage();
                    }

                    e.printStackTrace();
                    result = 0;
                }

            }

            return result;
        }


        @Override
        protected void onPostExecute(Integer result)
        {

            if (loading != null && loading.isShowing())
            {
                loading.dismiss();
            }

            sharingTaskCompleted = true;

            if (result > 0)
            {
                //Toast.makeText(activity, "USPESNO", Toast.LENGTH_SHORT).show();

                currentShareStatus = new ShareStatus(true, successfullySharedMessage);
            }
            else
            {

                /*if (errorMessage != null && !errorMessage.equalsIgnoreCase(""))
                {
                    Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                }

                if (shareStatusListener != null)
                {
                    shareStatusListener.onShareTaskFinished(false);
                }*/

                currentShareStatus = new ShareStatus(false, errorMessage);
                //doslo je do greske tipa ne posotji Activity koji moze da handle-uje ovo, zvekni odmah event posto je vec izlazi odavde
                informActivity();
            }

        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();

            if (loading != null && loading.isShowing())
            {
                loading.dismiss();
            }

            sharingTaskCompleted = true;
            currentShareStatus = new ShareStatus(false, cancelMessage);

            //Toast.makeText(activity, cancelMessage, Toast.LENGTH_SHORT).show();

        }
    }

    //share helpers

    private Uri getUriFromSavedBitmap(Context context, Bitmap shareableImage)
    {

//        try
//        {
//            File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));
//            if (!path.exists())
//            {
//                if (!path.mkdirs())
//                {
//                    return null;
//                }
//            }
//
//            File file = new File(path, SHARABLE_IMAGE_NAME);
//            FileOutputStream out;
//            out = new FileOutputStream(file);
//            shareableImage.compress(Bitmap.CompressFormat.PNG, 100, out);
//            out.flush();
//            out.close();
//
////            return FileProvider.getUriForFile(context, context.getPackageName(), file);
//            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }

//        return null;
        return SavePictureHelper.INSTANCE.saveImage(context, SHARABLE_IMAGE_NAME, context.getString(R.string.app_name), shareableImage, true);
    }


    //trenutno se ne koriste ove f-je, ali stoje tu zbog testova i ukoliko se u buducim verzijama ponovo budemo vratili na audio share preko interne memorije
    private void copy(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = input.read(buffer)))
        {
            output.write(buffer, 0, n);
        }
    }

    private Uri getUriFromSavedFile(Context context, String aufioFile)
    {

        InputStream inputStream = null;
        FileOutputStream outputStream;

        try
        {
            inputStream = context.getAssets().open(aufioFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File file;
        try
        {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(context.getCacheDir(), SHARABLE_SOUND);

            outputStream = new FileOutputStream(file);

            copy(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

            return FileProvider.getUriForFile(context, context.getPackageName(), file);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }


    private Uri getUriFromExternalStoredFile(Context context, String aufioFile)
    {

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();

        InputStream inputStream = null;
        FileOutputStream outputStream;

        try
        {
            inputStream = context.getAssets().open(aufioFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File file;
        try
        {
            // file = File.createTempFile("MyCache", null, getCacheDir());
            file = new File(root + "/" + aufioFile);

            outputStream = new FileOutputStream(file);

            copy(inputStream, outputStream);

            inputStream.close();
            outputStream.close();

            return Uri.fromFile(file);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }


    private boolean isAppInstalled(Context context, String appPackageName)
    {

        try
        {
            context.getPackageManager().getApplicationInfo(appPackageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {

        }

        return false;
    }

    private ApplicationInfo getInstalledAppInfo(Context context, String appPackageName)
    {

        ApplicationInfo appInfo = null;

        try
        {
            appInfo = context.getPackageManager().getApplicationInfo(appPackageName, 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {//not instaled
        }

        return appInfo;
    }

}
