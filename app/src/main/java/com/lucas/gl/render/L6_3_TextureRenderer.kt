package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import com.lucas.gl.R
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.base.GLConstants.POSITION_COMPONENT_COUNT
import com.lucas.gl.base.GLConstants.TEX_VERTEX_COMPONENT_COUNT
import com.lucas.gl.utils.BufferUtil
import com.lucas.gl.utils.ProjectionMatrixHelper
import com.lucas.gl.utils.TextureHelper
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 蒙版遮罩
 * Created by lucas on 2021/6/17.
 */
class L6_3_TextureRenderer(context: Context) : BaseRenderer(context) {

    companion object {
        val VERTEX_SHADER = """
           #version 300 es
            layout(location = 0) in vec4 a_Position;
            layout(location = 1) in vec2 a_TexCoord;
            layout(location = 2) in vec2 a_TexCoord2;
            
            out vec2 v_TexCoord;
            out vec2 v_TexCoord2;
            
            uniform mat4 u_Matrix;
            
            void main() {
                v_TexCoord = a_TexCoord;
                v_TexCoord2 = a_TexCoord2;
                gl_Position = u_Matrix * a_Position;
            }
        """
        val FRAGMENT_SHADER = """
            #version 300 es
            precision mediump float;
            in vec2 v_TexCoord;
            in vec2 v_TexCoord2;
            
            uniform sampler2D u_TextureUnit1;
            uniform sampler2D u_TextureUnit2;
            uniform sampler2D u_TextureUnit3;
            
            out vec4 fragColor;
            
            bool isOutRect(vec2 coord) {
                return coord.x < 0.0 || coord.x > 1.0 || coord.y < 0.0 || coord.y > 1.0;
            }
            
            void main() {
                vec4 texture1 = texture(u_TextureUnit1, v_TexCoord);
                vec4 texture2 = texture(u_TextureUnit2, v_TexCoord2);
                vec4 texture3 = texture(u_TextureUnit3, v_TexCoord2);
                
                bool isOut1 = isOutRect(v_TexCoord);
                bool isOut2 = isOutRect(v_TexCoord2);
                
                if (isOut2) {
                    // 贴纸范围外
                    if (!isOut1) {
                        // 背景范围内，绘制背景
                        fragColor = texture1;
                    }
                } else {
                    // 贴纸范围内
                    if (texture3.r == 0.0) {
                        // 蒙版内，画贴纸
                        fragColor = texture2;
                    } else if (!isOut1) {
                        // 蒙版外，背景内，画背景
                        fragColor = texture1;
                    }
                }
            }
            
        """

        private val DEFAULT_VERTEX_DATA = floatArrayOf(
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
        )

        private val PIKACHU_VERTEX_DATA = floatArrayOf(
            -0.2f, -0.2f,
            -0.2f, 0.2f,
            0.2f, 0.2f,
            0.2f, -0.2f
        )

        private val TEXTURE_DATA = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
    }

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private val textureBuffer2: FloatBuffer
    lateinit var projectionMatrixHelper: ProjectionMatrixHelper
    private var textureLocation1: Int = 0
    private var textureLocation2: Int = 0
    private var textureLocation3: Int = 0
    private var pikachuBean: TextureHelper.TextureBean? = null
    private var maskBean: TextureHelper.TextureBean? = null
    private var squirtleBean: TextureHelper.TextureBean? = null

    init {
        vertexBuffer = BufferUtil.createFloatBuffer(DEFAULT_VERTEX_DATA)
        val vertexToTexture = vertexToTexture(PIKACHU_VERTEX_DATA)
        textureBuffer = BufferUtil.createFloatBuffer(TEXTURE_DATA)
        textureBuffer2 = BufferUtil.createFloatBuffer(vertexToTexture)
    }

    fun vertexToTexture(vertex: FloatArray): FloatArray {
        return floatArrayOf(
            -(vertex[2] + 1.0f) / 2.0f, 2 - (vertex[3] + 1.0f) / 2.0f,
            -(vertex[0] + 1.0f) / 2.0f, -(vertex[1] + 1.0f) / 2.0f,
            2 - (vertex[6] + 1.0f) / 2.0f, -(vertex[7] + 1.0f) / 2.0f,
            2 - (vertex[4] + 1.0f) / 2.0f, 2 - (vertex[5] + 1.0f) / 2.0f
        )
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 0f, 0f, 1f)
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        projectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")
        textureLocation1 = getUniform("u_TextureUnit1")
        textureLocation2 = getUniform("u_TextureUnit2")
        textureLocation3 = getUniform("u_TextureUnit3")

        pikachuBean = TextureHelper.loadTexture(context, R.drawable.pikachu)
        maskBean = TextureHelper.loadTexture(context, R.drawable.square)
        squirtleBean = TextureHelper.loadTexture(context, R.drawable.squirtle)

        //配置顶点坐标
        vertexBuffer.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(0)

        //加载纹理坐标
        textureBuffer.position(0)
        GLES30.glVertexAttribPointer(
            1,
            TEX_VERTEX_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            textureBuffer
        )
        GLES30.glEnableVertexAttribArray(1)

        //加载纹理坐标2
        textureBuffer2.position(0)
        GLES30.glVertexAttribPointer(
            2,
            TEX_VERTEX_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            textureBuffer2
        )
        GLES30.glEnableVertexAttribArray(2)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        GLES30.glViewport(0, 0, width, height)
        projectionMatrixHelper.enable(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES30.glEnable(GL10.GL_BLEND)
        GLES30.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)

        drawPikachu()
        drawMask()
        drawTuzki()

        GLES30.glDrawArrays(
            GLES30.GL_TRIANGLE_FAN,
            0,
            DEFAULT_VERTEX_DATA.size / POSITION_COMPONENT_COUNT
        )
    }

    private fun drawPikachu() {

        //设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, pikachuBean!!.textureId)
        // 将纹理单元传递片段着色器的u_TextureUnit
        GLES30.glUniform1i(textureLocation1, 0)

    }

    private fun drawTuzki() {
        //设置当前活动的纹理单元为纹理单元1
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, squirtleBean!!.textureId)
        GLES30.glUniform1i(textureLocation2, 1)
    }

    private fun drawMask() {
        //设置当前活动的纹理单元为纹理单元2
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, maskBean!!.textureId)
        GLES30.glUniform1i(textureLocation3, 2)
    }
}