package com.gallery.data.loader;

import com.gallery.data.Data;
import com.gallery.model.PhotoDirectory;
import com.gallery.views.CustomPickPhotoView;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


/**
 * see https://github.com/donglua/PhotoPicker/blob/master/PhotoPicker/src/main/java/me/iwf/photopicker/utils/MediaStoreHelper.java
 */
public class MediaStoreHelper
{

    static int INDEX = 0;
    public static void getPhotoDirs(AppCompatActivity activity, Bundle args, PhotosResultCallback resultCallback)
    {
        activity.getSupportLoaderManager().initLoader(INDEX++, args, new PhotoDirLoaderCallbacks(activity, false /*args.getBoolean(PickConfig.EXTRA_CHECK_IMAGE)*/, resultCallback));
    }

    static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
    {

        private Context context;
        private PhotosResultCallback resultCallback;
        private boolean checkImageStatus;

        public PhotoDirLoaderCallbacks(Context context, boolean checkImageStatus, PhotosResultCallback resultCallback)
        {
            this.context = context;
            this.resultCallback = resultCallback;
            this.checkImageStatus = checkImageStatus;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE/*,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE*/
            };
            int type=args.getInt("type");
            String selection="";
            switch (type)
            {
                case CustomPickPhotoView.ALL:
                    selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                                + " OR "
                                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                    break;
                case CustomPickPhotoView.IMAGES:
                    selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                    break;
                case CustomPickPhotoView.VIDEOS:
                    selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                    break;
            }

            Uri queryUri = MediaStore.Files.getContentUri("external");

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    queryUri,
                    projection,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            );
            return cursorLoader;
//            return new PhotoDirectoryLoader(context, false/*args.getBoolean(PickConfig.EXTRA_SHOW_GIF, false)*/);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {

            if (data == null)
            {
                return;
            }
            if (!data.isClosed())
            {
                List<PhotoDirectory> directories = Data.getDataFromCursor(context, data, checkImageStatus);
                if (!data.isClosed())
                {
                    data.close();
                }

                if (resultCallback != null)
                {
                    resultCallback.onResultCallback(directories);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
            new PhotoDirectoryLoader(context, false/*args.getBoolean(PickConfig.EXTRA_SHOW_GIF, false)*/);
        }
    }


    public interface PhotosResultCallback
    {

        void onResultCallback(List<PhotoDirectory> directories);
    }

}
