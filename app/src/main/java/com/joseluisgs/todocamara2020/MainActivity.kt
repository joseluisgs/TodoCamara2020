package com.joseluisgs.todocamara2020

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    // Constantes
    private val GALERIA = 1
    private val CAMARA = 2

    // Si vamos a operar en modo público o privado (es decir si salvamos en nuestro directorio)
    private var PUBLICO = true

    // https://developer.android.com/training/data-storage/shared/media?hl=es-419
    private val IMAGEN_DIR = "/TodoCamara2020"
    private lateinit var IMAGEN_URI: Uri
    private lateinit var IMAGEN_MEDIA_URI: Uri
    private val PROPORCION = 600
    private var IMAGEN_NOMBRE = ""
    private var IMAGEN_COMPRES = 30


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
    }

    /**
     * Inicia la interfaz y los eventos de la apliación
     */
    private fun initUI() {

        // Inicamos la opcion
        initOpcion()
        initModo(PUBLICO)
        // Eventos botones
        initBotones()

        // Iniciamos los permisos
        initPermisos()
    }


    fun initOpcion() {
        mainSwOpcion.setOnCheckedChangeListener { compoundButton, b -> initModo(b) }
    }

    /**
     * Inicia los eventos de los botones
     */
    private fun initBotones() {
        mainBtnAccion.setOnClickListener {
            initDialogFoto()
        }

        mainBtnEliminar.setOnClickListener {
            eliminarImagen()
        }
    }

    /**
     * Iniciamos el modo de funcionamiento
     */
    private fun initModo(modo: Boolean) {
        PUBLICO = modo
        if (PUBLICO)
            mainTvModo.text = "MODO PÚBLICO"
        else
            mainTvModo.text = "MODO PRIVADO"
    }

    /**
     * Muestra el diálogo para tomar foto o elegir de la galería
     */
    private fun initDialogFoto() {
        val fotoDialogoItems = arrayOf(
            "Seleccionar fotografía de galería",
            "Capturar fotografía desde la cámara"
        )
        // Creamos el dialog con su builder
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Acción")
            .setItems(fotoDialogoItems) { dialog, modo ->
                when (modo) {
                    0 -> elegirFotoGaleria()
                    1 -> tomarFotoCamara()
                }
            }
            .show()
    }

    /**
     * Elige una foto de la galeria
     */
    private fun elegirFotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALERIA)
    }

    //Llamamos al intent de la camara
    // https://developer.android.com/training/camera/photobasics.html#TaskPath
    private fun tomarFotoCamara() {
        // Si queremos hacer uso de fotos en aklta calidad
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Eso para alta o baja
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = Utilidades.crearNombreFichero()
        // Salvamos el fichero
        val fichero = Utilidades.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, applicationContext)!!
        IMAGEN_URI = Uri.fromFile(fichero)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGEN_URI)
        // Esto para alta y baja
        startActivityForResult(intent, CAMARA)
    }

    /**
     * Siempre se ejecuta al realizar una acción
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("FOTO", "Opción::--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            Log.d("FOTO", "Se ha cancelado")
        }
        if (requestCode == GALERIA) {
            Log.d("FOTO", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                val contentURI = data.data!!
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
                    val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                    // Para jugar con las proporciones y ahorrar en memoria no cargando toda la foto, solo carga 600px max
                    val prop = PROPORCION / bitmap.width.toFloat()
                    // Actualizamos el bitmap para ese tamaño, luego podríamos reducir su calidad
                    val foto = Bitmap.createScaledBitmap(bitmap, PROPORCION, (bitmap.height * prop).toInt(), false)
                    Toast.makeText(this, "¡Foto rescatada de la galería!", Toast.LENGTH_SHORT).show()
                    mainIvImagen.setImageBitmap(bitmap)
                    mainTvPath.text = data.data.toString()

                    // Vamos a compiar nuestra imagen en nuestro directorio
                    Utilidades.copiarImagen(bitmap, IMAGEN_DIR, IMAGEN_COMPRES, applicationContext)


                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMARA) {
            Log.d("FOTO", "Entramos en Camara")
            // Cogemos la imagen, pero podemos coger la imagen o su modo en baja calidad (thumbnail)
            try {
                // Esta línea para baja calidad
                //thumbnail = (Bitmap) data.getExtras().get("data");
                // Esto para alta
                val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, IMAGEN_URI)
                val foto: Bitmap = ImageDecoder.decodeBitmap(source)

                // Vamos a probar a comprimir
                IMAGEN_COMPRES = mainSeekCompresion.progress * 10
                Utilidades.comprimirImagen(IMAGEN_URI.toFile(), foto, IMAGEN_COMPRES)

                // Si estamos en módo publico la añadimos en la biblioteca
                if (PUBLICO) {
                    // Por su queemos guardar el URI con la que se almacena en la Mediastore
                    IMAGEN_MEDIA_URI = Utilidades.añadirImagenGaleria(IMAGEN_URI, IMAGEN_NOMBRE, applicationContext)!!
                }

                // Mostramos
                mainIvImagen.setImageBitmap(foto)
                mainTvPath.text = IMAGEN_URI.toString()

                Toast.makeText(this, "¡Foto Salvada!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "¡Fallo Camara!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarImagen() {
        // La borramos de media
        // https://developer.android.com/training/data-storage/shared/media
        if (PUBLICO) {
            Utilidades.eliminarImageGaleria(IMAGEN_NOMBRE, applicationContext)
        }
        // La borramos del directorio
        try {
            Utilidades.eliminarImagen(IMAGEN_URI)
            Toast.makeText(this, "¡Foto Eliminada!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
        }
    }


    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos() {
        // Indicamos el permisos y el manejador de eventos de los mismos
        Dexter.withContext(this)
            // Lista de permisos a comprobar
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            // Listener a ejecutar
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // ccomprbamos si tenemos los permisos de todos ellos
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(applicationContext, "¡Todos los permisos concedidos!", Toast.LENGTH_SHORT).show()
                    }

                    // comprobamos si hay un permiso que no tenemos concedido ya sea temporal o permanentemente
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // abrimos un diálogo a los permisos
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { Toast.makeText(applicationContext, "Existe errores! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }


}