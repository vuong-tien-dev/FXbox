package com.vtstudio.fxbox.activity

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.vtstudio.fxbox.downloader.FXDownloader
import com.vtstudio.fxbox.downloader.FXDownloader.FxDownloadBinder
import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged
import com.vtstudio.fxbox.listeners.PipModeListener
import com.vtstudio.fxbox.listeners.OnServiceConnectionListener
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.player.FxPlayer
import com.vtstudio.fxbox.media.player.FxPlayerBinder

 open class FxBaseActivity : AppCompatActivity() {
    private val FLAG_PLAYER_SERVICE_BOUND = false
    var isDownloadServiceBound = false
        private set
    var isFxPlayerBound = false
        private set
    var downloadManager: FXDownloader? = null
        private set
    var fxPlayer: FxPlayer? = null
        private set
    var isModePipEnabled = false
        set(value) {
            field = value
            solveListener (value)
            notifyOnPipEnableChanged(value)
        }

    private var downloadManagerConnection: ServiceConnection? = null
    private var fxPlayerConnection: ServiceConnection? = null
    private var onDownloadServiceConnectionListener: ArrayList<OnServiceConnectionListener>? = null
    private var onFxPlayerConnectionListener: ArrayList<OnServiceConnectionListener>? = null
    private val onEnterPipListeners: ArrayList<PipModeListener> by lazy { ArrayList<PipModeListener>() }
    private var mListeners: OnMediaNotificationPlaybackChanged? = null
//    private val screenSize: Size by lazy {
//        var mSize = Size (270, 480)
//        mSize = if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
//            Size (windowMetrics.bounds.width(), windowMetrics.bounds.height())
//        } else {
//            val mDisplayMetrics: DisplayMetrics = DisplayMetrics()
//            windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
//            Size (mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels)
//        }
//        mSize
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    fun boundDownloadManager(onServiceConnectionListener: OnServiceConnectionListener) {

        if (isDownloadServiceBound) {
            onServiceConnectionListener.onServiceConnected()
            return
        }


        // Khởi động dịch vụ PlayerService
        startService(Intent(this, FXDownloader::class.java))
        downloadManagerConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as FxDownloadBinder
                downloadManager = binder.service
                onServiceConnectionListener.onServiceConnected()
                isDownloadServiceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                onServiceConnectionListener.onServiceDisconnected()
                isDownloadServiceBound = false
                downloadManager = null
                downloadManagerConnection = null
            }
        }

        // Kết nối với dịch vụ PlayerService
        val intent = Intent(this, FXDownloader::class.java)
        bindService(intent, downloadManagerConnection as ServiceConnection, BIND_AUTO_CREATE)
    }

    fun boundFxPlayer(onServiceConnectionListener: OnServiceConnectionListener) {
        if (isFxPlayerBound) {
            onServiceConnectionListener.onServiceConnected()
            return
        }

        // Khởi động dịch vụ FxPlayer
        startService(Intent(this, FxPlayer::class.java))
        fxPlayerConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as FxPlayerBinder
                fxPlayer = binder.service
                onServiceConnectionListener.onServiceConnected()
                isFxPlayerBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                onServiceConnectionListener.onServiceDisconnected()
                fxPlayer = null
                fxPlayerConnection = null
                isFxPlayerBound = false
            }
        }

        // Kết nối với dịch vụ FxPlayer
        val intent = Intent(applicationContext, FxPlayer::class.java)
        bindService(intent, fxPlayerConnection as ServiceConnection, BIND_AUTO_CREATE)
    }


