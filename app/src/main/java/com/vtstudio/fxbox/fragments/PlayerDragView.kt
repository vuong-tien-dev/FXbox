package com.vtstudio.fxbox.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.tuanhav95.drag.DragView
import com.tuanhav95.drag.utils.inflate
import com.tuanhav95.drag.utils.reWidth
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.fxviews.progressbar.FxExoTimeBar
import com.vtstudio.fxbox.listeners.ActionListener
import com.vtstudio.fxbox.listeners.FxLifecycle
import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.media.player.FxPlayer
import com.vtstudio.fxbox.utils.FormatUtils
import com.vtstudio.fxbox.utils.HashtagUtils
import com.vtstudio.fxbox.utils.TimeUtils
import java.io.File
import java.lang.Integer.min
import kotlin.math.max


class PlayerDragView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : DragView(context, attrs, defStyleAttr), ActionListener, FxLifecycle {

    companion object {
        const val ACTION_CLOSE = 0
        const val ACTION_PLAY = 1
        const val ACTION_PAUSE = 2
        const val ACTION_PREVIOUS = 3
        const val ACTION_NEXT = 4
        const val MEDIA_ID = 100

    }

    var hideOnClose: Boolean = false
    var isStop: Boolean = false
    private var isAttached: Boolean = false
    private var playerTopFragment: YoutubePlayerTop? = null
    private var playerBottomFragment: YoutubeBottomFragment? = null
    private var fragmentManager: FragmentManager? = null
    private var mListener: DragListener? = null
    private var mActionListener: ActionListener? = null
    private var mPlayButton: ImageView? = null
    private var mPauseButton: ImageView? = null
    private var mCloseButton: ImageView? = null
    private var mTitle: TextView? = null
    private var mTimeBar: FxExoTimeBar? = null
    private var mIsCrossThreshold: Boolean = false
    private var mFxPlayer: FxPlayer? = null
    private var fxPlayerCallback: OnMediaNotificationPlaybackChanged? = null
    private var rationalWidthMin = 22
    private var rationalHeightMin = 9

    var mWidthWhenMax = 0
    var mWidthWhenMiddle = 0
    var mWidthWhenMin = 0


    init {
        getFrameFirst().addView(inflate(R.layout.fragment_player))
        getFrameSecond().addView(inflate(R.layout.fragment_player_bottom))

        mPlayButton = findViewById(R.id.play_icon)
        mPauseButton = findViewById(R.id.pause_icon)
        mCloseButton = findViewById(R.id.close_icon)
        mTitle = findViewById(R.id.title)
        mTimeBar = findViewById(R.id.time_bar)

        mTimeBar?.hideScrubber(true)

        mPlayButton?.setOnClickListener {
            mActionListener?.onActionClick(ACTION_PLAY)
            mPauseButton?.visibility = VISIBLE
            it.visibility = GONE
            findPlayerView()?.player?.play()
        }

        mPauseButton?.setOnClickListener {
            mActionListener?.onActionClick(ACTION_PAUSE)
            mPlayButton?.visibility = VISIBLE
            it.visibility = GONE
            findPlayerView()?.player?.pause()
        }

        mCloseButton?.setOnClickListener {
            mActionListener?.onActionClick(ACTION_CLOSE)
            detachFragments();
            close()
        }

        fxPlayerCallback = buildPlayerCallback()
    }

    private fun buildPlayerCallback(): OnMediaNotificationPlaybackChanged {
        return object : OnMediaNotificationPlaybackChanged {
            override fun onPlay(pos: Int, fromNotification: Boolean) {
                if (isStop) return
                val index = mFxPlayer?.currentPlayingIndex
                index?.let { setDetailsAndResizeFrameFirst(it, mCurrentState == State.MAX) }
            }
        }
    }

    override fun initFrame() {
        mWidthWhenMax = width
        mWidthWhenMiddle = (width - mPercentWhenMiddle * mMarginEdgeWhenMin).toInt()
        mWidthWhenMin = (mHeightWhenMin * (rationalWidthMin / rationalHeightMin.toFloat())).toInt()
        super.initFrame()
    }

