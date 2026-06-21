package com.simplegallery

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.simplegallery.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {

    private lateinit var b: ActivityVideoBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val uri = Uri.parse(intent.getStringExtra("uri") ?: return finish())
        player = ExoPlayer.Builder(this).build().also { exo ->
            b.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
            exo.playWhenReady = true
        }
    }

    override fun onPause() { super.onPause(); player?.pause() }
    override fun onDestroy() { super.onDestroy(); player?.release(); player = null }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
