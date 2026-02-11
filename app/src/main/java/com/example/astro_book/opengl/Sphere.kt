package com.example.astro_book.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class Sphere(
    private val radius: Float,
    private val stacks: Int = 30,
    private val slices: Int = 30
) {
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val texCoordBuffer: FloatBuffer
    private var program: Int
    private val indexCount: Int
    private val vertexCount: Int
    private var textureId: Int = 0

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    init {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()
        val texCoords = mutableListOf<Float>()

        for (i in 0..stacks) {
            val stackAngle = PI / 2 - i * PI / stacks
            val xy = radius * cos(stackAngle)
            val z = radius * sin(stackAngle)

            for (j in 0..slices) {
                val sectorAngle = j * 2 * PI / slices
                val x = xy * cos(sectorAngle)
                val y = xy * sin(sectorAngle)

                vertices.add(x.toFloat())
                vertices.add(y.toFloat())
                vertices.add(z.toFloat())

                val s = j.toFloat() / slices
                val t = i.toFloat() / stacks
                texCoords.add(s)
                texCoords.add(t)
            }
        }

        vertexCount = vertices.size / 3

        for (i in 0 until stacks) {
            var k1 = i * (slices + 1)
            var k2 = k1 + slices + 1

            for (j in 0 until slices) {
                if (i != 0) {
                    indices.add(k1.toShort())
                    indices.add(k2.toShort())
                    indices.add((k1 + 1).toShort())
                }

                if (i != stacks - 1) {
                    indices.add((k1 + 1).toShort())
                    indices.add(k2.toShort())
                    indices.add((k2 + 1).toShort())
                }

                k1++
                k2++
            }
        }

        indexCount = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                vertices.forEach { put(it) }
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                indices.forEach { put(it) }
                position(0)
            }
        }

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                texCoords.forEach { put(it) }
                position(0)
            }
        }

        program = createProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun loadTexture(context: Context, resourceId: Int) {
        textureId = loadTextureFromResource(context, resourceId)
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        texCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadTextureFromResource(context: Context, resourceId: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        if (textureIds[0] == 0) {
            return 0
        }

        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        return textureIds[0]
    }

    private fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)

        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
