package com.roastos.app

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class BatchActualInput(
    val turningSec: Int,
    val yellowSec: Int,
    val firstCrackSec: Int,
    val dropSec: Int,
    val preFcRor: Double
)

data class CorrectionResult(
    val batchIndex: Int,
    val stageLabel: String,

    val deltaTurningSec: Int,
    val deltaYellowSec: Int,
    val deltaFcSec: Int,
    val deltaDropSec: Int,
    val deltaPreFcRor: Double,

    val correctedChargeBT: Int,

    val correctedH1W: Int,
    val correctedH2W: Int,
    val correctedH3W: Int,
    val correctedH4W: Int,
    val correctedH5W: Int,

    val correctedWind1Pa: Int,
    val correctedWind2Pa: Int,
    val correctedDevPa: Int,

    val correctedTurningTargetSec: Int,
    val correctedYellowTargetSec: Int,
    val correctedFcTargetSec: Int,
    val correctedDropTargetSec: Int,

    val correctedProtectSec: Int,

    val heatBiasScore: Double,
    val airflowBiasScore: Double,
    val inertiaBiasScore: Double,

    val diagnosis: List<String>,
    val actions: List<String>,
    val batch2ExecutionCard: String
)

object CorrectionEngine {

    private fun clampInt(v: Int, lo: Int, hi: Int): Int = max(lo, min(hi, v))
    private fun clampDouble(v: Double, lo: Double, hi: Double): Double = max(lo, min(hi, v))

    private fun roundPower(v: Int): Int {
        val steps = listOf(1160, 1200, 1260, 1320, 1380, 1450)
        return steps.minByOrNull { abs(it - v) } ?: v
    }

    private fun stageLabel(batchIndex: Int): String {
        return when (batchIndex) {
            1 -> "Cold Start Batch 1 → Batch 2 Correction"
            2 -> "Calibration Batch 2 → Batch 3 Lock"
            else -> "Stabilized Correction"
        }
    }

    private fun estimateYellowFromPlanner(pred: PlannerResult): Int {
        val approx = pred.h2Sec.roundToInt()
        return clampInt(approx, pred.h1Sec.roundToInt() + 60, pred.h3Sec.roundToInt() - 20)
    }

    private fun estimateTurningFromPlanner(pred: PlannerResult): Int {
        return clampInt((pred.h1Sec - 60.0).roundToInt(), 50, 120)
    }

