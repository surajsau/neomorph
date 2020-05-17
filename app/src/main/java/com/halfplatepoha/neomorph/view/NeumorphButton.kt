package com.halfplatepoha.neomorph.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.halfplatepoha.neomorph.R

class NeumorphButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.Widget_NeoMorph_Button
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val drawable: NeumorphDrawable

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NeumorphButton, defStyleAttr, defStyleRes)
        val fillColor = a.getColorStateList(R.styleable.NeumorphButton_nm_fillColor)
        val shadowElevation = a.getDimension(R.styleable.NeumorphButton_nm_elevation, 0f)
        val shadowColorLight = a.getColor(R.styleable.NeumorphButton_colorShadowLight,
            ContextCompat.getColor(context, R.color.shadowLight)
        )
        val shadowColorDark = a.getColor(R.styleable.NeumorphButton_colorShadowDark,
            ContextCompat.getColor(context, R.color.shadowDark)
        )
        val cornerSize = a.getDimension(R.styleable.NeumorphButton_nm_cornerSize, 0f)
        a.recycle()

        drawable = NeumorphDrawable(
            context,
            attrs,
            defStyleAttr,
            defStyleRes
        ).apply {
            setShadowElevation(shadowElevation)
            setShadowColorLight(shadowColorLight)
            setShadowColorDark(shadowColorDark)
            setCornerSize(cornerSize)
            setFillColor(fillColor)

            val left = paddingLeft
            val top = paddingTop
            val right = paddingRight
            val bottom = paddingBottom
            if (arrayOf(left, top, right, bottom).any { it > 0 }) {
                setPadding(left, top, right, bottom)
            }
        }
        setBackgroundInternal(drawable)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        drawable.setPadding(left, top, right, bottom)
    }

    private fun setBackgroundInternal(drawable: Drawable?) {
        super.setBackgroundDrawable(drawable)
    }

    override fun setTranslationZ(translationZ: Float) {
        super.setTranslationZ(translationZ)
        drawable.setTranslationZ(translationZ)
    }

}
