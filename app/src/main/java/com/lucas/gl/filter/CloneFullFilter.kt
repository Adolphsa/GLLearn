package com.lucas.gl.filter

import android.content.Context
import android.opengl.GLES30

/**
 * 完成克隆滤镜
 * 纹理坐标的范围通常是从(0, 0)到(1, 1)，
 * 那如果我们把纹理坐标设置在范围之外会发生什么？
 * OpenGL默认的行为是重复这个纹理图像（我们基本上忽略浮点纹理坐标的整数部分）
 * Created by lucas on 2021/6/21.
 */
class CloneFullFilter(context: Context) :
    BaseFilter(context, VERTEX_SHADER, FRAGMENT_SHADER) {
        companion object {
            const val FRAGMENT_SHADER = """
                #version 300 es
                precision mediump float;
                
                in vec2 v_TexCoord;
                uniform sampler2D u_TextureUnit;
                uniform float cloneCount;
                
                out vec4 fragColor;
                
                void main() {
                    fragColor = texture(u_TextureUnit, v_TexCoord * cloneCount);
                }
            """
        }

    override fun onCreated() {
        super.onCreated()
        GLES30.glUniform1f(getUniform("cloneCount"), 3.0f)
    }
}