package com.malcolm.joules.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.malcolm.joules.R;
import com.malcolm.joules.models.Playlist;

public class PlaylistPagerFragment extends Fragment {
    private static final String ARG_PAGE_NUMBER = "pageNumber";
    int[] foregroundColors = {R.color.pink_transparent,R.color.green_transparent,
            R.color.blue_transparent, R.color.red_transparent, R.color.purple_transparent};
    private int pageNumber, songCountInt;
    private int foregroundColor;
    private long firstAlbumID = -1;
    private Playlist playlist;
    private Context mContext;

    public static PlaylistPagerFragment newInstance(int pageNumber) {
        PlaylistPagerFragment fragment = new PlaylistPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_NUMBER, pageNumber);
        fragment.setArguments(bundle);
        return fragment;
    }

    // #TODO build layout and bind layout
}
