package com.gallery.parser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Created by petarljubic on 9/13/2016.
 */
public class PullParser
{
    protected Context context;

    protected  StringBuffer currentValue = null;


    public PullParser(Context c)
    {
        this.context = c;

    }
    private static String getStringFromInputStream(InputStream is)
    {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    public void parseString(String xmlInStringFormat) throws XmlPullParserException, IOException
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xmlInStringFormat));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                startDocument(xpp.getName(), xpp, xpp.getAttributeCount());
            }
            else
            if (eventType == XmlPullParser.START_TAG)
            {
                startElement(xpp.getName(), xpp, xpp.getAttributeCount());
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                endElement(xpp.getName(),xpp);
            }
            else if(eventType == XmlPullParser.TEXT) {
                currentText=xpp.getText();
            }
            eventType = xpp.next();
        }
    }
    public String currentText;
    public void parse(Context context, String xmlNameWithExtension) throws XmlPullParserException, IOException
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        InputStream is = (context.getResources().getAssets().open(xmlNameWithExtension));
        xpp.setInput(new StringReader(getStringFromInputStream(is)));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                startDocument(xpp.getName(), xpp, xpp.getAttributeCount());
            }
            else
            if (eventType == XmlPullParser.START_TAG)
            {
                startElement(xpp.getName(), xpp, xpp.getAttributeCount());
            }
            else if (eventType == XmlPullParser.END_TAG)
            {
                endElement(xpp.getName(),xpp);
            }
            else if(eventType == XmlPullParser.TEXT) {
                currentText=xpp.getText();
            }
            eventType = xpp.next();
        }
    }

    protected void startDocument(String localName, XmlPullParser xpp, int count)
    {
    }
    protected void startElement(String localName, XmlPullParser xpp, int count)
    {
        /*currentValue = new StringBuffer();
        int length = count;*/
    }

    protected void endElement(String localName, XmlPullParser xpp)
    {
        /*if ((localName.equals("question")))
        {
            myListOfQuestions.add(currentQuestion);
        }*/

    }

}
