package com.lucas.gl.filter

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import com.lucas.gl.base.GLConstants
import com.lucas.gl.base.GLConstants.POSITION_COMPONENT_COUNT
import com.lucas.gl.utils.*
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * 基础滤镜
 * Created by lucas on 2021/6/18.
 */
open class BaseFilter(
    val context: Context,
    private val vertexShader: String = VERTEX_SHADER,
    private val fragmentShader: String = FRAGMENT_SHADER
) {
    companion object {
        const val VERTEX_SHADER = """
            #version 300 es
            layout(location = 0) in vec4 a_position;
            layout(location = 1) in vec2 a_TexCoord;
            
            out vec2 v_TexCoord;
            
            uniform mat4 u_Matrix;
            
            void main() {
                v_TexCoord = a_TexCoord;
                gl_Position = u_Matrix * a_position;
            }
            
        """
        const val FRAGMENT_SHADER = """
            #version 300 es
            precision mediump float;
            
            in vec2 v_TexCoord;
            uniform sampler2D u_TextureUnit;
            
            out vec4 fragColor;
            
            void main() {
                fragColor = texture(u_TextureUnit, v_TexCoord);
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

    private var uTextureUnitLocation: Int = 0

    //纹理数据
    var textureBean: TextureHelper.TextureBean? = null
    private var projectionMatrixHelper: ProjectionMatrixHelper? = null

    var program = 0

    init {
        mVertexData = BufferUtil.createFloatBuffer(POINT_DATA)
        mTexVertexBuffer = BufferUtil.createFloatBuffer(TEX_VERTEX)
    }

    open fun onCreated() {
        makeProgram(vertexShader, fragmentShader)

        projectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")
        uTextureUnitLocation = getUniform("u_TextureUnit")

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

        //加载纹理坐标
        mTexVertexBuffer.position(0)
        GLES30.glVertexAttribPointer(
            1,
            GLConstants.POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mTexVertexBuffer
        )
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glClearColor(0f, 0f, 0f, 1f)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES30.glEnable(GL10.GL_BLEND)
        GLES30.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
    }

    open fun onSizeChanged(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        projectionMatrixHelper!!.enable(width, height)
    }

    open fun onDraw() {
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT)

        // 纹理单元：在OpenGL中，纹理不是直接绘制到片段着色器上，而是通过纹理单元去保存纹理
        // 设置当前活动的纹理单元为纹理单元0
        GLES30.glActiveTexture(GLES20.GL_TEXTURE0)

        // 将纹理ID绑定到当前活动的纹理单元上
        GLES30.glBindTexture(
            GLES20.GL_TEXTURE_2D,
            textureBean?.textureId ?: 0
        )

        // 将纹理单元传递片段着色器的u_TextureUnit
        GLES30.glUniform1i(uTextureUnitLocation, 0)

        GLES30.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,
            0,
            POINT_DATA.size / POSITION_COMPONENT_COUNT
        )
    }

    open fun onDestroy() {
        GLES30.glDeleteProgram(program)
        program = 0
    }

    /**
     * 创建OpenGL程序对象
     *
     * @param vertexShader   顶点着色器代码
     * @param fragmentShader 片段着色器代码
     */
    protected fun makeProgram(vertexShader: String, fragmentShader: String) {
        // 步骤1：编译顶点着色器
        val vertexShaderId = ShaderHelper.compileVertexShader(vertexShader)
        // 步骤2：编译片段着色器
        val fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShader)
        // 步骤3：将顶点着色器、片段着色器进行链接，组装成一个OpenGL程序
        program = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId)

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program)
        }

        // 步骤4：通知OpenGL开始使用该程序
        GLES30.glUseProgram(program)
    }

    protected fun getUniform(name: String): Int {
        return GLES30.glGetUniformLocation(program, name)
    }
}