package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.utils.BufferUtil
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 在OpenGL中，我们要渲染的所有物体都要映射到x轴、y轴、z轴上的[-1, 1]范围内，
 * 这个范围内的坐标被称为归一化设备坐标，其独立于屏幕的实际尺寸或者形状。
 * 归一化设备坐标假定的坐标空间是一个正方形
 * 但是我们手机设备一般都不是正方形的，而是长方形的。
 * 所以导致x和y两个方向上，同样的比例值，但是视觉上所占的长度却是不一样的。
 *
 * 解决这个问题，一般我们的解决方案步骤如下:
 * 1.在设置物体的坐标、尺寸时，将短边视为标准边，取值范围是[-1,1]，而较长边的取值范围则是[-N,N]，其中N≥1，N是长边/短边的比例系数。
 * 2.顶点着色器设置顶点参数的时候，将长边上的值从[-N,N]换算为[-1,1]的范围内。
 *
 * 正交投影
 * Created by lucas on 2021/5/28.
 */
class L3_1_OrthoRenderer(context: Context) : BaseRenderer(context){

    companion object{
        //顶点着色器
        private val VERTEX_SHADER = """
           #version 300 es
           layout(location = 0) in vec4 a_Position;
           uniform mat4 u_Matrix;
           
           void main() {
                gl_Position = u_Matrix * a_Position;
                gl_PointSize = 10.0f;
           }
        """

        //片段着色器
        private val FRAGMENT_SHADER = """
           #version 300 es
           precision mediump float;
           out vec4 fragColor;
           uniform vec4  u_Color;
           
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
    private var uMatrixLocation: Int = 0

    //多边形的顶点数，即边数
    private var mPolygonVertexCount = 3
    //绘制所需要的顶点数
    private lateinit var mPointData: FloatArray

    //矩阵数组
    private val mProjectionMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f)


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        uMatrixLocation = getUniform("u_Matrix")
        uColorLocation = getUniform("u_Color")

        GLES30.glEnableVertexAttribArray(0)

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        // 边长比(>=1)，非宽高比
        val aspectRatio = if (width > height)
            width.toFloat() / height.toFloat()
        else
            height.toFloat() / width.toFloat()

        // 1. 矩阵数组
        // 2. 结果矩阵起始的偏移量
        // 3. left：x的最小值
        // 4. right：x的最大值
        // 5. bottom：y的最小值
        // 6. top：y的最大值
        // 7. near：z的最小值
        // 8. far：z的最大值
        if (width > height) {
            //横屏
            Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            // 竖屏or正方形
            Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }

        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mProjectionMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        updateVertexData()
        drawShape()
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
        mPointData[mPolygonVertexCount * 2 + 2] = (RADIUS * Math.cos(
            START_POINT_RADIAN.toDouble())).toFloat()
        mPointData[mPolygonVertexCount * 2 + 3] = (RADIUS * Math.sin(
            START_POINT_RADIAN.toDouble())).toFloat()

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