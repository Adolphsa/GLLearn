package com.lucas.gl.filter

import android.content.Context

/**
 * 反色滤镜
 * RGB三个通道的颜色都取反，而alpha通道不变。
 * Created by lucas on 2021/6/18.
 */
class InverseFilter(context: Context) :
    BaseFilter(context, VERTEX_SHADER, FRAGMENT_SHADER) {
        companion object {
            const val FRAGMENT_SHADER = """
                #version 300 es
                precision mediump float;
                
                in vec2 v_TexCoord;
                uniform sampler2D u_TextureUnit;
                
                out vec4 fragColor;
                
                void main() {
                    vec4 src = texture(u_TextureUnit, v_TexCoord);
                    fragColor = vec4(1.0 - src.r, 1.0 - src.g, 1.0 - src.b, 1.0);
                }
            """
        }
}