package com.photostickers.helpers;

/**
 * Created by ivcha on 01/12/2017.
 */

public class NativeAdTitleFormat
{
    public static String formatTitleText(String titleText)
    {

        if(titleText != null && !titleText.equalsIgnoreCase("") && titleText.length() > 20)
        {
            titleText = titleText.subSequence(0, 20) + "\u2026";  //kod za "..."
        }

        return titleText;
    }
}
