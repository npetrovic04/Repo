package com.gallery.model;

/**
 * Created by petarljubic on 2/1/2018.
 */

public class AdapterItem
{
    public static final int GALLERY_ITEM=0;
    public static final int CUSTOM=-1;
    public static final int RESOURCE=-2;
    public static final int NATIVE=1;

    protected int itemType;

    public int getItemType()
    {
        return itemType;
    }
    public void setItemType(int itemType)
    {
        this.itemType = itemType;
    }
}
