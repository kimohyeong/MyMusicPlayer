package com.example.a502.mymusicplayer;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_MUSIC;

public class MainActivity extends AppCompatActivity {
    static IMusicService mBinder;
    ServiceConnection serviceConnection;
    MediaScannerConnection mediaScanner;
    MediaScannerConnection.MediaScannerConnectionClient mediaScannerClient;

    ListView musicList;
    MusicListAdapter musicListAdapter;
    ArrayList<String> musicListIds;
    Intent serviceIntent;
    MusicProgressBar musicProgressbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("log1", "onCreate() MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicList = (ListView) findViewById(R.id.music_list);

        musicListAdapter = new MusicListAdapter();
        musicListIds = new ArrayList<>();
        serviceIntent = new Intent(this, MusicService.class);

        musicList.setAdapter(musicListAdapter);

        mediaScannerClient = new MediaScannerConnection.MediaScannerConnectionClient(){
            public void onMediaScannerConnected() {
                Log.i("log1", "onMediaScannerConnected");
                File file = Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC);
                File[] musicLists = file.listFiles(new FilenameFilter(){
                    public boolean accept(File dir, String name){
                        return name.endsWith(".mp3");

                    }
                });

                if (musicLists != null)
                {
                    for (int i = 0; i < musicLists.length ; i++)
                    {
                        mediaScanner.scanFile(musicLists[i].getAbsolutePath(), null);
                        Log.i("log1", "onMediaScannerConnected : "+ musicLists[i]+".........."+musicLists[i].getAbsolutePath());
                        getMusicListsInfo(musicLists[i].toString());
                    }

                    serviceIntent.putExtra("musicList", musicListIds);
                    bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
                }
            }

            public void onScanCompleted(String path, Uri uri) {

            }
        };

        mediaScanner = new MediaScannerConnection(this, mediaScannerClient);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }
            else
            {
                mediaScanner.connect();
            }
        }
        else{

        }

        serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.e("log1","onServiceConnected");
                mBinder=IMusicService.Stub.asInterface(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.e("log1","onServiceDisconnected");
                serviceConnection=null;
                mBinder=null;

            }
        };

        //뮤직리스트에서 onClick이벤트 설정
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                MusicListItem item = (MusicListItem) parent.getItemAtPosition(position);

                //화면바꿈
                Intent intent=new Intent(MainActivity.this, PlayMusicActivity.class);
                startActivity(intent);

                //뮤직서비스 재생
                try{
                    if(mBinder.getIsServiceStart()) {
                        serviceIntent.setAction(MusicState.START);
                        startService(serviceIntent);
                        musicProgressbar=MusicProgressBar.getInstance(mBinder);
                        musicProgressbar.execute();
                    }
                    mBinder.play(position);
                }catch(RemoteException e)
                {
                    Log.e("log1", "RemoteException: " + e.getMessage());
                }

            }
        }) ;


    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        Log.e("log1", "onDestroy() MainActivity");
        super.onDestroy();
    }


    public  void getMusicListsInfo(String path){

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
        };

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, "_data='"+path+"'", null, null);

        while(cursor.moveToNext()){
            String title= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String albumId=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            musicListIds.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)));
            musicListAdapter.addItem(path, title, artist, albumId);
            musicListAdapter.notifyDataSetChanged();
        }
        cursor.close();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("log1","onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mediaScanner.connect();
        }
    }
}

