package com.example.a502.mymusicplayer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by 502 on 2018-12-20.
 */

public class PlayMusicActivity extends AppCompatActivity {
    String albumImgUri = "content://media/external/audio/albumart/";
    IMusicService mBinder=MainActivity.mBinder;

    ImageView albumImg;
    TextView titleTxt;
    TextView artistTxt;
    ImageView playBtn;
    TextView durationTxt;
    TextView curDurationTxt;
    ProgressBar progressBar;

    Boolean isPlaying;

    BroadcastReceiver broadcastReceiver;
    MusicProgressBar musicProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("log1", "onCreate : PlayMusicActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_music_activity);

        albumImg=(ImageView)findViewById(R.id.albumImage);
        titleTxt=(TextView)findViewById(R.id.titleText);
        artistTxt=(TextView)findViewById(R.id.artistText);
        playBtn=(ImageView)findViewById(R.id.playBtn);
        playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
        durationTxt=(TextView)findViewById(R.id.durationTxt);
        curDurationTxt=(TextView)findViewById(R.id.curDurationTxt);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);

        setMusicInfo();

        musicProgressBar= MusicProgressBar.getInstance(mBinder);
        musicProgressBar.setProgressBar(progressBar);
        musicProgressBar.setTextView(curDurationTxt);

        IntentFilter intentFilter =new IntentFilter();
        intentFilter.addAction(MusicState.PLAY);
        intentFilter.addAction(MusicState.PAUSE);

        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(MusicState.PLAY)){
                    Log.e("log1", "onReceive : PLAY");
                    isPlaying=true;
                    musicProgressBar.setIsPlaying(isPlaying);
                    playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
                    setMusicInfo();
                }
                else if(intent.getAction().equals(MusicState.PAUSE)){
                    Log.e("log1", "onReceive : PAUSE");
                    isPlaying=false;
                    musicProgressBar.setIsPlaying(isPlaying);
                    playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

    }

    void setMusicInfo()
    {
        try{
            albumImg.setImageURI(Uri.parse(albumImgUri+mBinder.getCurMusicAlbumId()));
            titleTxt.setText(mBinder.getCurMusicTitle());
            artistTxt.setText(mBinder.getCurMusicArtist());
            progressBar.setMax(mBinder.getDuration());
            progressBar.setProgress(mBinder.getCurrentPosition());
            durationTxt.setText(String.format("%02d:%02d",mBinder.getDuration()/60000,(mBinder.getDuration()%60000)/1000));
            curDurationTxt.setText(String.format("%02d:%02d",mBinder.getCurrentPosition()/60000,(mBinder.getCurrentPosition()%60000)/1000));
            isPlaying=mBinder.getIsPlaying();

        }catch (RemoteException e)
        {
            Log.e("log1", "RemoteException: " + e.getMessage());
        }
    }

    void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.prevBtn:
                try{
                    mBinder.prev();
                }catch(RemoteException e)
                {
                    Log.e("log1", "RemoteException: " + e.getMessage());
                }
                break;
            case R.id.playBtn:
                if(!isPlaying)
                {
                    try{
                        mBinder.replay();
                    }catch(RemoteException e)
                    {
                        Log.e("log1", "RemoteException: " + e.getMessage());
                    }
                }
                else{
                    try{
                        mBinder.pause();
                    }catch(RemoteException e)
                    {
                        Log.e("log1", "RemoteException: " + e.getMessage());
                    }
                }
                break;
            case R.id.nextBtn:
                try{
                    mBinder.next();

                }catch(RemoteException e)
                {
                    Log.e("log1", "RemoteException: " + e.getMessage());
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();

    }

}
