package com.malcolm.joules.models;

public class Playlist {

    public final long id;
    public final String name;
    public final int songCount;

    public Playlist() {
        this.id = -1;
        this.name = "";
        this.songCount = -1;
    }

    public Playlist(long id, String name, int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
    }
}