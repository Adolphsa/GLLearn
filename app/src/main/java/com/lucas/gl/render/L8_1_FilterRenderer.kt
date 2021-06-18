package com.lucas.gl.render

import android.content.Context
import com.lucas.gl.R
import com.lucas.gl.base.BaseRenderer
import com.lucas.gl.filter.BaseFilter
import com.lucas.gl.filter.InverseFilter
import com.lucas.gl.utils.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lucas on 2021/6/18.
 */
class L8_1_FilterRenderer(context: Context) : BaseRenderer(context){

    private val filterList = ArrayList<BaseFilter>()
    private var drawIndex = 0
    private var isChanged = false
    private var currentFilter: BaseFilter
    private var textureBean: TextureHelper.TextureBean? = null

    init {
        filterList.add(BaseFilter(context))
        filterList.add(InverseFilter(context))
        currentFilter = filterList[0]
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        currentFilter.onCreated()
        textureBean = TextureHelper.loadTexture(context, R.drawable.pikachu)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        currentFilter.onSizeChanged(width, height)
        currentFilter.textureBean = textureBean
    }

    override fun onDrawFrame(gl: GL10?) {
        if (isChanged) {
            currentFilter = filterList[drawIndex]

            filterList.forEach{
                if (it != currentFilter) {
                    it.onDestroy()
                }
            }

            currentFilter.onCreated()
            currentFilter.onSizeChanged(outputWidth, outputHeight)
            currentFilter.textureBean = textureBean
            isChanged = false
        }

        currentFilter.onDraw()
    }

    override fun onClick() {
        super.onClick()
        drawIndex++
        drawIndex = if (drawIndex >= filterList.size) 0 else drawIndex
        isChanged = true
    }
}