//    private fun notifyOnFxPlayerConnected () = onFxPlayerConnectionListener.forEach {
//        listener -> listener.onServiceConnected()
//    }
//
//    private fun notifyOnFxPlayerDisConnected () = onFxPlayerConnectionListener.forEach {
//            listener -> listener.onServiceDisconnected()
//    }
//
//    private fun notifyOnDownloaderConnected () = onDownloadServiceConnectionListener.forEach {
//            listener -> listener.onServiceConnected()
//    }
//
//    private fun notifyOnDownloaderDisConnected () = onDownloadServiceConnectionListener.forEach {
//            listener -> listener.onServiceDisconnected()
//    }
    // pip functions

    val isPipSupported: Boolean
        get() = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    private fun setupPip(isPlaying: Boolean) {
        val params = getPipParams(isPlaying)
        setPictureInPictureParams(params)
    }

    private fun enterPipMode() {
        val params = getPipParams()
        enterPictureInPictureMode(params)
    }

    private fun exitPipMode () {
        val intent = Intent(this, javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    private fun getPipParams(isPlaying: Boolean = false): PictureInPictureParams {

        val currentMedia: FxMediaVideo? = fxPlayer?.currentPlayingMedia as? FxMediaVideo
        val width = currentMedia?.width ?: 270
        val height = currentMedia?.height ?: 480

        val rational = if (width == 0 || height == 0) {
            Rational (16, 9)
        } else {
            Rational(width, height)
        }

        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(rational)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(isPlaying)
        }

        return builder.build()
    }

    fun addOnEnterPipMode(listener: PipModeListener) {
        onEnterPipListeners.add(listener)
    }

    fun removeOnEnterPipMode(listener: PipModeListener) {
        onEnterPipListeners.remove(listener)
    }

    private fun notifyOnEnterPipStateChanged(state: Boolean) = onEnterPipListeners.forEach { item ->
        item.onStateChange(state)
    }

    private fun notifyOnPipEnableChanged(enabled: Boolean) = onEnterPipListeners.forEach { item ->
        item.onEnterPipEnableChanged(enabled)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(isModePipEnabled) {
            enterPipMode()
        }
    }

    private fun solveListener (isEnter: Boolean) {
        if (isEnter) {
            mListeners = object : OnMediaNotificationPlaybackChanged {
                override fun onPrevious() {
                    Log.d("FxBaseActivity", "previous and change pip")
                    setPictureInPictureParams(getPipParams())
                }

                override fun onNext() {
                    setPictureInPictureParams(getPipParams())
                }
            }
            fxPlayer?.addListener(mListeners)
        } else {
            mListeners?.let {
                fxPlayer?.removeListener(it)
            }

        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        notifyOnEnterPipStateChanged(isInPictureInPictureMode)
        Log.d("FxBaseActivity", "width: ${newConfig.screenWidthDp}, height: ${newConfig.screenHeightDp}")
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

     fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + applicationContext.packageName))
        startActivityForResult(intent, 20)
    }

    private fun unEnableVideoWindow() {
        if(fxPlayer != null) {
            fxPlayer?.setVideoWindowModeEnabled(false, javaClass)
        } else {
            boundFxPlayer(object : OnServiceConnectionListener {
                override fun onServiceConnected() {
                    fxPlayer?.setVideoWindowModeEnabled(false, this@FxBaseActivity.javaClass)
                }

                override fun onServiceDisconnected() {

                }
            })
        }
    }


    override fun onStart() {
        super.onStart()
        window.decorView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
        unEnableVideoWindow()
    }

    override fun onRestart() {
        super.onRestart()
        unEnableVideoWindow()
    }


    @CallSuper
    override fun onDestroy() {
        downloadManagerConnection?.let {
            unbindService(it)
            downloadManagerConnection = null
            isDownloadServiceBound = false
        }
        downloadManager = null

        mListeners?.let {
            fxPlayer?.removeListener(it)
        }

        fxPlayerConnection?.let {
            unbindService(it)
            fxPlayerConnection = null
            isFxPlayerBound = false
        }
        fxPlayer = null

        Glide.get(this).clearMemory()
        onEnterPipListeners.clear()
        super.onDestroy()
    }


    /* abstract functions*/


    companion object {
        private const val PLAYER_SERVICE = 0
        private const val DOWNLOAD_SERVICE = 1
        private const val FXPLAYER = 2
    }
}