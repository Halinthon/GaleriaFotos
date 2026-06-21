package com.simplegallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplegallery.databinding.ActivityAlbumsBinding
import com.simplegallery.databinding.ItemAlbumBinding

class AlbumsActivity : AppCompatActivity() {

    private lateinit var b: ActivityAlbumsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAlbumsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        else
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        val denied = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (denied.isEmpty()) loadAlbums()
        else ActivityCompat.requestPermissions(this, denied.toTypedArray(), 1)
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, perms, results)
        if (results.all { it == PackageManager.PERMISSION_GRANTED }) loadAlbums()
        else Toast.makeText(this, "Se necesitan permisos para ver tus fotos", Toast.LENGTH_LONG).show()
    }

    private fun loadAlbums() {
        val albums = MediaLoader.loadAlbums(this)
        b.recycler.layoutManager = GridLayoutManager(this, 2)
        b.recycler.adapter = AlbumAdapter(albums) { album ->
            startActivity(Intent(this, PhotosActivity::class.java)
                .putExtra("album", album.name))
        }
    }
}

class AlbumAdapter(
    private val albums: List<Album>,
    private val onClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.VH>() {

    inner class VH(val b: ItemAlbumBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = albums.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val album = albums[position]
        holder.b.tvName.text = album.name
        holder.b.tvCount.text = "${album.count} elementos"
        Glide.with(holder.b.ivCover)
            .load(album.coverUri)
            .centerCrop()
            .into(holder.b.ivCover)
        holder.b.root.setOnClickListener { onClick(album) }
    }
}