    fun correct(
        plannerInput: PlannerInput,
        predicted: PlannerResult,
        actual: BatchActualInput,
        batchIndex: Int = 1
    ): CorrectionResult {

        val predTurning = estimateTurningFromPlanner(predicted)
        val predYellow = estimateYellowFromPlanner(predicted)
        val predFc = predicted.fcPredSec.roundToInt()
        val predDrop = predicted.dropSec.roundToInt()

        val deltaTT = actual.turningSec - predTurning
        val deltaTY = actual.yellowSec - predYellow
        val deltaFC = actual.firstCrackSec - predFc
        val deltaDrop = actual.dropSec - predDrop
        val deltaPreFcRor = actual.preFcRor - predicted.rorFull5[3]

        /*
         * 三个偏差评分
         * heatBiasScore   > 0 代表偏慢 / 吸热不足
         * airflowBiasScore > 0 代表脱水/排湿偏弱
         * inertiaBiasScore > 0 代表中后段拖、惯性重
         */
        val heatBiasScore =
            (deltaTT / 8.0) * 0.25 +
            (deltaTY / 15.0) * 0.30 +
            (deltaFC / 20.0) * 0.45

        val airflowBiasScore =
            (deltaTY / 12.0) * 0.65 +
            (if (actual.preFcRor > predicted.rorFull5[3] + 1.0) -0.25 else 0.15)

        val inertiaBiasScore =
            (deltaFC / 18.0) * 0.60 +
            (deltaDrop / 20.0) * 0.25 +
            (if (actual.preFcRor < predicted.rorFull5[3] - 0.8) 0.35 else 0.0)

        val diagnosis = mutableListOf<String>()
        val actions = mutableListOf<String>()

        var chargeAdjust = 0
        var h1Adjust = 0
        var h2Adjust = 0
        var h3Adjust = 0
        var h4Adjust = 0
        var h5Adjust = 0

        var wind1Adjust = 0
        var wind2Adjust = 0
        var devPaAdjust = 0

        var targetTurningAdjust = 0
        var targetYellowAdjust = 0
        var targetFcAdjust = 0
        var targetDropAdjust = 0
        var protectAdjust = 0

        // 1) 整体偏慢：吸热不足
        if (deltaTT > 5 && deltaTY > 10 && deltaFC > 12) {
            diagnosis += "整体节奏偏慢，判定为吸热不足。"
            chargeAdjust += 2
            h1Adjust += 60
            h2Adjust += 60
            h3Adjust += 60
            targetTurningAdjust -= 5
            targetYellowAdjust -= 10
            targetFcAdjust -= 12
            targetDropAdjust -= 12
            actions += "Charge +2℃"
            actions += "前段火力 H1/H2/H3 各 +1 档"
        }

        // 2) 前段慢，后段追回：脱水推进不足
        if (deltaTT > 5 && deltaTY > 12 && abs(deltaFC) <= 12) {
            diagnosis += "前段偏慢但后段追回，判定为脱水推进不足。"
            chargeAdjust += 1
            h1Adjust += 60
            h2Adjust += 60
            wind1Adjust += 1
            targetTurningAdjust -= 4
            targetYellowAdjust -= 8
            actions += "Charge +1℃"
            actions += "回温前后火力加强，脱水风压轻微上调"
        }

        // 3) 爆前冲高：热流过厚 / 收火偏晚
        if (actual.preFcRor > predicted.rorFull5[3] + 0.8 || deltaFC < -10) {
            diagnosis += "爆前 ROR 偏高或一爆提前，判定为爆前热流过厚。"
            chargeAdjust -= 1
            h3Adjust -= 60
            h4Adjust -= 60
            wind2Adjust += 2
            devPaAdjust += 1
            protectAdjust += 10
            targetFcAdjust += 8
            targetDropAdjust += 8
            actions += "Charge -1℃"
            actions += "转黄后提前收火，爆前风压 +2Pa"
        }

        // 4) 后段拖闷：收火过早 / 排湿过强 / 惯性不够
        if (deltaFC > 12 && actual.preFcRor < predicted.rorFull5[3] - 0.8) {
            diagnosis += "一爆偏慢且爆前 ROR 偏低，判定为后段拖闷。"
            h4Adjust += 60
            h5Adjust += 40
            wind2Adjust -= 1
            devPaAdjust -= 1
            targetFcAdjust -= 10
            targetDropAdjust -= 10
            actions += "爆前火力 +1 档"
            actions += "爆前风压微降，避免热量被过早抽走"
        }

        // 5) 黄点过慢，但 FC 不算慢：更偏向风系统问题
        if (deltaTY > 15 && deltaFC <= 10) {
            diagnosis += "黄点偏慢但一爆未明显拖后，判定为排湿/风压策略需修正。"
            wind1Adjust += 2
            actions += "脱水风压 +2Pa"
        }

        // 6) 黄点过快、FC也提前：前段火力或风量偏激进
        if (deltaTY < -12 && deltaFC < -12) {
            diagnosis += "中前段节奏偏快，判定为前段推进过猛。"
            chargeAdjust -= 1
            h1Adjust -= 60
            h2Adjust -= 60
            wind1Adjust += 1
            targetTurningAdjust += 4
            targetYellowAdjust += 8
            actions += "Charge -1℃"
            actions += "H1/H2 各 -1 档，风压轻微上调"
        }

        // 7) 根据综合分数做小幅校准
        if (heatBiasScore > 0.8) {
            h3Adjust += 40
            actions += "综合热偏差显示仍略偏慢，H3 微增"
        } else if (heatBiasScore < -0.8) {
            h3Adjust -= 40
            actions += "综合热偏差显示偏快，H3 微减"
        }

        if (airflowBiasScore > 0.8) {
            wind1Adjust += 1
            wind2Adjust += 1
            actions += "综合风系统偏差显示排湿不足，风压整体微增"
        } else if (airflowBiasScore < -0.8) {
            wind1Adjust -= 1
            wind2Adjust -= 1
            actions += "综合风系统偏差显示抽风偏强，风压整体微降"
        }

        if (inertiaBiasScore > 0.9) {
            protectAdjust -= 5
            h4Adjust += 40
            actions += "惯性偏重，保护点略后移，H4 微增"
        }

        if (diagnosis.isEmpty()) {
            diagnosis += "预测与实际偏差较小，判定为基线基本正确。"
            actions += "第二锅保持主体策略，仅做轻微微调。"
        }

        // 应用修正
        val correctedChargeBT = clampInt(predicted.chargeBT + chargeAdjust, 200, 206)

        val correctedH1W = roundPower(clampInt(predicted.h1W + h1Adjust, 1160, 1450))
        val correctedH2W = roundPower(clampInt(predicted.h2W + h2Adjust, 1160, 1450))
        val correctedH3W = roundPower(clampInt(predicted.h3W + h3Adjust, 1160, 1450))
        val correctedH4W = roundPower(clampInt(predicted.h4W + h4Adjust, 1160, 1450))
        val correctedH5W = roundPower(clampInt(predicted.h5W + h5Adjust, 1160, 1450))

        val correctedWind1Pa = clampInt(predicted.wind1Pa + wind1Adjust, 5, 20)
        val correctedWind2Pa = clampInt(predicted.wind2Pa + wind2Adjust, 8, 22)
        val correctedDevPa = clampInt(predicted.devPa + devPaAdjust, 8, 20)

        val correctedTurningTargetSec = clampInt(predTurning + targetTurningAdjust, 55, 120)
        val correctedYellowTargetSec = clampInt(predYellow + targetYellowAdjust, correctedTurningTargetSec + 80, 380)
        val correctedFcTargetSec = clampInt(predFc + targetFcAdjust, correctedYellowTargetSec + 180, 520)
        val correctedDropTargetSec = clampInt(predDrop + targetDropAdjust, correctedFcTargetSec + 60, 700)

        val correctedProtectSec = clampInt(predicted.protectSec.roundToInt() + protectAdjust, correctedYellowTargetSec + 120, correctedFcTargetSec - 10)

        val card = """
Batch ${batchIndex + 1} Execution Card
Stage: ${stageLabel(batchIndex)}

Charge
• ${correctedChargeBT}℃

Targets
• Turning ${RoastEngine.toMMSS(correctedTurningTargetSec.toDouble())}
• Yellow ${RoastEngine.toMMSS(correctedYellowTargetSec.toDouble())}
• FC ${RoastEngine.toMMSS(correctedFcTargetSec.toDouble())}
• Drop ${RoastEngine.toMMSS(correctedDropTargetSec.toDouble())}

Heat Plan
• H1 ${correctedH1W}W @ ${RoastEngine.toMMSS(predicted.h1Sec)}
• H2 ${correctedH2W}W @ ${RoastEngine.toMMSS(predicted.h2Sec)}
• H3 ${correctedH3W}W @ ${RoastEngine.toMMSS(predicted.h3Sec)}
• H4 ${correctedH4W}W @ ${RoastEngine.toMMSS(predicted.h4Sec)}
• H5 ${correctedH5W}W @ ${RoastEngine.toMMSS(predicted.h5Sec)}

Air Plan
• Wind1 ${correctedWind1Pa}Pa @ ${RoastEngine.toMMSS(predicted.wind1Sec)}
• Wind2 ${correctedWind2Pa}Pa @ ${RoastEngine.toMMSS(predicted.wind2Sec)}
• Dev ${correctedDevPa}Pa
• Protect @ ${RoastEngine.toMMSS(correctedProtectSec.toDouble())}

Diagnosis
${diagnosis.joinToString("\n") { "• $it" }}

Actions
${actions.distinct().joinToString("\n") { "• $it" }}
        """.trimIndent()

        return CorrectionResult(
            batchIndex = batchIndex,
            stageLabel = stageLabel(batchIndex),

            deltaTurningSec = deltaTT,
            deltaYellowSec = deltaTY,
            deltaFcSec = deltaFC,
            deltaDropSec = deltaDrop,
            deltaPreFcRor = deltaPreFcRor,

            correctedChargeBT = correctedChargeBT,

            correctedH1W = correctedH1W,
            correctedH2W = correctedH2W,
            correctedH3W = correctedH3W,
            correctedH4W = correctedH4W,
            correctedH5W = correctedH5W,

            correctedWind1Pa = correctedWind1Pa,
            correctedWind2Pa = correctedWind2Pa,
            correctedDevPa = correctedDevPa,

            correctedTurningTargetSec = correctedTurningTargetSec,
            correctedYellowTargetSec = correctedYellowTargetSec,
            correctedFcTargetSec = correctedFcTargetSec,
            correctedDropTargetSec = correctedDropTargetSec,

            correctedProtectSec = correctedProtectSec,

            heatBiasScore = clampDouble(heatBiasScore, -5.0, 5.0),
            airflowBiasScore = clampDouble(airflowBiasScore, -5.0, 5.0),
            inertiaBiasScore = clampDouble(inertiaBiasScore, -5.0, 5.0),

            diagnosis = diagnosis.distinct(),
            actions = actions.distinct(),
            batch2ExecutionCard = card
        )
    }
}
