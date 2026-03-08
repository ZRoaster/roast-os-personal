package com.roastos.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.roastos.app.CurveAnchor
import com.roastos.app.CurvePoint
import com.roastos.app.RoastCurveResult
import kotlin.math.max

class RoastCurveView(context: Context) : View(context) {

    private var curve: RoastCurveResult? = null

    private val axisPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val btPaint = Paint().apply {
        color = Color.parseColor("#D32F2F")
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val rorPaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val predictedAnchorPaint = Paint().apply {
        color = Color.parseColor("#9E9E9E")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val actualAnchorPaint = Paint().apply {
        color = Color.parseColor("#388E3C")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
        isAntiAlias = true
    }

    private val smallTextPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 22f
        isAntiAlias = true
    }

    fun setCurve(curveResult: RoastCurveResult) {
        curve = curveResult
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 720
        val resolvedWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(resolvedWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val data = curve ?: run {
            canvas.drawText("No curve data", 40f, 80f, textPaint)
            return
        }

        if (data.points.isEmpty()) {
            canvas.drawText("No curve points", 40f, 80f, textPaint)
            return
        }

        val left = 90f
        val top = 60f
        val right = width - 40f
        val bottom = height - 90f

        val plotWidth = right - left
        val plotHeight = bottom - top

        canvas.drawRect(left, top, right, bottom, axisPaint)

        drawGrid(canvas, left, top, right, bottom)

        val maxTime = max(1, data.points.maxOf { it.timeSec })
        val minBt = data.points.minOf { it.bt }
        val maxBt = data.points.maxOf { it.bt }
        val minRor = data.points.minOf { it.ror }
        val maxRor = data.points.maxOf { it.ror }

        val btMin = minBt - 5.0
        val btMax = maxBt + 5.0
        val rorMin = minRor - 2.0
        val rorMax = maxRor + 2.0

        drawAnchors(
            canvas = canvas,
            anchors = data.anchors,
            left = left,
            top = top,
            bottom = bottom,
            plotWidth = plotWidth,
            maxTime = maxTime
        )

        drawBtCurve(
            canvas = canvas,
            points = data.points,
            left = left,
            bottom = bottom,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            minBt = btMin,
            maxBt = btMax
        )

        drawRorCurve(
            canvas = canvas,
            points = data.points,
            left = left,
            bottom = bottom,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            minRor = rorMin,
            maxRor = rorMax
        )

        drawLabels(
            canvas = canvas,
            left = left,
            top = top,
            bottom = bottom,
            right = right,
            btMin = btMin,
            btMax = btMax,
            rorMin = rorMin,
            rorMax = rorMax,
            maxTime = maxTime
        )
    }

    private fun drawGrid(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val hLines = 5
        val vLines = 6

        for (i in 1 until hLines) {
            val y = top + (bottom - top) * i / hLines.toFloat()
            canvas.drawLine(left, y, right, y, gridPaint)
        }

        for (i in 1 until vLines) {
            val x = left + (right - left) * i / vLines.toFloat()
            canvas.drawLine(x, top, x, bottom, gridPaint)
        }
    }

    private fun drawAnchors(
        canvas: Canvas,
        anchors: List<CurveAnchor>,
        left: Float,
        top: Float,
        bottom: Float,
        plotWidth: Float,
        maxTime: Int
    ) {
        anchors.forEach { anchor ->
            val x = left + (anchor.timeSec.toFloat() / maxTime.toFloat()) * plotWidth
            val paint = if (anchor.isActual) actualAnchorPaint else predictedAnchorPaint
            canvas.drawLine(x, top, x, bottom, paint)

            val label = if (anchor.isActual) "${anchor.label} A" else "${anchor.label} P"
            canvas.drawText(label, x + 6f, top + 28f, smallTextPaint)
        }
    }

    private fun drawBtCurve(
        canvas: Canvas,
        points: List<CurvePoint>,
        left: Float,
        bottom: Float,
        plotWidth: Float,
        plotHeight: Float,
        maxTime: Int,
        minBt: Double,
        maxBt: Double
    ) {
        if (points.size < 2) return

        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]

            val x0 = left + (p0.timeSec.toFloat() / maxTime.toFloat()) * plotWidth
            val y0 = bottom - (((p0.bt - minBt) / (maxBt - minBt)).toFloat() * plotHeight)

            val x1 = left + (p1.timeSec.toFloat() / maxTime.toFloat()) * plotWidth
            val y1 = bottom - (((p1.bt - minBt) / (maxBt - minBt)).toFloat() * plotHeight)

            canvas.drawLine(x0, y0, x1, y1, btPaint)
        }
    }

    private fun drawRorCurve(
        canvas: Canvas,
        points: List<CurvePoint>,
        left: Float,
        bottom: Float,
        plotWidth: Float,
        plotHeight: Float,
        maxTime: Int,
        minRor: Double,
        maxRor: Double
    ) {
        if (points.size < 2) return

        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]

            val x0 = left + (p0.timeSec.toFloat() / maxTime.toFloat()) * plotWidth
            val y0 = bottom - (((p0.ror - minRor) / (maxRor - minRor)).toFloat() * plotHeight)

            val x1 = left + (p1.timeSec.toFloat() / maxTime.toFloat()) * plotWidth
            val y1 = bottom - (((p1.ror - minRor) / (maxRor - minRor)).toFloat() * plotHeight)

            canvas.drawLine(x0, y0, x1, y1, rorPaint)
        }
    }

    private fun drawLabels(
        canvas: Canvas,
        left: Float,
        top: Float,
        bottom: Float,
        right: Float,
        btMin: Double,
        btMax: Double,
        rorMin: Double,
        rorMax: Double,
        maxTime: Int
    ) {
        canvas.drawText("BT", left, top - 16f, textPaint)
        canvas.drawText("ROR", right - 70f, top - 16f, textPaint)

        canvas.drawText("${"%.0f".format(btMax)}", 20f, top + 10f, smallTextPaint)
        canvas.drawText("${"%.0f".format(btMin)}", 20f, bottom, smallTextPaint)

        canvas.drawText("${"%.1f".format(rorMax)}", right - 70f, top + 20f, smallTextPaint)
        canvas.drawText("${"%.1f".format(rorMin)}", right - 70f, bottom, smallTextPaint)

        canvas.drawText("0:00", left, bottom + 40f, smallTextPaint)
        canvas.drawText(
            "${maxTime / 60}:${(maxTime % 60).toString().padStart(2, '0')}",
            right - 90f,
            bottom + 40f,
            smallTextPaint
        )
    }
}
