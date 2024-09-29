package com.gallery.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gallery.PickConfig;
import com.gallery.model.AdapterItem;
import com.gallery.model.CustomComponent;
import com.gallery.model.NativeItem;
import com.gallery.model.NativeSettings;
import com.gallery.model.Photo;
import com.gallery.model.Resource;
import com.gallery.util.UriUtil;
import com.gallery.views.CustomPickPhotoView;
import com.gallery.widget.ThumbPhotoView;
import com.photostickers.R;

import java.util.ArrayList;
import java.util.List;

import static com.gallery.model.AdapterItem.CUSTOM;
import static com.gallery.model.AdapterItem.GALLERY_ITEM;
import static com.gallery.model.AdapterItem.NATIVE;
import static com.gallery.model.AdapterItem.RESOURCE;


/**
 * Adapter koji sluzi da se prikazu slike i nativi
 */
public class ThumbPhotoAdapter extends RecyclerView.Adapter<ThumbPhotoAdapter.UniversalHolder> {

    private Activity context;
    private List<Photo> photos = new ArrayList<>();
    private int width;
    private Toolbar toolbar;
    public int maxPickSize;
    private int pickMode;
    private int spanCount;
    FrameLayout.LayoutParams elementParams;
    public FrameLayout.LayoutParams nativeParams;
    LayoutInflater layoutInflater;
    int rowsToNative;
    int rowsToFirstNative = -1;
    private ArrayList<AdapterItem> selectedItems = new ArrayList<>();

    public boolean nativeIsAvailable = false;
    public String actionId = "";
    CustomPickPhotoView.TYPE type;
    public String clickedAdId;
    private NativeSettings mNativeSettings;

    /**
     * @param context      activity
     * @param type         tip moze biti galerija ili resurs
     * @param spanCount    koliko ima kolona
     * @param maxPickSize  Maksimalan broj elemenata za selektovanje
     * @param pickMode     multi ili single
     * @param toolbar      Ne koristi se
     * @param rowsToNative Broj redova do sledeceg native-a
     */
    public ThumbPhotoAdapter(Activity context, CustomPickPhotoView.TYPE type, int spanCount, int maxPickSize, int pickMode, Toolbar toolbar, int rowsToNative, int rowsToFirstNative) {
        this.context = context;
        this.type = type;
        this.layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.spanCount = spanCount;
        this.rowsToNative = rowsToNative;
        this.rowsToFirstNative = rowsToFirstNative;
        this.width = context.getResources().getDisplayMetrics().widthPixels / spanCount;
        this.maxPickSize = maxPickSize;
        this.pickMode = pickMode;
        this.toolbar = toolbar;
        this.mNativeSettings = new NativeSettings(context);
        elementParams = new FrameLayout.LayoutParams(width, width);
        nativeParams = new FrameLayout.LayoutParams(width * spanCount, (int) (width * spanCount * 0.66f));

        items = new ArrayList<>();
        getItemsTypes();
        /*GridLayoutManager manager = new GridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
            @Override
            public int getSpanSize(int position)
            {
                if (!nativeIsAvailable)
                {
                    return 1;
                }

                else
                {
                    return (items.get(position) instanceof NativeItem ? ThumbPhotoAdapter.this.spanCount : 1);
                }
            }
        });*/
    }


    public void getItemsTypes() {
        items.clear();
        if (customComponents.size() > 0) {
            items.addAll(customComponents);
        }
        if (resources.size() > 0) {
            items.addAll(resources);
        }
        if (photos.size() > 0) {
            items.addAll(photos);
        }
        if (nativeIsAvailable) {
            int startIndex = 0;
            if (rowsToFirstNative != -1 && rowsToNative != 0) {
                startIndex = spanCount * rowsToFirstNative;
            } else {
                startIndex = spanCount * rowsToNative;
            }
            if (startIndex > 0) {
                int count = 0;
                while (startIndex < items.size()) {
                    try {
                        items.add(startIndex + count++, new NativeItem());
                    } catch (Exception ignore) {
                        break;
                    }
                    if (rowsToNative == -1 || rowsToNative == 0) {
                        break;
                    }
                    startIndex += spanCount * rowsToNative;
                }
            }
        }
    }

