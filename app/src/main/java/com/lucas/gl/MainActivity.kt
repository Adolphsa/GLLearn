package com.lucas.gl

import android.Manifest
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lucas.gl.base.BaseRenderer
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {


    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var root: ViewGroup
    private lateinit var listView: ListView
    private var glSurfaceView: GLSurfaceView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        root = findViewById<View>(R.id.main_root) as ViewGroup
        listView = findViewById<View>(R.id.main_list) as ListView
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            MainListItems.ITEMS)
        listView.adapter = adapter
        listView.onItemClickListener = this@MainActivity

        permissionCheck()

    }

    //权限检查
    private fun permissionCheck() {
        if (EasyPermissions.hasPermissions(
                this@MainActivity,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
        } else {
            EasyPermissions.requestPermissions(
                this@MainActivity,
                "权限申请",
                CAMERA_PERMISSION_REQUEST_CODE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (glSurfaceView != null) {
            glSurfaceView!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (glSurfaceView != null) {
            glSurfaceView!!.onPause()
        }
    }

    override fun onBackPressed() {
        if (glSurfaceView != null) {
            // 展示了GLSurfaceView，则删除ListView之外的其余控件
            var childCount = root.childCount
            var i = 0
            while (i < childCount) {
                if (root.getChildAt(i) !== listView) {
                    root.removeViewAt(i)
                    childCount--
                    i--
                }
                i++
            }
            glSurfaceView = null
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val clickClass = MainListItems.getClass(position)

        glSurfaceView = GLSurfaceView(this@MainActivity)
        root.addView(glSurfaceView)

        glSurfaceView!!.setEGLContextClientVersion(2)
        glSurfaceView!!.setEGLConfigChooser(false)

        val renderer = MainListItems.getRenderer(clickClass, this)
        if (renderer == null) {
            Toast.makeText(this, "反射构建渲染器失败", Toast.LENGTH_SHORT).show()
            return
        }

        glSurfaceView!!.setRenderer(renderer)
        glSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY


        glSurfaceView!!.setOnClickListener {
            glSurfaceView!!.requestRender()
            if (renderer is BaseRenderer) {
                renderer.onClick()
            }

        }
    }


}