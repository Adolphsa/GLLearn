package com.lucas.gl.render

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.lucas.gl.utils.LoggerConfig
import com.lucas.gl.utils.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 基础概念 + 点 的绘制
 * Created by lucas on 2021/5/27.
 */
class L1_1_PointRenderer(private val mContext: Context) : GLSurfaceView.Renderer {

    private companion object {
        private const val TAG = "L1_1_PointRenderer"
        //顶点着色器
        private val VERTEX_SHADER = """
            #version 300 es
            layout (location = 0) in vec4 a_position;
             
             void main() {
                //gl_Position: GL中默认定义的输出变量，决定了当前顶点顶点最终位置
                gl_Position = a_position;
                gl_PointSize = 30.0f;
             }
        """

        //片段着色器
        private val FRAGMENT_SHADER = """
            #version 300 es
            //定义了所有浮点数据类型的默认精度：有lowp、mediump、highp 三种，但只有部分硬件支持片段着色器使用highp。(顶点着色器默认highp)
            precision mediump float;
            
            uniform vec4 u_Color;
            
            out vec4 fragColor;
            
            void main() {
                fragColor = u_Color;
            }
        """

        //顶点数据
        private val POINT_DATA = floatArrayOf(
            0.0f, 0.0f,
            0.2f, -0.3f,
            -0.5f, -0.6f
        )


        //每个顶点数据关联的分量个数：当前案例只有x、y，故为2
        private val POSITION_COMPONENT_COUNT = 2

        //Float类型占4Byte
        private val BYTES_PER_FLOAT = 4
    }

    private var mProgram: Int = 0

    //颜色uniform在OpenGL程序中的索引
    private var uColorLocation: Int = 0

     //顶点坐标数据缓冲区
    private val mVertexData: FloatBuffer

    init {
        // 分配一个块Native内存，用于与GL通讯传递。(我们通常用的数据存在于Dalvik的内存中，1.无法访问硬件；2.会被垃圾回收)
        mVertexData = ByteBuffer
            // 分配顶点坐标分量个数 * Float占的Byte位数
            .allocateDirect(POINT_DATA.size * BYTES_PER_FLOAT)
            // 按照本地字节序排序
            .order(ByteOrder.nativeOrder())
            // Byte类型转Float类型
            .asFloatBuffer()

        // 将Dalvik的内存数据复制到Native内存中
        mVertexData.put(POINT_DATA)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 设置刷新屏幕时候使用的颜色值,顺序是RGBA，值的范围从0~1。GLES30.glClear调用时使用该颜色值。
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        //步骤1：编译顶点着色器
        val vertexShader = ShaderHelper.compileVertexShader(VERTEX_SHADER)

        //步骤2：编译片段着色器
        val fragmentShader = ShaderHelper.compileFragmentShader(FRAGMENT_SHADER)

        //步骤3：将顶点着色器，片段着色器进行链接，组装成一个OpenGL程序
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader)

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(mProgram)
        }

        //步骤4：通知OpenGL开始使用该程序
        GLES30.glUseProgram(mProgram)

        // 步骤5：获取颜色Uniform在OpenGL程序中的索引
        uColorLocation = GLES30.glGetUniformLocation(mProgram, "u_Color")

        // 将缓冲区的指针移动到头部，保证数据是从最开始处读取
        mVertexData.position(0)

        // 步骤7：关联顶点坐标属性和缓存数据
        // 1. 位置索引；
        // 2. 每个顶点属性需要关联的分量个数(必须为1、2、3或者4。初始值为4。)；
        // 3. 数据类型；
        // 4. 指定当被访问时，固定点数据值是否应该被归一化(GL_TRUE)或者直接转换为固定点值(GL_FALSE)(只有使用整数数据时)
        // 5. 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。
        // 6. 数据缓冲区
        GLES30.glVertexAttribPointer(
            0,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            0,
            mVertexData)

        // 步骤8：通知GL程序使用指定的顶点属性索引
        GLES30.glEnableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 步骤1：使用glClearColor设置的颜色，刷新Surface
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 步骤2：更新u_Color的值，即更新画笔颜色
        GLES30.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)

        // 步骤3：使用数组绘制图形：1.绘制的图形类型；2.从顶点数组读取的起点；3.从顶点数组读取的顶点个数
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 3)
    }
}