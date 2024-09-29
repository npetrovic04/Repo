package com.photostickers.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterWithNative extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private boolean showBigNative;
    private NativeAdSettings mNativeAdSettings;
    private Activity mActivity;
    ArrayList<Object> mData;
    private final int ITEM = 0, EMPTY = 2, AD = 1;

    public static class NativeAdItem
    {}

    public static class EmptyItem
    {}

    AdapterWithNative(Activity mA, ArrayList<Object> myData, NativeAdSettings settings, boolean bigNative)
    {
        mActivity = mA;
        mData = myData;
        mNativeAdSettings = settings;
        showBigNative = bigNative;

    }

    @Override
    public int getItemViewType(int position)
    {
        if (mData.get(position) instanceof NativeAdItem)
        {
            return AD;
        }
        else if (mData.get(position) instanceof EmptyItem)
        {
            return EMPTY;
        }
        else
        {
            return getItem(position);
        }
    }

    public int getItem(int position)
    {
        return ITEM;
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == AD)
        {
            viewHolder = new NativeHolder(new RelativeLayout(mActivity));
        }
        else if (viewType == EMPTY)
        {
            viewHolder = new EmptyHolder(new RelativeLayout(mActivity));
        }
        else
        {
            viewHolder = createViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
        }

        return viewHolder;
    }

    public RecyclerView.ViewHolder createViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType)
    {
        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position)
    {

        if (getItemViewType(position) == AD)
        {
            /*
            CMSAd nativeAd = CMSMain.getNativeAdForActionID(mActivity, mActivity.getString(R.string.cms_native));
            RelativeLayout container = (RelativeLayout) viewHolder.itemView;
            container.removeAllViews();
            if (nativeAd != null)
            {
                LayoutInflater inflater = (LayoutInflater) mActivity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (inflater != null)
                {
                    ViewGroup maincontainer;

                    View adcontainer;
                    if (showBigNative)
                    {
                        adcontainer = inflater.inflate(R.layout.native_ad_item, null);
                    }
                    else
                    {
                        adcontainer = inflater.inflate(R.layout.native_ad_item_small, null);
                    }

                    if (nativeAd instanceof CMSAdFacebook)
                    {
                        maincontainer = new com.facebook.ads.NativeAdLayout(mActivity);

                    }
                    else if (nativeAd instanceof CMSAdAdmob)
                    {
                        maincontainer = new com.google.android.gms.ads.formats.UnifiedNativeAdView(mActivity);
                    }
                    else
                    {
                        maincontainer = new RelativeLayout(mActivity);
                    }

                    maincontainer.addView(adcontainer);

                    RelativeLayout mediaContainer = maincontainer.findViewById(R.id.mediaContainer);
                    TextView title = maincontainer.findViewById(R.id.nativeTitle);
                    TextView cta = maincontainer.findViewById(R.id.nativeCTA);
                    RelativeLayout adLabelContainer = maincontainer.findViewById(R.id.nativeAdLabelContainer);
                    RelativeLayout mustIncludeContainer = maincontainer.findViewById(R.id.nativeMustIncludeContainer);

                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    if (mNativeAdSettings.isRadius())
                    {
                        float radii = convertDpToPixel(5, mActivity);
                        shape.setCornerRadii(new float[]{radii, radii, radii, radii, radii, radii, radii, radii});
                    }
                    shape.setColor(mNativeAdSettings.getCtaBgdColor());

                    if (mNativeAdSettings.isStroke())
                    {
                        int stroke = (int) convertDpToPixel(3, mActivity);
                        shape.setStroke(stroke, mNativeAdSettings.getCtaStrokeColor());
                    }
                    cta.setBackground(shape);
                    cta.setTextColor(mNativeAdSettings.getCtaTextColor());

                    title.setTextColor(mNativeAdSettings.getTitleColor());

                    maincontainer.setBackgroundColor(mNativeAdSettings.getBgdColor());

                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(title);
                    clickableViews.add(cta);

                    clickableViews.add(mediaContainer);

                    nativeAd.prepareNativeAdView(mActivity, maincontainer, null, mediaContainer, title, cta, adLabelContainer, mustIncludeContainer, clickableViews);
                    container.setPadding(0, (int) convertDpToPixel(10, mActivity), 0, (int) convertDpToPixel(10, mActivity));
                    container.addView(maincontainer);
                }

                CMSMain.nativeAdUsed(mActivity, nativeAd.getAdID());
            }
            */
        }
        else if (getItemViewType(position) != EMPTY)
        {
            bind(viewHolder, position);
        }

    }

    public void bind(RecyclerView.ViewHolder viewHolder, int position)
    {

    }

    public class NativeHolder extends RecyclerView.ViewHolder
    {

        NativeHolder(View itemView)
        {
            super(itemView);
        }

    }

    public class EmptyHolder extends RecyclerView.ViewHolder
    {

        EmptyHolder(View itemView)
        {
            super(itemView);
        }

    }

    public static class NativeAdSettings
    {

        boolean radius = true;
        boolean stroke = true;
        int bgdColor = Color.WHITE;
        int titleColor = Color.BLACK;
        int ctaTextColor = Color.WHITE;
        int ctaBgdColor = Color.parseColor("#69b73f");
        int ctaStrokeColor = Color.parseColor("#4fa12a");

        public NativeAdSettings()
        {
        }
        public NativeAdSettings(boolean radius, boolean stroke, int bgdColor, int titleColor, int ctaTextColor, int ctaBgdColor, int ctaStrokeColor)
        {
            this.radius = radius;
            this.stroke = stroke;
            this.bgdColor = bgdColor;
            this.titleColor = titleColor;
            this.ctaTextColor = ctaTextColor;
            this.ctaBgdColor = ctaBgdColor;
            this.ctaStrokeColor = ctaStrokeColor;
        }

        public boolean isRadius()
        {
            return radius;
        }
        public boolean isStroke()
        {
            return stroke;
        }
        public int getBgdColor()
        {
            return bgdColor;
        }
        public int getTitleColor()
        {
            return titleColor;
        }
        public int getCtaTextColor()
        {
            return ctaTextColor;
        }
        public int getCtaBgdColor()
        {
            return ctaBgdColor;
        }
        public int getCtaStrokeColor()
        {
            return ctaStrokeColor;
        }
        public void setRadius(boolean radius)
        {
            this.radius = radius;
        }
        public void setStroke(boolean stroke)
        {
            this.stroke = stroke;
        }
        public void setBgdColor(int bgdColor)
        {
            this.bgdColor = bgdColor;
        }
        public void setTitleColor(int titleColor)
        {
            this.titleColor = titleColor;
        }
        public void setCtaTextColor(int ctaTextColor)
        {
            this.ctaTextColor = ctaTextColor;
        }
        public void setCtaBgdColor(int ctaBgdColor)
        {
            this.ctaBgdColor = ctaBgdColor;
        }
        public void setCtaStrokeColor(int ctaStrokeColor)
        {
            this.ctaStrokeColor = ctaStrokeColor;
        }
    }
}
