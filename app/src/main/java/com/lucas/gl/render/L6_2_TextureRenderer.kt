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
 * 1.多次绘制，单纹理单元
 * Created by lucas on 2021/6/11.
 */
class L6_2_TextureRenderer(context: Context) : BaseRenderer(context){

    companion object {
        private val VERTEX_SHADER = """
           #version 300 es
            layout(location = 0) in vec4 a_Position;
            layout(location = 1) in vec2 a_TexCoord;
            
            out vec2 v_TexCoord;
            
            uniform mat4 u_Matrix;
            
            void main() {
                v_TexCoord = a_TexCoord;
                gl_Position = u_Matrix * a_Position;
            }
        """

        private val FRAGMENT_SHADER = """
           #version 300 es
            precision mediump float;
            in vec2 v_TexCoord;
            
            //sampler2D：二维纹理数据的数组
            uniform sampler2D u_TextureUnit;
            
            out vec4 fragColor;
            
            void main() {
                fragColor = texture(u_TextureUnit, v_TexCoord);
            }
        """

        private val POSITION_COMPONENT_COUNT = 2

        private val POINT_DATA2 = floatArrayOf(
            -0.5f, -0.5f,
            -0.5f, 0.5f,
            0.5f, 0.5f,
            0.5f, -0.5f)

        private val POINT_DATA = floatArrayOf(
            2 * -0.5f, -0.5f * 2,
            2 * -0.5f, 0.5f * 2,
            2 * 0.5f, 0.5f * 2,
            2 * 0.5f, -0.5f * 2)

        /**
         * 纹理坐标
         */
        private val TEX_VERTEX = floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f)

        /**
         * 纹理坐标中每个点占的向量个数
         */
        private val TEX_VERTEX_COMPONENT_COUNT = 2
    }

    private val mVertexData: FloatBuffer
    private val mVertexData2: FloatBuffer

    private var uTextureUnitLocation: Int = 0
    private val mTexVertexBuffer: FloatBuffer

    /**
     * 纹理数据
     */
    private var mTextureBean: TextureHelper.TextureBean? = null
    private var mTextureBean2: TextureHelper.TextureBean? = null
    private var mProjectionMatrixHelper: ProjectionMatrixHelper? = null

    init {
        mVertexData = BufferUtil.createFloatBuffer(POINT_DATA)
        mVertexData2 = BufferUtil.createFloatBuffer(POINT_DATA2)
        mTexVertexBuffer = BufferUtil.createFloatBuffer(TEX_VERTEX)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        mProjectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")
        uTextureUnitLocation = getUniform("u_TextureUnit")

        //纹理数据
        mTextureBean = TextureHelper.loadTexture(context, R.drawable.pikachu)
        mTextureBean2 = TextureHelper.loadTexture(context, R.drawable.squirtle)

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

        GLES30.glClearColor(0f, 0f, 0f, 1f)
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
    }

    private fun drawPikachu() {
        //配置顶点坐标
        mVertexData.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mVertexData)
        GLES30.glEnableVertexAttribArray(0)

        //设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureBean!!.textureId)
        // 将纹理单元传递片段着色器的u_TextureUnit
        GLES30.glUniform1i(uTextureUnitLocation, 0)
        GLES30.glDrawArrays(
            GLES30.GL_TRIANGLE_FAN,
            0,
            POINT_DATA.size/ POSITION_COMPONENT_COUNT
        )

    }

    private fun drawTuzki() {
        //配置顶点坐标
        mVertexData2.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mVertexData2)
        GLES30.glEnableVertexAttribArray(0)

        //绑定新的纹理ID到已激活的纹理单元上
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureBean2!!.textureId)
        GLES30.glDrawArrays(
            GLES30.GL_TRIANGLE_FAN,
            0,
            POINT_DATA.size/ POSITION_COMPONENT_COUNT
        )
    }
}