package com.halfplatepoha.neomorph.view.core

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import com.halfplatepoha.neomorph.view.core.NeumorphDrawable.DrawableState

internal interface IShadowRenderer {
    fun setDrawableState(newDrawableState: DrawableState)
    fun draw(canvas: Canvas, outlinePath: Path)
    fun updateShadowBitmap(bounds: Rect)
}
