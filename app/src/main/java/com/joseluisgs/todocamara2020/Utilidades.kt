package com.joseluisgs.todocamara2020

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.util.*


object Utilidades {

    /**
     * Función para opbtener el nombre del fichero
     */
    public fun crearNombreFichero(): String {
        return "camara-" + UUID.randomUUID().toString() + ".jpg"
    }

    /**
     * Salva un fichero en un directorio
     */
    fun salvarImagen(path: String, nombre: String, compresion: Int, context: Context): File? {
        // Almacenamos en nuestro directorio de almacenamiento externo asignado en Pictures
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + path)
        // Si no existe el directorio, lo creamos solo si es publico
        if (!dirFotos.exists()) {
            dirFotos.mkdirs()
        }
        try {
            val f = File(dirFotos, nombre)
            f.createNewFile()

            return f
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return null
    }

    fun añadirImagenGaleria(foto: File, nombre: String, context: Context) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "imagen")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, nombre)
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATA, foto.absolutePath)
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    }

    fun borrarFichero(path: String) {
        // Borramos la foto de alta calidad
        val fdelete = File(path)
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("FOTO", "Foto borrada::--->$path")
            } else {
                Log.d("FOTO", "Foto NO borrada::--->$path")
            }
        }
    }
}
