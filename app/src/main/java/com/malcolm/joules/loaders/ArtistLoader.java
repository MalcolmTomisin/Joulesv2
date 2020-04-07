package com.malcolm.joules.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.malcolm.joules.models.Artist;
import com.malcolm.joules.utiils.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader {
    public static Cursor makeArtistCursor(Context context, String selection, String[] paramArrayOfString){
        final String artistSortOrder = PreferencesUtil.getInstance(context).getArtistSortOrder();
        return context.getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{"_id", "artist", "number_of_albums"},
                selection, paramArrayOfString, artistSortOrder);
    }
    public static Artist getArtist (Cursor cursor) {
        Artist artist = new Artist();
        if (cursor != null) {
            if (cursor.moveToFirst())
                artist = new Artist(cursor.getLong(0), cursor.getString(1),
                        cursor.getInt(2),cursor.getInt(3));
        }
        if (cursor != null)
            cursor.close();
        return artist;
    }
    public static List<Artist> getArtistsForCursor(Cursor cursor){
        ArrayList <Artist>arrayList = new ArrayList<Artist>();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                arrayList.add(new Artist(cursor.getLong(0), cursor.getString(1),
                        cursor.getInt(2),cursor.getInt(3)));
            } while (cursor.moveToNext());
            if (cursor != null)
                cursor.close();
            return arrayList;
    }

    public static List<Artist> getAllArtists(Context context) {
        return getArtistsForCursor(makeArtistCursor(context, null, null));
    }

    public static Artist getArtist(Context context, long id){
        return getArtist(makeArtistCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }
    public static List<Artist> getArtists(Context context, String paramString) {
        return getArtistsForCursor(makeArtistCursor(context,"artist LIKE ?", new String[]{"%" + paramString + "%"}));
    }
}
