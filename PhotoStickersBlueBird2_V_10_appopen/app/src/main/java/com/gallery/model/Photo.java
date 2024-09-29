package com.gallery.model;

import android.net.Uri;

/**
 * Created by donglua on 15/6/30.
 */
public class Photo extends AdapterItem
{

    private int id;
    private String path;
    private Uri uri;
//  public boolean isChecked=false;

    public Photo(int id, String path)
    {
        itemType = GALLERY_ITEM;
        this.id = id;
        this.path = path;
    }
    public Photo(int id, Uri uri)
    {
        itemType = GALLERY_ITEM;
        this.id = id;
        this.uri = uri;
    }

    public Photo()
    {
        itemType = GALLERY_ITEM;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Photo))
        {
            return false;
        }

        Photo photo = (Photo) o;

        return id == photo.id;
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Uri getUri() {
        return uri;
    }
    public void setUri(Uri uri){
        this.uri = uri;
    }
}
