package com.malcolm.joules;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.palette.graphics.Palette;

import com.google.android.exoplayer2.ExoPlayer;
import com.malcolm.joules.models.Song;
import com.malcolm.joules.utiils.JoulesUtil;
import com.malcolm.joules.utiils.PlaybackStatus;
import com.malcolm.joules.utiils.StorageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

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
    private static final String MEDIA_ROOT_ID = "com.malcolm.joules.musicservice.media_id";
    private static final String EMPTY_ROOT_ID = "com.malcolm.joules.musicservice.empty_root";
    private static final int NOTIFICATION_ID = 345;
    private static final String CHANNEL_ID = "Joules Channel";
    private final IBinder iBinder = new LocalBinder();
    private int songIndex = -1;
    private ArrayList<Song> songsList;
    private int[] ShuffledSongsIndex;
    private Song activeSong;
    private JoulesUtil joulesUtil;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private boolean IS_SHUFFLING = false;
    private BroadcastReceiver playNewSong = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            songIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (songIndex != -1 && songIndex < songsList.size()){
                activeSong = songsList.get(songIndex);
            }else{
                stopSelf();
            }
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMediaMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(noisyReceiver, intentFilter);
    }

    public MusicService() {
    }

    private void registerPlayNewSong() {
        //TODO REGISTER PLAYNEWSONG RECIEVER
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
            if (songIndex == songsList.size() - 1){
                songIndex = 0;
                activeSong = songsList.get(songIndex);
            } else {
                activeSong = songsList.get(songIndex++);
            }

            new StorageUtil(getApplicationContext()).storeAudioIndex(songIndex);
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
    }
    private void skipToPrevious(){
        if (songIndex == 0){
            songIndex = songsList.size() - 1;
            activeSong = songsList.get(songIndex);
        } else {
            activeSong = songsList.get(songIndex--);
        }
        new StorageUtil(getApplicationContext()).storeAudioIndex(songIndex);
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
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
        if (!isValidClient(clientPackageName,clientUid))
            return new BrowserRoot(EMPTY_ROOT_ID, null);
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        if (TextUtils.equals(EMPTY_ROOT_ID, parentId)){
            result.sendResult(null);
            return;
        }
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (MEDIA_ROOT_ID.equals(parentId)){
            for (Song song : songsList ){
                mediaItems.add(new MediaBrowserCompat.MediaItem(metadataBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,song.artistName)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                        .build().getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));

            }
            result.sendResult(mediaItems);
        }
        else {
            result.sendError(null);
        }

    }

    private boolean isValidClient (String callingName, int client){
        if (client == android.os.Process.myPid() ||
                client == android.os.Process.getUidForName(callingName)){
            return true;
        }
        return callingName.startsWith("com.android");
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

        callStateListener();
        registerNoisyReceiver();
        registerPlayNewSong();
        stateBuilder = new PlaybackStateCompat.Builder();
        metadataBuilder = new MediaMetadataCompat.Builder();
        initMediaSession();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        if(phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        removeNotification();
        unregisterReceiver(noisyReceiver);
        unregisterReceiver(playNewSong);
    }

    private void initMediaSession () {
        mediaSession = new MediaSessionCompat(this,TAG);
        this.setSessionToken(mediaSession.getSessionToken());
        PlaybackStateCompat playbackStateCompat = stateBuilder
                .setState(PlaybackStateCompat.STATE_NONE,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PREPARE)
                .build();
        mediaSession.setPlaybackState(playbackStateCompat);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        updateMediaMetaData();
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
                buildNotification(PlaybackStatus.PLAYING);
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
                buildNotification(PlaybackStatus.PAUSED);
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
                updateMediaMetaData();
                buildNotification(PlaybackStatus.PLAYING);
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
                updateMediaMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                stopMedia();
                mediaSession.setPlaybackState(stateBuilder
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS| PlaybackStateCompat.ACTION_SEEK_TO
                                | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                        .build()
                );
                removeNotification();
                stopSelf();
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
        Bitmap albumArt = ImageLoader.getInstance().loadImageSync(JoulesUtil.getAlbumArtUri(activeSong.albumId).toString());
        mediaSession.setMetadata(metadataBuilder
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.albumName)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,activeSong.artistName)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.title)
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        .build()
        );
    }

    private void buildNotification(PlaybackStatus playbackStatus){
        MediaControllerCompat mediaControllerCompat = mediaSession.getController();
        MediaMetadataCompat mediaMetadataCompat = mediaControllerCompat.getMetadata();
        MediaDescriptionCompat mediaDescription = mediaMetadataCompat.getDescription();
        int notificationAction = R.drawable.exo_notification_pause;
        int PAUSE_FLAG = 1;
        int PLAY_FLAG = 0;
        int PREVIOUS_FLAG = 3;
        int NEXT_FLAG = 4;

        PendingIntent playPauseAction = null;
        if (playbackStatus == PlaybackStatus.PLAYING){
            notificationAction = R.drawable.exo_notification_pause;
            playPauseAction = playbackActionIntent(PAUSE_FLAG);
        }else if (playbackStatus == PlaybackStatus.PAUSED){
            notificationAction = R.drawable.exo_notification_play;
            playPauseAction = playbackActionIntent(PLAY_FLAG);
        }
        Bitmap albumArt =
                ImageLoader.getInstance().loadImageSync(JoulesUtil.getAlbumArtUri(activeSong.albumId).toString());
        if (albumArt == null){
            albumArt = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_launcher_foreground);
        }
        // TODO implement content pending intent to open up mediaactivity
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0,1,2,3))
                .setColor(Palette.from(albumArt)
                        .generate().getDarkVibrantColor(Color.parseColor("#403f4d")))
                .setLargeIcon(albumArt)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(activeSong.artistName)
                .setContentTitle(activeSong.title)
                .setContentInfo(activeSong.albumName)
                .setContentIntent(mediaControllerCompat.getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))
                .addAction(R.drawable.exo_icon_previous,"previous", playbackActionIntent(PREVIOUS_FLAG))
                .addAction(R.drawable.exo_icon_next, "next", playbackActionIntent(NEXT_FLAG))
                .addAction(new NotificationCompat.Action(R.drawable.exo_icon_pause,
                        "pause",MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        startForeground(NOTIFICATION_ID, builder.build());

//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//
//        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());
    }
    private void removeNotification(){
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackActionIntent(int actionNumber){
        Intent playbackAction = new Intent(this,MusicService.class);
        switch (actionNumber){
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber,playbackAction,0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this,actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction,0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        MediaControllerCompat controllerCompat = mediaSession.getController();
        MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name,importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
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

    private void callStateListener(){
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                switch (state){
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if(mediaPlayer != null){
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null){
                            if (ongoingCall){
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public class LocalBinder extends Binder {
        public  MusicService getService(){
            return MusicService.this;
        }
    }
}
