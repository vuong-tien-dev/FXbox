package com.vtstudio.fxbox.media.player;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.activity.PlayVideo;
import com.vtstudio.fxbox.broadcast.BluetoothBroadcastReceiver;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.fxviews.FlipImageView;
import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.MediaSegment;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.notifications.MediaNotificationCreator;
import java.util.ArrayList;
import java.util.List;

public class FxPlayer extends SmartyFloatyService implements Player.Listener, OnMediaNotificationPlaybackChanged {
    // Khai báo lớp Binder con
    // Khai báo đối tượng Binder
    private FxPlayerBinder mBinder = null;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("player service", "onUnbind");
        bound = false;
        if (mediaRequests != null && mediaRequests.isEmpty()) stop();
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        bound = true;
        Log.d("player service", "onRebind");
    }

    @Override
    public void onCreate() {

        player = new FxExoPlayer(new SimpleExoPlayer.Builder(getApplicationContext()));

        player.addListener(this);

        final int sessionId = player.getAudioSessionId();

        try {
            loudnessenhancer = new LoudnessEnhancer(sessionId);
            loudnessenhancer.setTargetGain(0);
            loudnessenhancer.setEnabled(true);
        } catch (Exception e) {
            Log.d("FxPlayer", "Cannot create loudness enhancer ! with error " + e.getMessage());
        }

        mediaRequests = new ArrayList<>();

        mBinder = new FxPlayerBinder(this);

        mListeners = new ArrayList<>();

        mDatabase = FxRoomDB.get(this);

        // tạo kênh thông báo
        createChannel();

        // khởi tạo trình gửi thông báo
        mediaNotificationCreator = new MediaNotificationCreator(this);

        //đăng ký cuộc gọi cho seekbar trên thanh thông báo
        mediaNotificationCreator.setOnMediaNotificationPlaybackChanged(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothReceiver = new BluetoothBroadcastReceiver();
            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
            //filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);

            registerReceiver(bluetoothReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {

        if (player != null) {
            player.removeListener(this);
            player.release();
            player = null;
        }

        if (mediaNotificationCreator != null) {
            notificationManager.cancelAll();
            mediaNotificationCreator.release();
            notificationManager = null;
            mediaNotificationCreator = null;
        }

        if(loudnessenhancer != null) {
            loudnessenhancer.setTargetGain(0);
            loudnessenhancer.setEnabled(false);
        }

        if (mediaRequests != null) {
            mediaRequests.clear();
            mediaRequests = null;
        }

        if (currentRequest != null) {
            currentRequest.setMediaList(null);
            currentRequest = null;
        }

        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }

        Log.d("FxPlayer", "fxOnDestroy");
        super.onDestroy();
    }

    //public static final String ACTION_START = "start";
    private NotificationManager notificationManager;
    private MediaNotificationCreator mediaNotificationCreator;
    private BluetoothBroadcastReceiver bluetoothReceiver;
    private List<OnMediaNotificationPlaybackChanged> mListeners;
    private List<MediaRequest> mediaRequests;
    private FxExoPlayer player;
    private MediaRequest currentRequest;
    private LoudnessEnhancer loudnessenhancer;
    private FxRoomDB mDatabase;
    private boolean startedBackground;
    private boolean bound;

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(MediaNotificationCreator.CHANNEL_ID,
                getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    // fields getter and setter method
    public FxExoPlayer getPlayer() {
        return player;
    }

    public boolean isStartedBackground() {
        return startedBackground;
    }

    public void addListener(OnMediaNotificationPlaybackChanged listener) {
        if (listener != null && mListeners != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(OnMediaNotificationPlaybackChanged listener) {
        if (mListeners != null && listener != null) {
            mListeners.remove(listener);
        }
    }

    // request method

    public boolean addRequest(MediaRequest request) {
        for (int i = 0; i < mediaRequests.size(); i++) {
            MediaRequest r = mediaRequests.get(i);
            if (r.getId() == request.getId()) return false;
        }
        request.setFxPlayer(this);
        mediaRequests.add(request);
        Log.d("FxPlayer", "add " + request.getId());
        return true;
    }

    public boolean removeRequest(int id) {
        for (int i = 0; i < mediaRequests.size(); i++) {
            MediaRequest r = mediaRequests.get(i);
            if (r.getId() == id) {
                if (currentRequest != null && currentRequest.getId() == id) {
                    currentRequest.setCanRemove(true);
                    Log.d("FxPlayer", "Fxplayer must wait for " + id + " until not playing request selected");
                    return false;
                }
                Log.d("FxPlayer", "Fxplayer has been removed " + id);
                mediaRequests.remove(i);
                return true;
            }
        }
        return false;
    }

    public void cancelRemoved(int id) {
        for (int i = 0; i < mediaRequests.size(); i++) {
            MediaRequest r = mediaRequests.get(i);
            if (r.getId() == id) {
                r.setCanRemove(false);
                return;
            }
        }
    }


    public void removeRequest(MediaRequest request) {

        if (currentRequest != null && request != null && currentRequest.getId() == request.getId()) {
            currentRequest.setCanRemove(true);
            return;
        }

        if (request != null) mediaRequests.remove(request);
    }

    private void cleanRequest() {
        Log.d("fx_player", "size before: " + mediaRequests.size());
        mediaRequests.removeIf(MediaRequest::canRemove);
        Log.d("fx_player", "size after: " + mediaRequests.size());
    }

    public MediaRequest getRequestById(int id) {
        for (int i = 0; i < mediaRequests.size(); i++) {
            MediaRequest request = mediaRequests.get(i);
            if (request.getId() == id) return request;
        }
        return null;
    }

    public MediaRequest getCurrentRequest() {
        return currentRequest;
    }

    // end request methods

    // play state methods
    public boolean isPlaying() {
        return currentRequest != null && currentRequest.isPlaying();
    }

    public int getCurrentRequestId() {
        return currentRequest != null ? currentRequest.getId() : -1;
    }

    public List<Media> getCurrentMediaList() {
        return currentRequest != null ? currentRequest.getMediaList() : null;
    }

    public Media getCurrentPlayingMedia() {
        if (currentRequest != null) {
            List<Media> mediaList = currentRequest.getMediaList();
            if (mediaList != null && mediaList.size() > 0) {
                return currentRequest.getMediaList().get(currentRequest.getCurrentPlayingIndex());
            }
        }
        return null;
    }

    public int getCurrentPlayingIndex() {
        return currentRequest != null ? currentRequest.getCurrentPlayingIndex() : -1;
    }

    public long getCurrentPosition() {
        return currentRequest != null ? currentRequest.getCurrentPosition() : -1;
    }

    public boolean isFirstPlayed() {
        return currentRequest != null && currentRequest.isFirstPlayed();
    }
    // end play state methods

    // play back methods
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d("FxPlayer", "FxPlayer onStartCommand");
        if (action != null && !action.equals("")) {
            switch (action) {
                // playback action
                case MediaNotificationCreator.ACTION_PREVIOUS:
                    onPrevious();
                    break;
                case MediaNotificationCreator.ACTION_PLAY:
                    onPlay();
                    break;
                case MediaNotificationCreator.ACTION_NEXT:
                    onNext();
                    break;
                case MediaNotificationCreator.ACTION_PAUSE:
                    onPause();
                    break;
                case MediaNotificationCreator.ACTION_STOP:
                    onStop();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void startBackground() {
        if (!startedBackground) {
            if (mediaNotificationCreator == null) return;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(MediaNotificationCreator.NOTIFICATION_ID, mediaNotificationCreator.getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(MediaNotificationCreator.NOTIFICATION_ID, mediaNotificationCreator.getNotification());
            }
            startedBackground = true;
        }
    }

    public void stopBackground() {
        if (startedBackground) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            startedBackground = false;
        }
    }
    // notification playback event solve methods


    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
       Log.d("FxPlayer", "onPositionDiscontinuity: " + newPosition.positionMs);
        pushNotification();

    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if(currentRequest != null) {
            currentRequest.setPlaying(isPlaying);
            pushNotification();
        }
    }

    @Override
    public void onPlaybackStateChanged(int state) {

        if (state == Player.STATE_ENDED) {
            if (currentRequest != null) {
                switch (currentRequest.getPlayBackBehavior()) {
                    case PlaybackBehavior.BEHAVIOR_NEXT:
                        next();
                        break;
                    case PlaybackBehavior.BEHAVIOR_REPEAT:
                        seekTo(0);
                }
            }
        }
//        } else if (state == Player.STATE_READY) {
//            Log.d("fx_player", "play ready");
//            pushNotification();
//        } else if (state == Player.STATE_IDLE) {
//            pushNotification();
//        }
    }

    @Override
    public void onPlay() {
        play();
        notifyOnResume();
    }

    @Override
    public void onPause() {
        pause();
    }


    @Override
    public void onNext() {
        next();
    }

    @Override
    public void onPrevious() {
        previous();
    }

    @Override
    public void onSeekTo(long pos) {
        seekTo(pos);
    }

    @Override
    public void onStop() {
        stop();
    }

    // end notification playback event solve methods

    // media playback methods

    public void prepare(int position, boolean override, boolean fromNotification) {

        MediaRequest request = currentRequest;

        if (player == null || request == null || position < 0 || request.getMediaList() == null || position >= request.getMediaList().size())
            return;

        //Log.d("FxPlayer", "onPrepare: new: " + position + ", old: " + request.getCurrentPlayingIndex());

        if (position == request.getCurrentPlayingIndex() && !override && request.firstPlayed && !fromNotification)
            return;

        Media media = request.getMediaList().get(position);

        if (media == null) return;
        MediaItem mediaItem = MediaItem.fromUri(media.getPlayUri());
        if(media instanceof FxMediaVideo ) {
            final FxMediaVideo mediaVideo = (FxMediaVideo) media;
            if(mediaVideo.getCurrentSegment() != null) {
                mediaItem = mediaItem.buildUpon().setClipStartPositionMs(mediaVideo.getCurrentSegment().getStartTime())
                        .setClipEndPositionMs(mediaVideo.getCurrentSegment().getEndTime())
                        .build();
            }
        }
        player.setMediaItem(mediaItem);
        player.prepare();

        if (player.getVolume() < 1) {
            player.setVolume(1.f);
        }

        if(media instanceof FxMediaVideo) {
            float volume = ((FxMediaVideo) media).getVolume()  - 1;
            loudnessenhancer.setTargetGain((int) (volume*1000));
        }
        request.setCurrentPlayingIndex(position);
        request.firstPlayed = true;
        notifyOnPlay(position, fromNotification);

        if (videoWindowModeEnabled) {
            updateWindowSizeWithAnim(200);
        }

        if (position >= request.getMediaList().size() - 2) {
            request.queryMediaIfHasMore(mDatabase);
        }

    }

    public void prepare(int position, boolean fromNotification) {
        prepare(position, false, fromNotification);
    }

    public void prepare(int position, int requestId, boolean override, boolean fromNotification) {

        MediaRequest request = null;
        if (currentRequest != null && currentRequest.getId() == requestId) {
            request = currentRequest;
        } else {
            cleanRequest();
            currentRequest = getRequestById(requestId);
        }

        prepare(position, override, fromNotification);
    }

    public void prepare(int position, int requestId, boolean fromNotification) {
        prepare(position, requestId, false, fromNotification);
    }


//    public void prepare(int position, int requestId, boolean overwrite, boolean fromNotification) {
//
//        MediaRequest request = null;
//
//        if (currentRequest != null && currentRequest.getId() == requestId) {
//            request = currentRequest;
//        } else {
//            cleanRequest();
//            request = getRequestById(requestId);
//        }
//
//        if (player == null || request == null || position < 0 || request.getMediaList() == null || position >= request.getMediaList().size())
//            return;
//
//        Log.d("fx_player", "onPrepared");
//
//        if (position == request.getCurrentPlayingIndex() && !overwrite && !fromNotification) return;
//
//
//        Media media = request.getMediaList().get(position);
//
//        if (media == null) return;
//
//        player.setMediaItem(MediaItem.fromUri(media.getPlayUri()));
//        player.prepare();
//
//        if (player.getVolume() < 1) {
//            player.setVolume(1.f);
//        }
//
//        request.setCurrentPlayingIndex(position);
//        request.firstPlayed = true;
//        currentRequest = request;
//        notifyOnPlay(position, fromNotification);
//    }

    public void play() {
        if (player == null || currentRequest == null) return;
        //Log.d("fx_player", "play");

        currentRequest.setPlaying(true);
        player.setPlayWhenReady(true);
        pushNotification();
    }

    public void pause() {
        if (player == null || currentRequest == null) return;
        currentRequest.setPlaying(false);
        player.setPlayWhenReady(false);
        notifyOnPause();
        pushNotification();
    }

    public void next() {
        if (player == null || currentRequest == null) return;

        int currentPosition = currentRequest.getCurrentPlayingIndex();
        int size = currentRequest.getMediaList().size();
        int targetPosition = currentPosition >= size - 1 ? 0 : currentPosition + 1;

        prepare(targetPosition, true);
        notifyOnNext();
        play();

    }

    public void previous() {
        if (player == null || currentRequest == null) return;

        int currentPosition = currentRequest.getCurrentPlayingIndex();
        int size = currentRequest.getMediaList().size();
        int targetPosition = currentPosition <= 0 ? size - 1 : currentPosition - 1;

        prepare(targetPosition, true);
        notifyOnPrevious();
        play();
    }

    public void seekTo(long position) {

        if (player == null || currentRequest == null) return;

        currentRequest.setCurrentPosition(position);
        player.seekTo(position);
        pushNotification();
        notifyOnSeekTo(position);
    }

    private void stop() {
        if (bound) {
            notifyOnStop();
        } else {
            stopBackground();
            stopSelf();
        }

    }

    public void refreshVolume() {
        Media media = getCurrentPlayingMedia();
        if(loudnessenhancer != null && media instanceof FxMediaVideo) {
            loudnessenhancer.setTargetGain((int) ((((FxMediaVideo) media).getVolume() - 1)*1000));
        }
    }

    private void pushNotification() {

        if (mediaNotificationCreator == null
                || currentRequest == null
                || currentRequest.getMediaList() == null
                || currentRequest.getMediaList().isEmpty()
                || currentRequest.getCurrentPlayingIndex() < 0
                || currentRequest.getCurrentPlayingIndex() >= currentRequest.getMediaList().size())
            return;

        Media media = currentRequest.getMediaList().get(currentRequest.getCurrentPlayingIndex());

        if (media instanceof ShortsVideo) {
            ShortsVideo shortsVideo = (ShortsVideo) media;
            if (shortsVideo.getDuration() == 0) shortsVideo.setDuration(player.getDuration());
            int player_state = currentRequest.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mediaNotificationCreator.createNotification(shortsVideo, currentRequest.isPlaying(), player_state, (int) player.getCurrentPosition());
        } else if (media instanceof YTVideo) {
            YTVideo yt = (YTVideo) media;
            if (yt.getDuration() == 0) yt.setDuration(player.getDuration());
            int player_state = currentRequest.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mediaNotificationCreator.createNotification(yt, currentRequest.isPlaying(), player_state, (int) player.getCurrentPosition());
        } else if (media instanceof FxMediaVideo) {
            FxMediaVideo fxMediaVideo = (FxMediaVideo) media;
            if (fxMediaVideo.getDuration() == 0) fxMediaVideo.setDuration(player.getDuration());
            int player_state = currentRequest.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mediaNotificationCreator.createNotification(fxMediaVideo, currentRequest.isPlaying(), player_state, (int) player.getCurrentPosition());
        }

    }

    /*
     *  @Notify notify playback listener methods
     *  các phương thức dưới đây sẽ thông báo cuộc gọi cho trình lắng nghe sự kiện phát lại
     */

    private void notifyOnPause() {
        if (mListeners != null) {
            mListeners.forEach(OnMediaNotificationPlaybackChanged::onPause);
        }
    }

    private void notifyOnPlay(int pos, boolean fromNotification) {
        if (mListeners != null) {
            mListeners.forEach(listener -> listener.onPlay(pos, fromNotification));
        }
    }

    private void notifyOnResume() {
        if (mListeners != null) {
            mListeners.forEach(OnMediaNotificationPlaybackChanged::onResume);
        }
    }

    private void notifyOnStop() {
        if (mListeners != null) {
            mListeners.forEach(OnMediaNotificationPlaybackChanged::onStop);
        }
    }

    private void notifyOnPrevious() {
        if (mListeners != null) {
            mListeners.forEach(OnMediaNotificationPlaybackChanged::onPrevious);
        }
    }

    private void notifyOnNext() {
        if (mListeners != null) {
            mListeners.forEach(OnMediaNotificationPlaybackChanged::onNext);
        }
    }

    private void notifyOnEnd() {
        if (mListeners != null) {
            mListeners.forEach(OnMediaNotificationPlaybackChanged::onEnd);
        }
    }

    private void notifyOnSeekTo(long pos) {
        if (mListeners != null) {
            mListeners.forEach(listener -> listener.onSeekTo(pos));
        }
    }

    /* kết thúc các phương thức notify listener */

    /* Bắt đầu các phương thức cho trình phát nổi*/
    private Player.Listener mPipPlayerListener = null;
    private OnMediaNotificationPlaybackChanged mPipMediaPlaybackChanged = null;
    private boolean videoWindowModeEnabled;

    public boolean setVideoWindowModeEnabled(boolean enabled, Class<? extends FxBaseActivity> cls) {
        if (enabled && !videoWindowModeEnabled && Settings.canDrawOverlays(this)) {
            this.videoWindowModeEnabled = true;
            super.setClass(cls);
            super.onStartCommand(new Intent(), 0, 0);
            Log.d("FxPlayer", "player has been entered into video window mode by class " + cls.getName());
            return true;
        } else if (!enabled && videoWindowModeEnabled) {
            videoWindowModeEnabled = false;
            super.onDestroy();
            if (mPipPlayerListener != null) {
                player.removeListener(mPipPlayerListener);
                mPipPlayerListener = null;
            }
            removeListener(mPipMediaPlaybackChanged);
            mPipMediaPlaybackChanged = null;
            Log.d("FxPlayer", "player has been exited from video window mode by class " + cls.getName());
            return true;
        }
        return false;
    }

    public boolean isVideoWindowModeEnabled() {
        return videoWindowModeEnabled;
    }

    @Override
    protected void onViewOverlayCreated(@NonNull View view) {

        Log.d("FxPlayer", "onViewOverlayCreated");
        PlayerView playerView = view.findViewById(R.id.floating_player_view);
        View playerControlView = view.findViewById(R.id.video_floating_controller);
        ImageButton nextButton = playerControlView.findViewById(R.id.next);
        ImageButton prevButton = playerControlView.findViewById(R.id.previous);
        ImageButton playOrPause = playerControlView.findViewById(R.id.play_or_pause);
        ImageView exitOverlay = playerControlView.findViewById(R.id.floating_leave_button);
        ImageView expand = playerControlView.findViewById(R.id.floating_expand_button);
        FlipImageView thumbnail = view.findViewById(R.id.video_floating_thumbnail);
        ImageView repeatMode = playerControlView.findViewById(R.id.repeat_mode);

        nextButton.setOnClickListener(v -> {
            waitController(getShowControllerMillis());
            onNext();
        });

        prevButton.setOnClickListener(v -> {
            waitController(getShowControllerMillis());
            onPrevious();
        });

        playOrPause.setImageResource(isPlaying() ?
                com.google.android.exoplayer2.ui.R.drawable.exo_controls_pause :
                com.google.android.exoplayer2.ui.R.drawable.exo_controls_play);

        playOrPause.setOnClickListener(v -> {
            waitController(getShowControllerMillis());
            if (isPlaying()) {
                if (thumbnail != null) {
                    thumbnail.stopFlipping();
                }
                pause();
            } else {
                if (thumbnail != null) {
                    thumbnail.startFlipping();
                }
                play();
            }
        });

        MediaRequest request = getCurrentRequest();
        if (request != null) {
            if (request.getPlayBackBehavior() == PlaybackBehavior.BEHAVIOR_NEXT) {
                repeatMode.setImageResource(R.drawable.repeat_all);
            } else {
                repeatMode.setImageResource(R.drawable.repeat_one);
            }
        }

        repeatMode.setOnClickListener(v -> {
            MediaRequest rq = getCurrentRequest();
            if (rq != null) {
                if (rq.getPlayBackBehavior() == PlaybackBehavior.BEHAVIOR_NEXT) {
                    rq.setPlayBackBehavior(PlaybackBehavior.BEHAVIOR_REPEAT);
                    repeatMode.setImageResource(R.drawable.repeat_one);
                } else {
                    rq.setPlayBackBehavior(PlaybackBehavior.BEHAVIOR_NEXT);
                    repeatMode.setImageResource(R.drawable.repeat_all);
                }
            }
        });
        // cấu hình trình lắng nghe nếu có lỗi phát
        mPipPlayerListener = new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    playOrPause.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_pause);
                } else {
                    playOrPause.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_play);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
            }

        };
        mPipMediaPlaybackChanged = new OnMediaNotificationPlaybackChanged() {
            @Override
            public void onPlay(int pos, boolean fromNotification) {
                Media media = getCurrentPlayingMedia();
                if (media == null) {
                    return;
                }

                boolean isLoaded = false;
                if (media instanceof ShortsVideo) {
                    ShortsVideo shortsVideo = (ShortsVideo) media;
                    if (shortsVideo.isImageList() && shortsVideo.getImageListPath() != null && !shortsVideo.getImageListPath().isEmpty()) {
                        thumbnail.setVisibility(View.VISIBLE);
                        thumbnail.setImagePaths(shortsVideo.getImageListPath());
                        thumbnail.setOnFlipImageListener(new FlipImageView.OnFlipImageListener() {
                            @Override
                            public void onFlip(@NonNull FlipImageView view, int index) {
                                disiredWindowWidth = view.getCurrentImageWidth();
                                disiredWindowHeight = view.getCurrentImageHeight();
                                updateWindowSizeWithAnim(200);
                                disiredWindowWidth = 0;
                                disiredWindowHeight = 0;
                            }
                        });
                        isLoaded = true;
                    }
                }

                if (!isLoaded) {
                    thumbnail.setImageDrawable(null);
                    thumbnail.setVisibility(View.GONE);
                    thumbnail.setImagePaths(null);
                }
            }
        };

        mPipMediaPlaybackChanged.onPlay(getCurrentPlayingIndex(), false);
        player.addListener(mPipPlayerListener);
        playerView.setControllerAutoShow(false);
        playerView.setPlayer(player);
        addListener(mPipMediaPlaybackChanged);
        exitOverlay.setOnClickListener(v -> setVideoWindowModeEnabled(false, super.getRequestClass()));
        expand.setOnClickListener(v -> {
            setVideoWindowModeEnabled(false, super.getRequestClass());
            MediaRequest currentRequest = getCurrentRequest();
            if (super.getRequestClass() == PlayVideo.class) {
                PlayVideo.setCurrentPlayingRequestId(getCurrentRequestId());
            }
            super.startTargetActivity();
        });
    }

    private int disiredWindowWidth;
    private int disiredWindowHeight;

    @Override
    protected int getWindowWidth() {

        if (disiredWindowWidth > 0) return disiredWindowWidth;

        Media media = getCurrentPlayingMedia();
        if (media instanceof FxMediaVideo) {
            FxMediaVideo video = (FxMediaVideo) media;
            if (video.getWidth() > 0 && video.getHeight() > 0) {
                return video.getWidth();
            }
        }
        return super.getWindowWidth();
    }

    @Override
    protected int getWindowHeight() {

        if (disiredWindowHeight > 0) return disiredWindowHeight;

        Media media = getCurrentPlayingMedia();
        if (media instanceof FxMediaVideo) {
            FxMediaVideo video = (FxMediaVideo) media;
            if (video.getWidth() > 0 && video.getHeight() > 0) {
                return video.getHeight();
            }
        }
        return super.getWindowHeight();
    }

    @Override
    protected boolean canInitializeFloatingWindow() {
        return videoWindowModeEnabled;
    }

    @Nullable
    FxRoomDB getDatabase() {
        return mDatabase;
    }
}
