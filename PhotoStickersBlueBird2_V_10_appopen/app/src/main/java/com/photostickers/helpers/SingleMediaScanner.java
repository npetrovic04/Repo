package com.photostickers.helpers;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

import java.io.File;

public class SingleMediaScanner implements MediaScannerConnectionClient
{

    private MediaScannerConnection mediaScanner;
    private File file;

    public SingleMediaScanner(Context context, File f)
    {
        file = f;
        mediaScanner = new MediaScannerConnection(context, this);
        mediaScanner.connect();
    }


    public void onMediaScannerConnected()
    {
        mediaScanner.scanFile(file.getAbsolutePath(), null);

    }

    public void onScanCompleted(String path, Uri uri)
    {
        mediaScanner.disconnect();
    }

}
