package com.roastos.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.roastos.app.RoastSessionEngine

class RoastCurvePanel(context: Context) : View(context) {

    private val btHistory = mutableListOf<Double>()
    private val rorHistory = mutableListOf<Double>()

    private val maxPoints = 120

    private val btPaint = Paint().apply {
        color = Color.rgb(220, 120, 40)
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val rorPaint = Paint().apply {
        color = Color.rgb(60, 140, 220)
        strokeWidth = 4f
        style = Paint.Style.STROKE
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

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (btHistory.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()

        val stepX = w / maxPoints

        var lastX = 0f
        var lastY = h

        btHistory.forEachIndexed { index, value ->

            val x = index * stepX
            val y = h - (value * 2).toFloat()

            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, btPaint)
            }

            lastX = x
            lastY = y
        }

        lastX = 0f
        lastY = h

        rorHistory.forEachIndexed { index, value ->

            val x = index * stepX
            val y = h - (value * 10).toFloat()

            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, rorPaint)
            }

            lastX = x
            lastY = y
        }
    }
}