    public ArrayList<CustomComponent> customComponents = new ArrayList<>();
    public ArrayList<Resource> resources = new ArrayList<>();
    public ArrayList<AdapterItem> items = new ArrayList<>();

    //Dodavanje SLIKA ako je u pitanju galerija slika
    public void addData(List<Photo> photos) {
        this.photos.clear();
        this.photos.addAll(photos);
        getItemsTypes();
        notifyDataSetChanged();
        if (listener != null) {
            listener.dataReady();
        }
    }

    //Deselekt slike iz galerije na osnovu putanje do slike
    public void deselect(String path) {
        boolean isUriLogic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        for (AdapterItem adapterItem : selectedItems) {
            if (isUriLogic) {
                if (adapterItem != null && adapterItem instanceof Photo && ((Photo) adapterItem).getUri() != null && ((Photo) adapterItem).getUri().toString().equalsIgnoreCase(path)) {
                    selectedItems.remove(adapterItem);
                    notifyDataSetChanged();
                    return;
                }
            } else {
                if (adapterItem != null && adapterItem instanceof Photo && ((Photo) adapterItem).getPath() != null && ((Photo) adapterItem).getPath().equalsIgnoreCase(path)) {
                    selectedItems.remove(adapterItem);
                    notifyDataSetChanged();
                    return;
                }
            }
        }

    }

    public void deselect(Photo photo) {
        selectedItems.remove(photo);
    }

    //Deselektovanje resursa na osnovu resursa
    public void deselect(Resource resource) {
        selectedItems.remove(resource);
    }

    public void clearSelected() {
        selectedItems.clear();
    }

    public void selectAll() {
        clearSelected();
        selectAllFromGallery();
        selectAllFromResources();
        if (listener != null) {
            listener.allSelected();
        }
        notifyDataSetChanged();
    }

