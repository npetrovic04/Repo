package com.photostickers.helpers;


import com.photostickers.R;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by petarljubic on 3/3/2016.
 */
public class Constants
{

    public final static int REQUEST_EXTERNAL_STORAGE_CODE = 102;
    private ArrayList<String> names;
    public ArrayList<Integer> stickers;

    private static Constants ourInstance;

    public static Constants getInstance()
    {
        if (ourInstance == null)
        {
            ourInstance = new Constants();
        }
        return ourInstance;

    }



    private Constants()
    {

        stickers = new ArrayList<>();
    }



    public void listRaw(String prefix, Context context)
    {
        Field[] fields = R.drawable.class.getFields();
        if (stickers != null)
        {
            stickers.clear();
        }
        stickers = new ArrayList<>();
        if (names != null)
        {
            names.clear();
        }
        names = new ArrayList<>();
        for (Field field : fields)
        {
            if (field.getName().startsWith(prefix))
            {
                names.add(field.getName());
            }
        }
        Collections.sort(names, new Comparator<String>()
        {
            @Override
            public int compare(String s, String t1)
            {
                int firstNum = Integer.parseInt(s.substring(5));
                int secondNum = Integer.parseInt(t1.substring(5));
                return firstNum - secondNum;
            }
        });
        for (String name : names)
        {
            stickers.add(getRes(name, context));
        }
    }
    private static int getRes(String name, Context context)
    {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}
