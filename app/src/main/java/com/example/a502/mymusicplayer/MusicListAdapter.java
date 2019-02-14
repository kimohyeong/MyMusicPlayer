package com.example.a502.mymusicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by 502 on 2018-12-18.
 */

public class MusicListAdapter extends BaseAdapter {

    private ArrayList<MusicListItem> musicList = new ArrayList<MusicListItem>() ;
    private String albumImgUri = "content://media/external/audio/albumart/";
    public MusicListAdapter() {

    }

    @Override
    public int getCount() {
        return musicList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.music_list_item, parent, false);
        }

        ImageView albumImage = (ImageView) convertView.findViewById(R.id.albumImage) ;
        TextView titleText = (TextView) convertView.findViewById(R.id.titleText) ;
        TextView artistText = (TextView) convertView.findViewById(R.id.artistText) ;

        MusicListItem item = musicList.get(position);

        albumImage.setImageURI(Uri.parse(albumImgUri+item.getAlbumId()));
        titleText.setText(item.getTitle());
        artistText.setText(item.getArtist());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position) ;
    }

    public void addItem(String path, String title, String artist, String id) {
        MusicListItem item = new MusicListItem();

        item.setPath(path);
        item.setTitle(title);
        item.setArtist(artist);
        item.setAlbumId(id);
        musicList.add(item);
    }


}
