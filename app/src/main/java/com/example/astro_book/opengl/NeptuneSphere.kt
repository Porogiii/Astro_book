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

class NeptuneSphere(private val context: Context) {
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
        uniform float uTime;

        attribute vec4 vPosition;
        attribute vec3 aNormal;
        attribute vec2 aTexCoord;

        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying vec2 vTexCoord;
        varying float vWave;

        float wave(vec3 pos, vec3 dir, float freq, float amp, float speed) {
            return amp * sin(dot(pos, dir) * freq + uTime * speed);
        }

        void main() {
            float displacement = 0.0;
            displacement += wave(vec3(vPosition), vec3(1.0, 0.5, 0.3), 4.0, 0.018, 1.2);
            displacement += wave(vec3(vPosition), vec3(-0.5, 1.0, 0.7), 6.0, 0.012, 1.8);
            displacement += wave(vec3(vPosition), vec3(0.3, -0.8, 1.0), 8.0, 0.008, 2.5);
            displacement += wave(vec3(vPosition), vec3(0.7,  0.6,-0.5), 11.0, 0.005, 3.0);

            vWave = displacement;
            vec4 displaced = vPosition + vec4(aNormal * displacement, 0.0);

            vec4 worldPos = uModelMatrix * displaced;
            vFragPos = vec3(worldPos);
            vNormal = uNormalMatrix * aNormal;
            
            vTexCoord = aTexCoord + vec2(
                sin(aTexCoord.y * 6.28 + uTime * 0.3) * 0.008,
                cos(aTexCoord.x * 6.28 + uTime * 0.25) * 0.006
            );

            gl_Position = uMVPMatrix * displaced;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;

        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying vec2 vTexCoord;
        varying float vWave;

        uniform sampler2D uTexture;
        uniform vec3 uLightPos;
        uniform vec3 uViewPos;

        void main() {
            vec3 norm     = normalize(vNormal);
            vec3 lightDir = normalize(uLightPos - vFragPos);
            vec3 viewDir  = normalize(uViewPos  - vFragPos);

            vec4 texColor = texture2D(uTexture, vTexCoord);

            vec3 ambient = 0.18 * vec3(texColor);

            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * vec3(texColor) * 1.1;

            vec3 halfDir = normalize(lightDir + viewDir);
            float spec = pow(max(dot(norm, halfDir), 0.0), 120.0);
            vec3 specular = spec * vec3(0.5, 0.7, 1.0) * 0.7;

            float crest = smoothstep(0.02, 0.04, vWave);
            vec3 crestColor = vec3(0.4, 0.65, 1.0) * crest * 0.3;

            vec3 result = ambient + diffuse + specular + crestColor;
            gl_FragColor = vec4(result, 1.0);
        }
    """.trimIndent()

    init {
        val vertices = mutableListOf<Float>()
        val normals  = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices  = mutableListOf<Short>()

        for (i in 0..stacks) {
            val stackAngle = PI / 2 - i * PI / stacks
            val xy = radius * cos(stackAngle)
            val z  = radius * sin(stackAngle)

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
                k1++; k2++
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
        textureId = loadTexture(R.drawable.neptune)
    }

    fun draw(mvpMatrix: FloatArray, modelMatrix: FloatArray, time: Float) {
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

        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uTime"), time)
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
        val vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vs, vertexCode)
        GLES20.glCompileShader(vs)
        val fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fs, fragmentCode)
        GLES20.glCompileShader(fs)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        return prog
    }
}
