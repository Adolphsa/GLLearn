package com.lucas.gl.filter

import android.content.Context

/**
 * 灰色滤镜
 * 让RGB三个通道的颜色取均值
 * Created by lucas on 2021/6/21.
 */
class GrayFilter(context: Context) :
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
                float gray = (src.r + src.g + src.b) / 3.0;
                fragColor = vec4(gray, gray, gray, 1.0);
            }
            
        """
    }
}