    public void selectAllFromGallery() {

        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            if (!selectedItems.contains(photo)) {
                selectedItems.add(photo);
            }
        }
    }

    public void notifyAllFromGallerySelected() {
        if (listener != null) {
            listener.allSelectedFromGallery();
        }
    }

    public void selectAllFromResources() {

        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            if (!selectedItems.contains(resource)) {
                selectedItems.add(resource);
            }
        }
    }

    public void notifyAllFromResourcesSelected() {
        if (listener != null) {
            listener.allSelectedFromResources();
        }
    }

    //Selektovanje slike iz galerije na osnovu putanje do te slike
    public int select(String path) {
        int index = indexOfPhoto(path);
        if (index != -1) {
            Photo photo = photos.get(index);
            if (!selectedItems.contains(photo)) {
                selectedItems.add(photo);
            }
            notifyItemChanged(items.indexOf(photo));
            if (listener != null) {
                if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                    listener.itemSingleClicked(path);
                } else {
                    listener.itemSelected(path);
                }
            }
            return index;
        }
        return -1;
    }

    public int select(int res) {
        int index = indexOfResource(res);
        if (index != -1) {
            Resource resource = getItemResources(index);
            int position = (index);
            if (!selectedItems.contains(resource)) {
                selectedItems.add(resource);
            }
            notifyItemChanged(position);
            if (listener != null) {
                if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                    listener.itemSingleClicked(res);
                } else {
                    listener.itemSelected(res);
                }
            }
            return position;
        }
        return -1;
    }

    public int select(Bitmap bmp) {
        int index = indexOfBitmap(bmp);
        if (index != -1) {
            Resource resource = getItemResources(index);
            int position = (index);
            if (!selectedItems.contains(resource)) {
                selectedItems.add(resource);
            }
            notifyItemChanged(position);
            if (listener != null) {
                if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                    listener.itemSingleClicked(bmp);
                } else {
                    listener.itemSelected(bmp);
                }
            }
            return position;
        }
        return -1;
    }

    public void clearAdapterOnStop() {
        this.photos.clear();
        this.resources.clear();
    }

    public void clearAdapter() {
        this.photos.clear();
        this.resources.clear();
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedItems.clear();
    }

    ArrayList<Integer> nativeHeights;
    public float aspect = 1f;

    @Override
    public UniversalHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch ((viewType)) {
            case CUSTOM:
                return new CustomHolder(layoutInflater.inflate(R.layout.item_custom_item, parent, false));
            case GALLERY_ITEM:
                return new ThumbHolder(new ThumbPhotoView(context));
            case RESOURCE:
                return new ResourcesHolder(new ThumbPhotoView(context));
            case NATIVE:
                View itemView = new RelativeLayout(context);
                return new NativeHolder(itemView);
            default:
                return new ThumbHolder(new ThumbPhotoView(context));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }

    @Override
    public void onBindViewHolder(UniversalHolder holder, int position) {
        if (position == 25) {
            Log.i("", "");
        }
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    /**
     * @return Vraca selektovane slike
     */
    public ArrayList<String> getSelectedImages() {
        ArrayList<String> selectedImages = new ArrayList<>();
        for (AdapterItem adapterItem : selectedItems) {
            if (adapterItem instanceof Photo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    selectedImages.add(((Photo) adapterItem).getUri().toString());
                else
                    selectedImages.add(((Photo) adapterItem).getPath());
            }
        }
        return selectedImages;
    }

    /**
     * @return Vraca selektovane resurse (int ili bitmape)
     */
    public ArrayList<Resource> getSelectedResources() {
        ArrayList<Resource> selectedRes = new ArrayList<>();
        for (AdapterItem adapterItem : selectedItems) {
            if (adapterItem instanceof Resource) {
                selectedRes.add((Resource) adapterItem);
            }
        }
        return selectedRes;
    }

    public Resource getItemResources(int position) {
        if (position < this.resources.size()) {
            return this.resources.get(position);
        } else {
            return null;
        }
    }

    public int indexOfPhoto(String path) {
        for (Photo p : photos) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (p.getUri().toString().equals(path)) {
                    return photos.indexOf(p);
                }
            } else {
                if (p.getPath().equals(path)) {
                    return photos.indexOf(p);
                }
            }
        }
        return -1;
    }

    public int indexOfResource(int res) {
        for (Resource r : resources) {
            if (r.getValue() != null && r.getValue() instanceof Integer && res == (int) r.getValue()) {
                return resources.indexOf(r);
            }
        }
        return -1;
    }

    public int indexOfBitmap(Bitmap bmp) {
        for (Resource r : resources) {
            if (r.getValue() != null && r.getValue() instanceof Bitmap && bmp == r.getValue()) {
                return resources.indexOf(r);
            }
        }
        return -1;
    }


    public void setNativeSettings(NativeSettings nativeSettings) {
        mNativeSettings = nativeSettings;
    }

    public NativeSettings getNativeSettings() {
        return mNativeSettings;
    }


    public class UniversalHolder extends RecyclerView.ViewHolder {

        public UniversalHolder(View itemView) {
            super(itemView);
        }

        public void setData(final int position) {

        }
    }

    class UniversalElementHolder extends UniversalHolder {

        public UniversalElementHolder(View itemView) {
            super(itemView);
            itemView.setLayoutParams(elementParams);
        }

        @Override
        public void setData(int position) {
        }
    }

    class ResourcesHolder extends UniversalElementHolder {

        public ThumbPhotoView thumbPhotoView;

        public ResourcesHolder(View itemView) {
            super(itemView);
            thumbPhotoView = (ThumbPhotoView) itemView;
        }

        @Override
        public void setData(int position) {
            Resource resource = (Resource) items.get(position);
            if (resource != null && resource.getValue() != null) {
//                photo_thumbview.setImageResource((Integer) resources.get(position));

                if (resource.getValue() instanceof Integer) {
                    thumbPhotoView.loadData((Integer) resource.getValue(), pickMode);
                } else if (resource.getValue() instanceof Bitmap) {
                    if ((Bitmap) resource.getValue() != null && !((Bitmap) resource.getValue()).isRecycled()) {
                        thumbPhotoView.loadData((Bitmap) resource.getValue(), pickMode);
                    }
                }
                if (selectedItems.contains(resource)) {
                    thumbPhotoView.showSelected(true);
                } else {
                    thumbPhotoView.showSelected(false);
                }
                thumbPhotoView.setTag(resource);
                thumbPhotoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Resource resource = (Resource) view.getTag();

                        if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                            AdapterItem lastRes = null;
                            if (selectedItems.size() > 0) {
                                lastRes = selectedItems.get(selectedItems.size() - 1);
                            }
                            selectedItems.clear();
                            int lastSelectedPosition = items.indexOf(lastRes);
                            if (lastSelectedPosition != -1) {
                                notifyItemChanged(lastSelectedPosition);
                            }
                            if (lastRes != (resource)) {
                                selectedItems.add(resource);
                                thumbPhotoView.showSelected(true);
                            } else {
                                thumbPhotoView.showSelected(false);
                            }
                            if (listener != null) {
                                listener.itemSingleClicked(resource);
                            }
                        } else {
                            if (selectedItems.contains(resource)) {
                                selectedItems.remove(resource);
                                thumbPhotoView.showSelected(false);
                                if (listener != null) {
                                    listener.itemDeselected(resource);
                                }
                            } else {
                                if (selectedItems.size() == maxPickSize) {
                                    if (listener != null) {
                                        listener.itemNumOverflow(resource);
                                    }
                                    return;
                                } else {
                                    selectedItems.add(resource);
                                    thumbPhotoView.showSelected(true);
                                    if (listener != null) {
                                        listener.itemSelected(resource);
                                    }
                                    if (selectedItems.size() == maxPickSize) {
                                        if (listener != null) {
                                            listener.itemMaxReached(resource);
                                        }
                                    }
                                }
                            }
                            setTitle(selectedItems.size());
                        }
                        if (listener != null) {
                            listener.itemClick(resource);
                        }
                    }
                });
            }
        }
    }

    class CustomHolder extends UniversalElementHolder {

        ImageView photo_thumbview;

        public CustomHolder(View itemView) {
            super(itemView);
            photo_thumbview = (ImageView) itemView.findViewById(R.id.photo_thumbview);
        }

        int prevPos;

        @Override
        public void setData(int position) {
            CustomComponent customComponent = (CustomComponent) items.get(position);
            prevPos = position;
            if (position < 0) {
                return;
            }
            if (customComponent != null) {
                if (customComponent.getValue() instanceof String) {
                    String path = (String) customComponent.getValue();
                    if (path.startsWith("file:/")) {
                        path = path.substring(5);
                    }
                    Uri uri = UriUtil.generatorUri(path, UriUtil.LOCAL_FILE_SCHEME);
                    Glide.with(context).load(uri).thumbnail(0.3f).into(photo_thumbview);
                } else if (customComponent.getValue() instanceof Integer) {
                    int res = (int) customComponent.getValue();
                    Glide.with(context).load(res).thumbnail(0.3f).into(photo_thumbview);
                } else if (customComponent.getValue() instanceof Bitmap) {
                    Bitmap bmp = (Bitmap) customComponent.getValue();
                    if (bmp != null && !bmp.isRecycled()) {
                        photo_thumbview.setImageBitmap(bmp);
                    }
                }
            }
            itemView.setTag(position);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
                    if (mCustomComponentListener != null) {
                        mCustomComponentListener.onCustomComponentClicked(position);
                    }
                }
            });
        }
    }

    class ThumbHolder extends UniversalElementHolder {

        public ThumbPhotoView thumbPhotoView;

        public ThumbHolder(View itemView) {
            super(itemView);
            thumbPhotoView = (ThumbPhotoView) itemView;
        }

        public void setData(final int position) {
            Photo imageInfo = (Photo) ThumbPhotoAdapter.this.items.get(position);

            if (imageInfo == null) {
                return;
            }
            thumbPhotoView.setLayoutParams(elementParams);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                thumbPhotoView.loadData(imageInfo.getUri(), pickMode);
            else
                thumbPhotoView.loadData(imageInfo.getPath(), pickMode);

            if (selectedItems.contains(imageInfo)) {
                thumbPhotoView.showSelected(true);
            } else {
                thumbPhotoView.showSelected(false);
            }
            if (thumbPhotoView.photo_video_flag != null) {
                if (imageInfo.getPath() != null)
                    if (imageInfo.getPath().contains(".mp4")) {
                        thumbPhotoView.photo_video_flag.setVisibility(View.VISIBLE);
                    } else {
                        thumbPhotoView.photo_video_flag.setVisibility(View.GONE);
                    }
                else if (imageInfo.getUri() != null) {
                    ContentResolver cR = context.getContentResolver();
                    String type = cR.getType(imageInfo.getUri());
                    if (imageInfo.getUri().toString().startsWith("video")) {
                        thumbPhotoView.photo_video_flag.setVisibility(View.VISIBLE);
                    } else {
                        thumbPhotoView.photo_video_flag.setVisibility(View.GONE);
                    }
                }
            }

            thumbPhotoView.setTag(imageInfo);
            thumbPhotoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Photo imageInfo = (Photo) view.getTag();
                    if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                        AdapterItem lastPath = null;
                        if (selectedItems.size() > 0) {
                            lastPath = selectedItems.get(selectedItems.size() - 1);
                        }
                        selectedItems.clear();
                        int lastSelectedPosition = items.indexOf(lastPath);
                        if (lastSelectedPosition != -1) {
                            notifyItemChanged(lastSelectedPosition);
                        }
                        if (lastPath != imageInfo) {
                            selectedItems.add(imageInfo);
                            thumbPhotoView.showSelected(true);
                            lastSelectedPosition = position;
                        } else {
                            thumbPhotoView.showSelected(false);
                        }
                        if (listener != null) {
                            listener.itemSingleClicked(imageInfo);
                        }

                        /**Ovo je stari nacin da se putanja prosledi predhodnom ekranu*/
                        /*Intent intent = new Intent();
                        intent.putStringArrayListExtra(PickConfig.EXTRA_STRING_ARRAYLIST, selectedImages);
                        context.setResult(context.RESULT_OK, intent);
                        context.finish();*/
                    } else {
                        if (selectedItems.contains(imageInfo)) {
                            selectedItems.remove(imageInfo);
                            thumbPhotoView.showSelected(false);
                            if (listener != null) {
                                listener.itemDeselected(imageInfo);
                            }
                        } else {
                            if (selectedItems.size() == maxPickSize) {
                                if (listener != null) {
                                    listener.itemNumOverflow(imageInfo);
                                }
                                return;
                            } else {
                                selectedItems.add(imageInfo);
                                thumbPhotoView.showSelected(true);
                                if (listener != null) {
                                    listener.itemSelected(imageInfo);
                                }
                                if (selectedItems.size() == maxPickSize) {
                                    if (listener != null) {
                                        listener.itemMaxReached(imageInfo);
                                    }
                                }
                            }
                        }
                        setTitle(selectedItems.size());
                    }
                    if (listener != null) {
                        listener.itemClick(imageInfo);
                    }
                }
            });
        }
    }

    class NativeHolder extends UniversalHolder {


        String adId;


        public NativeHolder(View itemView) {
            super(itemView);


        }

        @Override
        public void setData(int position) {
            /*
            CMSAd nativeAd = null;
            if (clickedAdId != null && adId != null)
            {
                if (clickedAdId.equalsIgnoreCase(adId))
                {
                    nativeAd = CMSMain.getNativeAdForActionID(context, actionId);
                    clickedAdId = null;
                }
                else
                {
                    return;
                }
            }
            else
            {
                nativeAd = CMSMain.getNativeAdForActionID(context, actionId);
            }
*/
            ((RelativeLayout) itemView).removeAllViews();
            itemView.setVisibility(View.GONE);
            itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
/*
            if (nativeAd == null)
            {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            }
            else
            {
                adId = nativeAd.getAdID();

                LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (inflater != null)
                {
                    ViewGroup maincontainer;

                    View adcontainer = inflater.inflate(R.layout.native_ad_item, null);

                    if (nativeAd instanceof CMSAdFacebook)
                    {
                        maincontainer = new com.facebook.ads.NativeAdLayout(context);

                    }
                    else if (nativeAd instanceof CMSAdAdmob)
                    {
                        maincontainer = new com.google.android.gms.ads.formats.UnifiedNativeAdView(context);
                    }
                    else
                    {
                        maincontainer = new RelativeLayout(context);
                    }

                    maincontainer.addView(adcontainer);

                    RelativeLayout mediaContainer = maincontainer.findViewById(R.id.mediaContainer);
                    TextView title = maincontainer.findViewById(R.id.nativeTitle);
                    TextView cta = maincontainer.findViewById(R.id.nativeCTA);
                    RelativeLayout adLabelContainer = maincontainer.findViewById(R.id.nativeAdLabelContainer);
                    RelativeLayout mustIncludeContainer = maincontainer.findViewById(R.id.nativeMustIncludeContainer);

                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    if (getNativeSettings().isNativeCtaRadius())
                    {
                        float radii = convertDpToPixel(5, context);
                        shape.setCornerRadii(new float[]{radii, radii, radii, radii, radii, radii, radii, radii});
                    }
                    shape.setColor(getNativeSettings().getNativeCtaBgdColor());

                    if (getNativeSettings().isNativeCtaStroke())
                    {
                        int stroke = (int) convertDpToPixel(3, context);
                        shape.setStroke(stroke, getNativeSettings().getNativeCtaStrokeColor());
                    }
                    cta.setBackground(shape);
                    cta.setTextColor(getNativeSettings().getNativeCtaTextColor());

                    title.setTextColor(getNativeSettings().getNativeTitleColor());

                    maincontainer.setBackgroundColor(getNativeSettings().getNativeBgdColor());

                    List<View> clickableViews = new ArrayList<>();
                    clickableViews.add(title);
                    clickableViews.add(cta);

                    clickableViews.add(mediaContainer);

                    nativeAd.prepareNativeAdView(context, maincontainer, null, mediaContainer, title, cta, adLabelContainer, mustIncludeContainer, clickableViews);
                    ((RelativeLayout)itemView).setPadding(0, (int) convertDpToPixel(20, context), 0, (int) convertDpToPixel(20, context));
                    ((RelativeLayout)itemView).addView(maincontainer);
                }

                CMSMain.nativeAdUsed(context, adId);
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(nativeParams);


            }
            */
//            makeRoundCorner( Color.parseColor("#66000000"), Math.round(convertDpToPixel(5f, context)), llMustViewAdTextHolder, 0, 0);
        }

        private float convertDpToPixel(float dp, Context context) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            return px;
        }

        private void makeRoundCorner(int bgcolor, int radius, View v, int strokeWidth, int strokeColor) {
            GradientDrawable gdDefault = new GradientDrawable();
            gdDefault.setColor(bgcolor);
            gdDefault.setCornerRadius(radius);
            gdDefault.setStroke(strokeWidth, strokeColor);
            v.setBackgroundDrawable(gdDefault);
        }
    }

    public static String formatTitleText(String titleText) {

        if (titleText != null && !titleText.equalsIgnoreCase("") && titleText.length() > 20) {
            titleText = titleText.subSequence(0, 20) + "\u2026";  //kod za "..."
        }

        return titleText;
    }

    public void setCustomComponentListener(ICustomComponentListener customComponentListener) {
        mCustomComponentListener = customComponentListener;
    }

    ICustomComponentListener mCustomComponentListener;

    public interface ICustomComponentListener {

        void onCustomComponentClicked(int position);
    }

    public void setListener(IItemsListener listener) {
        this.listener = listener;
    }

    IItemsListener listener;

    public interface IItemsListener {

        //Ako je kliknuto na element iz galerije, argument funkcija je putanja do slike kao String, ako su prikazani resursi onda vraca int ili bitmapu
        void itemClick(Object pathOrRes);

        void itemSelected(Object pathOrRes);

        void itemDeselected(Object pathOrRes);

        void itemSingleClicked(Object pathOrRes);

        void itemNumOverflow(Object pathOrRes);

        void itemMaxReached(Object pathOrRes);

        void allSelected();

        void allSelectedFromGallery();

        void allSelectedFromResources();

        /**
         * Kada su ucitani svi elementi poziva se ova funkcija, unutar nje treba pozivati funkcije nalik selectAll ili select(path) ...
         */
        void dataReady();
    }

    public void setTitle(int selectCount) {
        if (toolbar != null) {
            toolbar.setTitle(selectCount + "/" + maxPickSize);
        }
    }

}
