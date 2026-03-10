package com.roastos.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.roastos.app.RoastSessionEngine
import com.roastos.app.RoastSessionPhase
import kotlin.math.max
import kotlin.math.min

class RoastCurvePanel(context: Context) : View(context) {

    private data class PhaseMarker(
        val index: Int,
        val label: String
    )

    private val btHistory = mutableListOf<Double>()
    private val rorHistory = mutableListOf<Double>()
    private val phaseMarkers = mutableListOf<PhaseMarker>()

    private val maxPoints = 180
    private var lastPhase: RoastSessionPhase? = null

    private val btPaint = Paint().apply {
        color = Color.rgb(220, 120, 40)
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val rorPaint = Paint().apply {
        color = Color.rgb(60, 140, 220)
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.rgb(210, 210, 210)
        strokeWidth = 1f
    }

    private val markerPaint = Paint().apply {
        color = Color.rgb(140, 140, 140)
        strokeWidth = 2f
    }

    private val markerTextPaint = Paint().apply {
        color = Color.rgb(110, 110, 110)
        textSize = 24f
        isAntiAlias = true
    }

    fun update() {
        val session = RoastSessionEngine.currentState()

        btHistory.add(session.lastBeanTemp)
        rorHistory.add(session.lastRor)

        if (btHistory.size > maxPoints) {
            btHistory.removeAt(0)
        }

        if (rorHistory.size > maxPoints) {
            rorHistory.removeAt(0)
        }

        val currentPhase = session.phase
        if (currentPhase != lastPhase) {
            phaseMarkers.add(
                PhaseMarker(
                    index = btHistory.lastIndex.coerceAtLeast(0),
                    label = shortPhaseLabel(currentPhase)
                )
            )
            lastPhase = currentPhase
        }

        if (phaseMarkers.size > 24) {
            phaseMarkers.removeAt(0)
        }

        shiftMarkersIfNeeded()

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        drawGrid(canvas, w, h)

        if (btHistory.size < 2) return

        val minTemp = min(btHistory.minOrNull() ?: 0.0, 0.0)
        val maxTemp = max(btHistory.maxOrNull() ?: 200.0, 50.0)

        val minRor = min(rorHistory.minOrNull() ?: -5.0, -5.0)
        val maxRor = max(rorHistory.maxOrNull() ?: 20.0, 20.0)

        drawCurve(
            canvas = canvas,
            data = btHistory,
            minVal = minTemp,
            maxVal = maxTemp,
            w = w,
            h = h,
            paint = btPaint
        )

        drawCurve(
            canvas = canvas,
            data = rorHistory,
            minVal = minRor,
            maxVal = maxRor,
            w = w,
            h = h,
            paint = rorPaint
        )

        drawPhaseMarkers(canvas, w, h)
    }

    private fun drawGrid(canvas: Canvas, w: Float, h: Float) {
        val rows = 5
        val cols = 6

        for (i in 1 until rows) {
            val y = h * i / rows
            canvas.drawLine(0f, y, w, y, gridPaint)
        }

        for (i in 1 until cols) {
            val x = w * i / cols
            canvas.drawLine(x, 0f, x, h, gridPaint)
        }
    }

    private fun drawCurve(
        canvas: Canvas,
        data: List<Double>,
        minVal: Double,
        maxVal: Double,
        w: Float,
        h: Float,
        paint: Paint
    ) {
        val range = (maxVal - minVal).takeIf { it > 0 } ?: 1.0
        val stepX = w / maxPoints

        var lastX = 0f
        var lastY = h

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val normalized = ((value - minVal) / range).toFloat()
            val y = h - normalized * h

            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, paint)
            }

            lastX = x
            lastY = y
        }
    }

    private fun drawPhaseMarkers(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        val stepX = w / maxPoints

        phaseMarkers.forEach { marker ->
            val x = marker.index * stepX
            canvas.drawLine(x, 0f, x, h, markerPaint)
            canvas.drawText(marker.label, x + 6f, 28f, markerTextPaint)
        }
    }

    private fun shiftMarkersIfNeeded() {
        if (btHistory.size <= maxPoints) return

        val shift = btHistory.size - maxPoints
        if (shift <= 0) return

        val shifted = phaseMarkers.mapNotNull { marker ->
            val newIndex = marker.index - shift
            if (newIndex >= 0) marker.copy(index = newIndex) else null
        }

        phaseMarkers.clear()
        phaseMarkers.addAll(shifted)
    }

    private fun shortPhaseLabel(phase: RoastSessionPhase): String {
        return when (phase) {
            RoastSessionPhase.IDLE -> "IDLE"
            RoastSessionPhase.PREHEAT -> "PH"
            RoastSessionPhase.CHARGE -> "CH"
            RoastSessionPhase.TURNING -> "TP"
            RoastSessionPhase.DRYING -> "DRY"
            RoastSessionPhase.MAILLARD -> "MAI"
            RoastSessionPhase.FIRST_CRACK_WINDOW -> "FC"
            RoastSessionPhase.DEVELOPMENT -> "DEV"
            RoastSessionPhase.DROP -> "DROP"
            RoastSessionPhase.COOLING -> "COOL"
            RoastSessionPhase.END -> "END"
        }
    }
}
