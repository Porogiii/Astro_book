package com.example.astro_book.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var square: Square
    private lateinit var solarSystem: SolarSystem

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var lastTime = System.currentTimeMillis()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        square = Square(context)
        solarSystem = SolarSystem(context)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastTime) / 1000f
        lastTime = currentTime

        solarSystem.update(deltaTime)

        // квадрат-фон
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        val squareMatrix = FloatArray(16)
        Matrix.setIdentityM(squareMatrix, 0)
        Matrix.translateM(squareMatrix, 0, 0f, 0f, -15f)
        Matrix.scaleM(squareMatrix, 0, 12f, 12f, 1f)

        val squareMVP = FloatArray(16)
        Matrix.multiplyMM(squareMVP, 0, mvpMatrix, 0, squareMatrix, 0)
        square.draw(squareMVP)

        // солнечная система
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 2.5f, 6f,
            0f, 0f, 0f,
            0f, 1.0f, 0.0f
        )
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        solarSystem.draw(mvpMatrix)
    }
}
