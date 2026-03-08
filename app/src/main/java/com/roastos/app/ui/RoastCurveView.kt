package com.roastos.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.roastos.app.CurveAnchor
import com.roastos.app.CurveDeviation
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
        strokeWidth = 5f
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
        color = Color.parseColor("#1B5E20")
        strokeWidth = 4f
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

    private val rorBandPaint = Paint().apply {
        color = Color.parseColor("#BBDEFB")
        style = Paint.Style.FILL
        alpha = 90
        isAntiAlias = true
    }

    private val rorBandEdgePaint = Paint().apply {
        color = Color.parseColor("#64B5F6")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val infoBoxPaint = Paint().apply {
        color = Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
        alpha = 220
        isAntiAlias = true
    }

    private val infoBorderPaint = Paint().apply {
        color = Color.parseColor("#BDBDBD")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun setCurve(curveResult: RoastCurveResult) {
        curve = curveResult
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 780
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
        val bottom = height - 110f

        val plotWidth = right - left
        val plotHeight = bottom - top

        canvas.drawRect(left, top, right, bottom, axisPaint)
        drawGrid(canvas, left, top, right, bottom)

        val maxTime = max(1, allPoints.maxOf { it.timeSec })
        val minBt = allPoints.minOf { it.bt }
        val maxBt = allPoints.maxOf { it.bt }

        drawRorTargetBand(
            canvas = canvas,
            anchors = data.anchors,
            left = left,
            top = top,
            bottom = bottom,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime
        )

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

        drawLegend(
            canvas = canvas,
            left = left,
            top = top
        )

        drawDeviationSummary(
            canvas = canvas,
            deviations = data.deviations,
            right = right,
            top = top
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

    private fun drawRorTargetBand(
        canvas: Canvas,
        anchors: List<CurveAnchor>,
        left: Float,
        top: Float,
        bottom: Float,
        plotWidth: Float,
        plotHeight: Float,
        maxTime: Int
    ) {
        val predTurning = anchors.firstOrNull { it.label == "Turning" && !it.isActual }?.timeSec ?: return
        val predYellow = anchors.firstOrNull { it.label == "Yellow" && !it.isActual }?.timeSec ?: return
        val predFc = anchors.firstOrNull { it.label == "FC" && !it.isActual }?.timeSec ?: return
        val predDrop = anchors.firstOrNull { it.label == "Drop" && !it.isActual }?.timeSec ?: return

        drawBandSegment(
            canvas = canvas,
            startSec = predTurning,
            endSec = predYellow,
            upperRatio = 0.16f,
            lowerRatio = 0.28f,
            left = left,
            top = top,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            label = "ROR D"
        )

        drawBandSegment(
            canvas = canvas,
            startSec = predYellow,
            endSec = predFc,
            upperRatio = 0.24f,
            lowerRatio = 0.36f,
            left = left,
            top = top,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            label = "ROR M"
        )

        drawBandSegment(
            canvas = canvas,
            startSec = predFc,
            endSec = predDrop,
            upperRatio = 0.34f,
            lowerRatio = 0.46f,
            left = left,
            top = top,
            plotWidth = plotWidth,
            plotHeight = plotHeight,
            maxTime = maxTime,
            label = "ROR DEV"
        )
    }

    private fun drawBandSegment(
        canvas: Canvas,
        startSec: Int,
        endSec: Int,
        upperRatio: Float,
        lowerRatio: Float,
        left: Float,
        top: Float,
        plotWidth: Float,
        plotHeight: Float,
        maxTime: Int,
        label: String
    ) {
        if (endSec <= startSec) return

        val x0 = left + (startSec.toFloat() / maxTime) * plotWidth
        val x1 = left + (endSec.toFloat() / maxTime) * plotWidth
        val y0 = top + plotHeight * upperRatio
        val y1 = top + plotHeight * lowerRatio

        canvas.drawRect(x0, y0, x1, y1, rorBandPaint)
        canvas.drawRect(x0, y0, x1, y1, rorBandEdgePaint)
        canvas.drawText(label, x0 + 8f, y0 + 26f, smallTextPaint)
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
            val labelY = if (anchor.isActual) top + 52f else top + 24f
            canvas.drawText(label, x + 4f, labelY, smallTextPaint)
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

        canvas.drawText("0:00", left, bottom + 44f, smallTextPaint)
        canvas.drawText(
            "${maxTime / 60}:${(maxTime % 60).toString().padStart(2, '0')}",
            right - 90f,
            bottom + 44f,
            smallTextPaint
        )
    }

    private fun drawLegend(
        canvas: Canvas,
        left: Float,
        top: Float
    ) {
        val legendY = top - 18f

        canvas.drawLine(left, legendY, left + 40f, legendY, predictedBtPaint)
        canvas.drawText("Pred BT", left + 48f, legendY + 8f, smallTextPaint)

        val secondX = left + 170f
        canvas.drawLine(secondX, legendY, secondX + 40f, legendY, actualBtPaint)
        canvas.drawText("Actual BT", secondX + 48f, legendY + 8f, smallTextPaint)
    }

    private fun drawDeviationSummary(
        canvas: Canvas,
        deviations: List<CurveDeviation>,
        right: Float,
        top: Float
    ) {
        if (deviations.isEmpty()) return

        val boxWidth = 250f
        val lineHeight = 28f
        val boxHeight = 40f + deviations.size * lineHeight + 16f
        val left = right - boxWidth
        val bottom = top + boxHeight

        canvas.drawRect(left, top + 8f, right, bottom, infoBoxPaint)
        canvas.drawRect(left, top + 8f, right, bottom, infoBorderPaint)

        canvas.drawText("Deviation", left + 12f, top + 34f, smallTextPaint)

        deviations.forEachIndexed { index, deviation ->
            val y = top + 62f + index * lineHeight
            val line = "${deviation.label} ${formatSigned(deviation.deltaSec)}s ${deviation.severity}"
            canvas.drawText(line, left + 12f, y, smallTextPaint)
        }
    }

    private fun formatSigned(value: Int): String {
        return if (value > 0) "+$value" else value.toString()
    }
}
