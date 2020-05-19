package com.malcolm.joules.utiils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.malcolm.joules.R;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class JoulesUtil {
    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
    public static Uri getAlbumArtUri(long albumId){
        if (ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId) != null)
            return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);

        Context context = null;
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        tempDir.mkdir();
        String albumArtTitle = "albumart";
        File tempFile = null;
        try {
            tempFile = File.createTempFile(albumArtTitle,".png",tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap defaultBitMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.exo_icon_vr);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        defaultBitMap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        byte[] bitMapData = bytes.toByteArray();

        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitMapData);
            fos.flush();
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }

    public static ArrayList shuffleAudioList(int startIndex, int lastIndex) {
        int size = lastIndex - startIndex + 1;
        int[] array = new int[size];

        for(int i = 0; i < size; i++){
            array[i] = i;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ArrayList songsIndexList = new ArrayList(Arrays.asList(Arrays.stream(array)
            .boxed()
            .toArray(Integer[]::new)
            ));
            Collections.shuffle(songsIndexList);
            return songsIndexList;
        }

        ArrayList songsIndexList = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(array))) ;
        Collections.shuffle(songsIndexList);
        return  songsIndexList;
    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }

        return 0;
    }

    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }
    public enum PlaylistType {
        LastAdded(-1, R.string.playlist_last_added),
        RecentlyPlayed(-2, R.string.playlist_recently_played),
        TopTracks(-3, R.string.playlist_top_tracks);

        public long mId;
        public int mTitleId;

        PlaylistType(long id, int titleId) {
            mId = id;
            mTitleId = titleId;
        }

        public static PlaylistType getTypeById(long id) {
            for (PlaylistType type : PlaylistType.values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            return null;
        }
    }
}
