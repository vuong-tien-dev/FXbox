package com.vtstudio.fxbox.fragments

import android.graphics.Color
import android.icu.text.Collator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.MediaItem

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.FragmentTopPlayerBinding
import com.vtstudio.fxbox.listeners.ActionListener
import com.vtstudio.fxbox.listeners.FxLifecycle
import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.Media
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.media.player.FxPlayer
import com.vtstudio.fxbox.media.player.MediaRequest
import com.vtstudio.fxbox.media.player.PlaybackBehavior
import com.vtstudio.fxbox.media.utils.ModelUtils
import java.util.stream.Collectors

class YoutubePlayerTop(var fxPlayer: FxPlayer,var fxLifecycle: FxLifecycle) : Fragment(), Player.Listener{


    private var _binding: FragmentTopPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var _player: SimpleExoPlayer
    var player
        get() = _player
        set(value) {
            _player = value
        }

    var videos: MutableList<FxMediaVideo>? = mutableListOf()
    private var actionCallback: ActionListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    init {
        player = fxPlayer.player!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(fxPlayer.currentMediaList == null || fxPlayer.currentRequestId != PlayerDragView.MEDIA_ID) {
            val db = FxRoomDB.get(requireContext())
            val details = ModelUtils.fromYTDetailsList(db.ytvDetailsDao().allDetails)

            details.forEach { media -> videos?.add(media) }

            val request = MediaRequest(PlayerDragView.MEDIA_ID)
            request.currentPlayingIndex = 0
            request.mediaList = (videos as List<Media>)
            request.isPlaying = true
            request.currentPosition = 0
            request.playBackBehavior = PlaybackBehavior.BEHAVIOR_NEXT
            fxPlayer.addRequest(request)
            fxPlayer.prepare(0, PlayerDragView.MEDIA_ID,false)
            fxPlayer.play()
        } else {
            fxPlayer.currentMediaList.filterIsInstance<FxMediaVideo>().forEach{ media -> videos?.add(media) }
        }

        player.addListener(this)

        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = player
        binding.playerView.requestFocus()

        val next: View? = binding.playerView.findViewById(R.id.next)
        val previous: View? = binding.playerView.findViewById(R.id.previous)
        val collapse: View? = binding.playerView.findViewById(R.id.collapse_button)
        val windowMode: View? = binding.playerView.findViewById(R.id.floating_mode_button)

        next?.setOnClickListener {
            actionCallback?.onActionClick(PlayerDragView.ACTION_NEXT)
        }

        previous?.setOnClickListener {
            actionCallback?.onActionClick(PlayerDragView.ACTION_PREVIOUS)
        }

        collapse?.setOnClickListener {  }
        windowMode?.setOnClickListener {  }

        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        Log.d("Fragment", "onPlayerTop created")
    }


    fun setActionCallback(actionCallback: ActionListener) {
        this.actionCallback = actionCallback
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.playWhenReady = true
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if(isPlaying) {
            actionCallback?.onActionClick(PlayerDragView.ACTION_PLAY)
        } else {
            actionCallback?.onActionClick(PlayerDragView.ACTION_PAUSE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.removeListener(this)
        _binding = null
    }

    override fun onStop() {
        fxLifecycle.fxOnStop()
        super.onStop()
    }

    override fun onResume() {
        fxLifecycle.fxOnResume()
        super.onResume()
    }

    override fun onPause() {
        fxLifecycle.fxOnPause()
        super.onPause()
    }

    fun isBindingInit(): Boolean {
        return _binding != null
    }


}
