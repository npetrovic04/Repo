package com.gallery.saferxjava;


import org.reactivestreams.Subscriber;

import java.lang.ref.WeakReference;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class WeakSubscriberDecorator<T> implements Observer<T>
{

    private final WeakReference<Subscriber<T>> mWeakSubscriber;

    public WeakSubscriberDecorator(Subscriber<T> subscriber) {

        this.mWeakSubscriber = new WeakReference<Subscriber<T>>(subscriber);
    }

    @Override
    public void onError(Throwable e) {

        Subscriber<T> subscriber = mWeakSubscriber.get();

        if (subscriber != null) {

            subscriber.onError(e);
        }

    }
    @Override
    public void onComplete()
    {
        Subscriber<T> subscriber = mWeakSubscriber.get();

        if (subscriber != null) {

            subscriber.onComplete();
        }
    }

    @Override
    public void onSubscribe(Disposable d)
    {

    }
    @Override
    public void onNext(T t) {

        Subscriber<T> subscriber = mWeakSubscriber.get();

        if (subscriber != null) {

            subscriber.onNext(t);
        }

    }
}