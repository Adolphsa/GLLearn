package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.utils.BufferUtil
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 图形 - 多边形
 * Created by lucas on 2021/5/27.
 */
open class L2_2_ShapeRenderer(context: Context) : BaseRenderer(context){

    companion object{
        //顶点着色器
        private val VERTEX_SHADER = """
           #version 300 es
           layout(location = 0) in vec4 a_Position;
           
           void main() {
                gl_Position = a_Position;
                gl_PointSize = 10.0f;
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

        private val POSITION_COMPONENT_COUNT = 2

        //多边形顶点与中心点的距离
        private val RADIUS = 0.5f

        //起始点的弧度
        private val START_POINT_RADIAN = (2 * Math.PI / 4).toFloat()
    }

    private var vertexData: FloatBuffer? = null
    private var uColorLocation: Int = 0

    //多边形的顶点数，即边数
    private var mPolygonVertexCount = 3

    //绘制所需要的顶点数
    private lateinit var mPointData: FloatArray


    open val vertexShader: String
        get() = VERTEX_SHADER

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        makeProgram(vertexShader, FRAGMENT_SHADER)

        uColorLocation = getUniform("u_Color")

        GLES30.glEnableVertexAttribArray(0)

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        updateVertexData()
//        drawShape()
        drawLine()
        drawPoint()
        updatePolygonVertexCount()
    }

    private fun updateVertexData() {
        // 边数+中心点+闭合点；一个点包含x、y两个向量
        mPointData = FloatArray((mPolygonVertexCount + 2) * 2)

        // 组成多边形的每个三角形的中心点角的弧度
        val radian = (2 * Math.PI / mPolygonVertexCount).toFloat()
        //中心点
        mPointData[0] = 0f
        mPointData[1] = 0f

        // 多边形的顶点数据
        for (i in 0 until mPolygonVertexCount) {
            mPointData[2 * i + 2] = (RADIUS * Math.cos((radian * i + START_POINT_RADIAN).toDouble())).toFloat()
            mPointData[2 * i + 1 + 2] = (RADIUS * Math.sin((radian * i + START_POINT_RADIAN).toDouble())).toFloat()
        }

        // 闭合点：与多边形的第一个顶点重叠
        mPointData[mPolygonVertexCount * 2 + 2] = (RADIUS * Math.cos(START_POINT_RADIAN.toDouble())).toFloat()
        mPointData[mPolygonVertexCount * 2 + 3] = (RADIUS * Math.sin(START_POINT_RADIAN.toDouble())).toFloat()

        vertexData = BufferUtil.createFloatBuffer(mPointData!!)
        vertexData!!.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            vertexData)
    }

    private fun drawShape() {
        GLES30.glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, mPolygonVertexCount + 2)
    }

    private fun drawPoint() {
        GLES30.glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPolygonVertexCount + 2)
    }

    private fun drawLine() {
        GLES30.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 1, mPolygonVertexCount)
    }

    /**
     * 更新多边形的边数
     */
    private fun updatePolygonVertexCount() {
        mPolygonVertexCount++
        mPolygonVertexCount = if (mPolygonVertexCount > 32) 3 else mPolygonVertexCount
    }
}