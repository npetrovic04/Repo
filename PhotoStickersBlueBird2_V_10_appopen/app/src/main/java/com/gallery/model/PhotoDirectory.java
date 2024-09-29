package com.gallery.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/6/28.
 */
public class PhotoDirectory
{

    private String id;
    private String coverPath;
    private Uri coverUri;
    private String name;
    private String type;
    private long dateAdded;
    private List<Photo> photos = new ArrayList<>();

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PhotoDirectory))
        {
            return false;
        }

        PhotoDirectory directory = (PhotoDirectory) o;

        if (id != null && directory.id != null)
        {

            if (!id.equals(directory.id))
            {
                return false;
            }
            if (name != null)
            {
                return name.equals(directory.name);
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCoverPath()
    {
        return coverPath;
    }

    public void setCoverPath(String coverPath)
    {
        this.coverPath = coverPath;
    }
    public Uri getCoverUri(){
        return this.coverUri;
    }
    public void setCoverUri(Uri coverUri)
    {
        this.coverUri = coverUri;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public long getDateAdded()
    {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded)
    {
        this.dateAdded = dateAdded;
    }

    public List<Photo> getPhotos()
    {
        return photos;
    }

    public void setPhotos(List<Photo> photos)
    {
        this.photos = photos;
    }
    public String getType()
    {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public List<String> getPhotoPaths()
    {
        List<String> paths = new ArrayList<>(photos.size());
        for (Photo photo : photos)
        {
            paths.add(photo.getPath());
        }
        return paths;
    }
    public List<Uri> getPhotoUris()
    {
        List<Uri> paths = new ArrayList<>(photos.size());
        for (Photo photo : photos)
        {
            paths.add(photo.getUri());
        }
        return paths;
    }

    public void addPhoto(int id, String path, Uri uri)
    {
        if(path!= null)
            photos.add(new Photo(id, path));
        else
            photos.add(new Photo(id, uri));
    }

}
