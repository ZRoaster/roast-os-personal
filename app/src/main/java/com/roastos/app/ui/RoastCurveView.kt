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
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val predictedBtPaint = Paint().apply {
        color = Color.parseColor("#E53935")
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val actualBtPaint = Paint().apply {
        color = Color.parseColor("#2E7D32")
        strokeWidth = 4f
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

    private val predictedDevPaint = Paint().apply {
        color = Color.parseColor("#FFE0B2")
        style = Paint.Style.FILL
        alpha = 110
        isAntiAlias = true
    }

    private val actualDevPaint = Paint().apply {
        color = Color.parseColor("#C8E6C9")
        style = Paint.Style.FILL
        alpha = 120
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

        val allPoints = data.predictedPoints + data.actualPoints

        if (allPoints.isEmpty()) {
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

        val maxTime = max(1, allPoints.maxOf { it.timeSec })
        val minBt = allPoints.minOf { it.bt }
        val maxBt = allPoints.maxOf { it.bt }

        drawDevelopmentWindow(
            canvas = canvas,
            anchors = data.anchors,
            left = left,
            top = top,
            bottom = bottom,
            plotWidth = plotWidth,
            maxTime = maxTime
        )

        drawAnchors(
            canvas = canvas,
            anchors = data.anchors,
            left = left,
            top = top,
            bottom = bottom,
            plotWidth = plotWidth,
            maxTime = maxTime
        )

        drawCurve(
            canvas = canvas,
            points = data.predictedPoints,
            paint = predictedBtPaint,
            left = left,
            bottom = bottom,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            minBt = minBt,
            maxBt = maxBt
        )

        drawCurve(
            canvas = canvas,
            points = data.actualPoints,
            paint = actualBtPaint,
            left = left,
            bottom = bottom,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            minBt = minBt,
            maxBt = maxBt
        )

        drawLabels(
            canvas = canvas,
            left = left,
            bottom = bottom,
            right = right,
            minBt = minBt,
            maxBt = maxBt,
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
            val y = top + (bottom - top) * i / hLines
            canvas.drawLine(left, y, right, y, gridPaint)
        }

        for (i in 1 until vLines) {
            val x = left + (right - left) * i / vLines
            canvas.drawLine(x, top, x, bottom, gridPaint)
        }
    }

    private fun drawDevelopmentWindow(
        canvas: Canvas,
        anchors: List<CurveAnchor>,
        left: Float,
        top: Float,
        bottom: Float,
        plotWidth: Float,
        maxTime: Int
    ) {
        val actualFc = anchors.firstOrNull { it.label == "FC" && it.isActual }?.timeSec
        val actualDrop = anchors.firstOrNull { it.label == "Drop" && it.isActual }?.timeSec

        if (actualFc != null && actualDrop != null && actualDrop > actualFc) {
            val x0 = left + (actualFc.toFloat() / maxTime) * plotWidth
            val x1 = left + (actualDrop.toFloat() / maxTime) * plotWidth
            canvas.drawRect(x0, top, x1, bottom, actualDevPaint)
            canvas.drawText("DEV A", x0 + 8f, top + 52f, smallTextPaint)
            return
        }

        val predFc = anchors.firstOrNull { it.label == "FC" && !it.isActual }?.timeSec
        val predDrop = anchors.firstOrNull { it.label == "Drop" && !it.isActual }?.timeSec

        if (predFc != null && predDrop != null && predDrop > predFc) {
            val x0 = left + (predFc.toFloat() / maxTime) * plotWidth
            val x1 = left + (predDrop.toFloat() / maxTime) * plotWidth
            canvas.drawRect(x0, top, x1, bottom, predictedDevPaint)
            canvas.drawText("DEV P", x0 + 8f, top + 52f, smallTextPaint)
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
            val x = left + (anchor.timeSec.toFloat() / maxTime) * plotWidth
            val paint = if (anchor.isActual) actualAnchorPaint else predictedAnchorPaint

            canvas.drawLine(x, top, x, bottom, paint)

            val label = if (anchor.isActual) "${anchor.label} A" else "${anchor.label} P"
            canvas.drawText(label, x + 4f, top + 24f, smallTextPaint)
        }
    }

    private fun drawCurve(
        canvas: Canvas,
        points: List<CurvePoint>,
        paint: Paint,
        left: Float,
        bottom: Float,
        plotWidth: Float,
        plotHeight: Float,
        maxTime: Int,
        minBt: Double,
        maxBt: Double
    ) {
        if (points.size < 2) return
        if (maxBt <= minBt) return

        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]

            val x0 = left + (p0.timeSec.toFloat() / maxTime) * plotWidth
            val x1 = left + (p1.timeSec.toFloat() / maxTime) * plotWidth

            val y0 = bottom - ((p0.bt - minBt) / (maxBt - minBt)).toFloat() * plotHeight
            val y1 = bottom - ((p1.bt - minBt) / (maxBt - minBt)).toFloat() * plotHeight

            canvas.drawLine(x0, y0, x1, y1, paint)
        }
    }

    private fun drawLabels(
        canvas: Canvas,
        left: Float,
        bottom: Float,
        right: Float,
        minBt: Double,
        maxBt: Double,
        maxTime: Int
    ) {
        canvas.drawText("${maxBt.toInt()}°", 10f, 80f, textPaint)
        canvas.drawText("${minBt.toInt()}°", 10f, bottom, textPaint)

        canvas.drawText("0:00", left, bottom + 40f, smallTextPaint)
        canvas.drawText(
            "${maxTime / 60}:${(maxTime % 60).toString().padStart(2, '0')}",
            right - 90f,
            bottom + 40f,
            smallTextPaint
        )

        canvas.drawText("Pred BT", left, 36f, smallTextPaint)
        canvas.drawText("Actual BT", left + 140f, 36f, smallTextPaint)
    }
}
