package com.malcolm.joules.models;

public class Album {
    public final long artistId;
    public final String artistName;
    public final long id;
    public final int songCount;
    public final String title;
    public final int year;

    public Album() {
        this.id = -1;
        this.title = "";
        this.artistName = "";
        this.artistId = -1;
        this.songCount = -1;
        this.year = -1;
    }

    public Album(long id, String title, String artistName, long artistId, int songCount, int year) {
        this.id = id;
        this.artistId = artistId;
        this.artistName = artistName;
        this.songCount = songCount;
        this.title = title;
        this.year = year;
    }
}
