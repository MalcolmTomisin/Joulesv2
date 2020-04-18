package com.malcolm.joules.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {

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

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    protected Song(Parcel in) {
        albumId = in.readLong();
        albumName = in.readString();
        artistId = in.readLong();
        artistName = in.readString();
        duration = in.readInt();
        id = in.readLong();
        title = in.readString();
        trackNumber = in.readInt();
        uri = Uri.parse(in.readString());
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(albumId);
        dest.writeLong(id);
        dest.writeLong(artistId);
        dest.writeInt(duration);
        dest.writeInt(trackNumber);
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeString(title);
        dest.writeString(uri.toString());
    }
}
