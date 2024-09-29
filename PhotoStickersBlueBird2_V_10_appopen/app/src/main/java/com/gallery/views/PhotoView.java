package com.gallery.views;

import com.gallery.model.PhotoDirectory;

import java.util.List;

/**
 * Created by yuweichen on 15/12/9.
 */
public interface PhotoView extends BaseView
{
    /**
     * showPhotosView
     * @param photoDirectories
     */
    void showPhotosView(List<PhotoDirectory> photoDirectories);

    /**
     * show exception message
     */
    void showException(String msg);

}
