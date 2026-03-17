package com.example.astro_book.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class BlackHole {
    private val vertexBuffer: FloatBuffer
    private val texBuffer: FloatBuffer
    private val program: Int

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
        
        void main() {
            vec2 uv = vTexCoord * 2.0 - 1.0;
            float r = length(uv);
            float eventHorizon = 0.18;
            float diskInner = 0.19;
            float diskOuter = 0.75;
        
            if (r < eventHorizon) {
                gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
                return;
            }
        

            if (r < diskOuter) {
                float flatten = 0.18;
                float band = exp(-pow(uv.y / (flatten * r + 0.01), 2.0));
        
                float radial = smoothstep(diskOuter, diskInner, r);
        
                float intensity = band * radial * 2.2;
        
                float t = smoothstep(diskInner, diskOuter, r);
                vec3 col = mix(vec3(1.0, 0.95, 0.8), vec3(0.9, 0.3, 0.05), t);
        
                float alpha = clamp(intensity, 0.0, 1.0);
        
                float edgeMask = smoothstep(eventHorizon, eventHorizon + 0.02, r);
                gl_FragColor = vec4(col * intensity * edgeMask, alpha * edgeMask);
                return;
            }
        
            gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        }

    """.trimIndent()

    private val coords = floatArrayOf(
        -0.6f,  0.6f, 0f,
        -0.6f, -0.6f, 0f,
        0.6f, -0.6f, 0f,
        0.6f,  0.6f, 0f
    )
    private val texCoords = floatArrayOf(
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(coords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(coords); position(0)
            }
        texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(texCoords); position(0)
            }
        program = createProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glUseProgram(program)
        val posHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        val texHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texHandle)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 8, texBuffer)
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, mvpMatrix, 0
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
        GLES20.glDisableVertexAttribArray(posHandle)
        GLES20.glDisableVertexAttribArray(texHandle)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun createProgram(vsCode: String, fsCode: String) = GLES20.glCreateProgram()
        .also { prog ->
            GLES20.glAttachShader(prog, loadShader(GLES20.GL_VERTEX_SHADER, vsCode))
            GLES20.glAttachShader(prog, loadShader(GLES20.GL_FRAGMENT_SHADER, fsCode))
            GLES20.glLinkProgram(prog)
        }

    private fun loadShader(type: Int, code: String) = GLES20.glCreateShader(type)
        .also { shader -> GLES20.glShaderSource(shader, code); GLES20.glCompileShader(shader) }
}
