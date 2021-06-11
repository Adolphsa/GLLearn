package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import android.util.proto.ProtoOutputStream
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.utils.BufferUtil
import com.lucas.gl.utils.ProjectionMatrixHelper
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 索引绘制
 * Created by lucas on 2021/5/31.
 */
class L5_IndexRenderer(context: Context) : BaseRenderer(context) {

    companion object {
        private val VERTEX_SHADER = """
           #version 300 es
           layout(location = 0) in vec4 a_Position;
           uniform mat4 u_Matrix;
           
           void main() {
                gl_Position = u_Matrix * a_Position;
           }
        """
        private val FRAGMENT_SHADER = """
           #version 300 es
           precision mediump float;
           out vec4 fragColor;
           uniform vec4 u_Color;
            
           void main() {
                fragColor = u_Color;
           } 
        """

        private val POINT_DATA = floatArrayOf(
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f,
            0f, -1.0f,
            0f, 1.0f
        )

        //数组绘制的索引:当前绘制三角形，所以三个元素构成一个绘制顺序
        private val VERTEX_INDEX = shortArrayOf(
            0, 1, 2,
            0, 2, 3,
            0, 4, 1,
            3, 2, 5
        )

        private val POSITION_COMPONENT_COUNT = 2
    }

    private val mVertexData: FloatBuffer

    //顶点索引数据缓冲区：ShortBuff，占2位的Byte
    private val mVextexIndexBuffer: ShortBuffer

    private var uColorLocation = 0

    private var mProjectionMatrixHelper: ProjectionMatrixHelper? = null

    init {
        mVertexData = BufferUtil.createFloatBuffer(POINT_DATA)
        mVextexIndexBuffer = BufferUtil.createShortBuffer(VERTEX_INDEX)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        makeProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        uColorLocation = getUniform("u_Color")
        mProjectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")

        mVertexData.position(0)
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mVertexData)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        mProjectionMatrixHelper!!.enable(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)

        mVextexIndexBuffer.position(0)

        // 绘制相对复杂的图形时，若顶点有较多重复时，对比数据占用空间而言，glDrawElements会比glDrawArrays小很多，也会更高效
        // 因为在有重复顶点的情况下，glDrawArrays方式需要的3个顶点位置是用Float型的，占3*4的Byte值；
        // 而glDrawElements需要3个Short型的，占3*2Byte值
        // 1. 图形绘制方式； 2. 绘制的顶点数； 3. 索引的数据格式； 4. 索引的数据Buffer
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            VERTEX_INDEX.size,
            GLES30.GL_UNSIGNED_SHORT,
            mVextexIndexBuffer)
    }
}