package com.gallery.views;


import com.gallery.PickConfig;
import com.gallery.adapter.ThumbPhotoAdapter;
import com.gallery.model.CustomComponent;
import com.gallery.model.NativeItem;
import com.gallery.model.NativeSettings;
import com.gallery.model.PhotoDirectory;
import com.gallery.model.Resource;
import com.gallery.presenters.PhotoPresenterImpl;
import com.gallery.widget.AlbumPopupWindow;
import com.gallery.widget.ThumbPhotoView;
import com.photostickers.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by petarljubic on 9/21/2016.
 * Ovo je View koji prikazuje galeriju, sadrzi listu, loading, odabir foldera i native adove
 */

public class CustomPickPhotoView extends RelativeLayout implements PhotoView {

    RecyclerView recyclerView;
    /**
     * U ovaj textview se prikazuje ime foldera koji je prikazan
     */
    TextView btn_category;

    View mPopupAnchorView;

    PhotoPresenterImpl photoresenter;

    /**
     * Ovde se prikazuju svi dostupni folderi
     */
    AlbumPopupWindow albumPopupWindow;

    /**
     * Adapter u kome se prikazuju slike i native adovi
     */
    ThumbPhotoAdapter thumbPhotoAdapter;

    /**
     * Predstavlja broj kolona u galeriji
     */
    private int spanCount;
    /**
     * Nakon koliko redova da se prikaze novi native (ako je dostupan naravno)
     */
    public int rowsToNative = 5;

    /**
     * Maksimalan broj selektovanih slika u multi modu
     */
    private int maxPickSize;

    /**
     * Multi ili single
     */
    private int pickMode;

    private NativeSettings nativeSettings;

    public static boolean useCursorLoader = false;

    private Bundle bundle;

    private ArrayList<String> customPaths;

    //Ovo je meni vazan podatak da bih znao koliko da rasirim (prikazem) element u galeriji
    public static int EL_WIDTH;

    public Boolean nativeIsAvailable = null;

    public CustomPickPhotoView(Context context) {
        super(context);
        initData();
        initView();

        if (context instanceof ThumbPhotoAdapter.IItemsListener) {
            setupItemsClicksListener((ThumbPhotoAdapter.IItemsListener) context);
        }
        if (context instanceof ThumbPhotoAdapter.ICustomComponentListener) {
            setupCustomItemsListener((ThumbPhotoAdapter.ICustomComponentListener) context);
        }
    }

