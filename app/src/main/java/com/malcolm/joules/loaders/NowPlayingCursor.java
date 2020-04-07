package com.malcolm.joules.loaders;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

public class NowPlayingCursor extends AbstractCursor {
    private static final String[] PROJECTION = new String[]{

            BaseColumns._ID,

            MediaStore.Audio.AudioColumns.TITLE,

            MediaStore.Audio.AudioColumns.ARTIST,

            MediaStore.Audio.AudioColumns.ALBUM_ID,

            MediaStore.Audio.AudioColumns.ALBUM,

            MediaStore.Audio.AudioColumns.DURATION,

            MediaStore.Audio.AudioColumns.TRACK,

            MediaStore.Audio.AudioColumns.ARTIST_ID,

            MediaStore.Audio.AudioColumns.TRACK,
    };

    private final Context mContext;

    private long[] mNowPlaying;

    private long[] mCursorIndexes;

    private int mSize;

    private int mCurPos;

    private Cursor mQueueCursor;

    public NowPlayingCursor(Context mContext) {
        this.mContext = mContext;
       // makeNowPlayingCursor();
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String getString(int column) {
        return null;
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }

    @Override
    public boolean requery() {
        return super.requery();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return super.onMove(oldPosition, newPosition);
    }
}
