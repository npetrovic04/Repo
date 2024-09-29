package com.gallery.parser;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by petarljubic on 11/30/2017.
 */

public class NativeSettingsParser extends PullParser
{
    HashMap<String, String> settings;
    public NativeSettingsParser(Context c,HashMap<String, String> settings)
    {
        super(c);
        this.settings=settings;
    }
    @Override
    protected void startElement(String localName, XmlPullParser xpp, int count)
    {
//        super.startElement(localName, xpp, count);
    }
    @Override
    protected void endElement(String localName, XmlPullParser xpp)
    {
        if(!"settings".equalsIgnoreCase(localName))
        {
            settings.put(localName, currentText);
        }
    }
}
