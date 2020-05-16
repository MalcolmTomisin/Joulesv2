package com.malcolm.joules.utiils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public final class PreferencesUtil {
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";
    private static final String NOW_PLAYING_SELECTOR = "now_paying_selector";
    private static final String TOGGLE_ANIMATIONS = "toggle_animations";
    private static final String TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations";
    private static final String TOGGLE_ARTIST_GRID = "toggle_artist_grid";
    private static final String TOGGLE_ALBUM_GRID = "toggle_album_grid";
    private static final String TOGGLE_HEADPHONE_PAUSE = "toggle_headphone_pause";
    private static final String THEME_PREFERNCE = "theme_preference";
    private static final String START_PAGE_INDEX = "start_page_index";
    private static final String START_PAGE_PREFERENCE_LASTOPENED = "start_page_preference_latopened";
    private static final String NOW_PLAYNG_THEME_VALUE = "now_playing_theme_value";
    private static final String TOGGLE_XPOSED_TRACKSELECTOR = "toggle_xposed_trackselector";
    private static PreferencesUtil sInstance;

    private static SharedPreferences mPreferences;

    public PreferencesUtil (final Context context){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferencesUtil getInstance(final Context context){
        if (sInstance == null){
            sInstance = new PreferencesUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setOnSharedPreferenceChangeListener (SharedPreferences.OnSharedPreferenceChangeListener listener){
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private static void setSortOrder (final String key, final String value){
         final AsyncTask<Void, Void, Void> asyncTask;
        asyncTask = new AsyncTask<Void, Void, Void>() {

           @Override
           protected Void doInBackground(Void... voids) {
               final SharedPreferences.Editor editor = mPreferences.edit();
               editor.putString(key, value);
               editor.apply();
               return null;
           }
       };
        asyncTask.execute();
    }

    public final String getArtistSortOrder (){
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }
    public void setArtistSortOrder(final String value) {
        setSortOrder(ARTIST_SORT_ORDER, value);
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public void setArtistSongSortOrder(final String value) {
        setSortOrder(ARTIST_SONG_SORT_ORDER, value);
    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
    }

    public void setArtistAlbumSortOrder(final String value) {
        setSortOrder(ARTIST_ALBUM_SORT_ORDER, value);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(final String value) {
        setSortOrder(ALBUM_SORT_ORDER, value);
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public void setAlbumSongSortOrder(final String value) {
        setSortOrder(ALBUM_SONG_SORT_ORDER, value);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void setSongSortOrder(final String value) {
        setSortOrder(SONG_SORT_ORDER, value);
    }

    public boolean getAnimations(){return mPreferences.getBoolean(TOGGLE_ANIMATIONS, true);}

    public boolean isArtistsInGrid(){return mPreferences.getBoolean(TOGGLE_ARTIST_GRID, true);}

    public static void setArtistsInGrid(final boolean b){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(TOGGLE_ARTIST_GRID, b);
                editor.apply();
                return null;
            }
        }.execute();
    }

    public boolean isAlbumsInGrid() {
        return mPreferences.getBoolean(TOGGLE_ALBUM_GRID, true);
    }

    public static void setAlbumsInGrid(final boolean b){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(TOGGLE_ALBUM_GRID, b);
                editor.apply();
                return null;
            }
        }.execute();
    }

    public int getStartPageIndex() {
        return mPreferences.getInt(START_PAGE_INDEX, 0);
    }

    public static void setStartPageIndex(final int index){
      new AsyncTask<Void,Void,Void>(){
          @Override
          protected Void doInBackground(Void... voids){
              final SharedPreferences.Editor editor = mPreferences.edit();
              editor.putInt(START_PAGE_INDEX, index);
              editor.apply();
              return null;
          }

        }.execute();
    }

    public void setLastOpenedAsStartPagePreference(boolean preference) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(START_PAGE_PREFERENCE_LASTOPENED, preference);
        editor.apply();
    }

    public boolean lastOpenedIsStartPagePreference() {
        return mPreferences.getBoolean(START_PAGE_PREFERENCE_LASTOPENED, true);
    }


}

