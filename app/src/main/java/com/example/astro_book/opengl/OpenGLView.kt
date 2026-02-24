package com.example.astro_book.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class OpenGLView(context: Context) : GLSurfaceView(context) {
    val renderer: OpenGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = OpenGLRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
