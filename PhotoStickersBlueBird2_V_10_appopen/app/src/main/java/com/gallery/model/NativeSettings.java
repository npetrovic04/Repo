package com.gallery.model;

import com.gallery.parser.NativeSettingsParser;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by petarljubic on 11/30/2017.
 */

public class NativeSettings
{
    private int nativeBgdColor=Color.TRANSPARENT;
    private int nativeTitleColor=Color.BLACK;
    private int nativeCtaTextColor=Color.BLACK;
    private int nativeCtaBgdColor=Color.GRAY;
    private boolean nativeCtaStroke=false;
    private int nativeCtaStrokeColor=Color.BLACK;
    private boolean nativeCtaRadius=false;

    private boolean usePaddingValues=false;
    private int paddingCtaLeftRight=20;
    private int paddingCtaTopBottom=10;

    public NativeSettings(Context context)
    {
        init(context);
    }
    private void init(Context context)
    {
        paddingCtaLeftRight= (int) convertDpToPixel(20,context);
        paddingCtaTopBottom= (int) convertDpToPixel(5,context);
    }
    HashMap<String,String> settings;
    public NativeSettings(Context context,String xml)
    {
        init(context);
        settings=new HashMap<>();
        NativeSettingsParser colorParser = new NativeSettingsParser(context,settings);
        try
        {
            colorParser.parseString(xml);
        }
        catch (XmlPullParserException | IOException e)
        {
            e.printStackTrace();
        }
        setNativeSettings(settings);
    }

    public NativeSettings(HashMap<String,String> settings)
    {
        setNativeSettings(settings);
    }
    public void setNativeSettings(HashMap<String,String> settings)
    {
        setNativeBgdColor(Color.parseColor("#"+settings.get("nativeBgdColor")));
        setNativeTitleColor(Color.parseColor("#"+settings.get("nativeTitleColor")));
        setNativeCtaTextColor(Color.parseColor("#"+settings.get("nativeCtaTextColor")));
        setNativeCtaBgdColor(Color.parseColor("#"+settings.get("nativeCtaBgdColor")));
        setNativeCtaStroke("YES".equalsIgnoreCase(settings.get("nativeCtaStroke")));
        setNativeCtaStrokeColor(Color.parseColor("#"+settings.get("nativeCtaStrokeColor")));
        setNativeCtaRadius("YES".equalsIgnoreCase(settings.get("nativeCtaRadius")));
    }
    public int getNativeBgdColor()
    {
        return nativeBgdColor;
    }
    public void setNativeBgdColor(int nativeBgdColor)
    {
        this.nativeBgdColor = nativeBgdColor;
    }
    public int getNativeTitleColor()
    {
        return nativeTitleColor;
    }
    public void setNativeTitleColor(int nativeTitleColor)
    {
        this.nativeTitleColor = nativeTitleColor;
    }
    public int getNativeCtaTextColor()
    {
        return nativeCtaTextColor;
    }
    public void setNativeCtaTextColor(int nativeCtaTextColor)
    {
        this.nativeCtaTextColor = nativeCtaTextColor;
    }
    public int getNativeCtaBgdColor()
    {
        return nativeCtaBgdColor;
    }
    public void setNativeCtaBgdColor(int nativeCtaBgdColor)
    {
        this.nativeCtaBgdColor = nativeCtaBgdColor;
    }
    public boolean isNativeCtaStroke()
    {
        return nativeCtaStroke;
    }
    public void setNativeCtaStroke(boolean nativeCtaStroke)
    {
        this.nativeCtaStroke = nativeCtaStroke;
    }
    public int getNativeCtaStrokeColor()
    {
        return nativeCtaStrokeColor;
    }
    public void setNativeCtaStrokeColor(int nativeCtaStrokeColor)
    {
        this.nativeCtaStrokeColor = nativeCtaStrokeColor;
    }
    public boolean isNativeCtaRadius()
    {
        return nativeCtaRadius;
    }
    public void setNativeCtaRadius(boolean nativeCtaRadius)
    {
        this.nativeCtaRadius = nativeCtaRadius;
    }
    public boolean isUsePaddingValues()
    {
        return usePaddingValues;
    }
    public void setUsePaddingValues(boolean usePaddingValues)
    {
        this.usePaddingValues = usePaddingValues;
    }
    public int getPaddingCtaLeftRight()
    {
        return paddingCtaLeftRight;
    }
    public void setPaddingCtaLeftRight(int paddingCtaLeftRight)
    {
        this.paddingCtaLeftRight = paddingCtaLeftRight;
    }
    public int getPaddingCtaTopBottom()
    {
        return paddingCtaTopBottom;
    }
    public void setPaddingCtaTopBottom(int paddingCtaTopBottom)
    {
        this.paddingCtaTopBottom = paddingCtaTopBottom;
    }
    private float convertDpToPixel(float dp, Context context)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
