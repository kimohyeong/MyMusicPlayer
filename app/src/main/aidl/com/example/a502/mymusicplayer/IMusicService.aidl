// IMusicService.aidl
package com.example.a502.mymusicplayer;

// Declare any non-default types here with import statements

interface IMusicService {
    void play(int pos);
    void replay();
    void pause();
    void next();
    void prev();

    String getCurMusicAlbumId();
    String getCurMusicArtist();
    String getCurMusicTitle();

    boolean getIsServiceStart();
    boolean getIsPlaying();

    int getDuration();
    int getCurrentPosition();

}
