package com.example.a502.mymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.BoolRes;
import android.support.annotation.IntDef;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
/**
 * Created by 502 on 2018-12-20.
 */

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private ArrayList<String> musicList;
    private String albumImgUri = "content://media/external/audio/albumart/";
    private RemoteViews remoteView;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    int notifyID=1;

    MusicListItem item;
    int curPosition;

    boolean isServiceStart=true;
    boolean isPlaying=false;


    private IMusicService.Stub mBinder = new IMusicService.Stub(){

        @Override
        public void play(int pos) throws RemoteException {
            curPosition=pos;
            queryMusicInfo(curPosition);
            String path=item.getPath();
            isPlaying=true;
            Log.e("log1", "play(int pos) : " + curPosition + "// " +path);
            try{
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();

            }catch (Exception e)
            {
                Log.d("log1", "onStartCommand error : " +e.getMessage());
            }
        }

        @Override
        public void replay() throws RemoteException {
            Log.e("log1", "replay()");
            mediaPlayer.start();
            isPlaying=true;
            Intent intent = new Intent(MusicState.PLAY);
            sendBroadcast(intent);
            notificationManager.notify(notifyID, replayNotiBuilder().build());

        }

        @Override
        public void pause() throws RemoteException {
            Log.e("log1", "pause()" );
            mediaPlayer.pause();
            isPlaying=false;
            Intent intent = new Intent(MusicState.PAUSE);
            sendBroadcast(intent);

            notificationManager.notify(notifyID, pasueNotiBuilder().build());
        }

        @Override
        public void next() throws RemoteException {
            Log.e("log1", "next()");
            curPosition++;
            if(curPosition>musicList.size()-1)
                curPosition=0;

            queryMusicInfo(curPosition);
            String path=item.getPath();
            Log.e("log1", "play(int pos) : " + curPosition + "// " +path);
            try{
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();

            }catch (Exception e)
            {
                Log.d("log1", "onStartCommand error : " +e.getMessage());
            }
        }
        @Override
        public void prev() throws RemoteException {
            Log.e("log1", "prev()");
            curPosition--;
            if(curPosition<0)
                curPosition=musicList.size()-1;

            queryMusicInfo(curPosition);
            String path=item.getPath();
            Log.e("log1", "play(int pos) : " + curPosition + "// " +path);
            try{
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();

            }catch (Exception e)
            {
                Log.d("log1", "onStartCommand error : " +e.getMessage());
            }
        }

        @Override
        public String getCurMusicAlbumId() throws RemoteException {
            return item.getAlbumId();
        }

        @Override
        public String getCurMusicArtist() throws RemoteException {
            return item.getArtist();
        }

        @Override
        public String getCurMusicTitle() throws RemoteException {
            return item.getTitle();
        }


        @Override
        public boolean getIsServiceStart() throws RemoteException {
            return isServiceStart;
        }

        @Override
        public boolean getIsPlaying() throws RemoteException {
            return isPlaying;
        }

        @Override
        public int getDuration() throws RemoteException {
            return mediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mediaPlayer.getCurrentPosition();
        }

    };

    @Override
    public void onCreate() {
        Log.e("log1","onCreate Service");
        super.onCreate();

        musicList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        remoteView=new RemoteViews(getPackageName(), R.layout.music_notification_activity);
        builder=new NotificationCompat.Builder(this);

        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {    //prepare동작이 완료되었을때
                Log.e("log1","onPrepared");
                mp.start();

                Intent intent = new Intent(MusicState.PLAY);
                sendBroadcast(intent);

                notificationManager.notify(notifyID, getNotiBuilder().build());
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {   //끝까지 재생했을때
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("log1","onCompletion");

                try{
                    mBinder.next();

                }catch (RemoteException e){
                    Log.e("log1", "setOnCompletionListener Error : "+e.getMessage());
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() { //에러발생했을때
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("log1","onError");
                return false;
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {   //재생 위치변경이 완료되었을때
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.e("log1","onSeekComplete");
            }
        });
    }

    void queryMusicInfo(int index)
    {
        String id=musicList.get(index);

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };

        String selection = MediaStore.Audio.Media._ID+"="+id;
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, selection, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                item = new MusicListItem();
                item.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                item.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                item.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                item.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

            }
            cursor.close();
        }
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        musicList=intent.getStringArrayListExtra("musicList");
        Log.e("log1","start : "+ musicList.size());
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.e("log1","onDestroy");
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("log1","onStartCommand");
        isServiceStart=false;
        if(intent.getAction().equals(MusicState.START))
        {
            startForeground(notifyID, getNotiBuilder().build());
        }
        else if(intent.getAction().equals(MusicState.PAUSE))
        {
            try
            {
                mBinder.pause();
            }catch (RemoteException e)
            {
                Log.e("log1", "remote Exception PLAY : " +  e.getMessage());
            }
        }
        else if(intent.getAction().equals(MusicState.PLAY))
        {
            try
            {
                mBinder.replay();
            }catch (RemoteException e)
            {
                Log.e("log1", "remote Exception PLAY : " +  e.getMessage());
            }
        }
        else if(intent.getAction().equals(MusicState.PREV))
        {
            try
            {
                mBinder.prev();
            }catch (RemoteException e)
            {
                Log.e("log1", "remote Exception PREV : " +  e.getMessage());
            }
        }
        else if(intent.getAction().equals(MusicState.NEXT))
        {
            try
            {
                mBinder.next();
            }catch (RemoteException e)
            {
                Log.e("log1", "remote Exception NEXT : " +  e.getMessage());
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    NotificationCompat.Builder getNotiBuilder()
    {
        Intent notiIntent =new Intent(this, PlayMusicActivity.class);
        PendingIntent albumImgPendingIntent=PendingIntent.getActivity(this,0,notiIntent, 0);
        PendingIntent PauseBtnPendingIntent=PendingIntent.getService(this, 0, new Intent(this, MusicService.class).setAction(MusicState.PAUSE), 0);
        PendingIntent prevBtnPendingIntent=PendingIntent.getService(this, 0, new Intent(this, MusicService.class).setAction(MusicState.PREV), 0);
        PendingIntent nextBtnPendingIntent=PendingIntent.getService(this, 0, new Intent(this, MusicService.class).setAction(MusicState.NEXT), 0);

        remoteView.setTextViewText(R.id.titleText, item.getTitle());
        remoteView.setTextViewText(R.id.artistText, item.getArtist());
        remoteView.setImageViewResource(R.id.playBtn, R.drawable.ic_pause_white_24dp);
        remoteView.setImageViewUri(R.id.albumImage, Uri.parse(albumImgUri+item.getAlbumId()));

        remoteView.setOnClickPendingIntent(R.id.albumImage,albumImgPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.playBtn, PauseBtnPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.prevBtn, prevBtnPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.nextBtn, nextBtnPendingIntent);

        builder.setSmallIcon(R.drawable.ic_pause_circle_outline_white_24dp);
        builder.setCustomBigContentView(remoteView);

        return builder;

    }
    NotificationCompat.Builder pasueNotiBuilder()
    {
        PendingIntent playBtnPendingIntent=PendingIntent.getService(this, 0, new Intent(this, MusicService.class).setAction(MusicState.PLAY), 0);
        remoteView.setOnClickPendingIntent(R.id.playBtn, playBtnPendingIntent);
        remoteView.setImageViewResource(R.id.playBtn, R.drawable.ic_play_arrow_white_24dp);
        builder.setCustomBigContentView(remoteView);
        builder.setSmallIcon(R.drawable.ic_play_circle_outline_white_24dp);

        return builder;

    }

    NotificationCompat.Builder replayNotiBuilder()
    {
        PendingIntent pauseBtnPendingIntent=PendingIntent.getService(this, 0, new Intent(this, MusicService.class).setAction(MusicState.PAUSE), 0);
        remoteView.setOnClickPendingIntent(R.id.playBtn, pauseBtnPendingIntent);
        remoteView.setImageViewResource(R.id.playBtn, R.drawable.ic_pause_white_24dp);
        builder.setCustomBigContentView(remoteView);
        builder.setSmallIcon(R.drawable.ic_pause_circle_outline_white_24dp);

        return builder;

    }




}
