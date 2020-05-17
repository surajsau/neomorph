package com.halfplatepoha.neomorph.view.core

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import com.halfplatepoha.neomorph.view.core.NeumorphDrawable.DrawableState

const val RENDERER_TYPE_INNER = 0
const val RENDERER_TYPE_OUTER = 1
const val RENDERER_TYPE_COMBINED = 2

internal interface IShadowRenderer {
    fun setDrawableState(newDrawableState: DrawableState)
    fun draw(canvas: Canvas, outlinePath: Path)
    fun updateShadowBitmap(bounds: Rect)
}
