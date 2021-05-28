package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.utils.BufferUtil
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 图形
 * Created by lucas on 2021/5/27.
 */
class L2_1_ShapeRenderer(context: Context) : BaseRenderer(context) {

    companion object {

        private const val TAG = "L2_1_ShapeRenderer"

        //顶点着色器
        private val VERTEX_SHADER = """
            #version 300 es
            layout(location = 0) in vec4 a_Position;
            
            void main() {
                gl_Position = a_Position;
                gl_PointSize = 30.0f;
            }
        """

        //片段着色器
        private val FRAGMENT_SHADER = """
           #version 300 es
           precision mediump float;
           uniform vec4 u_Color;
           out vec4 fragColor;
            
           void main() {
                fragColor = u_Color;
           }
        """

        private val POINT_DATA = floatArrayOf(
            // 两个点的x,y坐标（x，y各占1个分量）
            0f, 0f,
            0f, 0.5f,
            -0.5f, 0f,
            0f, 0f - 0.5f,
            0.5f, 0f - 0.5f,
            0.5f, 0.5f - 0.5f
        )

        private val POSITION_COMPONENT_COUNT = 2
        private val DRAW_COUNT = POINT_DATA.size / POSITION_COMPONENT_COUNT
    }

    private val vertexData: FloatBuffer
    private var uColorLocation: Int = 0
    private var drawIndex = 0

    init {
        vertexData = BufferUtil.createFloatBuffer(POINT_DATA)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        uColorLocation = getUniform("u_Color")

        vertexData.position(0)

        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            vertexData)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        drawIndex++

        Log.i(TAG, "onDrawFrame: drawIndex = $drawIndex")
        drawTriangle()
        drawLine()
        drawPoint()

        if (drawIndex >= DRAW_COUNT) {
            drawIndex = 0
        }
    }

    private fun drawPoint() {
        GLES30.glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, drawIndex)
    }

    private fun drawLine() {
        // GL_LINES：每2个点构成一条线段
        // GL_LINE_LOOP：按顺序将所有的点连接起来，包括首位相连
        // GL_LINE_STRIP：按顺序将所有的点连接起来，不包括首位相连
        GLES30.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, drawIndex)
    }

    private fun drawTriangle() {
        // GL_TRIANGLES：每3个点构成一个三角形
        // GL_TRIANGLE_STRIP：相邻3个点构成一个三角形,不包括首位两个点
        // GL_TRIANGLE_FAN：第一个点和之后所有相邻的2个点构成一个三角形
        GLES30.glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, drawIndex)
    }
}