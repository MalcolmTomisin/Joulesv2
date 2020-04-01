package com.malcolm.joules.models;

public class Artist {
    public final int albumCount;
    public final long id;
    public final String name;
    public final int songCount;

    public Artist() {
        this.id = -1;
        this.name = "";
        this.songCount = -1;
        this.albumCount = -1;
    }

    public Artist(long id, String name, int songCount, int albumCount) {
        this.albumCount = albumCount;
        this.id = id;
        this.name = name;
        this.songCount = songCount;
    }
}
