package com.photostickers.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExifUtil
{

    public static Bitmap rotateBitmap(String src, Context c, Bitmap bitmap)
    {
        try
        {
            int orientation = -1;
            try (InputStream inputStream = c.getContentResolver().openInputStream(Uri.parse(src))) {
                ExifInterface exif = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    exif = new ExifInterface(inputStream);
                } else {
                    exif = new ExifInterface(UriHelper.INSTANCE.getRealPathFromURI(c, Uri.parse(src)));
                }
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Matrix matrix = new Matrix();
            switch (orientation)
            {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try
            {
                if (!bitmap.isRecycled())
                {
                    Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    bitmap.recycle();
                    return oriented;
                }
                else
                {
                    return null;
                }
            }
            catch (OutOfMemoryError e)
            {
                e.printStackTrace();
                return bitmap;
            }
        }
        catch (Exception ignored)
        {

        }

        return bitmap;
    }

    private static int getExifOrientation(String src)
    {
        int orientation = 1;

        try
        {

            Class<?> exifClass = Class.forName("android.media.ExifInterface");
            Constructor<?> exifConstructor = exifClass.getConstructor(String.class);
            Object exifInstance = exifConstructor.newInstance(src);
            Method getAttributeInt = exifClass.getMethod("getAttributeInt", String.class, int.class);
            Field tagOrientationField = exifClass.getField("TAG_ORIENTATION");
            String tagOrientation = (String) tagOrientationField.get(null);
            orientation = (Integer) getAttributeInt.invoke(exifInstance, new Object[]{tagOrientation, 1});
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        return orientation;
    }
}