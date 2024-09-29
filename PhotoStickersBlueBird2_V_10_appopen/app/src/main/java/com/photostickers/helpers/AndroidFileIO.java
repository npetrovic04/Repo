package com.photostickers.helpers;


import com.photostickers.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AndroidFileIO implements FileIO
{

    private AssetManager assets;
    private String externalStoragePath;

    public AndroidFileIO(Context c)
    {
        this.assets = c.getAssets();

        this.externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + c.getString(R.string.albumName) + File.separator;

        File file = new File(Environment.getExternalStorageDirectory(), c.getString(R.string.albumName));
        if (!file.exists())
        {
            file.mkdirs();
        }
    }

    public boolean fileExsist(String fileName)
    {
        return new File(externalStoragePath + fileName).exists();
    }

    public File returnFile(String fileName)
    {

        return new File(externalStoragePath + fileName);
    }

    public InputStream readAsset(String fileName) throws IOException
    {
        return assets.open(fileName);
    }

    public InputStream readFile(String fileName) throws IOException
    {
        return new FileInputStream(externalStoragePath + fileName);
    }

    public OutputStream writeFile(String fileName) throws IOException
    {
        return new FileOutputStream(externalStoragePath + fileName);
    }
}
