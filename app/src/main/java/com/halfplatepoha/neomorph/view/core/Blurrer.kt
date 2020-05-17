package com.halfplatepoha.neomorph.view.core

import android.content.Context
import android.graphics.*
import androidx.renderscript.*
import java.lang.ref.WeakReference

internal class Blurrer(context: Context) {

    private val contextRef = WeakReference(context)

    fun blur(source: Bitmap, radius: Int): Bitmap? {
        val width = source.width
        val height = source.height
        if (width == 0 || height == 0) {
            return null
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .onCanvas {
                val paint = Paint().apply {
                    flags = Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
                    colorFilter = PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP)
                }
                drawBitmap(source, 0f, 0f, paint)
            }
        val blurBitmap: Bitmap? = try {
            rs(bitmap, radius)
        } catch (e: RSRuntimeException) {
            e.printStackTrace()
            null
        }

        return blurBitmap?.let {
            val scaled = Bitmap.createScaledBitmap(it, source.width, source.height, true)
            it.recycle()
            scaled
        }
    }

    @Throws(RSRuntimeException::class)
    private fun rs(bitmap: Bitmap, radius: Int): Bitmap? {
        val context = contextRef.get() ?: return null
        var rs: RenderScript? = null
        var input: Allocation? = null
        var output: Allocation? = null
        var blur: ScriptIntrinsicBlur? = null
        var type: Type
        try {
            rs = RenderScript.create(context)
            rs.messageHandler = RenderScript.RSMessageHandler()
            input = Allocation.createFromBitmap(
                rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )
            output = Allocation.createTyped(rs, input.type)
            blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            blur.setInput(input)
            blur.setRadius(radius.toFloat())
            blur.forEach(output)
            output.copyTo(bitmap)
        } finally {
            rs?.destroy()
            input?.destroy()
            output?.destroy()
            blur?.destroy()
        }
        return bitmap
    }

}

internal data class Blur(val radius: Int, val color: Int)
