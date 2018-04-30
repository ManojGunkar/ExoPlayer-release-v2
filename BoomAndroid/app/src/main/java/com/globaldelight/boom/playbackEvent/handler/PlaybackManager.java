package com.globaldelight.boom.playbackEvent.handler;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.playbackEvent.controller.callbacks.IUpNextMediaEvent;
import com.globaldelight.boom.radio.webconnector.RadioRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.model.RadioPlayResponse;
import com.globaldelight.boom.utils.helpers.DropBoxAPI;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import com.globaldelight.boom.player.AudioEffect;
import com.globaldelight.boom.player.AudioPlayer;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Response;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlaybackManager implements IUpNextMediaEvent, AudioManager.OnAudioFocusChangeListener, Observer {
    private static final String TAG = "PlaybackManager";
    public static boolean isLibraryResumes = false;

    private static final int NEXT = 0;
    private static final int PREVIOUS = 1;


    private ArrayList<Listener> mListeners = new ArrayList<>();
    private IMediaElement playingItem;
    private AudioPlayer mPlayer;
    private Context context;
    private AudioManager audioManager;
    private MediaSession session;
    private GoogleDriveHandler googleDriveHandler;
    private Handler mHandler;
    private UpNextPlayingQueue mQueue;
    private int skipDirection;
    private PlayingItemChanged mItemChangeTask = null;


    AudioPlayer.Callback IPlayerEvents = new AudioPlayer.Callback() {

        @Override
        public void onStateChange(@AudioPlayer.State int state) {
            switch (state) {
                case AudioPlayer.LOADING:
                    notifyMediaChanged();
                    if(null != getPlayingItem() && getPlayingItem().getMediaType() != MediaType.DEVICE_MEDIA_LIB){
                        notifyPlayerStateChanged();
                    }
                    break;

                case AudioPlayer.PLAYING:
                    notifyPlayerStateChanged();
                    break;

                case AudioPlayer.PAUSED:
                    notifyPlayerStateChanged();
                    break;

                case AudioPlayer.STOPPED:
                    notifyPlayerStateChanged();
                    break;
            }

        }

        @Override
        public void onPlayTimeUpdate(final long currentms, final long totalms) {
            notifyPositionUpdate();
        }

        @Override
        public void onComplete() {
            playNextSong(false);
        }


        @Override
        public void onError() {
            if(isNext() && skipDirection == NEXT) {
                playNextSong(false);
            }else if(isPrevious() && skipDirection == PREVIOUS) {
                playPrevSong();
            }
            notifyError();
        }
    };

    private MediaSession.Callback mediaSessionCallback = new MediaSession.Callback(){
        @Override
        public void onPlay() {
            if ( mPlayer.getDataSourceId() == null ) {
                onPlayingItemChanged();
                return;
            }

            setSessionState(PlaybackState.STATE_PLAYING);
        }

        @Override
        public void onPause() {
            setSessionState(PlaybackState.STATE_PAUSED);
        }

        @Override
        public void onSkipToNext() {
            playNextSong(true);
        }

        @Override
        public void onSkipToPrevious() {
            playPrevSong();
        }

        @Override
        public void onStop() {
            setSessionState(PlaybackState.STATE_STOPPED);
        }
    };


    private static  PlaybackManager instance = null;
    public static PlaybackManager getInstance(Context context) {
        if ( instance == null ) {
            instance = new PlaybackManager(context.getApplicationContext());
        }
        return instance;
    }

    private PlaybackManager(Context context){
        this.context = context;
        mPlayer = new AudioPlayer(context, IPlayerEvents);
        mQueue = new UpNextPlayingQueue(context);
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mQueue.setUpNextMediaEvent(this);
        googleDriveHandler = new GoogleDriveHandler(context);
        googleDriveHandler.connectToGoogleAccount();
        mHandler = new Handler();
        AudioEffect.getInstance(context).addObserver(this);
        registerSession();
    }


    public UpNextPlayingQueue queue() {
        return mQueue;
    }

    public void registerListener(Listener listener) {
        if ( mListeners.indexOf(listener) == -1) {
            mListeners.add(listener);
        }

    }

    public void unregisterListener(Listener listener) {
        if ( mListeners.indexOf(listener) != -1) {
            mListeners.remove(listener);
        }
    }

    private void notifyMediaChanged() {
        for ( Listener aListener: mListeners ) {
            aListener.onMediaChanged();
        }
    }

    private void notifyPlayerStateChanged() {
        for ( Listener aListener: mListeners ) {
            aListener.onPlayerStateChanged();
        }
    }

    private void notifyError() {
        for ( Listener aListener: mListeners ) {
            aListener.onPlayerError();
        }
    }

    private void notifyComplete() {
        for ( Listener aListener: mListeners ) {
            aListener.onPlaybackCompleted();
        }
    }

    private void notifyPositionUpdate() {
        for ( Listener aListener: mListeners ) {
            aListener.onUpdatePlayerPosition();
        }
    }



    public String getPlayerDataSourceId(){
        return mPlayer.getDataSourceId();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public boolean isPaused(){
        return mPlayer.isPause();
    }

    public boolean isLoading() {
        return mPlayer.isLoading();
    }

    public boolean isStopped() {
        return mPlayer.isStopped();
    }

    public long getDuration() {
        return Math.max(0, mPlayer.getDuration());
    }

    public long getPosition() {
        return Math.max(0,mPlayer.getCurrentPosition());
    }

    @Override
    public void onPlayingItemChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                changePlayingItem();
            }
        });
    }

    private void changePlayingItem() {
        if ( isPlaying() ) {
            setSessionState(PlaybackState.STATE_PAUSED);
        }

        notifyMediaChanged();
        playingItem = mQueue.getPlayingItem();

        if ( mItemChangeTask != null && mItemChangeTask.getStatus() != AsyncTask.Status.FINISHED ) {
            mItemChangeTask.cancel(true);
            mItemChangeTask = null;
        }

        if ( playingItem != null ) {
            mItemChangeTask = new PlayingItemChanged();
            mItemChangeTask.execute(playingItem);
        }
        else {
            setSessionState(PlaybackState.STATE_STOPPED);
        }
    }

    public boolean isTrackWaiting() {
        return (mItemChangeTask != null && mItemChangeTask.getStatus() != AsyncTask.Status.FINISHED);
    }

    public boolean isTrackLoading(){
        return isLoading() || isTrackWaiting();
    }

    public boolean isTrackPlaying() {
        return isPlaying() || isTrackLoading();
    }

    @Override
    public void update(Observable o, Object arg) {
        String changed = (String)arg;
        AudioEffect effect = AudioEffect.getInstance(context);

        switch (changed) {
            case AudioEffect.HEAD_PHONE_TYPE_PROPERTY:
                mPlayer.setHeadPhone(effect.getHeadPhoneType());
                break;
            case AudioEffect.AUDIO_EFFECT_PROPERTY:
                mPlayer.setEnableEffect(effect.isAudioEffectOn());
                break;
            case AudioEffect.SURROUND_SOUND_PROPERTY:
                mPlayer.setEnable3DAudio(effect.is3DSurroundOn());
                break;
            case AudioEffect.FULL_BASS_PROPERTY:
                mPlayer.setEnableSuperBass(effect.isFullBassOn());
                break;
            case AudioEffect.INTENSITY_STATE_PROPERTY:
                mPlayer.setIntensity(effect.isIntensityOn()? effect.getIntensity() : 0);
                break;
            case AudioEffect.INTENSITY_PROPERTY:
                mPlayer.setIntensity(effect.getIntensity());
                break;
            case AudioEffect.EQUALIZER_STATE_PROPERTY:
                mPlayer.setEqualizerGain(effect.isEqualizerOn()? effect.getSelectedEqualizerPosition() : 7);
                break;
            case AudioEffect.EQUALIZER_PROPERTY:
                mPlayer.setEqualizerGain(effect.getSelectedEqualizerPosition());
                break;
            case AudioEffect.SPEAKER_LEFT_FRONT_PROPERTY:
                mPlayer.setSpeakerEnable(AudioEffect.SPEAKER_FRONT_LEFT, effect.isLeftFrontSpeakerOn());
                break;
            case AudioEffect.SPEAKER_RIGHT_FRONT_PROPERTY:
                mPlayer.setSpeakerEnable(AudioEffect.SPEAKER_FRONT_RIGHT, effect.isRightFrontSpeakerOn());
                break;
            case AudioEffect.SPEAKER_LEFT_SURROUND_PROPERTY:
                mPlayer.setSpeakerEnable(AudioEffect.SPEAKER_SURROUND_LEFT, effect.isLeftSurroundSpeakerOn());
                break;
            case AudioEffect.SPEAKER_RIGHT_SURROUND_PROPERTY:
                mPlayer.setSpeakerEnable(AudioEffect.SPEAKER_SURROUND_RIGHT, effect.isRightSurroundSpeakerOn());
                break;
            case AudioEffect.SPEAKER_TWEETER_PROPERTY:
                mPlayer.setSpeakerEnable(AudioEffect.SPEAKER_TWEETER, effect.isTweeterOn());
                break;
            case AudioEffect.SPEAKER_WOOFER_PROPERTY:
                mPlayer.setSpeakerEnable(AudioEffect.SPEAKER_WOOFER, effect.isWooferOn());
                break;
        }

    }

    private Bitmap getAlbumart(Context context, String album_id) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {

            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(album_id));

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
            }
        } catch (Error ee) {
        } catch (Exception e) {
        }

        if (null != albumArtBitMap) {
            return albumArtBitMap;
        }
        return null;
    }


    private class PlayingItemChanged extends AsyncTask<IMediaElement, Void, String>{
        IMediaElement mediaItemBase;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(IMediaElement... params) {
            String dataSource = null;
            mediaItemBase = params[0];
            if(mediaItemBase.getMediaType() == MediaType.DEVICE_MEDIA_LIB){
                dataSource = ((MediaItem)mediaItemBase).getItemUrl();
            }else if(mediaItemBase.getMediaType() == MediaType.DROP_BOX){
                return DropBoxAPI.getInstance(context).getStreamingUrl(((MediaItem)mediaItemBase).getItemUrl());
            }else if(mediaItemBase.getMediaType() == MediaType.GOOGLE_DRIVE){
                String access_token = googleDriveHandler.getAccessTokenApi();
                if(null != access_token) {
                    return ((MediaItem)mediaItemBase).getItemUrl() + access_token;
                }
                return null;
            }
            else if(mediaItemBase.getMediaType() == MediaType.RADIO) {
                RadioRequestController.RequestCallback requestCallback = null;
                try {
                    requestCallback = RadioRequestController.getClient(context, RadioApiUtils.BASE_URL);
                    Call<RadioPlayResponse> call = requestCallback.getRadioPlayService(mediaItemBase.getId());
                    Response<RadioPlayResponse> resp = call.execute();
                    if ( resp.isSuccessful() ) {
                        List<RadioPlayResponse.Stream> streams = resp.body().getBody().getContent().getStreams();
                        if ( streams.size() > 0 ) {
                            return streams.get(0).getUrl();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;

            }
            return dataSource;
        }

        @Override
        protected void onPostExecute(String dataSource) {
            super.onPostExecute(dataSource);
            if( null != mediaItemBase && null != dataSource) {
                if ( requestAudioFocus() ) {
                    mPlayer.setPath(dataSource);
                    mPlayer.setDataSourceId(mediaItemBase.getId());

                    if ( mediaItemBase instanceof  MediaItem ) {
                        MediaItem item = (MediaItem)mediaItemBase;

                        MediaMetadata.Builder builder = new MediaMetadata.Builder();
                        builder.putString(MediaMetadata.METADATA_KEY_TITLE, item.getTitle());
                        builder.putString(MediaMetadata.METADATA_KEY_ALBUM, item.getItemAlbum());
                        builder.putString(MediaMetadata.METADATA_KEY_ARTIST, item.getItemArtist());
                        Bitmap bitmap = getAlbumart(context,item.getItemAlbumId());
                        if ( bitmap != null ) {
                            builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap);
                        }
                        session.setMetadata(builder.build());
                    }

                    setSessionState(PlaybackState.STATE_PLAYING);
                }
            }

            if ( mediaItemBase == null || dataSource == null )
            {
                mPlayer.setPath(null);
                mPlayer.setDataSourceId(null);
                setSessionState(PlaybackState.STATE_STOPPED);
                if ( ConnectivityReceiver.isNetworkAvailable(context, true) ) {
                    Toast.makeText(context, context.getResources().getString(R.string.loading_problem), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void playNextSong(boolean isUser) {
        skipDirection = NEXT;
        if ( isNext() || mQueue.repeatSong() ) {
            mQueue.setNextPlayingItem(isUser);
        }
        else {
            setSessionState(PlaybackState.STATE_STOPPED);
            notifyComplete();
        }
    }

    public void playPrevSong() {
        skipDirection = PREVIOUS;
        mQueue.setPreviousPlayingItem();
    }

    @Override
    public void onPlayingItemClicked() {
        playPause();
    }

    @Override
    public void onQueueUpdated() {
        for (Listener aListener: mListeners ) {
            aListener.onQueueUpdated();
        }
    }

    public void playPause() {
        if ( mPlayer.getDataSourceId() == null ) {
            onPlayingItemChanged();
            return;
        }

        if(isPlaying()){
            setSessionState(PlaybackState.STATE_PAUSED);
        } else {
            if ( requestAudioFocus() ) {
                setSessionState(PlaybackState.STATE_PLAYING);
            }
            else {
                setSessionState(PlaybackState.STATE_STOPPED);
            }
        }
    }

    public void stop() {
        setSessionState(PlaybackState.STATE_STOPPED);
    }

    public IMediaElement getPlayingItem() {
        return (IMediaElement) mQueue.getPlayingItem();
    }

    public void seek(final int progress) {
        if( !isTrackWaiting() ){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlayer.seek(progress);
                }
            });
        }
    }

    public boolean resetShuffle() {
        return mQueue.resetShuffle();
    }

    public boolean resetRepeat() {
        return mQueue.resetRepeat();
    }

    public boolean isPrevious() {
        return mQueue.isPrevious();
    }

    public boolean isNext() {
        return mQueue.isNext();
    }

    void registerSession() {
        if ( session == null ) {
            session = new MediaSession(context, context.getPackageName());
            session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            setSessionState(PlaybackState.STATE_NONE);
            session.setCallback(mediaSessionCallback);
        }
        session.setActive(true);
    }


    void setSessionState(int state)
    {
        long actions = PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS ;
        if ( state == PlaybackState.STATE_PLAYING ) {
            actions |= (PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_STOP);
        }
        else {
            actions |= PlaybackState.ACTION_PLAY;
        }

        PlaybackState pbState = new PlaybackState.Builder()
                .setActions(actions)
                .setState(state, 0, 1, 0)
                .build();
        session.setPlaybackState(pbState);
        switch ( state ) {
            case PlaybackState.STATE_PLAYING:
                mPlayer.play();
                break;

            case PlaybackState.STATE_PAUSED:
                mPlayer.pause();
                break;

            case PlaybackState.STATE_STOPPED:
                mPlayer.stop();
                break;
        }
    }

    public boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            session.setActive(true);
            return true;
        }

        return false;
    }

    public void releaseAudioFocus() {
        audioManager.abandonAudioFocus(this);
        session.setActive(false);
    }

    private static final float VOLUME_DUCK = 0.05f;
    private static final float VOLUME_FULL = 1.0f;
    private boolean mIsVolumeDucked = false;

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch ( focusChange ) {
            case AUDIOFOCUS_LOSS_TRANSIENT:
                if ( isPlaying() ) {
                    mPlayer.setVolume(VOLUME_DUCK);
                    mIsVolumeDucked = true;
                }
                break;

            case AUDIOFOCUS_GAIN_TRANSIENT:
                mPlayer.setVolume(VOLUME_FULL);
                break;

            case AUDIOFOCUS_LOSS:
                setSessionState(PlaybackState.STATE_PAUSED);
                releaseAudioFocus();
                break;

            case AUDIOFOCUS_GAIN:
                if ( isPlaying() ) {
                    mPlayer.setVolume(VOLUME_FULL);
                    mIsVolumeDucked = false;
                }
        }
    }

    public interface Listener {
        // when the song changes
        void onMediaChanged();

        // when all items in the queue are played
        void onPlaybackCompleted();

        // when player state changes
        void onPlayerStateChanged();

        // when there is an error in playing the song
        void onPlayerError();

        // when song is being played
        void onUpdatePlayerPosition();

        void onQueueUpdated();
    }
}
