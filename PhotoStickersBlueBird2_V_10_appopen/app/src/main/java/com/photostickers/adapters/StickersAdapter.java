package com.photostickers.adapters;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.photostickers.R;

import java.util.ArrayList;


/**
 * Created by petarljubic on 6/13/2016.
 */
public class StickersAdapter extends AdapterWithNative {

    private StickerAdapterInterface mStickerAdapterInterface;


    public StickersAdapter(Activity c, ArrayList<Object> mData, NativeAdSettings settings, boolean bigNative) {
        super(c, mData, settings, bigNative);
    }


    @Override
    public RecyclerView.ViewHolder createViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_sticker, parent, false));
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void bind(RecyclerView.ViewHolder viewHolder, int position) {
        ((UniversalHolder) viewHolder).setData(position);
    }

    public void setStickerAdapterInterface(StickerAdapterInterface stickerAdapterInterface) {
        this.mStickerAdapterInterface = stickerAdapterInterface;
    }


    public interface StickerAdapterInterface {

        void onClick(int res);
    }

    public class UniversalHolder extends RecyclerView.ViewHolder {

        UniversalHolder(View itemView) {
            super(itemView);
        }

        public void setData(int position) {
        }
    }

    public class ViewHolder extends UniversalHolder {

        private ImageView imagePreview;

        ViewHolder(View itemView) {
            super(itemView);

            imagePreview = itemView.findViewById(R.id.imagePreview);
        }

        @Override
        public void setData(int position) {
            int resource = (int) mData.get(position);
            imagePreview.setImageResource(resource);
            imagePreview.setTag(resource);
            imagePreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mStickerAdapterInterface != null) {
                        mStickerAdapterInterface.onClick((Integer) v.getTag());
                    }
                }
            });
        }
    }


}
