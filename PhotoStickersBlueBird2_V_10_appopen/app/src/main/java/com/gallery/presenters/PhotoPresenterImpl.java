package com.gallery.presenters;

import com.gallery.data.loader.MediaStoreHelper;
import com.gallery.data.normal.PhotoObserver;
import com.gallery.model.PhotoDirectory;
import com.gallery.views.PhotoView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


/**
 * Created by yuweichen on 15/12/9.
 */
public class PhotoPresenterImpl extends SafePresenter<PhotoView>
{

    public AppCompatActivity context;
    public PhotoPresenterImpl(AppCompatActivity context, PhotoView photoView) {
        super(photoView);
        this.context = context;
    }

    @Override
    public void initialized(Object... objects) {

        boolean useCursorLoader = (boolean) objects[0];
        Bundle bundle = (Bundle) objects[1];

        if(useCursorLoader){
            getPhotosByLoader(bundle);
        }else{
            boolean checkImage = true;// bundle.getBoolean(PickConfig.EXTRA_CHECK_IMAGE,PickConfig.DEFALUT_CHECK_IMAGE);
            boolean showGif = false;// bundle.getBoolean(PickConfig.EXTRA_SHOW_GIF,PickConfig.DEFALUT_SHOW_GIF);
            PhotoObserver.getPhotos(context,checkImage,showGif).subscribe(safeSubscriber(albumSubcriber));
//            PhotoObserver.getPhotos(context,checkImage,showGif).sub((Observer<? super List<PhotoDirectory>>) safeSubscriber(albumSubcriber));
        }
    }

    public void getPhotosByLoader(Bundle args){
        MediaStoreHelper.getPhotoDirs(context, args, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> directories) {
                PhotoView photoView = getView();
                if(photoView!=null){
                    photoView.showPhotosView(directories);
                }
            }
        });
    }


    Subscriber<List<PhotoDirectory>> albumSubcriber = new Subscriber<List<PhotoDirectory>>() {


        @Override
        public void onError(Throwable e) {
            PhotoView photoView = getView();
            if(photoView!=null){
                photoView.showException(e.getMessage());
            }
        }
        @Override
        public void onComplete()
        {

        }

        @Override
        public void onSubscribe(Subscription s)
        {

        }
        @Override
        public void onNext(List<PhotoDirectory> albumInfos) {
           PhotoView photoView = getView();
           if(photoView!=null){
               photoView.showPhotosView(albumInfos);
           }
        }
    };

}
