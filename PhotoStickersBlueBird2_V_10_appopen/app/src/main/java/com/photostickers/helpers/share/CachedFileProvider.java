package com.photostickers.helpers.share;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by marko on 3/23/2016.
 */
public class CachedFileProvider extends ContentProvider
{

    private static final String CLASS_NAME = "CachedFileProvider";

    // The authority is the symbolic name for the provider class
    //public static final String AUTHORITY = "com.sharemanagermodul.provider";

    // UriMatcher used to match against incoming requests
    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate()
    {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


        // Add a URI to the matcher which will match against the form
        // 'content://com.stephendnicholas.gmailattach.provider/*'
        // and return 1 in the case that the incoming Uri matches this pattern
        if(getContext()!=null)
        uriMatcher.addURI(getContext().getPackageName() + ".provider", "*", 1);

        return true;
    }


    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException
    {
        switch (uriMatcher.match(uri))
        {
            case 1:
                String fileLocation = getContext().getCacheDir() + File.separator + uri.getLastPathSegment();
                return ParcelFileDescriptor.open(new File(fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
            default:

                throw new FileNotFoundException("Unsupported uri: " + uri.toString());
        }
    }



    @Override
    public int update(@NonNull Uri uri, ContentValues contentvalues, String s, String[] as) { return 0; }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] as) { return 0; }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentvalues) { return null; }

    @Override
    public String getType(@NonNull Uri uri) {

        switch (uriMatcher.match(uri)) {

            // If it returns 1 - then it matches the Uri defined in onCreate
            case 1:
                return "image/png"; // Use an appropriate mime type here
            default:
                return null;
        }
    }




    //preuzeto sa
    //http://stackoverflow.com/a/22311107/1485837
    //http://stackoverflow.com/questions/17082417/share-an-image-with-a-content-provider-in-android-app

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        if (projection == null) {
            projection = new String[] {
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.MIME_TYPE
            };
        }

        final long time = System.currentTimeMillis();
        MatrixCursor result = new MatrixCursor(projection);
        File file = new File(getContext().getCacheDir() + File.separator + uri.getLastPathSegment());

        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {

            if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DISPLAY_NAME) == 0) {
                row[i] = uri.getLastPathSegment();
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.SIZE) == 0) {
                row[i] = file.length();
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DATA) == 0) {
                row[i] = file;
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.MIME_TYPE)==0) {
                row[i] = "image/png";
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DATE_ADDED)==0 ||
                    projection[i].compareToIgnoreCase(MediaStore.MediaColumns.DATE_MODIFIED)==0 ||
                    projection[i].compareToIgnoreCase("datetaken")==0) {
                row[i] = time;
            } else if (projection[i].compareToIgnoreCase(MediaStore.MediaColumns._ID)==0) {
                row[i] = 0;
            } else if (projection[i].compareToIgnoreCase("orientation")==0) {
                row[i] = "vertical";
            }
        }

        result.addRow(row);
        return result;
    }



}