    override fun setDragListener(dragListener: DragListener) {
        mListener = object : DragView.DragListener {
            override fun onExpanded() {
                dragListener.onExpanded()
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onChangePercent(percent: Float) {
                dragListener.onChangePercent(percent)

                if (percent > mPercentWhenMiddle && mPercentWhenMiddle < 1) {
                    val playerView = findPlayerView()
                    playerView?.hideController()
                    playerView?.useController = false
                    playerView?.controllerAutoShow = false
                    val p = (percent - mPercentWhenMiddle) / (1 - mPercentWhenMiddle)
                    mTitle?.alpha = p
                    mPlayButton?.alpha = p
                    mPauseButton?.alpha = p
                    mCloseButton?.alpha = p
                } else if (percent <= mPercentWhenMiddle && mPercentWhenMiddle < 1) {
                    val playerView = findPlayerView()
                    playerView?.controllerAutoShow = true
                    playerView?.useController = true
                }

                if (!mIsCrossThreshold) {
                    // Nếu người dùng kéo player xuống, kiểm tra height expand của player view để quyết định nên chọn resize mode nào
                    if (percent > 0) {
                        mIsCrossThreshold = true
                        playerTopFragment?.view?.let {
                            if (it.height >= mHeightWhenMax * 0.95f) {
                                findPlayerView()?.resizeMode =
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            } else if (it.height <= mHeightWhenMax) {
                                findPlayerView()?.resizeMode =
                                    AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                            }
                        }
                    }
                } else {
                    // Nếu dang ở trạng thái Max, player view có resize mdoe fixed height
                    if (percent <= 0f) {
                        mIsCrossThreshold = false
                        val playerView = findPlayerView()
                        playerView?.let {
                            it.scaleX = 1f
                            it.scaleY = 1f
                            it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        }
                    }

                }

                mTimeBar?.scaleY = max(0.5f, 1 - percent)
                setBackgroundColor(
                    android.graphics.Color.argb(
                        ((1 - percent) * 255).toInt(),
                        0,
                        0,
                        0
                    )
                )
            }

            override fun onChangeState(state: State) {
                dragListener.onChangeState(state)
            }
        }
        super.setDragListener(mListener as DragListener)
    }


    override fun onActionClick(action: Int) {
        Log.d("Fragment", "onActionClick")
        when (action) {
            ACTION_PREVIOUS -> {
                playerTopFragment?.player?.let {
                    mFxPlayer?.previous()
                }
            }

            ACTION_NEXT -> {
                playerTopFragment?.player?.let {
                    mFxPlayer?.next()
                }
            }

            ACTION_PLAY -> {
                setButtonState(true)
            }

            ACTION_PAUSE -> {
                setButtonState(false)
            }
        }
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        if (isAttached) {
            detachFragments()
        }
        this.fragmentManager = fragmentManager
    }

    fun attachFragments() {

        if (fragmentManager == null) return

        isAttached = true
        playerTopFragment = YoutubePlayerTop(mFxPlayer!!, this)
        playerBottomFragment = YoutubeBottomFragment()
        mFxPlayer?.player?.let { playerTopFragment?.player = it }

        fragmentManager!!.beginTransaction().add(R.id.player_frame_top, playerTopFragment!!)
            .commit()
        fragmentManager!!.beginTransaction().add(
            R.id.player_frame_bottom,
            playerBottomFragment!!
        ).commit()


        post {

            playerTopFragment?.setActionCallback(this)

            mFxPlayer?.let {
                setDetailsAndResizeFrameFirst(it.currentPlayingIndex, false)
            }

            findPlayerView()?.let {
                it.setControllerVisibilityListener { visibility ->
                    if (visibility == View.VISIBLE) {
                        mTimeBar?.scaleY = 2f
                    } else {
                        mTimeBar?.scaleY = 1f
                    }
                }
            }

            val player = playerTopFragment?.player

            if (mCurrentPercent >= mPercentWhenMiddle) {
                findPlayerView()?.useController = false
            }

            player?.let {
                mTimeBar?.setUpWithExoPlayer(it, 500)
            }

        }
    }

    private fun setButtonState(isPlaying: Boolean) {
        if (isPlaying) {
            mPlayButton?.visibility = View.GONE
            mPauseButton?.visibility = View.VISIBLE
        } else {
            mPlayButton?.visibility = View.VISIBLE
            mPauseButton?.visibility = View.GONE
        }
    }

    private fun setDetailsAndResizeFrameFirst(index: Int, goToMax: Boolean = false) {

        if (playerTopFragment?.isBindingInit() == false ||
            playerBottomFragment?.isBindingInit() == false ||
            playerTopFragment?.videos?.isEmpty() == true
        ) return

        val isOutOfIndex: Boolean = playerTopFragment?.videos?.let { index < 0 || index >= it.size } ?: true

        if(isOutOfIndex) return

        post {
            val video = playerTopFragment?.videos?.get(index)
            if (video is YTVideo) {
                video.let {
                    setTitle(it.title)
                    playerBottomFragment?.setTitle(
                        HashtagUtils.formatHashtags(
                            it.title,
                            context!!.getColor(R.color.colorAccent)
                        )
                    )
                    it.user?.let { user ->
                        playerBottomFragment?.setUserName(user.channelName)
                        playerBottomFragment?.setUserImageFile(File(user.avatarPath))
                        Log.d("Fragment", user.avatarPath)
                        playerBottomFragment?.setUserSubscriberCount(
                            FormatUtils.formatCountThousand(
                                user.subscriberCount
                            )
                        )
                    }
                    playerBottomFragment?.setViewCountAndCreateTime(
                        FormatUtils.formatCount(video.playCount) + " "
                                + context.getString(R.string.views)
                                + " • " + TimeUtils.getTimeAgo(it.mediaStoreDayAdded, context)
                    )
                    if (it.width != 0 && it.height != 0) {
                        resizeFrame(it.width.toFloat() / it.height, goToMax)
                    }
                }
            } else if (video is ShortsVideo) {

                setTitle(video.description)
                playerBottomFragment?.setTitle(
                    HashtagUtils.formatHashtags(
                        video.description,
                        context!!.getColor(R.color.colorAccent)
                    )
                )

                video.shortsUser?.let {
                    user ->
                    playerBottomFragment?.setUserName(user.nickName)
                    playerBottomFragment?.setUserImageFile(File(user.avatarPath))
                    Log.d("Fragment", user.avatarPath)
                    playerBottomFragment?.setUserSubscriberCount(
                        FormatUtils.formatCountThousand(
                            user.followerCount
                        )
                    )
                }

                playerBottomFragment?.setViewCountAndCreateTime(
                    FormatUtils.formatCount(video.playCount) + " "
                            + context.getString(R.string.views)
                            + " • " + TimeUtils.getTimeAgo(video.mediaStoreDayAdded, context)
                )
                if (video.width != 0 && video.height != 0) {
                    resizeFrame(video.width.toFloat() / video.height, goToMax)
                }
            }
            else if (video is FxMediaVideo) {
                setTitle(video.mediaStoreName)
                playerBottomFragment?.setTitle(video.mediaStoreName)

                playerBottomFragment?.setUserName(video.mediaStoreParent)
                playerBottomFragment?.setUserImageFile(File(""))

                playerBottomFragment?.setViewCountAndCreateTime(
                    "0 " + context.getString(R.string.views)
                            + " • " + TimeUtils.getTimeAgo(video.mediaStoreDayAdded, context)
                )
                if (video.width != 0 && video.height != 0) {
                    resizeFrame(video.width.toFloat() / video.height, goToMax)
                }
            }
        }
    }

    private fun resizeFrame(ratio: Float, gotoMax: Boolean) {

        if (isStop) return

        val width = width
        var targetHeight = min((width / ratio).toInt(), (height * 0.7f).toInt())
        targetHeight = if (targetHeight > 0) targetHeight else targetHeight
        setHeightMax(targetHeight, gotoMax)
    }

    fun detachFragments() {
        if (isAttached) {
            fragmentManager!!.beginTransaction().remove(playerTopFragment!!).remove(
                playerBottomFragment!!
            ).commit()
            playerBottomFragment = null
            playerTopFragment = null
            isAttached = false
        }
    }

    fun setTitle(title: String?) {
        mTitle?.text = title
    }

    fun setOnActionEventListener(listener: ActionListener) {
        this.mActionListener = listener
    }

    private fun removeMediaCallback() {
        mFxPlayer?.let { fx ->
            fxPlayerCallback?.let {
                fx.removeListener(it)
            }
        }
    }

    private fun findPlayerView(): PlayerView? {
        val playerFrameTop = findViewById<FrameLayout>(R.id.player_frame_top)
        return playerFrameTop?.findViewById(R.id.player_view)
    }

    override fun refreshFrameFirst() {
        super.refreshFrameFirst()
        val width = if (mCurrentPercent < mPercentWhenMiddle) {
            (mWidthWhenMax - (mWidthWhenMax - mWidthWhenMiddle) * mCurrentPercent)
        } else {
            (mWidthWhenMiddle - (mWidthWhenMiddle - mWidthWhenMin) * (mCurrentPercent - mPercentWhenMiddle) / (1 - mPercentWhenMiddle))
        }

        val playerFrameTop = findViewById<FrameLayout>(R.id.player_frame_top)
        playerFrameTop.reWidth(width.toInt())
    }

    fun setFxPlayer(fxPlayer: FxPlayer) {
        this.mFxPlayer = fxPlayer

        if (mFxPlayer != null) {
            fxPlayer.addListener(fxPlayerCallback)
            setButtonState(fxPlayer.isPlaying)
        }
    }

    override fun fxOnDestroy() {
        mTimeBar?.release()
        removeMediaCallback();
    }

    fun getFxPlayer(): FxPlayer? {
        return this.mFxPlayer
    }

    override fun fxOnStop() {
        isStop = true
    }

    override fun fxOnResume() {
        if (isStop) {
        }
        isStop = false
    }
}
