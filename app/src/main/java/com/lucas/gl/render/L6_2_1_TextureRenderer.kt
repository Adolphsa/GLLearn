package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import com.lucas.gl.R
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.utils.BufferUtil
import com.lucas.gl.utils.ProjectionMatrixHelper
import com.lucas.gl.utils.TextureHelper
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 多纹理绘制
 * 2.单次绘制，多纹理单元
 * Created by lucas on 2021/6/11.
 */
class L6_2_1_TextureRenderer(context: Context) : BaseRenderer(context) {

    companion object {
        private val VERTEX_SHADER = """
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

        private val FRAGMENT_SHADER = """
           #version 300 es
            precision mediump float;
            in vec2 v_TexCoord;
            in vec2 v_TexCoord2;
            
            //sampler2D：二维纹理数据的数组
            uniform sampler2D u_TextureUnit1;
            uniform sampler2D u_TextureUnit2;
            
            out vec4 fragColor;
            
            bool isOutRect(vec2 coord) {
                return coord.x < 0.0 || coord.x > 1.0 || coord.y < 0.0 || coord.y > 1.0;
            }
            
            void main() {
                //皮卡丘
                vec4 texture1 = texture(u_TextureUnit1, v_TexCoord);
                //杰尼龟
                vec4 texture2 = texture(u_TextureUnit2, v_TexCoord2);
                bool isOut = isOutRect(v_TexCoord2);
                
                if (isOut) {
                    fragColor = texture1;
                } else {
                    fragColor = texture2;
                }
                
            }
        """

        private val POSITION_COMPONENT_COUNT = 2

        private val DEFAULT_VERTEX_DATA = floatArrayOf(
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
        )

        /**
         * 纹理坐标
         */
        private val TEX_VERTEX = floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f
        )

        private val TEX_VERTEX2 = floatArrayOf(
            -0.4f, 1.4f,
            -0.4f, -0.4f,
            1.4f, -0.4f,
            1.4f, 1.4f
        )

        /**
         * 纹理坐标中每个点占的向量个数
         */
        private val TEX_VERTEX_COMPONENT_COUNT = 2
    }

    private val mVertexData: FloatBuffer

    private var uTextureUnitLocation1: Int = 0
    private var uTextureUnitLocation2: Int = 0
    private val mTexVertexBuffer: FloatBuffer
    private val mTexVertexBuffer2: FloatBuffer

    /**
     * 纹理数据
     */
    private var mTextureBean: TextureHelper.TextureBean? = null
    private var mTextureBean2: TextureHelper.TextureBean? = null
    private var mProjectionMatrixHelper: ProjectionMatrixHelper? = null

    init {
        mVertexData = BufferUtil.createFloatBuffer(DEFAULT_VERTEX_DATA)
//        val vertexToTexture = vertexToTexture(PIKACHU_VERTEX_DATA)
        mTexVertexBuffer = BufferUtil.createFloatBuffer(TEX_VERTEX)
        mTexVertexBuffer2 = BufferUtil.createFloatBuffer(TEX_VERTEX2)
    }



    fun vertexToTexture(vertex: FloatArray): FloatArray {
        return floatArrayOf(
            -0.4f, 1.4f,
            -0.4f, -0.4f,
            1.4f, -0.4f,
            1.4f, 1.4f
        )
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        mProjectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")
        uTextureUnitLocation1 = getUniform("u_TextureUnit1")
        uTextureUnitLocation2 = getUniform("u_TextureUnit2")

        //纹理数据
        mTextureBean = TextureHelper.loadTexture(context, R.drawable.pikachu)
        mTextureBean2 = TextureHelper.loadTexture(context, R.drawable.squirtle)

        //配置顶点坐标
        mVertexData.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mVertexData
        )
        GLES30.glEnableVertexAttribArray(0)

        //加载纹理坐标
        mTexVertexBuffer.position(0)
        GLES30.glVertexAttribPointer(
            1,
            TEX_VERTEX_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mTexVertexBuffer)
        GLES30.glEnableVertexAttribArray(1)

        //加载纹理坐标2
        mTexVertexBuffer2.position(0)
        GLES30.glVertexAttribPointer(
            2,
            TEX_VERTEX_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mTexVertexBuffer2)
        GLES30.glEnableVertexAttribArray(2)

        GLES30.glClearColor(1f, 0f, 0f, 1f)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES30.glEnable(GL10.GL_BLEND)
        GLES30.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        GLES30.glViewport(0, 0, width, height)
        mProjectionMatrixHelper!!.enable(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        drawPikachu()
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
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureBean!!.textureId)
        // 将纹理单元传递片段着色器的u_TextureUnit
        GLES30.glUniform1i(uTextureUnitLocation1, 0)

    }

    private fun drawTuzki() {
        //设置当前活动的纹理单元为纹理单元1
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureBean2!!.textureId)
        GLES30.glUniform1i(uTextureUnitLocation2, 1)
    }
}