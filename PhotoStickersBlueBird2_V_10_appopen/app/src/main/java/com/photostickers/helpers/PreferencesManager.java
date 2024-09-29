package com.photostickers.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager
{
    public static final String SHOW_TUTORIAL="ShowTutorialValue";
    private static PreferencesManager instance;
    private final SharedPreferences pref;

    private PreferencesManager(Context context)
    {
        pref = context.getSharedPreferences(context.getPackageName()+".PREF_NAME", Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    public void setBooleanValue(String key, boolean value)
    {
        pref.edit().putBoolean(key, value).apply();
    }
    public boolean getBooleanValue(String key, boolean defaultValue)
    {
        return pref.getBoolean(key, defaultValue);
    }


}
