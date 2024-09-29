package com.gallery.data.normal;


import com.gallery.model.PhotoDirectory;

import android.content.Context;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yuweichen on 15/12/8.
 */
public class PhotoObserver {

    public static Observable<List<PhotoDirectory>> getPhotos(final Context context,final boolean checkImage,final boolean showGif){
        return Observable.create(new ObservableOnSubscribe<List<PhotoDirectory>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PhotoDirectory>> subscriber) throws Exception
            {
                List<PhotoDirectory> photos = PhotoData.getPhotos(context,checkImage,showGif);
                subscriber.onNext(photos);
                subscriber.onComplete();
            }
            /*@Override
            public void call(Subscriber<? super List<PhotoDirectory>> subscriber) {
                List<PhotoDirectory> photos = PhotoData.getPhotos(context,checkImage,showGif);
                subscriber.onNext(photos);
                subscriber.onCompleted();
            }*/
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }
}
