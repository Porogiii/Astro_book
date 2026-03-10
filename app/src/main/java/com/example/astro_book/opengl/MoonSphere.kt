package com.example.astro_book.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.astro_book.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MoonSphere(private val context: Context) {
    private val radius = 1.0f
    private val stacks = 64
    private val slices = 64

    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int
    private var textureId: Int = 0
    private var program: Int = 0

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        uniform mat3 uNormalMatrix;

        attribute vec4 vPosition;
        attribute vec3 aNormal;
        attribute vec2 aTexCoord;

        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying vec2 vTexCoord;

        void main() {
            vec4 worldPos = uModelMatrix * vPosition;
            vFragPos = vec3(worldPos);
            vNormal = uNormalMatrix * aNormal;
            vTexCoord = aTexCoord;
            gl_Position = uMVPMatrix * vPosition;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;

        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying vec2 vTexCoord;

        uniform sampler2D uTexture;
        uniform vec3 uLightPos;
        uniform vec3 uViewPos;

        void main() {
            float ambientStrength = 0.15;
            vec3 ambient = ambientStrength * vec3(1.0, 1.0, 1.0);

            vec3 norm = normalize(vNormal);
            vec3 lightDir = normalize(uLightPos - vFragPos);
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * vec3(1.0, 1.0, 1.0);

            float specularStrength = 0.5;
            vec3 viewDir = normalize(uViewPos - vFragPos);
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
            vec3 specular = specularStrength * spec * vec3(1.0, 1.0, 1.0);

            vec4 texColor = texture2D(uTexture, vTexCoord);
            vec3 result = (ambient + diffuse + specular) * vec3(texColor);
            gl_FragColor = vec4(result, 1.0);
        }
    """.trimIndent()

    init {
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

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

                normals.add((x / radius).toFloat())
                normals.add((y / radius).toFloat())
                normals.add((z / radius).toFloat())

                texCoords.add(j.toFloat() / slices)
                texCoords.add(i.toFloat() / stacks)
            }
        }

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

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .apply { vertices.forEach { put(it) }; position(0) }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .apply { normals.forEach { put(it) }; position(0) }

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .apply { texCoords.forEach { put(it) }; position(0) }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
            .apply { indices.forEach { put(it) }; position(0) }

        program = createProgram(vertexShaderCode, fragmentShaderCode)
        textureId = loadTexture(R.drawable.moon)
    }

    fun draw(mvpMatrix: FloatArray, modelMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(posHandle)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        val normHandle = GLES20.glGetAttribLocation(program, "aNormal")
        GLES20.glEnableVertexAttribArray(normHandle)
        normalBuffer.position(0)
        GLES20.glVertexAttribPointer(normHandle, 3, GLES20.GL_FLOAT, false, 12, normalBuffer)

        val texHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texHandle)
        texCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(program, "uModelMatrix"), 1, false, modelMatrix, 0)

        val normalMatrix = floatArrayOf(
            modelMatrix[0], modelMatrix[1], modelMatrix[2],
            modelMatrix[4], modelMatrix[5], modelMatrix[6],
            modelMatrix[8], modelMatrix[9], modelMatrix[10]
        )
        GLES20.glUniformMatrix3fv(
            GLES20.glGetUniformLocation(program, "uNormalMatrix"), 1, false, normalMatrix, 0)

        GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "uLightPos"), 3.0f, 3.0f, 3.0f)
        GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "uViewPos"), 0f, 0f, 3f)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(normHandle)
        GLES20.glDisableVertexAttribArray(texHandle)
    }

    private fun loadTexture(resourceId: Int): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        return ids[0]
    }

    private fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also {
            GLES20.glShaderSource(it, vertexCode)
            GLES20.glCompileShader(it)
        }
        val fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also {
            GLES20.glShaderSource(it, fragmentCode)
            GLES20.glCompileShader(it)
        }
        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }
    }
}
