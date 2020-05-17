package com.halfplatepoha.neomorph.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import com.halfplatepoha.neomorph.view.NeumorphDrawable.DrawableState
import kotlin.math.roundToInt

internal class ShadowRenderer(private var drawableState: DrawableState) : IShadowRenderer {

    private var lightShadowBitmap: Bitmap? = null
    private var darkShadowBitmap: Bitmap? = null
    private val lightShadowDrawable = GradientDrawable()
    private val darkShadowDrawable = GradientDrawable()

    override fun setDrawableState(newDrawableState: DrawableState) {
        this.drawableState = newDrawableState
    }

    override fun draw(canvas: Canvas, outlinePath: Path) {
        canvas.withClipOut(outlinePath) {
            val elevation = drawableState.shadowElevation
            val z = drawableState.shadowElevation + drawableState.translationZ
            val left: Float
            val top: Float
            val padding = drawableState.padding
            if (padding != null) {
                left = padding.left.toFloat()
                top = padding.top.toFloat()
            } else {
                left = 0f
                top = 0f
            }
            lightShadowBitmap?.let {
                val offset = -elevation - z
                drawBitmap(it, offset + left, offset + top, null)
            }
            darkShadowBitmap?.let {
                val offset = -elevation + z
                drawBitmap(it, offset + left, offset + top, null)
            }
        }
    }

    override fun updateShadowBitmap(bounds: Rect) {

        lightShadowDrawable.apply { setColor(drawableState.shadowColorLight) }
            .setCornerShape(drawableState)

        darkShadowDrawable.apply { setColor(drawableState.shadowColorDark) }
            .setCornerShape(drawableState)

        val w = bounds.width()
        val h = bounds.height()
        lightShadowDrawable.setSize(w, h)
        lightShadowDrawable.setBounds(0, 0, w, h)
        darkShadowDrawable.setSize(w, h)
        darkShadowDrawable.setBounds(0, 0, w, h)
        lightShadowBitmap = lightShadowDrawable.toBlurredBitmap(w, h)
        darkShadowBitmap = darkShadowDrawable.toBlurredBitmap(w, h)
    }

    private fun Drawable.toBlurredBitmap(w: Int, h: Int): Bitmap? {
        fun Bitmap.blurred(radius: Int): Bitmap? {
            return drawableState.blurrer.blur(this, radius)
        }

        val shadowElevation = drawableState.shadowElevation
        val width = (w + shadowElevation * 2).roundToInt()
        val height = (h + shadowElevation * 2).roundToInt()
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .onCanvas {
                withTranslation(shadowElevation, shadowElevation) {
                    draw(this)
                }
            }
            .blurred(this@ShadowRenderer.drawableState.blurRadius)
    }
}