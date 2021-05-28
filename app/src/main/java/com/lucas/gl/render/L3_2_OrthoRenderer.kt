package com.lucas.gl.render

import android.content.Context
import com.lucas.gl.utils.ProjectionMatrixHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 正交投影 --- 代码封装
 * Created by lucas on 2021/5/28.
 */
class L3_2_OrthoRenderer(context: Context) : L2_2_ShapeRenderer(context) {
    companion object {
        private val VERTEX_SHADER = """
            #version 300 es
            layout(location = 0 ) in vec4 a_Position;
            uniform mat4 u_Matrix;
            
            void main() {
                gl_Position = u_Matrix * a_Position;
                gl_PointSize = 8.0f;
            }
        """
    }

    private var mProjectionMatrixHelper: ProjectionMatrixHelper? = null

    override val vertexShader: String
        get() = VERTEX_SHADER

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        mProjectionMatrixHelper = ProjectionMatrixHelper(program, "u_Matrix")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        mProjectionMatrixHelper!!.enable(width, height)
    }
}