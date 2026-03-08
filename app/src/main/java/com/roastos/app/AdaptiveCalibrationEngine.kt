package com.roastos.app

data class CalibrationState(
    val fcBiasSec: Double = 0.0,
    val dropBiasSec: Double = 0.0,
    val heatResponseBias: Double = 0.0,
    val airResponseBias: Double = 0.0,
    val beanLoadBias: Double = 0.0,
    val learningCount: Int = 0
)

data class CalibrationUpdateResult(
    val newState: CalibrationState,
    val summary: String
)

object AdaptiveCalibrationEngine {

    private const val LR_FC = 0.12
    private const val LR_DROP = 0.12
    private const val LR_RESPONSE = 0.06
    private const val LR_BEAN = 0.05

    fun update(
        current: CalibrationState,
        predictedFcSec: Int,
        predictedDropSec: Int,
        actualFcSec: Int,
        actualDropSec: Int,
        actualPreFcRor: Double,
        targetPreFcRor: Double = 9.5
    ): CalibrationUpdateResult {

        val fcError = (actualFcSec - predictedFcSec).toDouble()
        val dropError = (actualDropSec - predictedDropSec).toDouble()
        val rorError = actualPreFcRor - targetPreFcRor

        val newFcBias =
            (current.fcBiasSec + fcError * LR_FC)
                .coerceIn(-60.0, 60.0)

        val newDropBias =
            (current.dropBiasSec + dropError * LR_DROP)
                .coerceIn(-80.0, 80.0)

        val newHeatBias =
            (current.heatResponseBias + rorError * LR_RESPONSE)
                .coerceIn(-2.0, 2.0)

        val newAirBias =
            (current.airResponseBias - rorError * LR_RESPONSE)
                .coerceIn(-2.0, 2.0)

        val beanError = ((fcError * 0.6) + (dropError * 0.4)) / 30.0
        val newBeanBias =
            (current.beanLoadBias + beanError * LR_BEAN)
                .coerceIn(-2.5, 2.5)

        val newState = CalibrationState(
            fcBiasSec = newFcBias,
            dropBiasSec = newDropBias,
            heatResponseBias = newHeatBias,
            airResponseBias = newAirBias,
            beanLoadBias = newBeanBias,
            learningCount = current.learningCount + 1
        )

        val summary = buildString {
            appendLine("Adaptive Calibration Updated")
            appendLine()
            appendLine("FC Bias ${formatSigned(newFcBias)} sec")
            appendLine("Drop Bias ${formatSigned(newDropBias)} sec")
            appendLine("Heat Response Bias ${formatSigned1(newHeatBias)}")
            appendLine("Air Response Bias ${formatSigned1(newAirBias)}")
            appendLine("Bean Load Bias ${formatSigned1(newBeanBias)}")
            appendLine()
            appendLine("Observed Errors")
            appendLine("FC Error ${formatSigned(fcError)} sec")
            appendLine("Drop Error ${formatSigned(dropError)} sec")
            appendLine("Pre-FC ROR Error ${formatSigned1(rorError)}")
            appendLine()
            append("Learning Count ${newState.learningCount}")
        }

        return CalibrationUpdateResult(
            newState = newState,
            summary = summary.toString()
        )
    }

    fun applyFcBias(baseFcSec: Int, calibration: CalibrationState?): Int {
        if (calibration == null) return baseFcSec
        return (baseFcSec + calibration.fcBiasSec).toInt()
    }

    fun applyDropBias(baseDropSec: Int, calibration: CalibrationState?): Int {
        if (calibration == null) return baseDropSec
        return (baseDropSec + calibration.dropBiasSec).toInt()
    }

    fun applyBeanLoadBias(baseBeanLoad: Double, calibration: CalibrationState?): Double {
        if (calibration == null) return baseBeanLoad
        return baseBeanLoad + calibration.beanLoadBias
    }

    fun applyHeatBias(baseHeatSignal: Double, calibration: CalibrationState?): Double {
        if (calibration == null) return baseHeatSignal
        return baseHeatSignal + calibration.heatResponseBias
    }

    fun applyAirBias(baseAirSignal: Double, calibration: CalibrationState?): Double {
        if (calibration == null) return baseAirSignal
        return baseAirSignal + calibration.airResponseBias
    }

    private fun formatSigned(value: Double): String {
        val rounded = value.toInt()
        return if (rounded > 0) "+$rounded" else rounded.toString()
    }

    private fun formatSigned1(value: Double): String {
        val txt = "%.1f".format(value)
        return if (value > 0) "+$txt" else txt
    }
}
