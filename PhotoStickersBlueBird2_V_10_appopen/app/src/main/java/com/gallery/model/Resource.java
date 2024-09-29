package com.gallery.model;

/**
 * Created by petarljubic on 2/1/2018.
 */

public class Resource extends AdapterItem
{

    public Resource(Object value)
    {
        itemType = RESOURCE;
        this.value = value;
    }
    Object value;
    public Object getValue()
    {
        return value;
    }
    public void setValue(Object value)
    {
        this.value = value;
    }
}
