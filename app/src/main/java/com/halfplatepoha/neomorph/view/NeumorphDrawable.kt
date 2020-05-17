package com.halfplatepoha.neomorph.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import kotlin.math.roundToInt

class NeumorphDrawable : Drawable {

    private var drawableState: State

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.TRANSPARENT
    }

    private val lightShadowDrawable = GradientDrawable()
    private val darkShadowDrawable = GradientDrawable()

    private var lightShadowBitmap: Bitmap? = null
    private var darkShadowBitmap: Bitmap? = null

    private var isShapeDirty = false

    private var shapePath: Path = Path()

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : this()

    internal constructor() : this(State())

    private constructor(drawableState: State) : super() {
        this.drawableState = drawableState
    }

    override fun draw(canvas: Canvas) {
        //.. main drawing logic
        if(isShapeDirty) {
            this.isShapeDirty = false

            val bounds = RectF()
            bounds.set(getInternalBounds())
            calculatePath(bounds, this.shapePath)
            updateShadowBounds(this.getInternalBounds())
        }

        if(hasFill())
            drawFill(canvas)

        canvas.withClipOut(this.shapePath) {
            redrawShadows(this)
        }
    }

    private fun hasFill() = (this.drawableState.paintStyle == Paint.Style.FILL) or
            (this.drawableState.paintStyle == Paint.Style.FILL_AND_STROKE)

    private fun drawFill(canvas: Canvas) {
        canvas.drawPath(this.shapePath, this.fillPaint)
    }

    private fun calculatePath(bounds: RectF, path: Path) {
        val left = 0f
        val top = 0f
        val right = left + bounds.width()
        val bottom = top + bounds.height()

        val cornerSize = this.drawableState.cornerRadius

        path.reset()
        path.addRoundRect(left, top, right, bottom, cornerSize, cornerSize, Path.Direction.CW)
        path.close()
    }

    private fun invalidate(ignoreShape: Boolean = false) {
        this.isShapeDirty = ignoreShape.not()
        super.invalidateSelf()
    }

    private fun redrawShadows(canvas: Canvas) {
        val z = this.drawableState.elevation + this.drawableState.translationZ
        val elevation = this.drawableState.elevation

        val lightShadowOffset = -elevation - z
        val darkShadowOffset = -elevation + z

        val left = 0f
        val top = 0f

        this.lightShadowBitmap?.let {
            canvas.drawBitmap(it, left + lightShadowOffset, top + lightShadowOffset, null)
        }

        this.darkShadowBitmap?.let {
            canvas.drawBitmap(it, left + darkShadowOffset, top + darkShadowOffset, null)
        }
    }

    private fun getInternalBounds() = super.getBounds()

    private fun updateShadowBounds(bounds: Rect) {

        val elevation = this.drawableState.elevation

        fun GradientDrawable.toRoundedCorners(radius: Float?) {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = radius?.let {
                floatArrayOf(it, it, it, it, it, it, it, it)
            }
        }

        this.lightShadowDrawable.apply {
            setColor(Color.BLUE)
            toRoundedCorners(this@NeumorphDrawable.drawableState.cornerRadius)
        }

        this.darkShadowDrawable.apply {
            setColor(Color.RED)
            toRoundedCorners(this@NeumorphDrawable.drawableState.cornerRadius)
        }

        val width = bounds.width()
        val height = bounds.height()

        this.lightShadowDrawable.setSize(width, height)
        this.lightShadowDrawable.setBounds(0, 0, width, height)
        this.darkShadowDrawable.setSize(width, height)
        this.darkShadowDrawable.setBounds(0, 0, width, height)

        val actualWidth = (width + 2 * elevation).roundToInt()
        val actualHeight = (height + 2 * elevation).roundToInt()

        this.lightShadowBitmap = lightShadowDrawable.getBitmap(actualWidth, actualHeight)
        this.darkShadowBitmap = darkShadowDrawable.getBitmap(actualWidth, actualHeight)
    }

    private fun Drawable.getBitmap(width: Int, height: Int): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        .onCanvas {
            val elevation = this@NeumorphDrawable.drawableState.elevation
            withTranslate(elevation, elevation) {
                draw(this)
            }
        }

    fun setLightShadowColor(color: Int) {
        if(this.drawableState.lightShadowColor != color) {
            this.drawableState.lightShadowColor = color
            invalidate()
        }
    }

    fun setDarkShadowColor(color: Int) {
        if(this.drawableState.darkShadowColor != color) {
            this.drawableState.darkShadowColor = color
            invalidate()
        }
    }

    override fun setAlpha(alpha: Int) {
        //.. set alpha and invalidate
        if(this.drawableState.alpha != alpha) {
            this.drawableState.alpha = alpha
            invalidate(ignoreShape = true)
        }
    }

    override fun mutate(): Drawable {
        val state = State(this.drawableState)
        this.drawableState = state
        return this
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //.. do nothing
    }

    override fun onStateChange(state: IntArray?): Boolean {
        val invalidated = updateColors(state)
        if(invalidated)
            invalidate()
        return invalidated
    }

    override fun onBoundsChange(bounds: Rect?) {
        this.isShapeDirty = true
        super.onBoundsChange(bounds)
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(getInternalBounds(), this.drawableState.cornerRadius)
    }

    private fun updateColors(state: IntArray?): Boolean {
        var invalidate = false
        this.drawableState.fillColor?.let {
            val previousColor = this.fillPaint.color
            val currentColor = it.getColorForState(state, previousColor)

            if(previousColor != currentColor) {
                this.fillPaint.color = currentColor
                invalidate = true
            }
        }

        return invalidate
    }

    fun setTranslationZ(z: Float) {
        if(this.drawableState.translationZ != z) {
            this.drawableState.translationZ = z
            invalidate(ignoreShape = true)
        }
    }

    override fun isStateful(): Boolean = super.isStateful() || (this.drawableState.fillColor?.isStateful == true)

    override fun getConstantState(): ConstantState? = this.drawableState

    internal class State: ConstantState {

        var fillColor: ColorStateList? = null
        var lightShadowColor: Int = Color.WHITE
        var darkShadowColor: Int = Color.BLACK
        var alpha: Int = 255
        var elevation: Float = 6f
        var translationZ: Float = 10f
        var cornerRadius: Float = 25f
        var paintStyle: Paint.Style = Paint.Style.FILL_AND_STROKE

        constructor() {

        }

        constructor(orig: State) {
            alpha = orig.alpha
            elevation = orig.elevation
            lightShadowColor = orig.lightShadowColor
            darkShadowColor = orig.darkShadowColor
            fillColor = orig.fillColor
            paintStyle = orig.paintStyle
        }

        override fun newDrawable(): Drawable {
            return NeumorphDrawable(this).apply {
                this.isShapeDirty = true
            }
        }

        override fun getChangingConfigurations(): Int = 0

    }

}