    public CustomPickPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(attrs);
        initView();
        if (context instanceof ThumbPhotoAdapter.IItemsListener) {
            setupItemsClicksListener((ThumbPhotoAdapter.IItemsListener) context);
        }
        if (context instanceof ThumbPhotoAdapter.ICustomComponentListener) {
            setupCustomItemsListener((ThumbPhotoAdapter.ICustomComponentListener) context);
        }
    }

    public CustomPickPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(attrs, defStyleAttr);
        EL_WIDTH = context.getResources().getDisplayMetrics().widthPixels / 4;
        initView();
        if (context instanceof ThumbPhotoAdapter.IItemsListener) {
            setupItemsClicksListener((ThumbPhotoAdapter.IItemsListener) context);
        }
        if (context instanceof ThumbPhotoAdapter.ICustomComponentListener) {
            setupCustomItemsListener((ThumbPhotoAdapter.ICustomComponentListener) context);
        }
    }

    /**
     * Podesavanje podrazumevanih vrednosti
     */
    private void initData() {
        spanCount = PickConfig.DEFAULT_SPANCOUNT;
        pickMode = PickConfig.MODE_MULTIP_PICK;
        maxPickSize = 30;
        useCursorLoader = PickConfig.DEFALUT_USE_CURSORLOADER;
        nativeSettings = new NativeSettings(getContext());
    }

    /**
     * Ako ste ukljucili opcije kroz layout ovde se one podesavaju
     */
    @SuppressLint("ResourceType")
    private void initData(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomPickPhotoViewStyle);
        ThumbPhotoView.itemLayout = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_itemLayout, R.layout.item_pickphoto_view);
        spanCount = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_num_of_colons, PickConfig.DEFAULT_SPANCOUNT);
        pickMode = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_mode_multi_select, PickConfig.MODE_MULTIP_PICK);
        maxPickSize = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_max_pick_size, 10);
        rowsToNative = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_rows_to_native, 5);
        useCursorLoader = array.getBoolean(R.styleable.CustomPickPhotoViewStyle_use_cursor_loader, PickConfig.DEFALUT_USE_CURSORLOADER);
        array.recycle();
    }

    @SuppressLint("ResourceType")
    private void initData(AttributeSet attrs, int defStyle) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomPickPhotoViewStyle, defStyle, 0);
        ThumbPhotoView.itemLayout = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_itemLayout, R.layout.item_pickphoto_view);
        spanCount = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_num_of_colons, PickConfig.DEFAULT_SPANCOUNT);
        pickMode = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_mode_multi_select, PickConfig.MODE_MULTIP_PICK);
        maxPickSize = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_max_pick_size, 10);
        rowsToNative = array.getResourceId(R.styleable.CustomPickPhotoViewStyle_rows_to_native, 5);
        useCursorLoader = array.getBoolean(R.styleable.CustomPickPhotoViewStyle_use_cursor_loader, PickConfig.DEFALUT_USE_CURSORLOADER);
        array.recycle();
    }

    /**
     * Moze se dinamicki podesavati maksimalan broj selektovanih
     */
    public void setMaxSelected(int max) {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.maxPickSize = max;
        }
    }

    /**
     * Deselektovati sve slike iz galerije
     */
    public void deselectAll() {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.deselectAll();
        }
    }

    public void selectAll() {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.selectAll();
        }
    }

    public void selectAllFromGallery() {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.clearSelected();
            thumbPhotoAdapter.selectAllFromGallery();
            thumbPhotoAdapter.notifyAllFromGallerySelected();
            thumbPhotoAdapter.notifyDataSetChanged();
        }
    }

    public void selectAllFromResources() {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.clearSelected();
            thumbPhotoAdapter.selectAllFromResources();
            thumbPhotoAdapter.notifyAllFromResourcesSelected();
            thumbPhotoAdapter.notifyDataSetChanged();
        }
    }

    public boolean select(String path) {
        if (thumbPhotoAdapter != null) {
            int pos = thumbPhotoAdapter.select(path);
            if (pos != -1) {
                recyclerView.scrollToPosition(pos);
                return true;
            }
            return false;
        }
        return false;
    }

    public void onNativeClick(String adId) {
        thumbPhotoAdapter.clickedAdId = adId;
        thumbPhotoAdapter.notifyDataSetChanged();
    }

    public enum TYPE {
        GALLERY_WITH_IMAGES_AND_VIDEOS, GALLERY_WITH_IMAGES, GALLERY_WITH_VIDEOS, RESOURCES, CUSTOM_IMAGES
    }

    TYPE currentType = TYPE.GALLERY_WITH_IMAGES;

    /**
     * Parametre mozemo podesavati dinamicki kroz kod, nakon ovih podesavanja lista slika ce se refreshovati
     *
     * @param currentType       - TYPE moze biti galerija ili resource, i u zavisnosti od toga se zna sta ce da se prikazuje
     * @param itemLayout        - Mozete postaviti custom layout za prikaz jedne slike, stim sto mora da ima elemente koje ima i default-ni
     * @param span              - Broj colona u galeriji
     * @param pick              - Mode rada (mutli ili single)
     * @param max               - Maximalan broj selektovanih slika
     * @param useCursor         - Da li da koristi kursor za ucitavanje (NAPOMENA: zbog problema sa refresh-om ovo je uvek na false)
     * @param defaultError      - Kada ne uspe da ucita sliku, ili tek treba da je ucita, stoji ova slika. Ako se stavi -1, uzece defaultnu
     * @param defaultSelected   - Slika koja se prikazuje preko selektovane slike. Ako se stavi -1, uzece defaultnu
     * @param defaultUnselected - Kada nije selektovana slika, ovo stoji preko slike. Ako se stavi -1, uzece defaultnu
     * @param rowsToNative      - Broj redova do sledeceg nativa.
     */
    public void setup(TYPE currentType, ArrayList<String> paths, int itemLayout, int span, int pick, int max, boolean useCursor, int defaultError, int defaultSelected, int defaultUnselected, int rowsToNative, int rowsToFirstNative) {
        if (itemLayout != 0 && itemLayout != -1) {
            ThumbPhotoView.itemLayout = itemLayout;
        }
        spanCount = span;
        this.rowsToNative = rowsToNative;
        this.rowsToFirstNative = rowsToFirstNative;
        EL_WIDTH = getContext().getResources().getDisplayMetrics().widthPixels / 4;
        pickMode = pick;
        maxPickSize = max;
        useCursorLoader = useCursor;
        if (defaultError != -1) {
            ThumbPhotoView.defaultError = defaultError;
        }
        if (defaultSelected != -1) {
            ThumbPhotoView.defaultSelected = defaultSelected;
        }
        if (defaultUnselected != -1) {
            ThumbPhotoView.defaultUnselected = defaultUnselected;
        }
        this.customPaths = paths;
        this.currentType = currentType;
        refresh(currentType);
        if (currentType == TYPE.GALLERY_WITH_IMAGES_AND_VIDEOS || currentType == TYPE.GALLERY_WITH_IMAGES || currentType == TYPE.GALLERY_WITH_VIDEOS || currentType == TYPE.CUSTOM_IMAGES) {
            loadGallery(currentType);
        } else {
            loadResources();
        }
        thumbPhotoAdapter.notifyDataSetChanged();
        if (getContext() instanceof ThumbPhotoAdapter.IItemsListener) {
            setupItemsClicksListener((ThumbPhotoAdapter.IItemsListener) getContext());
        }
        if (getContext() instanceof ThumbPhotoAdapter.ICustomComponentListener) {
            setupCustomItemsListener((ThumbPhotoAdapter.ICustomComponentListener) getContext());
        }
    }

    ArrayList<Object> resources;

    public void setup(TYPE currentType, Object resources, int itemLayout, int span, int pick, int max, boolean useCursor, int defaultError, int defaultSelected, int defaultUnselected, int rowsToNative) {

        if (currentType == TYPE.CUSTOM_IMAGES) {
            if (resources instanceof ArrayList) {
                this.customPaths = (ArrayList<String>) resources;
            }
        } else {
            this.resources = (ArrayList<Object>) resources;
        }
        setup(currentType, customPaths, itemLayout, span, pick, max, useCursor, defaultError, defaultSelected, defaultUnselected, rowsToNative, rowsToFirstNative);
    }

    public void setup(TYPE currentType, String prefix, int itemLayout, int span, int pick, int max, boolean useCursor, int defaultError, int defaultSelected, int defaultUnselected, int rowsToNative) {
        this.resources = new ArrayList<>();
        resources.addAll(listRaw(prefix, getContext()));
        setup(currentType, null, itemLayout, span, pick, max, useCursor, defaultError, defaultSelected, defaultUnselected, rowsToNative, rowsToFirstNative);
    }

    public void setupSinglePickGalleryModeDefault(TYPE type) {
        setup(type, null, -1, 3, PickConfig.MODE_SINGLE_PICK, 1, false, -1, -1, -1, 2, rowsToFirstNative);
    }

    public void setupSinglePickResourceModeDefault(ArrayList<Object> resources) {
        this.resources = resources;
        setup(TYPE.RESOURCES, null, -1, 3, PickConfig.MODE_SINGLE_PICK, 1, false, -1, -1, -1, 2, rowsToFirstNative);
    }

    public void setupMultiPickResourcesModeDefault(ArrayList<Object> resources) {
        this.resources = resources;
        setup(TYPE.RESOURCES, null, -1, 3, PickConfig.MODE_MULTIP_PICK, Integer.MAX_VALUE, false, -1, -1, -1, 2, rowsToFirstNative);
    }

    public void setupSinglePickResourceModeDefault(String prefix) {
        this.resources = new ArrayList<>();
        resources.addAll(listRaw(prefix, getContext()));
        setup(TYPE.RESOURCES, null, -1, 3, PickConfig.MODE_SINGLE_PICK, 1, false, -1, -1, -1, 2, rowsToFirstNative);
    }

    public void setupMultiPickResourcesModeDefault(String prefix) {
        this.resources = new ArrayList<>();
        resources.addAll(listRaw(prefix, getContext()));
        setup(TYPE.RESOURCES, null, -1, 3, PickConfig.MODE_MULTIP_PICK, Integer.MAX_VALUE, false, -1, -1, -1, 2, rowsToFirstNative);
    }


    public ArrayList<Integer> listRaw(String prefix, Context context) {
        ArrayList<Integer> resources = new ArrayList<>();
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            if (field.getName().startsWith(prefix)) {
                resources.add(context.getResources().getIdentifier(field.getName(), "drawable", context.getPackageName()));
            }
        }
        return resources;
    }

    /**
     * Osvezavanje galerije
     */
    public void refreshAll() {
        loadGallery(currentType);
        thumbPhotoAdapter.notifyDataSetChanged();
    }

    /**
     * Ako hocemo da deselektujemo jedan element, navodimo putanju slike tog elementa u galeriji
     *
     * @param path - putanja do slike koju zelimo da deselektujemo
     */
    public void deselect(String path) {
        thumbPhotoAdapter.deselect(path);
    }


    private void initView() {
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.activity_pick_photo, this);
        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        btn_category = root.findViewById(R.id.btn_category);
        mPopupAnchorView = root.findViewById(R.id.photo_footer);

        refresh(currentType);

    }


    public void refresh(TYPE type) {
        thumbPhotoAdapter = new ThumbPhotoAdapter(((Activity) getContext()), type, spanCount, maxPickSize, pickMode, null, rowsToNative, rowsToFirstNative);
        recyclerView.setLayoutManager(bindGridLayoutManager(spanCount));
        thumbPhotoAdapter.setNativeSettings(nativeSettings);
        recyclerView.setAdapter(thumbPhotoAdapter);
        if (type == TYPE.GALLERY_WITH_IMAGES_AND_VIDEOS || type == TYPE.GALLERY_WITH_IMAGES || type == TYPE.GALLERY_WITH_VIDEOS) {
            albumPopupWindow = new AlbumPopupWindow(getContext());
            albumPopupWindow.setAnchorView(mPopupAnchorView);
            albumPopupWindow.setOnItemClickListener(onItemClickListener);
            btn_category.setVisibility(VISIBLE);
            mPopupAnchorView.setVisibility(VISIBLE);
            btn_category.setText("All Photos");
            mPopupAnchorView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    albumPopupWindow.show();
                }
            });
        } else {
            mPopupAnchorView.setVisibility(GONE);
            btn_category.setVisibility(GONE);
        }
    }

    int rowsToFirstNative = -1;

    public GridLayoutManager bindGridLayoutManager(final int spanCount) {

        GridLayoutManager manager = new GridLayoutManager(getContext(), spanCount, GridLayoutManager.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (nativeIsAvailable == null || !nativeIsAvailable) {
                    return 1;
                } else {
                    return (thumbPhotoAdapter.items.get(position) instanceof NativeItem ? spanCount : 1);
                }
               /* if (position == 0)
                {
                    return 1;
                }
                else
                {
                    if (rowsToFirstNative > 0 && position == (CustomPickPhotoView.this.spanCount * rowsToFirstNative))
                    {
                        return CustomPickPhotoView.this.spanCount;
                    }
                    int grid = CustomPickPhotoView.this.spanCount * rowsToNative;
                    int a = position;
                    boolean query = checkIfElementIsNative(a, grid);
                    if (query)
                    {
                        Log.i("TAGG", "" + position);
                    }
                    return (query ? CustomPickPhotoView.this.spanCount : 1);
                }*/
            }
        });
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
        return manager;
    }

    private boolean checkIfElementIsNative(int position, int grid) {
        if (rowsToFirstNative > 0) {
            position -= (CustomPickPhotoView.this.spanCount * rowsToFirstNative);
            if ((position/* + 1*/) % (grid + 1) == 0) {
                Log.i("TAGG", "" + position);
            }
            return (position/* + 1*/) % (grid + 1) == 0;
        }
        return (position + 1) % (grid + 1) == 0;
    }

    private void loadGallery(TYPE currentType) {
        if (progressSetter != null) {
            progressSetter.startProgressBar();
        }

        if (currentType != TYPE.CUSTOM_IMAGES) {
            photoresenter = new PhotoPresenterImpl(((AppCompatActivity) getContext()), this);
        }

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermission(((Activity) getContext()), Manifest.permission.READ_MEDIA_IMAGES)) {
                initialized(useCursorLoader, currentType);
            }
        } else {
            if (checkPermission(((Activity) getContext()), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                initialized(useCursorLoader, currentType);
            }
        }
    }

    public static boolean checkPermission(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int storagePermission = ActivityCompat.checkSelfPermission(activity, permission);

            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void loadResources() {
        if (resources != null) {
            if (thumbPhotoAdapter != null) {
                thumbPhotoAdapter.resources.clear();
                for (Object r : resources) {
                    Resource resource = new Resource(r);
                    thumbPhotoAdapter.resources.add(resource);
                }
                thumbPhotoAdapter.getItemsTypes();
                thumbPhotoAdapter.notifyDataSetChanged();
            }
        }
        invalidate();
    }

    public void insertCustomElements(ArrayList<Object> customElements) {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.customComponents.clear();
            for (Object o : customElements) {
                CustomComponent customComponent = new CustomComponent(o);
                thumbPhotoAdapter.customComponents.add(customComponent);
            }
            thumbPhotoAdapter.getItemsTypes();
            thumbPhotoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Funkcija koja treba da vrati sve slike koje su selektovane
     *
     * @return niz putanja do slika
     */
    public ArrayList<String> getSelectedImages() {
        if (thumbPhotoAdapter != null) {
            return thumbPhotoAdapter.getSelectedImages();
        } else {
            return null;
        }
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            albumPopupWindow.setSelectedIndex(i);
            albumPopupWindow.getListView().smoothScrollToPosition(i);
            PhotoDirectory albumInfo = albumPopupWindow.getItem(i);
            thumbPhotoAdapter.clearAdapter();
            thumbPhotoAdapter.addData(albumInfo.getPhotos());
            btn_category.setText(albumInfo.getName());
            recyclerView.scrollToPosition(0);
            albumPopupWindow.dismiss();
        }
    };

    public static final int ALL = 0;
    public static final int IMAGES = 1;
    public static final int VIDEOS = 2;
    public static final int CUSTOM_IMAGES = 3;

    public void initialized(boolean useCursorLoader, TYPE type) {
        bundle = new Bundle();
        switch (type) {
            case GALLERY_WITH_IMAGES_AND_VIDEOS:
                bundle.putInt("type", ALL);
                break;
            case GALLERY_WITH_IMAGES:
                bundle.putInt("type", IMAGES);
                break;
            case GALLERY_WITH_VIDEOS:
                bundle.putInt("type", VIDEOS);
                break;
            case CUSTOM_IMAGES:
                loadCustomImages();
                return;
        }
        photoresenter.initialized(useCursorLoader, bundle);
    }

    private void loadCustomImages() {
        if (customPaths != null && customPaths.size() > 0) {
            PhotoDirectory photoDirectory = new PhotoDirectory();
            photoDirectory.setId("CUSTOM");
            photoDirectory.setName("All Photos");
            for (int i = 0; i < customPaths.size(); i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoDirectory.addPhoto(i, null, Uri.parse(customPaths.get(i)));
                } else
                    photoDirectory.addPhoto(i, customPaths.get(i), null);
            }
            if (photoDirectory.getPhotoPaths().size() > 0) {
                photoDirectory.setCoverPath(photoDirectory.getPhotoPaths().get(0));
            }
            List<PhotoDirectory> photoDirectories = new ArrayList<>();
            photoDirectories.add(photoDirectory);
            showPhotosView(photoDirectories);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults != null && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialized(useCursorLoader, currentType);
            }

        }
    }

    @Override
    public void showPhotosView(List<PhotoDirectory> photoDirectories) {
        thumbPhotoAdapter.addData(photoDirectories.get(0).getPhotos());
        albumPopupWindow.addData(photoDirectories);
        if (progressSetter != null) {
            progressSetter.stopProgressBar();
        }
    }

    /**
     * Ako hocemo da hvatamo evente kao sto su selektovanje, deselektovanje, dostizanje maksimalnog broja elemenata itd, treba napraviti listener
     *
     * @param listener - klasa koja implementira @IItemsListener
     */
    public void setupItemsClicksListener(ThumbPhotoAdapter.IItemsListener listener) {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.setListener(listener);
        }
    }

    public void setupCustomItemsListener(ThumbPhotoAdapter.ICustomComponentListener listener) {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.setCustomComponentListener(listener);
        }
    }

    @Override
    public void showException(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void setProgressSetter(IProgressSetter progressSetter) {
        this.progressSetter = progressSetter;
    }

    IProgressSetter progressSetter;

    public interface IProgressSetter {

        void startProgressBar();

        void stopProgressBar();
    }

    /**
     * Ovu funkciju je vazno pozvati u onStop Activity-a koji je prikazao galeriju kako bi se ocistila memorija
     */
    public void stop() {
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.getSelectedImages().clear();
            thumbPhotoAdapter.clearAdapterOnStop();
            thumbPhotoAdapter.items.clear();
        }
    }

    int counter;

    /**
     * Ako zelimo da u galeriji imamo native adove, moramo da ih ubacimo u galeriju
     */
    public void refreshNativeAds(boolean haveNative, String actionId) {
        if (nativeIsAvailable == null || nativeIsAvailable != haveNative) {
            if (haveNative) {
                counter = 0;

                thumbPhotoAdapter.aspect = 1.913f; //Aspect 796x416
                int width = getResources().getDisplayMetrics().widthPixels;
                thumbPhotoAdapter.nativeParams = new FrameLayout.LayoutParams(width, (int) (width / thumbPhotoAdapter.aspect * 1.76f));
                nativeIsAvailable = true;
                thumbPhotoAdapter.actionId = actionId;
                thumbPhotoAdapter.nativeIsAvailable = true;
                thumbPhotoAdapter.getItemsTypes();

            } else {
                nativeIsAvailable = false;
                thumbPhotoAdapter.actionId = actionId;
                thumbPhotoAdapter.nativeIsAvailable = false;
                thumbPhotoAdapter.getItemsTypes();
            }
            thumbPhotoAdapter.clickedAdId = null;
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    thumbPhotoAdapter.notifyDataSetChanged();
                    invalidate();
                }
            }, 50);
            invalidate();
        }
    }

    public NativeSettings getNativeSettings() {
        return nativeSettings;
    }

    public void setNativeSettings(NativeSettings nativeSettings) {
        this.nativeSettings = nativeSettings;
        if (thumbPhotoAdapter != null) {
            thumbPhotoAdapter.setNativeSettings(this.nativeSettings);
            thumbPhotoAdapter.notifyDataSetChanged();
        }
    }
}
