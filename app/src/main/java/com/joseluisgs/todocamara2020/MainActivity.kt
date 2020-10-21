package com.joseluisgs.todocamara2020

import android.Manifest
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    // Constantes
    private val GALERIA = 1
    private val CAMARA = 2

    // Si vamos a operar en modo público o privado (es decir si salvamos en la galería o no)
    private val PRIVADO = true

    // Directorio para salvar las cosas
    private val IMAGE_DIRECTORY = "/camara2020"
    var photoURI: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
    }

    /**
     * Inicia la interfaz y los eventos de la apliación
     */
    private fun initUI() {
        // Iniciamos el Modo
        initModo()
        // Eventos botones
        initBotones()
        // Iniciamos los permisos
        initPermisos()
    }

    /**
     * Inicia los eventos de los botones
     */
    private fun initBotones() {
        mainBtnAccion.setOnClickListener {
            initDialogFoto()
        }
    }

    /**
     * Iniciamos el modo de funcionamiento
     */
    private fun initModo() {
        if (PRIVADO)
            mainTvModo.text = "MODO PRIVADO"
        else
            mainTvModo.text = "MODO PÚBLICO"
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

    private fun elegirFotoGaleria() {
        TODO("Not yet implemented")
    }

    private fun tomarFotoCamara() {
        TODO("Not yet implemented")
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