package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.MachineTelemetryEngine
import com.roastos.app.PlannerBaseline
import com.roastos.app.PlannerBaselineStore
import com.roastos.app.RoastCurveEngineV2
import com.roastos.app.RoastCurveEngineV3
import com.roastos.app.RoastCurvePredictionV3
import com.roastos.app.RoastLiveAssistEngine
import com.roastos.app.TelemetrySourceMode

object RoastPage {

    private var simulatorElapsed = 0

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Cockpit view driven by MachineTelemetryEngine, RoastLiveAssistEngine, PlannerBaselineStore, and curve prediction engines"
            )
        )
        root.addView(UiKit.spacer(context))

        val telemetryCard = UiKit.card(context)
        telemetryCard.addView(UiKit.cardTitle(context, "TELEMETRY STATUS"))
        val telemetryBody = UiKit.bodyText(context, "")
        telemetryCard.addView(telemetryBody)
        root.addView(telemetryCard)
        root.addView(UiKit.spacer(context))

        val baselineCard = UiKit.card(context)
        baselineCard.addView(UiKit.cardTitle(context, "PLANNER BASELINE"))
        val baselineBody = UiKit.bodyText(context, "")
        baselineCard.addView(baselineBody)
        root.addView(baselineCard)
        root.addView(UiKit.spacer(context))

        val cockpitCard = UiKit.card(context)
        cockpitCard.addView(UiKit.cardTitle(context, "COCKPIT"))
        val cockpitBody = UiKit.bodyText(context, "")
        cockpitCard.addView(cockpitBody)
        root.addView(cockpitCard)
        root.addView(UiKit.spacer(context))

        val controlCard = UiKit.card(context)
        controlCard.addView(UiKit.cardTitle(context, "TELEMETRY CONTROL"))
        controlCard.addView(
            UiKit.captionText(
                context,
                "Use simulator or machine mode to drive live roast state."
            )
        )

        val manualBtn = UiKit.secondaryButton(context, "Manual Mode")
        val simBtn = UiKit.secondaryButton(context, "Simulator Mode")
        val simStep10 = UiKit.primaryButton(context, "Sim +10s")
        val simStep30 = UiKit.primaryButton(context, "Sim +30s")
        val simReset = UiKit.dangerButton(context, "Reset Simulator")
        val machineBtn = UiKit.secondaryButton(context, "Machine Mode")

        controlCard.addView(manualBtn)
        controlCard.addView(simBtn)
        controlCard.addView(simStep10)
        controlCard.addView(simStep30)
        controlCard.addView(simReset)
        controlCard.addView(machineBtn)

        root.addView(controlCard)
        root.addView(UiKit.spacer(context))

        val curveCard = UiKit.card(context)
        curveCard.addView(UiKit.cardTitle(context, "ROAST CURVE"))
        val curveBody = UiKit.bodyText(context, "")
        curveCard.addView(curveBody)
        root.addView(curveCard)
        root.addView(UiKit.spacer(context))

        val anchorChainCard = UiKit.cardAlt(context)
        anchorChainCard.addView(UiKit.cardTitle(context, "ANCHOR CHAIN"))
        anchorChainCard.addView(
            UiKit.captionText(
                context,
                "Yellow → FC → Drop anchor chain from the primary V3.4 prediction layer."
            )
        )
        val anchorChainBody = UiKit.bodyText(context, "")
        anchorChainCard.addView(UiKit.tinySpacer(context))
        anchorChainCard.addView(anchorChainBody)
        root.addView(anchorChainCard)
        root.addView(UiKit.spacer(context))

        val baselineDeltaCard = UiKit.cardAlt(context)
        baselineDeltaCard.addView(UiKit.cardTitle(context, "BASELINE DELTA CHAIN"))
        baselineDeltaCard.addView(
            UiKit.captionText(
                context,
                "Predicted anchor offsets versus the active baseline."
            )
        )
        val baselineDeltaBody = UiKit.bodyText(context, "")
        baselineDeltaCard.addView(UiKit.tinySpacer(context))
        baselineDeltaCard.addView(baselineDeltaBody)
        root.addView(baselineDeltaCard)
        root.addView(UiKit.spacer(context))

        val forecastCard = UiKit.cardAlt(context)
        forecastCard.addView(UiKit.cardTitle(context, "FC / DROP / DEVELOPMENT FORECAST"))
        forecastCard.addView(
            UiKit.captionText(
                context,
                "Primary forecast layer from V3.4. Focus here first during roast."
            )
        )
        val forecastBody = UiKit.bodyText(context, "")
        forecastCard.addView(UiKit.tinySpacer(context))
        forecastCard.addView(forecastBody)
        root.addView(forecastCard)
        root.addView(UiKit.spacer(context))

        val predictionV3Card = UiKit.cardAlt(context)
        predictionV3Card.addView(UiKit.cardTitle(context, "PRIMARY CURVE PREDICTION"))
        predictionV3Card.addView(
            UiKit.captionText(
                context,
                "V3.4 uses smoothed BT, smoothed ROR, stabilized anchors, consistency constraints, baseline deltas, and confidence."
            )
        )
        val predictionV3Body = UiKit.bodyText(context, "")
        predictionV3Card.addView(UiKit.tinySpacer(context))
        predictionV3Card.addView(predictionV3Body)
        root.addView(predictionV3Card)
        root.addView(UiKit.spacer(context))

        val predictionV2Card = UiKit.card(context)
        predictionV2Card.addView(UiKit.cardTitle(context, "REFERENCE PREDICTION V2"))
        predictionV2Card.addView(
            UiKit.captionText(
                context,
                "V2 is kept as a simple comparison layer. Use it only as a lightweight reference."
            )
        )
        val predictionV2Body = UiKit.bodyText(context, "")
        predictionV2Card.addView(UiKit.tinySpacer(context))
        predictionV2Card.addView(predictionV2Body)
        root.addView(predictionV2Card)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "ROAST STATUS"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)
        root.addView(statusCard)

        fun refresh() {
            val telemetry = MachineTelemetryEngine.currentState()
            val assist = RoastLiveAssistEngine.buildFromTelemetry()
            val baseline = PlannerBaselineStore.current()

            val bt = telemetry.liveBtC ?: 0.0
            val et = telemetry.liveEtC
            val ror = telemetry.liveRorCPerMin ?: 0.0
            val power = telemetry.livePowerW
            val air = telemetry.liveAirflowPa
            val drum = telemetry.liveDrumRpm
            val time = telemetry.liveElapsedSec
            val machineState = telemetry.machineState
            val nowMillis = System.currentTimeMillis()

            RoastCurveEngineV2.record(
                bt = bt,
                timeMillis = nowMillis
            )

            RoastCurveEngineV3.record(
                bt = bt,
                timeMillis = nowMillis
            )

            val predictionV3 = RoastCurveEngineV3.predict()

            telemetryBody.text = MachineTelemetryEngine.summary()
            baselineBody.text = buildBaselineText()
            cockpitBody.text = assist.summary

            curveBody.text = """
Curve Monitor

BT
${"%.1f".format(bt)}℃

ET
${et?.let { "%.1f".format(it) + "℃" } ?: "-"}

ROR
${"%.1f".format(ror)}℃/min

Elapsed
${time}s

Interpretation
${assist.interpretation}

Baseline Reference
${buildBaselineReferenceText(baseline, time)}
            """.trimIndent()

            anchorChainBody.text = buildAnchorChain(predictionV3)
            baselineDeltaBody.text = buildBaselineDeltaChain(predictionV3)
            forecastBody.text = buildForecastHeadline(predictionV3)
            predictionV3Body.text = predictionV3.summary
            predictionV2Body.text = RoastCurveEngineV2.summary()

            statusBody.text = """
Machine State
$machineState

BT
${"%.1f".format(bt)}℃

ROR
${"%.1f".format(ror)}℃/min

Power
${power}W

Air
${air}Pa

Drum
${drum}rpm

Elapsed
${time}s

Source Mode
${telemetry.mode}
            """.trimIndent()
        }

        manualBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.MANUAL)
            refresh()
        }

        simBtn.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()
        }

        simStep10.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsed += 10
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsed)
            refresh()
        }

        simStep30.setOnClickListener {
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            simulatorElapsed += 30
            MachineTelemetryEngine.pushSimulatorFrame(simulatorElapsed)
            refresh()
        }

        simReset.setOnClickListener {
            simulatorElapsed = 0
            RoastCurveEngineV2.reset()
            RoastCurveEngineV3.reset()
            MachineTelemetryEngine.reset()
            MachineTelemetryEngine.setMode(TelemetrySourceMode.SIMULATOR)
            refresh()
        }

        machineBtn.setOnClickListener {
            MachineTelemetryEngine.connectMachine()
            refresh()
        }

        refresh()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildAnchorChain(prediction: RoastCurvePredictionV3): String {
        val yellowText = formatPredictionTime(prediction.predictedYellowTimeSec)
        val fcText = formatPredictionTime(prediction.predictedFcTimeSec)
        val dropText = formatPredictionTime(prediction.predictedDropTimeSec)
        val devText = prediction.predictedDevelopmentSec?.let { "%.0f".format(it) + "s" } ?: "-"
        val dtrText = prediction.predictedDtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"

        val yellowToFcText = if (
            prediction.predictedYellowTimeSec != null &&
            prediction.predictedFcTimeSec != null
        ) {
            val delta = prediction.predictedFcTimeSec - prediction.predictedYellowTimeSec
            if (delta >= 0.0) "%.0f".format(delta) + "s" else "-"
        } else {
            "-"
        }

        val fcToDropText = if (
            prediction.predictedFcTimeSec != null &&
            prediction.predictedDropTimeSec != null
        ) {
            val delta = prediction.predictedDropTimeSec - prediction.predictedFcTimeSec
            if (delta >= 0.0) "%.0f".format(delta) + "s" else "-"
        } else {
            "-"
        }

        return """
Predicted Yellow
$yellowText

Predicted FC
$fcText

Predicted Drop
$dropText

Yellow → FC
$yellowToFcText

FC → Drop
$fcToDropText

Predicted Development
$devText

Predicted DTR
$dtrText
        """.trimIndent()
    }

    private fun buildBaselineDeltaChain(prediction: RoastCurvePredictionV3): String {
        return """
Yellow Δ
${formatDelta(prediction.baselineYellowDeltaSec)}

FC Δ
${formatDelta(prediction.baselineFcDeltaSec)}

Drop Δ
${formatDelta(prediction.baselineDropDeltaSec)}

Interpretation
${buildBaselineDeltaInterpretation(prediction)}
        """.trimIndent()
    }

    private fun buildBaselineDeltaInterpretation(prediction: RoastCurvePredictionV3): String {
        val yellow = prediction.baselineYellowDeltaSec
        val fc = prediction.baselineFcDeltaSec
        val drop = prediction.baselineDropDeltaSec

        return when {
            fc != null && fc > 20.0 -> "FC is trending later than baseline"
            fc != null && fc < -20.0 -> "FC is trending earlier than baseline"
            drop != null && drop > 25.0 -> "Drop is trending later than baseline"
            drop != null && drop < -25.0 -> "Drop is trending earlier than baseline"
            yellow != null && yellow > 18.0 -> "Yellow is trending later than baseline"
            yellow != null && yellow < -18.0 -> "Yellow is trending earlier than baseline"
            else -> "Anchor chain is relatively close to baseline"
        }
    }

    private fun buildForecastHeadline(prediction: RoastCurvePredictionV3): String {
        val fcText = formatPredictionTime(prediction.predictedFcTimeSec)
        val dropText = formatPredictionTime(prediction.predictedDropTimeSec)
        val devText = prediction.predictedDevelopmentSec?.let {
            "%.0f".format(it) + "s"
        } ?: "-"

        val dtrText = prediction.predictedDtrPercent?.let {
            "%.1f".format(it) + "%"
        } ?: "-"

        return """
Predicted FC
$fcText

Predicted Drop
$dropText

Predicted Development
$devText

Predicted DTR
$dtrText

Phase
${prediction.phase}

Trend
${prediction.trend}

Confidence
${prediction.confidence}
        """.trimIndent()
    }

    private fun buildBaselineText(): String {
        val baseline = PlannerBaselineStore.current()
            ?: return """
Status
No active planner baseline

Next Step
Apply profile suggestion or capture current planner result as baseline
            """.trimIndent()

        val match = PlannerBaselineStore.evaluateMatchAgainstCurrentInput()

        return """
Source
${baseline.source}

Label
${baseline.label}

Match Grade
${formatBaselineMatch(match?.grade?.name)}

Turning
${baseline.turningSec?.toString()?.plus("s") ?: "-"}

Yellow
${baseline.yellowSec?.toString()?.plus("s") ?: "-"}

FC
${baseline.fcSec?.toString()?.plus("s") ?: "-"}

Drop
${baseline.dropSec?.toString()?.plus("s") ?: "-"}
        """.trimIndent()
    }

    private fun buildBaselineReferenceText(
        baseline: PlannerBaseline?,
        elapsedSec: Int
    ): String {
        baseline ?: return "No baseline active"

        return when {
            baseline.turningSec != null && elapsedSec < baseline.turningSec ->
                "Approaching Turning anchor"
            baseline.yellowSec != null && elapsedSec < baseline.yellowSec ->
                "Working toward Yellow anchor"
            baseline.fcSec != null && elapsedSec < baseline.fcSec ->
                "Working toward FC anchor"
            baseline.dropSec != null && elapsedSec < baseline.dropSec ->
                "Working toward Drop anchor"
            else ->
                "Past or near final baseline anchors"
        }
    }

    private fun formatPredictionTime(value: Double?): String {
        return when {
            value == null -> "-"
            value <= 0.0 -> "Now"
            else -> "%.0f".format(value) + "s"
        }
    }

    private fun formatDelta(value: Double?): String {
        return when {
            value == null -> "-"
            value > 0.0 -> "+" + "%.0f".format(value) + "s"
            else -> "%.0f".format(value) + "s"
        }
    }

    private fun formatBaselineMatch(raw: String?): String {
        return when (raw) {
            "EXACT_MATCH" -> "Exact Match"
            "SIMILAR_MATCH" -> "Similar Match"
            "REFERENCE_ONLY" -> "Reference Only"
            else -> "-"
        }
    }
}
