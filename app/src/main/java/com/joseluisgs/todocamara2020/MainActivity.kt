package com.joseluisgs.todocamara2020

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iniciarUI()
    }

    /**
     * Inicia la interfaz y los eventos de la apliación
     */
    private fun iniciarUI() {
        // Iniciamos los permisos
        iniciarPermisos()
    }

    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun iniciarPermisos() {
        TODO("Not yet implemented")
    }


}