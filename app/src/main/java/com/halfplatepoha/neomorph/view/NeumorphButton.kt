package com.halfplatepoha.neomorph.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class NeumorphButton @JvmOverloads constructor(context: Context,
                     attrs: AttributeSet? = null,
                     defStyleAttr: Int = 0
                     ) : AppCompatButton(context, attrs, defStyleAttr) {

    private val drawable: NeumorphDrawable

    init {
        drawable = NeumorphDrawable(context, attrs, defStyleAttr, 0).apply {
            setDarkShadowColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            setLightShadowColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            setCornerRadius(50f)
        }
        setBackgroundDrawable(drawable)
    }

    override fun setTranslationZ(translationZ: Float) {
        super.setTranslationZ(translationZ)
        Log.e("Neomorph", "$translationZ")
        this.drawable.setTranslationZ(translationZ)
    }

}