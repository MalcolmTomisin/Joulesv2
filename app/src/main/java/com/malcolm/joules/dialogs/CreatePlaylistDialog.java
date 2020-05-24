package com.malcolm.joules.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.malcolm.joules.fragments.PlaylistFragment;
import com.malcolm.joules.models.Song;
import com.malcolm.joules.utiils.JoulesUtil;

public class CreatePlaylistDialog extends DialogFragment {
    public static CreatePlaylistDialog newInstance(Song song) {
        long[] songs;
        if (song == null) {
            songs = new long[0];
        } else {
            songs = new long[1];
            songs[0] = song.id;
        }
        return newInstance(songs);
    }

    public static CreatePlaylistDialog newInstance(long[] songList) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static CreatePlaylistDialog newInstance() {
        return newInstance((Song) null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity()).positiveText("Create").negativeText("Cancel")
                .input("Enter playlist name", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    long[] songs = getArguments().getLongArray("songs");
                    long playlistId = JoulesUtil.createPlaylist(getActivity(), input.toString());
                        if (playlistId != -1) {
                            if (songs != null && songs.length != 0)
                                JoulesUtil.addToPlaylist(getActivity(), songs, playlistId);
                            else
                                Toast.makeText(getActivity(), "Created playlist", Toast.LENGTH_SHORT).show();
                            if (getParentFragment() instanceof PlaylistFragment) {
                                ((PlaylistFragment) getParentFragment()).updatePlaylists(playlistId);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Unable to create playlist", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build();
    }
}
