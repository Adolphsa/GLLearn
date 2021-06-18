package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import com.lucas.gl.R
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.base.GLConstants
import com.lucas.gl.utils.BufferUtil
import com.lucas.gl.utils.ProjectionMatrixHelper
import com.lucas.gl.utils.TextureHelper
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * FrameBuffer的使用： 屏幕外渲染 - RenderBuffer
 * Created by lucas on 2021/6/18.
 */
class L7_2_FBORenderer(context: Context) : BaseRenderer(context) {

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
            uniform sampler2D u_TextureUnit;
            
            out vec4 fragColor;
            
            void main() {
                vec4 pic = texture(u_TextureUnit, v_TexCoord);
                float gray = 1.0f - (pic.r + pic.g + pic.b)/3.0f;
                fragColor = vec4(gray, gray, gray, pic.a);
            }
        """

        //顶点坐标
        private val POINT_DATA = floatArrayOf(
            -1f, -1f,
            -1f, 1f,
            1f, 1f,
            1f, -1f
        )

        //纹理坐标
        private val TEX_VERTEX = floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f
        )
    }

    private val mVertexData: FloatBuffer
    private val mTexVertexBuffer: FloatBuffer
    private var uMatrixLocation: Int = 0
    private var uTextureUnitLocation: Int = 0

    private val mProjectionMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    //纹理数据
    private var mTextureBean: TextureHelper.TextureBean? = null

    private val mFrameBuffer = IntArray(1)
    private val mRenderBuffer = IntArray(1)
    private val mTexture = IntArray(1)

    init {
        mVertexData = BufferUtil.createFloatBuffer(POINT_DATA)
        mTexVertexBuffer = BufferUtil.createFloatBuffer(TEX_VERTEX)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        uMatrixLocation = getUniform("u_Matrix")
        uTextureUnitLocation = getUniform("u_TextureUnit")

        mTextureBean = TextureHelper.loadTexture(context, R.drawable.pikachu)

        //顶点坐标
        mVertexData.position(0)
        GLES30.glVertexAttribPointer(
            0,
            GLConstants.POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mVertexData
        )
        GLES30.glEnableVertexAttribArray(0)

        //纹理坐标
        mTexVertexBuffer.position(0)
        GLES30.glVertexAttribPointer(
            1,
            GLConstants.TEX_VERTEX_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mTexVertexBuffer
        )
        GLES30.glEnableVertexAttribArray(1)

        // 由于Android屏幕上绘制的起始点在左上角，而GL纹理坐标是在左下角，所以需要进行水平翻转，即Y轴翻转
        Matrix.scaleM(mProjectionMatrix, 0, 1f, -1f, 1f)

        GLES30.glClearColor(0f, 0f, 0f, 0f)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES30.glEnable(GL10.GL_BLEND)
        GLES30.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!isReadCurrentFrame) {
            return
        }

        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

        //1. 创建FrameBuffer、RenderBuffer、纹理对象
        createEnv()
        //2.配置FrameBuffer相关的绘制存储信息，并且绑定到当前的绘制环境上
        bindFrameBufferInfo()
        // 3. 更新视图区域
        GLES30.glViewport(0, 0, mTextureBean!!.width, mTextureBean!!.height)
        // 4. 绘制图片
        drawTexture()
        // 5. 读取当前画面上的像素信息
        onReadPixel(0, 0, mTextureBean!!.width, mTextureBean!!.height)
        // 6. 解绑FrameBuffer
//        unbindFrameBufferInfo()
        // 7. 删除FrameBuffer、纹理对象
        deleteEnv()
    }

    private fun createEnv() {
        // 一：RenderBuffer
        // 1. 创建RenderBuffer
        GLES30.glGenRenderbuffers(1, mRenderBuffer, 0)
        // 2. 绑定RenderBuffer
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, mRenderBuffer[0])
        // 3. 将RenderBuffer设置为深度类型，并设置大小
        GLES20.glRenderbufferStorage(
            GLES30.GL_RENDERBUFFER,
            GLES30.GL_DEPTH_COMPONENT16,
            mTextureBean!!.width,
            mTextureBean!!.height
        )
        // 4. 设置当前的RenderBuffer来存储FrameBuffer的深度信息
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_DEPTH_ATTACHMENT,
            GLES30.GL_RENDERBUFFER,
            mRenderBuffer[0]
        )
        // 5. 解绑RenderBuffer
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0)

        // 二：FrameBuffer
        //1.创建FrameBuffer
        GLES30.glGenFramebuffers(1, mFrameBuffer, 0)
        //2.1 生成纹理对象
        GLES30.glGenTextures(1, mTexture, 0)
        //2.2 绑定纹理对象
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTexture[0])
        //2.3 设置纹理对象的相关信息：颜色模式 大小
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA,
            mTextureBean!!.width,
            mTextureBean!!.height,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            null
        )
        //2.4 纹理过滤参数设置
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_NEAREST.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        //2.5 解绑当前纹理，避免后续无关的操作影响了纹理内容
//        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    private fun bindFrameBufferInfo() {
        //1. 绑定FrameBuffer到当前的绘制环境上
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffer[0])
        //2. 将纹理对象挂载到FrameBuffer上，存储颜色信息
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            mTexture[0],
            0
        )
        //3. 将RenderBuffer挂载到FrameBuffer上，存储深度信息
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_DEPTH_ATTACHMENT,
            GLES30.GL_RENDERBUFFER,
            mRenderBuffer[0]
        )
    }

    private fun drawTexture() {
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mProjectionMatrix, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureBean!!.textureId)
        GLES30.glUniform1i(uTextureUnitLocation, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4)
    }

    private fun unbindFrameBufferInfo() {
        //解绑FrameBuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    private fun deleteEnv() {
        GLES30.glDeleteTextures(1, mTexture, 0)
        GLES30.glDeleteRenderbuffers(1, mRenderBuffer, 0)
        GLES30.glDeleteFramebuffers(1, mFrameBuffer, 0)

    }
}