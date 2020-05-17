package com.halfplatepoha.neomorph.view

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import com.halfplatepoha.neomorph.view.NeumorphDrawable.DrawableState

internal interface IShadowRenderer {
    fun setDrawableState(newDrawableState: DrawableState)
    fun draw(canvas: Canvas, outlinePath: Path)
    fun updateShadowBitmap(bounds: Rect)
}
