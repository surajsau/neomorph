package com.halfplatepoha.neomorph.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import kotlin.math.roundToInt

internal class ShapeRenderer(private var drawableState: NeumorphDrawable.State) {

    private val lightShadowDrawable = GradientDrawable()
    private val darkShadowDrawable = GradientDrawable()

    private var lightShadowBitmap: Bitmap? = null
    private var darkShadowBitmap: Bitmap? = null

    fun setDrawableState(drawableState: NeumorphDrawable.State) {
        this.drawableState = drawableState
    }

    fun updateBounds(bounds: Rect) {
        fun GradientDrawable.toRoundedCorners(radius: Float?) {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radius?.let {
                floatArrayOf(it, it, it, it, it, it, it, it)
            }
        }

        this.lightShadowDrawable.apply {
            setColor(drawableState.lightShadowColor)
            toRoundedCorners(drawableState.cornerRadius)
        }

        this.darkShadowDrawable.apply {
            setColor(drawableState.darkShadowColor)
            toRoundedCorners(drawableState.cornerRadius)
        }

        val width = bounds.width()
        val height = bounds.height()

        this.lightShadowDrawable.setSize(width, height)
        this.lightShadowDrawable.setBounds(0, 0, width, height)
        this.darkShadowDrawable.setSize(width, height)
        this.darkShadowDrawable.setBounds(0, 0, width, height)

        this.lightShadowBitmap = lightShadowDrawable.toBitmap(width, height)
        this.darkShadowBitmap = darkShadowDrawable.toBitmap(width, height)
    }

    private fun Drawable.toBitmap(width: Int, height: Int): Bitmap {

        val elevation = drawableState.elevation
        val actualWidth = (width + 2 * elevation).roundToInt()
        val actualHeight = (height + 2 * elevation).roundToInt()

        return Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
            .onCanvas {
                withTranslate(elevation, elevation) {
                    draw(this)
                }
            }
    }

    fun onDraw(canvas: Canvas, clipPath: Path) {
        canvas.withClipOut(clipPath) {
            val z = drawableState.elevation + drawableState.translationZ
            val elevation = drawableState.elevation

            val left = 0f
            val top = 0f

            lightShadowBitmap?.let {
                val offset = -elevation - z
                canvas.drawBitmap(it, left + offset, top + offset, null)
            }

            darkShadowBitmap?.let {
                val offset = -elevation + z
                canvas.drawBitmap(it, left + offset, top + offset, null)
            }
        }
    }

}