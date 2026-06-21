package com.simplegallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val album: String,
    val isVideo: Boolean,
    val dateAdded: Long,
    val size: Long
)

data class Album(
    val name: String,
    val coverUri: Uri,
    val count: Int
)

object MediaLoader {

    fun loadAlbums(context: Context): List<Album> {
        val items = loadAll(context)
        return items
            .groupBy { it.album }
            .map { (name, list) -> Album(name, list.first().uri, list.size) }
            .sortedByDescending { it.count }
    }

    fun loadByAlbum(context: Context, albumName: String): List<MediaItem> {
        return loadAll(context).filter { it.album == albumName }
    }

    fun loadAll(context: Context): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        items += queryMedia(context, false)
        items += queryMedia(context, true)
        return items.sortedByDescending { it.dateAdded }
    }

    private fun queryMedia(context: Context, isVideo: Boolean): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val collection = if (isVideo)
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE
        )

        context.contentResolver.query(
            collection, projection, null, null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items += MediaItem(
                    id = id,
                    uri = uri,
                    name = cursor.getString(nameCol) ?: "Sin nombre",
                    album = cursor.getString(albumCol) ?: "Otros",
                    isVideo = isVideo,
                    dateAdded = cursor.getLong(dateCol),
                    size = cursor.getLong(sizeCol)
                )
            }
        }
        return items
    }

    fun delete(context: Context, item: MediaItem): Boolean {
        return try {
            context.contentResolver.delete(item.uri, null, null) > 0
        } catch (e: Exception) {
            false
        }
    }
}
