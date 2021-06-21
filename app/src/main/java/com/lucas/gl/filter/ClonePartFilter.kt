package com.lucas.gl.filter

import android.content.Context
import android.opengl.GLES30

/**
 * 部分克隆滤镜
 * Created by lucas on 2021/6/21.
 */
class ClonePartFilter(context: Context) :
    BaseFilter(context, VERTEX_SHADER, FRAGMENT_SHADER) {
    companion object {
        const val FRAGMENT_SHADER = """
                #version 300 es
                precision mediump float;
                
                in vec2 v_TexCoord;
                uniform sampler2D u_TextureUnit;
                uniform float isVertical;
                uniform float isHorizontal;
                uniform float cloneCount;
                
                out vec4 fragColor;
                
                void main() {
                    vec4 source  = texture(u_TextureUnit, v_TexCoord);
                    float coordX = v_TexCoord.x;
                    float coordY = v_TexCoord.y;
                    if (isVertical == 1.0) {
                        float width = 1.0 / cloneCount;
                        float startX = (1.0 - width) / 2.0;
                        coordX = mod(v_TexCoord.x, width) + startX;
                    }
                    if (isHorizontal == 1.0) {
                        float height = 1.0 / cloneCount;
                        float startY = (1.0 - height) / 2.0;
                        coordY = mod(v_TexCoord.y, height) + startY;
                    }
                    fragColor = texture(u_TextureUnit, vec2(coordX, coordY));
                }
            """
    }

    override fun onCreated() {
        super.onCreated()
        GLES30.glUniform1f(getUniform("isVertical"), 1.0f)
        GLES30.glUniform1f(getUniform("isHorizontal"), 1.0f)
        GLES30.glUniform1f(getUniform("cloneCount"), 3.0f)
    }
}