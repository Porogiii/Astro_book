package com.example.astro_book.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var square: Square
    private lateinit var cube: Cube

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        square = Square(context)
        cube = Cube()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        val squareMatrix = FloatArray(16)
        Matrix.setIdentityM(squareMatrix, 0)
        Matrix.translateM(squareMatrix, 0, 0f, 0f, -10f)
        Matrix.scaleM(squareMatrix, 0, 8f, 8f, 1f)

        val squareMVP = FloatArray(16)
        Matrix.multiplyMM(squareMVP, 0, mvpMatrix, 0, squareMatrix, 0)
        square.draw(squareMVP)

        val cubeMatrix = FloatArray(16)
        Matrix.setIdentityM(cubeMatrix, 0)
        Matrix.translateM(cubeMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(cubeMatrix, 0, System.currentTimeMillis() % 10000L / 10000f * 360f, 1f, 1f, 0f)

        val cubeMVP = FloatArray(16)
        Matrix.multiplyMM(cubeMVP, 0, mvpMatrix, 0, cubeMatrix, 0)
        cube.draw(cubeMVP)
    }
}
