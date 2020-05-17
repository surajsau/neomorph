package com.halfplatepoha.neomorph.view.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.graphics.drawable.GradientDrawable
import android.os.Build


internal fun GradientDrawable.setCornerShape(drawableState: NeumorphDrawable.DrawableState) {
    shape = GradientDrawable.RECTANGLE
    cornerRadii = drawableState.cornerSize.let {
        floatArrayOf(it, it, it, it, it, it, it, it)
    }
}

fun Bitmap.onCanvas(block: Canvas.() -> Unit): Bitmap = this.also {
    Canvas(it).run(block)
}

private fun Canvas.runWithRestore(block: Canvas.() -> Unit) {
    val previousState = save()
    try {
        block()
    } finally {
        restoreToCount(previousState)
    }
}

fun Canvas.withTranslation(x: Float = 0f, y: Float = 0f, block: Canvas.() -> Unit) = runWithRestore {
    translate(x, y)
    block()
}

fun Canvas.withClipOut(clipPath: Path, block: Canvas.() -> Unit) = runWithRestore {
    clipOutPathCompat(clipPath)
    block()
}

fun Canvas.withClip(clipPath: Path, block: Canvas.() -> Unit) = runWithRestore {
    clipPathCompat(clipPath)
    block()
}

fun Canvas.clipOutPathCompat(clipPath: Path) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        clipOutPath(clipPath)
    } else {
        clipPath(clipPath, Region.Op.DIFFERENCE)
    }
}

fun Canvas.clipPathCompat(clipPath: Path) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        clipPath(clipPath)
    } else {
        clipPath(clipPath, Region.Op.INTERSECT)
    }
}