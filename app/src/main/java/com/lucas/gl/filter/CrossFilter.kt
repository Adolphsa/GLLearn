package com.lucas.gl.filter

import android.content.Context
import android.opengl.GLES20

/**
 * 交叉位移滤镜
 * Created by lucas on 2021/6/21.
 */
class CrossFilter(context: Context) :
    BaseFilter(context, VERTEX_SHADER, FRAGMENT_SHADER) {
        companion object{
            const val FRAGMENT_SHADER = """
                #version 300 es
                precision mediump float;
                
                in vec2 v_TexCoord;
                uniform sampler2D u_TextureUnit;
                uniform float xV; 
                uniform float yV; 
                
                out vec4 fragColor;
                
                vec2 translate(vec2 srcCoord, float x, float y) {
                    if (mod(srcCoord.y, 0.25) > 0.125) {
                        return vec2(srcCoord.x + x, srcCoord.y + y);
                    } else {
                        return vec2(srcCoord.x - x, srcCoord.y + y);
                    }
                }
                
                void main() {
                    vec2 offsetTexCoord = translate(v_TexCoord, xV, yV);
                    if (offsetTexCoord.x >= 0.0 && offsetTexCoord.x <= 1.0 &&
                        offsetTexCoord.y >= 0.0 && offsetTexCoord.y <= 1.0) {
                        fragColor = texture(u_TextureUnit, offsetTexCoord);
                    }
                }
              
            """
        }

    private var xLocation: Int = 0
    private var yLocation: Int = 0
    private var startTime: Long = 0

    override fun onCreated() {
        super.onCreated()
        startTime = System.currentTimeMillis()
        xLocation = getUniform("xV")
        yLocation = getUniform("yV")
    }

    override fun onDraw() {
        super.onDraw()
        val intensity = Math.sin((System.currentTimeMillis() - startTime) / 1000.0) * 0.5
        GLES20.glUniform1f(xLocation, intensity.toFloat())
        GLES20.glUniform1f(yLocation, 0.0f)
    }
}