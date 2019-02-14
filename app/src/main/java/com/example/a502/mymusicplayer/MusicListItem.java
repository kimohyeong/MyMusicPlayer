package com.example.a502.mymusicplayer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by 502 on 2018-12-18.
 */

public class MusicListItem {
    private String path ;
    private String titleTxt ;
    private String artistTxt ;
    private String albumId;

    public void setPath(String p) {
        path = p;
    }
    public void setTitle(String title) {
        titleTxt = title;
    }
    public void setArtist(String artist) {
        artistTxt = artist;
    }
    public void setAlbumId(String id) {
        albumId = id;
    }

    public String getPath() {
        return this.path ;
    }
    public String getTitle() {
        return this.titleTxt ;
    }
    public String getArtist() {
        return this.artistTxt ;
    }
    public String getAlbumId(){
        return albumId;
    }
}