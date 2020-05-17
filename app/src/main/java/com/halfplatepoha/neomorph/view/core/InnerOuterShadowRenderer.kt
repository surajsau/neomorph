package com.halfplatepoha.neomorph.view.core

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect

internal class InnerOuterShadowRenderer(private val drawableState: NeumorphDrawable.DrawableState): IShadowRenderer {

    private val drawables = arrayOf(OuterShadowRenderer(drawableState), InnerShadowRenderer(drawableState))

    override fun setDrawableState(newDrawableState: NeumorphDrawable.DrawableState) {
        this.drawables.forEach { it.setDrawableState(newDrawableState) }
    }

    override fun draw(canvas: Canvas, outlinePath: Path) {
        this.drawables.forEach { it.draw(canvas, outlinePath) }
    }

    override fun updateShadowBitmap(bounds: Rect) {
        this.drawables.forEach { it.updateShadowBitmap(bounds) }
    }

}