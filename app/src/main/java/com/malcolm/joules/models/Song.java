package com.malcolm.joules.models;

import android.net.Uri;

public class Song {

    public final long albumId;
    public final String albumName;
    public final long artistId;
    public final String artistName;
    public final int duration;
    public final long id;
    public final String title;
    public final int trackNumber;
    public final Uri uri;

    public Song() {
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";
        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
        this.trackNumber = -1;
        this.uri = null;
    }

    public Song(long id,long albumId, long artistId,String title,  String artistName,String albumName, int duration,   int trackNumber, Uri content) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.duration = duration;
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.uri = content;
    }
}
