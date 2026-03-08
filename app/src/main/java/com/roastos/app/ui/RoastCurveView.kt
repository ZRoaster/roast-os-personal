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
    }

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
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

    private val rorPaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val anchorPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
    }

    fun setCurve(curveResult: RoastCurveResult) {
        curve = curveResult
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 700
        val resolvedWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(resolvedWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val data = curve ?: return

        val allPoints = data.predictedPoints + data.actualPoints

        if (allPoints.isEmpty()) {
            canvas.drawText("No curve data", 40f, 80f, textPaint)
            return
        }

        val left = 80f
        val top = 60f
        val right = width - 40f
        val bottom = height - 80f

        val plotWidth = right - left
        val plotHeight = bottom - top

        canvas.drawRect(left, top, right, bottom, axisPaint)

        drawGrid(canvas, left, top, right, bottom)

        val maxTime = max(1, allPoints.maxOf { it.timeSec })

        val minBt = allPoints.minOf { it.bt }
        val maxBt = allPoints.maxOf { it.bt }

        drawAnchors(
            canvas,
            data.anchors,
            left,
            top,
            bottom,
            plotWidth,
            maxTime
        )

        drawCurve(
            canvas,
            data.predictedPoints,
            predictedBtPaint,
            left,
            bottom,
            plotWidth,
            plotHeight,
            maxTime,
            minBt,
            maxBt
        )

        drawCurve(
            canvas,
            data.actualPoints,
            actualBtPaint,
            left,
            bottom,
            plotWidth,
            plotHeight,
            maxTime,
            minBt,
            maxBt
        )

        drawLabels(canvas, left, bottom, right, minBt, maxBt, maxTime)
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

            canvas.drawLine(x, top, x, bottom, anchorPaint)

            canvas.drawText(
                anchor.label,
                x + 4f,
                top + 24f,
                textPaint
            )
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

        canvas.drawText(
            "${maxBt.toInt()}°",
            10f,
            80f,
            textPaint
        )

        canvas.drawText(
            "${minBt.toInt()}°",
            10f,
            bottom,
            textPaint
        )

        canvas.drawText(
            "0:00",
            left,
            bottom + 40f,
            textPaint
        )

        canvas.drawText(
            "${maxTime / 60}:${(maxTime % 60).toString().padStart(2, '0')}",
            right - 80f,
            bottom + 40f,
            textPaint
        )
    }
}
