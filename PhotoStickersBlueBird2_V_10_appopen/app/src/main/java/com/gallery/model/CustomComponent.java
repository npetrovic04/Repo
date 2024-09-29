package com.gallery.model;

/**
 * Created by petarljubic on 2/1/2018.
 */

public class CustomComponent extends AdapterItem
{
    Object value;

    public CustomComponent(Object value)
    {
        this.value = value;
        itemType=CUSTOM;
    }
    public Object getValue()
    {
        return value;
    }
    public void setValue(Object value)
    {
        this.value = value;
    }
}
