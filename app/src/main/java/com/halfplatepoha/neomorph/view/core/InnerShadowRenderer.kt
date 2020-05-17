package com.halfplatepoha.neomorph.view.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import kotlin.math.min

internal class InnerShadowRenderer(private var drawableState: NeumorphDrawable.DrawableState): IShadowRenderer {

    private var innerShadowBitmap: Bitmap? = null
    private var lightShadownDrawable = GradientDrawable()
    private var darkShadownDrawable = GradientDrawable()

    override fun setDrawableState(newDrawableState: NeumorphDrawable.DrawableState) {
        this.drawableState = newDrawableState
    }

    override fun draw(canvas: Canvas, outlinePath: Path) {
        canvas.withClip(outlinePath) {
            val (left, top) = drawableState.padding?.let {
                Pair(it.left.toFloat(), it.top.toFloat())
            }?: Pair(0f, 0f)

            innerShadowBitmap?.let {
                drawBitmap(it, left, top, null)
            }
        }
    }

    override fun updateShadowBitmap(bounds: Rect) {
        val elevation = drawableState.shadowElevation.toInt()
        val w = bounds.width()
        val h = bounds.height()
        val width = w + elevation
        val height = h + elevation
        val cornerRadius = min(drawableState.cornerSize, min(h/2f, w/2f))

        this.lightShadownDrawable.apply {
            setSize(width, height)
            setStroke(elevation, drawableState.shadowColorLight)

            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, cornerRadius, cornerRadius, 0f, 0f)
        }

        this.darkShadownDrawable.apply {
            setSize(width, height)
            setStroke(elevation, drawableState.shadowColorDark)

            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(cornerRadius, cornerRadius, 0f, 0f, 0f, 0f, 0f, 0f)
        }

        this.lightShadownDrawable.setSize(width, height)
        this.lightShadownDrawable.setBounds(0, 0, width, height)
        this.darkShadownDrawable.setSize(width, height)
        this.darkShadownDrawable.setBounds(0, 0, width, height)
        this.innerShadowBitmap = getShadownBitmap(w, h)
    }

    fun getShadownBitmap(width: Int, height: Int): Bitmap? {
        fun Bitmap.blurred(radius: Int): Bitmap? {
            return drawableState.blurrer.blur(this, radius)
        }

        val shadowElevation = drawableState.shadowElevation
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).onCanvas {
            withTranslation(-shadowElevation, -shadowElevation) {
                lightShadownDrawable.draw(this)
            }
            darkShadownDrawable.draw(this)
        }.blurred(drawableState.blurRadius)

    }

}