package com.vtstudio.fxbox.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.adapters.PlaylistListAdapter
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.ActivityPlaylistListBinding
import com.vtstudio.fxbox.media.models.tiktok.Playlist

class PlaylistList : AppCompatActivity() {
    private var _binding: ActivityPlaylistListBinding? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlaylistListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    fun initViews () {
        val playlistDao = FxRoomDB.get(this).playlistDao()
        val adapter = PlaylistListAdapter(playlistDao.allPlaylists)
        binding.acPlaylistListview.adapter = adapter
        binding.acPlaylistListview.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener { p ->
            val playlist = adapter.playlistList[p]
            val intent = Intent(this@PlaylistList, PlaylistVideo::class.java)
            intent.putExtra(PlaylistVideo.KEY_PLAYLIST_ID, playlist.id)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        setSupportActionBar(binding.playlistToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.playlistToolbar.setNavigationOnClickListener { _ -> finish() }

        supportActionBar!!.title = getString(R.string.playlist_text)
    }
}