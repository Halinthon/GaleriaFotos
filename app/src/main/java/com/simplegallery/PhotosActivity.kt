package com.simplegallery

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplegallery.databinding.ActivityPhotosBinding
import com.simplegallery.databinding.ItemPhotoBinding

class PhotosActivity : AppCompatActivity() {

    private lateinit var b: ActivityPhotosBinding
    private lateinit var items: MutableList<MediaItem>
    private lateinit var adapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPhotosBinding.inflate(layoutInflater)
        setContentView(b.root)

        val albumName = intent.getStringExtra("album") ?: return finish()
        setSupportActionBar(b.toolbar)
        supportActionBar?.title = albumName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        items = MediaLoader.loadByAlbum(this, albumName).toMutableList()
        adapter = PhotoAdapter(items,
            onClick = { item, pos ->
                if (item.isVideo) {
                    startActivity(Intent(this, VideoActivity::class.java)
                        .putExtra("uri", item.uri.toString()))
                } else {
                    startActivity(Intent(this, ViewerActivity::class.java)
                        .putExtra("uri", item.uri.toString())
                        .putExtra("pos", pos)
                        .putParcelableArrayListExtra("items",
                            ArrayList(items.map { it.uri })))
                }
            },
            onLongClick = { item, pos -> confirmDelete(item, pos) }
        )
        b.recycler.layoutManager = GridLayoutManager(this, 3)
        b.recycler.adapter = adapter
    }

    private fun confirmDelete(item: MediaItem, pos: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Eliminar \"${item.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                if (MediaLoader.delete(this, item)) {
                    items.removeAt(pos)
                    adapter.notifyItemRemoved(pos)
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

class PhotoAdapter(
    private val items: List<MediaItem>,
    private val onClick: (MediaItem, Int) -> Unit,
    private val onLongClick: (MediaItem, Int) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.VH>() {

    inner class VH(val b: ItemPhotoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        Glide.with(holder.b.iv)
            .load(item.uri)
            .centerCrop()
            .into(holder.b.iv)
        holder.b.ivVideo.visibility = if (item.isVideo) View.VISIBLE else View.GONE
        holder.b.root.setOnClickListener { onClick(item, position) }
        holder.b.root.setOnLongClickListener { onLongClick(item, position); true }
    }
}
