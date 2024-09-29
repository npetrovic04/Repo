package com.photostickers.helpers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object SavePictureHelper {
    fun saveImage(context: Context, name: String, bucketName: String, bmp: Bitmap, forShare: Boolean): Uri? {

        if(forShare){
            val cache: String = context.cacheDir.absolutePath

            val image = File(cache, "${name}.png")
            val imageOutStream = FileOutputStream(image)
            imageOutStream.use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            SingleMediaScanner(context, image)
            return FileProvider.getUriForFile(context, context.packageName + ".provider", image)
        }
        else {
            var returnUri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$bucketName/")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val imageUri = context.contentResolver.insert(collection, values)

                if (imageUri != null) {
                    context.contentResolver.openOutputStream(imageUri).use { out ->
                        if(out != null) {
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }

                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(imageUri, values, null, null)
                    returnUri = imageUri
                }
            } else {
                val externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val album = File(externalStorage, bucketName)
                if (!album.exists()) {
                    album.mkdirs()
                }
                val image = File(album, "${name}.png")
                val imageOutStream = FileOutputStream(image)
                imageOutStream.use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                SingleMediaScanner(context, image)
                returnUri = FileProvider.getUriForFile(context, context.packageName + ".provider", image)
            }
            return returnUri
        }
    }
}