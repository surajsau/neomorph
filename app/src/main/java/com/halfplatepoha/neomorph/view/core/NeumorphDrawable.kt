package com.halfplatepoha.neomorph.view.core

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes

class NeumorphDrawable : Drawable {

    private var drawableState: DrawableState

    private var dirty = false

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.TRANSPARENT
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.TRANSPARENT
    }

    private val rectF = RectF()

    private val outlinePath = Path()
    private var shadowRenderer: IShadowRenderer? = null

    constructor(context: Context) : this(
        Blurrer(
            context
        )
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : this(Blurrer(context))

    internal constructor(blurrer: Blurrer) : this(DrawableState(blurrer))

    private constructor(drawableState: DrawableState) : super() {
        this.drawableState = drawableState
        this.shadowRenderer = getShadowRenderer(drawableState)
    }

    private fun getShadowRenderer(drawableState: DrawableState) = when(drawableState.type) {
        RENDERER_TYPE_OUTER -> OuterShadowRenderer(drawableState)
        RENDERER_TYPE_INNER -> InnerShadowRenderer(drawableState)
        RENDERER_TYPE_COMBINED -> InnerOuterShadowRenderer(drawableState)
        else -> throw IllegalArgumentException("Wrong renderer type ${drawableState.type}")
    }

    override fun getConstantState(): ConstantState? {
        return drawableState
    }

    override fun mutate(): Drawable {
        val newDrawableState =
            DrawableState(
                drawableState
            )
        drawableState = newDrawableState
        shadowRenderer?.setDrawableState(newDrawableState)
        return this
    }

    fun setFillColor(fillColor: ColorStateList?) {
        if (drawableState.fillColor != fillColor) {
            drawableState.fillColor = fillColor
            onStateChange(state)
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        if (drawableState.alpha != alpha) {
            drawableState.alpha = alpha
            invalidate(ignoreShape = true)
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // not supported yet
    }

    private fun getBoundsInternal(): Rect {
        return drawableState.padding?.let { padding ->
            val bounds = super.getBounds()
            Rect(
                bounds.left + padding.left,
                bounds.top + padding.top,
                bounds.right - padding.right,
                bounds.bottom - padding.bottom
            )
        } ?: super.getBounds()
    }

    private fun getBoundsAsRectF(): RectF {
        rectF.set(getBoundsInternal())
        return rectF
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        if (drawableState.padding == null) {
            drawableState.padding = Rect()
        }
        drawableState.padding?.set(left, top, right, bottom)
        invalidate()
    }

    fun setShadowElevation(shadowElevation: Float) {
        if (drawableState.shadowElevation != shadowElevation) {
            drawableState.shadowElevation = shadowElevation
            invalidate()
        }
    }

    fun setShadowColorLight(@ColorInt shadowColor: Int) {
        if (drawableState.shadowColorLight != shadowColor) {
            drawableState.shadowColorLight = shadowColor
            invalidate()
        }
    }

    fun setShadowColorDark(@ColorInt shadowColor: Int) {
        if (drawableState.shadowColorDark != shadowColor) {
            drawableState.shadowColorDark = shadowColor
            invalidate()
        }
    }

    fun setTranslationZ(translationZ: Float) {
        if (drawableState.translationZ != translationZ) {
            drawableState.translationZ = translationZ
            invalidate(ignoreShape = true)
        }
    }
    
    fun setRendererType(rendererType: Int) {
        if(drawableState.type != rendererType) {
            drawableState.type = rendererType
            this.shadowRenderer = getShadowRenderer(drawableState)
            invalidate()
        }
    }

    private fun invalidate(ignoreShape: Boolean = false) {
        this.dirty = ignoreShape.not()
        super.invalidateSelf()
    }

    private fun hasFill(): Boolean {
        return (drawableState.paintStyle === Paint.Style.FILL_AND_STROKE
                || drawableState.paintStyle === Paint.Style.FILL)
    }

    private fun hasStroke(): Boolean {
        return ((drawableState.paintStyle == Paint.Style.FILL_AND_STROKE
                || drawableState.paintStyle == Paint.Style.STROKE)
                && strokePaint.strokeWidth > 0)
    }

    override fun onBoundsChange(bounds: Rect) {
        dirty = true
        super.onBoundsChange(bounds)
    }

    override fun draw(canvas: Canvas) {
        val prevAlpha = fillPaint.alpha
        fillPaint.alpha =
            modulateAlpha(
                prevAlpha,
                drawableState.alpha
            )

        strokePaint.strokeWidth = drawableState.strokeWidth
        val prevStrokeAlpha = strokePaint.alpha
        strokePaint.alpha =
            modulateAlpha(
                prevStrokeAlpha,
                drawableState.alpha
            )

        if (dirty) {
            calculateOutlinePath(getBoundsAsRectF(), outlinePath)
            shadowRenderer?.updateShadowBitmap(getBoundsInternal())
            dirty = false
        }

        if (hasFill()) {
            drawFillShape(canvas)
        }

        shadowRenderer?.draw(canvas, outlinePath)

        if (hasStroke()) {
            drawStrokeShape(canvas)
        }

        fillPaint.alpha = prevAlpha
        strokePaint.alpha = prevStrokeAlpha
    }

    private fun drawFillShape(canvas: Canvas) {
        canvas.drawPath(outlinePath, fillPaint)
    }

    private fun drawStrokeShape(canvas: Canvas) {
        canvas.drawPath(outlinePath, strokePaint)
    }

    private fun calculateOutlinePath(bounds: RectF, path: Path) {
        val left = drawableState.padding?.left?.toFloat() ?: 0f
        val top = drawableState.padding?.top?.toFloat() ?: 0f
        val right = left + bounds.width()
        val bottom = top + bounds.height()
        path.reset()
        val cornerSize = drawableState.cornerSize
        path.addRoundRect(
            left, top, right, bottom,
            cornerSize, cornerSize,
            Path.Direction.CW
        )
        path.close()
    }

    override fun getOutline(outline: Outline) {
        val cornerSize = drawableState.cornerSize
        outline.setRoundRect(getBoundsInternal(), cornerSize)
    }

    override fun isStateful(): Boolean {
        return (super.isStateful()
                || drawableState.fillColor?.isStateful == true)
    }

    override fun onStateChange(state: IntArray): Boolean {
        val invalidateSelf = updateColorsForState(state)
        if (invalidateSelf) {
            invalidate()
        }
        return invalidateSelf
    }

    private fun updateColorsForState(state: IntArray): Boolean {
        var invalidateSelf = false
        drawableState.fillColor?.let { fillColor ->
            val previousFillColor: Int = fillPaint.color
            val newFillColor: Int = fillColor.getColorForState(state, previousFillColor)
            if (previousFillColor != newFillColor) {
                fillPaint.color = newFillColor
                invalidateSelf = true
            }
        }
        drawableState.strokeColor?.let { strokeColor ->
            val previousStrokeColor = strokePaint.color
            val newStrokeColor = strokeColor.getColorForState(state, previousStrokeColor)
            if (previousStrokeColor != newStrokeColor) {
                strokePaint.color = newStrokeColor
                invalidateSelf = true
            }
        }
        return invalidateSelf
    }

    fun setCornerSize(cornerSize: Float) {
        if (this.drawableState.cornerSize != cornerSize) {
            this.drawableState.cornerSize = cornerSize
            invalidate()
        }
    }

    internal class DrawableState : ConstantState {

        var type: Int = RENDERER_TYPE_OUTER
        val blurrer: Blurrer

        var padding: Rect? = null
        var fillColor: ColorStateList? = null
        var strokeColor: ColorStateList? = null
        var strokeWidth = 0f

        var cornerSize: Float = 0f

        var alpha = 255

        var shadowElevation: Float = 0f
        var shadowColorLight: Int = Color.WHITE
        var shadowColorDark: Int = Color.BLACK
        var translationZ = 0f

        var blurRadius: Int = 25

        var paintStyle: Paint.Style = Paint.Style.FILL_AND_STROKE

        constructor(blurrer: Blurrer) {
            this.blurrer = blurrer
        }

        constructor(orig: DrawableState) {
            blurrer = orig.blurrer
            alpha = orig.alpha
            shadowElevation = orig.shadowElevation
            shadowColorLight = orig.shadowColorLight
            shadowColorDark = orig.shadowColorDark
            fillColor = orig.fillColor
            strokeColor = orig.strokeColor
            strokeWidth = orig.strokeWidth
            paintStyle = orig.paintStyle
            if (orig.padding != null) {
                padding = Rect(orig.padding)
            }
        }

        override fun newDrawable(): Drawable {
            return NeumorphDrawable(this).apply {
                // Force the calculation of the path for the new drawable.
                dirty = true
            }
        }

        override fun getChangingConfigurations(): Int {
            return 0
        }
    }

    companion object {

        private fun modulateAlpha(paintAlpha: Int, alpha: Int): Int {
            val scale = alpha + (alpha ushr 7) // convert to 0..256
            return paintAlpha * scale ushr 8
        }
    }
}
