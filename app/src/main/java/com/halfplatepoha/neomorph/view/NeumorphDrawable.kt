package com.halfplatepoha.neomorph.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class NeumorphDrawable : Drawable {

    constructor(context: Context): super()

    override fun draw(canvas: Canvas) {
        //.. main drawing logic
    }

    override fun setAlpha(alpha: Int) {
        //.. set alpha and invalidate
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //.. do nothing
    }

}