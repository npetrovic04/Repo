package com.gallery.widget;

import com.bumptech.glide.Glide;
import com.gallery.PickConfig;
import com.gallery.util.UriUtil;
import com.photostickers.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static android.R.drawable.ic_media_play;


/**
 * Created by yuweichen on 15/12/9.
 */
public class ThumbPhotoView extends RelativeLayout
{


    ImageView photo_thumbview;

    ImageView photo_thumbview_selected;
    public ImageView photo_video_flag;

    public static int defaultError = R.drawable.default_error;
    public static int defaultSelected = R.drawable.photo_selected;
    public static int defaultUnselected = R.drawable.photo_unselected;
    public static int defaultVideoFlag = ic_media_play;
    public static int itemLayout = R.layout.item_pickphoto_view;

    public ThumbPhotoView(Context context)
    {
        super(context);
        initView(context);
    }

    public ThumbPhotoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    public ThumbPhotoView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context)
    {
        View view = inflate(context, itemLayout, this);
        photo_thumbview = (ImageView) view.findViewById(R.id.photo_thumbview);
        photo_thumbview_selected = (ImageView) view.findViewById(R.id.photo_thumbview_selected);
        photo_video_flag = (ImageView) view.findViewById(R.id.photo_video_flag);
    }


    public void loadData(String folderPath, int pickMode)
    {
        Uri uri = UriUtil.generatorUri(folderPath, UriUtil.LOCAL_FILE_SCHEME);
        Glide.with(getContext()).load(uri).thumbnail(0.3f).into(photo_thumbview);
        photo_thumbview_selected.setVisibility(VISIBLE);
    }

    public void loadData(Uri uri, int pickMode)
    {
        Glide.with(getContext()).load(uri).thumbnail(0.3f).into(photo_thumbview);
        photo_thumbview_selected.setVisibility(VISIBLE);
    }
    public void loadData(int imageResource, int pickMode)
    {
        Glide.with(getContext()).load(imageResource).thumbnail(0.3f).into(photo_thumbview);
//        photo_thumbview.setImageResource(imageResource);
        if (pickMode == PickConfig.MODE_MULTIP_PICK)
        {
            photo_thumbview_selected.setVisibility(VISIBLE);
        }
        else
        {
            photo_thumbview_selected.setVisibility(VISIBLE);
//            photo_thumbview_selected.setVisibility(GONE);
        }
    }
    public void loadData(Bitmap bmp, int pickMode)
    {
        if (bmp != null)
        {
            photo_thumbview.setImageBitmap(bmp);
        }
        if (pickMode == PickConfig.MODE_MULTIP_PICK)
        {
            photo_thumbview_selected.setVisibility(VISIBLE);
        }
        else
        {
            photo_thumbview_selected.setVisibility(VISIBLE);
//            photo_thumbview_selected.setVisibility(GONE);
        }
    }

    public void showSelected(boolean showSelected)
    {
        if (showSelected)
        {
            photo_thumbview_selected.setBackgroundResource(defaultSelected);
        }
        else
        {
            photo_thumbview_selected.setBackgroundResource(defaultUnselected);
        }
    }

}
