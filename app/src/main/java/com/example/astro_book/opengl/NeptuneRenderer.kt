package com.example.astro_book.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NeptuneRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private lateinit var neptuneSphere: NeptuneSphere

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private var rotationAngle = 0f
    private var time = 0f
    private var lastTime = System.currentTimeMillis()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.02f, 0.02f, 0.08f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        neptuneSphere = NeptuneSphere(context)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastTime) / 1000f
        lastTime = currentTime

        rotationAngle += 0.3f
        time += deltaTime

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0f, 1f, 0f)

        val finalMVP = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(finalMVP, 0, mvpMatrix, 0, modelMatrix, 0)

        neptuneSphere.draw(finalMVP, modelMatrix, time)
    }
}
