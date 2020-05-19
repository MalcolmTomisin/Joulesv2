package com.malcolm.joules.loaders;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.malcolm.joules.models.Playlist;
import com.malcolm.joules.utiils.JoulesUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLoader {
    private static ArrayList<Playlist> playlists;

    public static List<Playlist> getPlaylists(Context context, boolean defaultIncluded){
        playlists = new ArrayList<>();
        if (defaultIncluded){
            makeDefaultPlaylists(context);
        }
        Cursor cursor = makePlaylistCursor(context);
        if (cursor != null && cursor.moveToFirst()){
            do {
                final long id = cursor.getLong(0);
                final String name = cursor.getString(1);
                final int songCount = JoulesUtil.getSongCountForPlaylist(context, id);

                final Playlist playlist = new Playlist(id, name, songCount);
                playlists.add(playlist);
            } while (cursor.moveToNext());
        }
        return playlists;
    }

    private static void makeDefaultPlaylists(Context context){
        final Resources resources = context.getResources();

        /* Last added list */
        final Playlist lastAdded = new Playlist(JoulesUtil.PlaylistType.LastAdded.mId,
                resources.getString(JoulesUtil.PlaylistType.LastAdded.mTitleId), -1);
        playlists.add(lastAdded);

        /* Recently Played */
        final Playlist recentlyPlayed = new Playlist(JoulesUtil.PlaylistType.RecentlyPlayed.mId,
                resources.getString(JoulesUtil.PlaylistType.RecentlyPlayed.mTitleId), -1);
        playlists.add(recentlyPlayed);

        /* Top Tracks */
        final Playlist topTracks = new Playlist(JoulesUtil.PlaylistType.TopTracks.mId,
                resources.getString(JoulesUtil.PlaylistType.TopTracks.mTitleId), -1);
        playlists.add(topTracks);
    }

    public static final Cursor makePlaylistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID,
                        MediaStore.Audio.PlaylistsColumns.NAME
                }, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }
}
