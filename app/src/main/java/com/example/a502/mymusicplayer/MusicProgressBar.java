package com.example.a502.mymusicplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by 502 on 2018-12-21.
 */

public class MusicProgressBar extends AsyncTask<Integer, Integer, Void>
{
    IMusicService mBinder;

    private static MusicProgressBar uniqueInstance;
    private ProgressBar progressBar;
    private TextView textView;

    boolean isPlaying=false;

    public MusicProgressBar(IMusicService binder) {
        mBinder=binder;
    }

    public static MusicProgressBar getInstance(IMusicService binder) {
        if(uniqueInstance==null)
        {
            uniqueInstance=new MusicProgressBar(binder);
        }
        return uniqueInstance;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Integer... params) {
        Log.e("num","doInBackground");
        while(true){
            try {
                if(isPlaying){
                    publishProgress();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        try{
            progressBar.setProgress(mBinder.getCurrentPosition());
            Log.e("num",mBinder.getCurrentPosition()+"");
            String time =String.format("%02d:%02d",mBinder.getCurrentPosition()/60000,(mBinder.getCurrentPosition()%60000)/1000);
            textView.setText(time);
        }catch (RemoteException e)
        {
            Log.e("log1", "onProgressUpdate Error : "+ e.getMessage());
        }
    }

    public void setIsPlaying(boolean playing) {
        isPlaying = playing;
    }

    void setProgressBar(ProgressBar p)
    {
        progressBar=p;
    }
    void setTextView(TextView t)
    {
        textView=t;
    }
}
