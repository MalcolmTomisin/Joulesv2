package com.malcolm.joules.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.malcolm.joules.R;
import com.malcolm.joules.databinding.FragmentPlaylistBinding;
import com.malcolm.joules.dialogs.CreatePlaylistDialog;
import com.malcolm.joules.loaders.PlaylistLoader;
import com.malcolm.joules.models.Playlist;
import com.malcolm.joules.widgets.MultiViewPager;

import java.util.List;

public class PlaylistFragment extends Fragment {
    int PlaylistCount;
    FragmentStateAdapter adapter;
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

        final List<Playlist>  playlists = PlaylistLoader.getPlaylists(getActivity(),true);
        PlaylistCount = playlists.size();

        adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return PlaylistPagerFragment.newInstance(position);
            }

            @Override
            public int getItemCount() {
                return PlaylistCount;
            }
        };
        // #TODO rewrite loader with viewmodel
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_new_playlist:
                CreatePlaylistDialog.newInstance().show(getChildFragmentManager(),"CREATE PLAYLIST");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updatePlaylists(final long id) {
        final List<Playlist> playlists = PlaylistLoader.getPlaylists(getActivity(), true);
        PlaylistCount = playlists.size();
        adapter.notifyDataSetChanged();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < playlists.size(); i++) {
                    long playlistid = playlists.get(i).id;
                    if (playlistid == id) {
                        pager.setCurrentItem(i);
                        break;
                    }
                }
            }
        }, 200);

    }
}
