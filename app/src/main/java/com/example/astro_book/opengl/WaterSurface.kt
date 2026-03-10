package com.example.astro_book.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class WaterSurface {
    private val gridSize = 80
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int
    private var program: Int = 0
    private var time: Float = 0f

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform float uTime;
        attribute vec2 aPosition;

        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying float vHeight;

        float wave(vec2 pos, vec2 dir, float freq, float amp, float speed, float sharp) {
            float phase = dot(normalize(dir), pos) * freq + uTime * speed;

            return amp * pow((sin(phase) + 1.0) * 0.5, sharp) * 2.0 - amp;
        }

        float waveHeight(vec2 pos) {
            float h = 0.0;

            h += wave(pos, vec2(1.0,  0.4), 1.8, 0.08, 0.8, 1.8);
            h += wave(pos, vec2(0.6, -1.0), 2.3, 0.06, 1.1, 1.6);

            h += wave(pos, vec2(-1.0, 0.7), 3.5, 0.035, 1.4, 1.3);
            h += wave(pos, vec2(0.3,  1.0), 4.1, 0.025, 1.7, 1.2);

            h += wave(pos, vec2(1.0, -0.3), 7.0, 0.012, 2.5, 1.0);
            h += wave(pos, vec2(-0.5, 1.0), 9.0, 0.008, 3.0, 1.0);
            h += wave(pos, vec2(0.8,  0.6), 11.0, 0.005, 3.5, 1.0);
            return h;
        }

        void main() {
            float z = waveHeight(aPosition);
            vHeight = z;
            vFragPos = vec3(aPosition.x, aPosition.y, z);

            float eps = 0.015;
            float hL = waveHeight(aPosition - vec2(eps, 0.0));
            float hR = waveHeight(aPosition + vec2(eps, 0.0));
            float hD = waveHeight(aPosition - vec2(0.0, eps));
            float hU = waveHeight(aPosition + vec2(0.0, eps));
            vNormal = normalize(vec3(hL - hR, hD - hU, 2.0 * eps));

            gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, z, 1.0);
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;

        varying vec3 vNormal;
        varying vec3 vFragPos;
        varying float vHeight;

        void main() {
            vec3 lightDir  = normalize(vec3(0.5, 0.3, 1.0));
            vec3 viewDir   = normalize(vec3(0.0, -0.8, 1.0));
            vec3 norm      = normalize(vNormal);

            vec3 deepColor  = vec3(0.002, 0.018, 0.12);
            vec3 midColor   = vec3(0.005, 0.07,  0.28);
            vec3 crestColor = vec3(0.02,  0.22,  0.55);
            float t = clamp((vHeight + 0.18) / 0.36, 0.0, 1.0);
            vec3 baseColor = mix(deepColor, mix(midColor, crestColor, t * t), t);

            vec3 ambient = 0.15 * baseColor;

            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * vec3(0.4, 0.55, 0.85) * baseColor * 2.5;

            vec3 halfDir = normalize(lightDir + viewDir);
            float spec = pow(max(dot(norm, halfDir), 0.0), 220.0);
            vec3 specular = spec * vec3(0.6, 0.75, 1.0) * 0.5;

            float fresnel = pow(1.0 - max(dot(norm, viewDir), 0.0), 4.0);
            vec3 fresnelColor = fresnel * vec3(0.03, 0.10, 0.30) * 0.4;

            float foam = smoothstep(0.14, 0.20, vHeight);
            vec3 foamColor = vec3(0.6, 0.75, 0.9);

            vec3 result = ambient + diffuse + specular + fresnelColor;
            result = mix(result, foamColor, foam * 0.25);

            gl_FragColor = vec4(result, 1.0);
        }
    """.trimIndent()


    init {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        val step = 2.0f / gridSize
        for (row in 0..gridSize) {
            for (col in 0..gridSize) {
                vertices.add(-1.0f + col * step)
                vertices.add(-1.0f + row * step)
            }
        }

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val tl = (row * (gridSize + 1) + col).toShort()
                val tr = (row * (gridSize + 1) + col + 1).toShort()
                val bl = ((row + 1) * (gridSize + 1) + col).toShort()
                val br = ((row + 1) * (gridSize + 1) + col + 1).toShort()
                indices.add(tl); indices.add(bl); indices.add(tr)
                indices.add(tr); indices.add(bl); indices.add(br)
            }
        }

        indexCount = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
            .apply { vertices.forEach { put(it) }; position(0) }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
            .apply { indices.forEach { put(it) }; position(0) }

        program = createProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun update(deltaTime: Float) {
        time += deltaTime
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val posHandle = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(posHandle)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, mvpMatrix, 0
        )
        GLES20.glUniform1f(
            GLES20.glGetUniformLocation(program, "uTime"), time
        )

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )

        GLES20.glDisableVertexAttribArray(posHandle)
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