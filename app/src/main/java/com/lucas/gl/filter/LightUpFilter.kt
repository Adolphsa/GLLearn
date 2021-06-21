package com.lucas.gl.filter

import android.content.Context
import android.opengl.GLES30
import kotlin.math.abs
import kotlin.math.sin

/**
 * 发光滤镜
 * Created by lucas on 2021/6/21.
 */
class LightUpFilter(context: Context) :
    BaseFilter(context, VERTEX_SHADER, FRAGMENT_SHADER) {

        companion object {
            const val FRAGMENT_SHADER = """
                #version 300 es
                precision mediump float;
                
                in vec2 v_TexCoord;
                uniform sampler2D u_TextureUnit;
                uniform float intensity;
                
                out vec4 fragColor;
                
                void main() {
                    vec4 src = texture(u_TextureUnit, v_TexCoord);
                    vec4 addColor = vec4(intensity, intensity, intensity, 1.0);
                    fragColor = src + addColor;
                }
            """
        }

    private var intensityLocation: Int = 0
    private var startTime: Long = 0

    override fun onCreated() {
        super.onCreated()
        startTime = System.currentTimeMillis()
        intensityLocation = getUniform("intensity")
    }

    override fun onDraw() {
        super.onDraw()
        val intensity = Math.abs(Math.sin((System.currentTimeMillis() - startTime) / 1000.0)) / 4.0
        GLES30.glUniform1f(intensityLocation, intensity.toFloat())
    }
}