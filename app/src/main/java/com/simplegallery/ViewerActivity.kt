package com.simplegallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.simplegallery.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {

    private lateinit var b: ActivityViewerBinding
    private var uris: List<Uri> = emptyList()
    private var currentPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // FIX: cargar URIs desde MediaLoader usando album + pos, nunca desde el Intent
        val albumName = intent.getStringExtra("album") ?: return finish()
        currentPos   = intent.getIntExtra("pos", 0)

        // Filtrar solo fotos (sin videos) para el visor
        uris = MediaLoader.loadByAlbum(this, albumName)
            .filter { !it.isVideo }
            .map { it.uri }

        b.pager.adapter = ViewerAdapter(uris)
        b.pager.setCurrentItem(currentPos, false)
        b.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPos = position
                supportActionBar?.title = "${position + 1} / ${uris.size}"
            }
        })
        supportActionBar?.title = "${currentPos + 1} / ${uris.size}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.viewer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> { shareCurrentPhoto(); true }
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareCurrentPhoto() {
        val uri = uris.getOrNull(currentPos) ?: return
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Compartir foto"
        ))
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

class ViewerAdapter(private val uris: List<Uri>) :
    RecyclerView.Adapter<ViewerAdapter.VH>() {

    inner class VH(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val pv = PhotoView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return VH(pv)
    }

    override fun getItemCount() = uris.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.photoView)
            .load(uris[position])
            .into(holder.photoView)
    }
}
