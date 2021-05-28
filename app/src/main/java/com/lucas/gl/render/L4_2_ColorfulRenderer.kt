package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.utils.BufferUtil
import com.lucas.gl.utils.ProjectionMatrixHelper
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 渐变色 - 数据传递优化
 * Created by lucas on 2021/5/28.
 */
class L4_2_ColorfulRenderer(context: Context) : BaseRenderer(context) {

    companion object {
        private val VERTEX_SHADER = """
           #version 300 es
            layout(location = 0) in vec4 a_Position;
            layout(location = 1) in vec4 a_Color;
            
            uniform mat4 u_Matrix;
            
            out vec4 v_Color;
            
            void main() {
                gl_Position = u_Matrix * a_Position;
                v_Color = a_Color;
                gl_PointSize = 10.0f;
            }
        """

        private val FRAGMENT_SHADER = """
           #version 300 es
            precision mediump float;
            in vec4 v_Color;
            out vec4 fragColor;
             
            void main() {
                fragColor = v_Color;
            }
        """
        //顶点数据 + 颜色数据
        private val POINT_DATA = floatArrayOf(
            // 一个顶点有5个向量数据：x、y、r、g、b
            -0.5f, -0.5f, 1f, 0.5f, 0.5f,
            0.5f, -0.5f, 1f, 0f, 1f,
            -0.5f, 0.5f, 0f, 1f, 1f,
            0.5f, 0.5f, 1f, 1f, 0f)

        //顶点坐标占用的向量个数
        private val POSITION_COMPONENT_COUNT = 2
        //颜色占用的向量个数
        private val COLOR_COMPONENT_COUNT = 3
        private val BYTES_PER_FLOAT = 4

        //数据数组中每个顶点起始数据的间距：数组中每个顶点相关属性占的Byte值
        private val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

    private val mVertexData: FloatBuffer
    private var mProjectionMatrixHelper: ProjectionMatrixHelper? = null

    init {
        mVertexData = BufferUtil.createFloatBuffer(POINT_DATA)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        mProjectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")

        //配置顶点数据
        mVertexData.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            STRIDE,
            mVertexData)
        GLES30.glEnableVertexAttribArray(0)

        //配置颜色数据
        // 将数组的初始读取位置右移2位，所以数组读取的顺序是r1, g1, b1, x2, y2, r2, g2, b2...
        mVertexData.position(POSITION_COMPONENT_COUNT)
        // COLOR_COMPONENT_COUNT：从数组中每次读取3个向量
        // STRIDE：每次读取间隔是 (2个位置 + 3个颜色值) * Float占的Byte位
        GLES30.glVertexAttribPointer(
            1,
            COLOR_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            STRIDE,
            mVertexData)
        GLES30.glEnableVertexAttribArray(1)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        mProjectionMatrixHelper!!.enable(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, POINT_DATA.size/ (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT))
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, POINT_DATA.size/ POSITION_COMPONENT_COUNT)
    }
}