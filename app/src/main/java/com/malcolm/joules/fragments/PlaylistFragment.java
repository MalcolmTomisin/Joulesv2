package com.malcolm.joules.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.malcolm.joules.R;
import com.malcolm.joules.databinding.FragmentPlaylistBinding;
import com.malcolm.joules.widgets.MultiViewPager;

public class PlaylistFragment extends Fragment {
    int PlaylistCount;
    FragmentStatePagerAdapter adapter;
    MultiViewPager pager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPlaylistBinding binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        final ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            bar.setHomeAsUpIndicator(R.drawable.ic_home_icon_active);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("Playlists");
        }

        // #TODO rewrite loader with viewmodel
        return binding.getRoot();
    }
}
