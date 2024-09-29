package com.gallery.data;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.gallery.model.PhotoDirectory;
import com.gallery.util.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

/**
 * Created by yuweichen on 15/12/22.
 */
public class Data {
    public final static int INDEX_ALL_PHOTOS = 0;

    public static List<PhotoDirectory> getDataFromCursor(Context context, Cursor data, boolean checkImageStatus) {
        List<PhotoDirectory> directories = new ArrayList<>();
        PhotoDirectory photoDirectoryAll = new PhotoDirectory();
        photoDirectoryAll.setName("All Photos");
        photoDirectoryAll.setId("ALL");

        while (data != null && !data.isClosed() && data.moveToNext()) {

            int imageId = data.getInt(data.getColumnIndexOrThrow(_ID));
            String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
            String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
            String path = data.getString(data.getColumnIndexOrThrow(DATA));
            String type = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    data.getInt(data.getColumnIndex(MediaStore.Images.ImageColumns._ID)));

            boolean isUriLogic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
            if (checkImageStatus) {
                if (!BitmapUtil.checkImgCorrupted(path)) {
                    PhotoDirectory photoDirectory = new PhotoDirectory();
                    photoDirectory.setId(bucketId);
                    photoDirectory.setName(name);
                    photoDirectory.setType(type);


                    if (!directories.contains(photoDirectory)) {
                        if (isUriLogic)
                            photoDirectory.setCoverUri(uri);
                        else
                            photoDirectory.setCoverPath(path);
                        photoDirectory.addPhoto(imageId, isUriLogic ? null : path, isUriLogic ? uri : null);
                        photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                        directories.add(photoDirectory);
                    } else {
                        directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, isUriLogic ? null : path, isUriLogic ? uri : null);
                    }

                    photoDirectoryAll.addPhoto(imageId, isUriLogic ? null : path, isUriLogic ? uri : null);
                }
            } else {

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);
                photoDirectory.setType(type);

                if (!directories.contains(photoDirectory)) {
                    if (isUriLogic)
                        photoDirectory.setCoverUri(uri);
                    else
                        photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(imageId, isUriLogic ? null : path, isUriLogic ? uri : null);
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                    directories.add(photoDirectory);
                } else {
                    directories.get(directories.indexOf(photoDirectory)).addPhoto(imageId, isUriLogic ? null : path, isUriLogic ? uri : null);
                }

                photoDirectoryAll.addPhoto(imageId, isUriLogic ? null : path, isUriLogic ? uri : null);
            }


        }
        if (photoDirectoryAll.getPhotoPaths().size() > 0) {
            photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotoPaths().get(0));
        } else if(photoDirectoryAll.getPhotoUris().size() > 0) {
            photoDirectoryAll.setCoverUri(photoDirectoryAll.getPhotoUris().get(0));
        }
        directories.add(INDEX_ALL_PHOTOS, photoDirectoryAll);
//        data.close();
        return directories;
    }

}
