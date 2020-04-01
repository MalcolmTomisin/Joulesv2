package com.malcolm.joules.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.malcolm.joules.models.Album;
import com.malcolm.joules.utiils.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class AlbumLoader {

    public static Album getAlbum (Cursor cursor){
        Album album = new Album();
        if (cursor != null){
            if (cursor.moveToFirst())
                album = new Album(cursor.getLong(0),cursor.getString(1),
                        cursor.getString(2), cursor.getLong(3),
                        cursor.getInt(4), cursor.getInt(5));
        }
        if (cursor != null)
            cursor.close();
        return album;
    }

    public static List <Album> getAlbumsForCursor (Cursor cursor) {
        ArrayList<Album> arrayList = new ArrayList<Album>();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                arrayList.add(new Album(cursor.getLong(0), cursor.getString(1),
                        cursor.getString(2), cursor.getLong(3),
                        cursor.getInt(4), cursor.getInt(5)));
            }
        while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return arrayList;
    }

    public static Cursor makeAlbumCursor(Context context, String selection, String[] paramArrayOfString){
        final String albumSortOrder = PreferencesUtil.getInstance(context).getAlbumSortOrder();
        return context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{"_id","album", "artist", "artist_id", "numsongs", "minyear"},selection,
                paramArrayOfString, albumSortOrder);
    }
}
