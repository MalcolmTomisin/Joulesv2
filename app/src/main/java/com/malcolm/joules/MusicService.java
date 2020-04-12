package com.malcolm.joules;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.malcolm.joules.models.Song;
import com.malcolm.joules.utiils.JoulesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {
    private MediaPlayer mediaPlayer;
    private ExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private int resumePosition = 0;
    private AudioManager audioManager;
    private AudioAttributes mPlayBack;
    private AudioFocusRequest focusRequest;
    //path to the audio file
    private String mediaFile;
    private String TAG = MusicService.class.getSimpleName();
    public static final String ACTION_PLAY = "com.malcolm.joules.musicservice.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.malcolm.joules.musicservice.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.malcolm.joules.musicservice.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.malcolm.joules.musicservice.ACTION_NEXT";
    public static final String ACTION_STOP = "com.malcolm.joules.musicservice.ACTION_STOP";
    private final IBinder iBinder = new LocalBinder();
    private int songIndex = 0;
    private ArrayList<Song> songsList;
    private Song activeSong;
    private JoulesUtil joulesUtil;


    public MusicService() {
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            if (resumePosition != 0)
            {mediaPlayer.seekTo(resumePosition);}
            else{playMedia();}
        }
    }

    private void skipToNext(){

    }
    private void skipToPrevious(){

    }
    private void seekPos(long pos){
        if (mediaPlayer == null)
            return;
        mediaPlayer.seekTo((int) pos);
        mediaPlayer.start();
    }

    private void initMediaPlayer () {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mPlayBack = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlayBack)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setAudioAttributes(mPlayBack);
        //TODO
        // set audio source and prepare mediaplayer
        // temp set
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(this, songsList.get(songIndex).uri);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }


    @Override
    public void onAudioFocusChange(int focusChange) {
            switch (focusChange){
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mediaPlayer == null) initMediaPlayer();
                    else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f,1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer =  null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f,0.1f);
                    break;
            }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
             result = audioManager != null ? audioManager.requestAudioFocus(focusRequest) : 0;
        }
        else {
             result = audioManager != null ? audioManager
                    .requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) : 0;
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stateBuilder = new PlaybackStateCompat.Builder();
        metadataBuilder = new MediaMetadataCompat.Builder();
        initMediaSession();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initMediaSession () {
        mediaSession = new MediaSessionCompat(this,TAG);
        this.setSessionToken(mediaSession.getSessionToken());
        PlaybackStateCompat playbackStateCompat = stateBuilder
                .setState(PlaybackStateCompat.STATE_NONE,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PREPARE)
                .build();
        mediaSession.setPlaybackState(playbackStateCompat);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return super.onMediaButtonEvent(mediaButtonEvent);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                .build());
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                        .build());
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                // TODO adjust playback state actions on realities of arraylists
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                        .build());
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                // TODO adjust playback state actions on realities of arraylists
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                                | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                                .build()
                );
            }

            @Override
            public void onStop() {
                super.onStop();
                stopMedia();
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY)
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                                | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                        .build()
                );
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                seekPos(pos);
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_PAUSED, pos, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                .build());
            }
        });
        mediaSession.setActive(true);
    }

    private void updatePlaybackStateCompat () {

    }

    private void updateMediaMetaData(){
        mediaSession.setMetadata(metadataBuilder
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.albumName)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,activeSong.artistName)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.title)
        .build()
        );
    }

    private boolean removeAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocusRequest(focusRequest);
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        String errorUnknown = "Mediaplayer error";
        switch (what) {
            case MediaPlayer.MEDIA_INFO_UNKNOWN :
                Log.d(errorUnknown, "MEDIA ERROR UNKNOWN " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.d(errorUnknown, "INPUT ERROR " + extra );
                break;

        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
      playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        public  MusicService getService(){
            return MusicService.this;
        }
    }
}
