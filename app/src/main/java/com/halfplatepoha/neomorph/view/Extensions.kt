package com.halfplatepoha.neomorph.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.os.Build

fun Bitmap.onCanvas(block: Canvas.() -> Unit) = this.also {
    Canvas(it).run(block)
}

fun Canvas.withTranslate(x: Float, y: Float, block: Canvas.() -> Unit) {
    val save = save()
    translate(x, y)
    block()
    restoreToCount(save)
}

fun Canvas.withClipOut(path: Path, block: Canvas.() -> Unit) {
    val save = save()
    clipOutPathCompat(path)
    block()
    restoreToCount(save)
}

fun Canvas.clipOutPathCompat(path: Path) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        clipOutPath(path)
    } else {
        clipPath(path, Region.Op.DIFFERENCE)
    }
}