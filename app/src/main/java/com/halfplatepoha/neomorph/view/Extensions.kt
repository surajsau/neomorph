package com.halfplatepoha.neomorph.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.os.Build

fun Bitmap.onCanvas(block: Canvas.() -> Unit): Bitmap = this.also {
    Canvas(it).run(block)
}

fun Canvas.withTranslation(x: Float = 0f, y: Float = 0f, block: Canvas.() -> Unit) {
    val previousState = save()
    translate(x, y)

    try {
        block()
    } finally {
        restoreToCount(previousState)
    }
}

fun Canvas.withClipOut(clipPath: Path, block: Canvas.() -> Unit) {
    val previousState = save()
    clipOutPathCompat(clipPath)
    try {
        block()
    } finally {
        restoreToCount(previousState)
    }
}

fun Canvas.clipOutPathCompat(clipPath: Path) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        clipOutPath(clipPath)
    } else {
        clipPath(clipPath, Region.Op.DIFFERENCE)
    }
}