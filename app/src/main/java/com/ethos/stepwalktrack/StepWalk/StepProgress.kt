package com.ethos.stepwalktrack.StepWalk

/**
 * Created by Ashish Kr on 13,August,2025
 */

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.getOrNull
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.let
import kotlin.math.min
import kotlin.text.isNotEmpty
import kotlin.text.split
import kotlin.text.toIntOrNull
import kotlin.text.trim
import com.ethos.stepwalktrack.R

///////////////////////////////////////5
class StepProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Milestone(
        val step: Int,
        val label: String,
        var markerColor: Int? = null,
        var markerDrawable: Drawable? = null
    )

    private var maxSteps = 100
    private var currentSteps = 0
    private var milestones = mutableListOf<Milestone>()

    private var gradientStart = Color.GREEN
    private var gradientCenter = Color.GRAY
    private var gradientEnd = Color.BLUE
    private var markerColor = Color.WHITE // default
    private var markerDrawable: Drawable? = null // default
    private var thumbDrawable: Drawable? = null
    private var progressWidth = 20f

    private var topPaddingExtra = 0f
    private var bottomPaddingExtra = 0f

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.LEFT
    }

    // Glow effect
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.CYAN
        //color = Color.parseColor("#E744BD80")
        maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
    }
    private var pulseAlpha = 0
    private var pulseAnimator: ValueAnimator? = null

    init {
        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.StepProgressView, 0, 0)
        try {
            maxSteps = ta.getInt(R.styleable.StepProgressView_maxSteps, 100)
            gradientStart = ta.getColor(R.styleable.StepProgressView_gradientStart, Color.GREEN)
            gradientCenter = ta.getColor(R.styleable.StepProgressView_gradientCenter, Color.GRAY)
            gradientEnd = ta.getColor(R.styleable.StepProgressView_gradientEnd, Color.BLUE)
            markerColor = ta.getColor(R.styleable.StepProgressView_markerColor, Color.WHITE)
            progressWidth = ta.getDimension(R.styleable.StepProgressView_progressWidth, 20f)

            topPaddingExtra = ta.getDimension(R.styleable.StepProgressView_topPaddingExtra, 0f)
            bottomPaddingExtra = ta.getDimension(R.styleable.StepProgressView_bottomPaddingExtra, 0f)

            val milestoneStr = ta.getString(R.styleable.StepProgressView_milestones)
            milestones.clear()
            milestoneStr?.split(",")?.forEach {
                val parts = it.trim().split(":")
                val step = parts.getOrNull(0)?.toIntOrNull()
                val label = parts.getOrNull(1)?.trim() ?: ""
                if (step != null) milestones.add(Milestone(step, label))
            }

            val markerResId = ta.getResourceId(R.styleable.StepProgressView_markerDrawable, 0)
            if (markerResId != 0) markerDrawable = ContextCompat.getDrawable(context, markerResId)

            val thumbResId = ta.getResourceId(R.styleable.StepProgressView_thumbDrawable, 0)
            if (thumbResId != 0) thumbDrawable = ContextCompat.getDrawable(context, thumbResId)

        } finally {
            ta.recycle()
        }

        progressPaint.style = Paint.Style.FILL
        markerPaint.style = Paint.Style.FILL
        markerPaint.color = markerColor

        bgPaint.style = Paint.Style.FILL
        bgPaint.color = Color.parseColor("#22FFFFFF")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val barTop = topPaddingExtra
        val barBottom = height.toFloat() - bottomPaddingExtra
        val barHeight = barBottom - barTop

        // Background bar
        canvas.drawRoundRect(
            centerX - progressWidth / 2, barTop,
            centerX + progressWidth / 2, barBottom,
            progressWidth / 2, progressWidth / 2, bgPaint
        )

        // Gradient progress fill
        val shader = LinearGradient(
            centerX, barBottom,
            centerX, barTop,
            intArrayOf(gradientStart, gradientCenter, gradientEnd),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        progressPaint.shader = shader

        val progressHeight = (currentSteps.toFloat() / maxSteps) * barHeight
        val progressTop = barBottom - progressHeight
        canvas.drawRoundRect(
            centerX - progressWidth / 2,
            progressTop,
            centerX + progressWidth / 2,
            barBottom,
            progressWidth / 2, progressWidth / 2,
            progressPaint
        )

        // Draw milestones with glow
        milestones.forEach { milestone ->
            val yPos = barBottom - (milestone.step.toFloat() / maxSteps) * barHeight

            val distance = milestone.step - currentSteps
            val distanceFactor = when {
                distance <= 0 -> 0f
                distance < 10 -> 1f
                distance < 30 -> 0.6f
                distance < 50 -> 0.3f
                else -> 0f
            }

//            if (distanceFactor > 0f) {
//                glowPaint.alpha = (pulseAlpha * distanceFactor).toInt().coerceIn(0, 255)
//                canvas.drawCircle(centerX, yPos, progressWidth * 1.2f, glowPaint)
//            }
            if (distanceFactor > 0f) {
                glowPaint.color = milestone.markerColor ?: markerColor
                glowPaint.alpha = (pulseAlpha * distanceFactor).toInt().coerceIn(0, 255)
                canvas.drawCircle(centerX, yPos, progressWidth * 1.2f, glowPaint)
            }

            val drawableToUse = milestone.markerDrawable ?: markerDrawable
            val colorToUse = milestone.markerColor ?: markerColor

            if (drawableToUse != null) {
                val size = (progressWidth * 1.8).toInt()
                val left = (centerX - size / 2).toInt()
                val top = (yPos - size / 2).toInt()
                drawableToUse.setBounds(left, top, left + size, top + size)
                drawableToUse.draw(canvas)
            } else {
                markerPaint.color = colorToUse
                canvas.drawCircle(centerX, yPos, progressWidth / 1.5f, markerPaint)
            }

            if (milestone.label.isNotEmpty()) {
                val textX = centerX + progressWidth
                val textY = yPos + (textPaint.textSize / 3)
                canvas.drawText(milestone.label, textX + 20f, textY, textPaint)
            }
        }

        // Thumb
        thumbDrawable?.let {
            val size = (progressWidth * 2).toInt()
            val left = (centerX - size / 2).toInt()
            val top = (progressTop - size / 2).toInt()
            it.setBounds(left, top, left + size, top + size)
            it.draw(canvas)
        }
    }

    fun setSteps(steps: Int, animate: Boolean = true) {
        val closest = milestones.minOfOrNull { Math.abs(it.step - steps) } ?: Int.MAX_VALUE
        if (closest < 50 && steps < maxSteps) {
            startPulseAnimation()
        } else {
            stopPulseAnimation()
        }

        if (animate) {
            ValueAnimator.ofInt(currentSteps, steps).apply {
                duration = 500
                addUpdateListener {
                    currentSteps = it.animatedValue as Int
                    invalidate()
                }
                start()
            }
        } else {
            currentSteps = steps
            invalidate()
        }
    }

    fun setMaxSteps(max: Int) {
        maxSteps = max
        invalidate()
    }

    fun setMilestones(milestoneList: List<Pair<Int, String>>) {
        milestones.clear()
        milestones.addAll(milestoneList.map { Milestone(it.first, it.second) })
        invalidate()
    }

    fun setMilestoneDrawable(index: Int, drawable: Drawable?) {
        if (index in milestones.indices) {
            milestones[index].markerDrawable = drawable
            invalidate()
        }
    }

    fun setMilestoneColor(index: Int, color: Int) {
        if (index in milestones.indices) {
            milestones[index].markerColor = color
            invalidate()
        }
    }

    private fun startPulseAnimation() {
        if (pulseAnimator?.isRunning == true) return
        pulseAnimator = ValueAnimator.ofInt(50, 255).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                pulseAlpha = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
    }
}

