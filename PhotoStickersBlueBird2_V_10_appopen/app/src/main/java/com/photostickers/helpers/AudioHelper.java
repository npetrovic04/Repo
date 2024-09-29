package com.photostickers.helpers;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.photostickers.R;

public class AudioHelper {

    private String TAG = getClass().getSimpleName();
    private MediaPlayer mediaPlayer;
    private boolean hasBackgroundMusic;
    public static AudioHelper instance;

    public AudioHelper(Context context){
        if(instance != null){
            return;
        }

        instance = this;
        hasBackgroundMusic = context.getResources().getBoolean(R.bool.background_music);
        mediaPlayer = MediaPlayer.create(context, R.raw.background_music);
    }

    public static AudioHelper getInstance() {
        return instance;
    }

    public void onStart(){
        if(hasBackgroundMusic){
            try {
                //mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
        }
    }

    public void onStop(){
        if(hasBackgroundMusic){
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
