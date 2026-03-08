package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.CurveEngine
import com.roastos.app.DecisionEngine
import com.roastos.app.LiveAssistEngine
import com.roastos.app.PhaseEngine
import com.roastos.app.RoastEngine
import com.roastos.app.TimelineEngine

object LiveAssistPage {

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val predicted = AppState.lastPlannerResult
        val plannerInput = AppState.lastPlannerInput

        if (predicted == null || plannerInput == null) {
            val t = TextView(context)
            t.text = "Run Planner first."
            container.addView(t)
            return
        }

        val predTurning = (predicted.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = predicted.h2Sec.toInt()
        val predFc = predicted.fcPredSec.toInt()
        val predDrop = predicted.dropSec.toInt()

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "LIVE ASSIST"
        title.textSize = 22f

        val plannerSummary = TextView(context)
        plannerSummary.text = """
Planner Baseline

Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}
        """.trimIndent()

        val timelineTitle = TextView(context)
        timelineTitle.text = "ROAST TIMELINE"
        timelineTitle.textSize = 18f

        val timelineCard = TextView(context)
        timelineCard.text = buildTimelineCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        val phaseTitle = TextView(context)
        phaseTitle.text = "AUTO PHASE ENGINE"
        phaseTitle.textSize = 18f

        val phaseCard = TextView(context)
        phaseCard.text = buildPhaseCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        val curveTitle = TextView(context)
        curveTitle.text = "CURVE PREDICTION"
        curveTitle.textSize = 18f

        val curveCard = TextView(context)
        curveCard.text = buildCurvePredictionCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        val decisionTitle = TextView(context)
        decisionTitle.text = "DECISION NOW"
        decisionTitle.textSize = 18f

        val decisionCard = TextView(context)
        decisionCard.text = buildDecisionCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        val currentCardTitle = TextView(context)
        currentCardTitle.text = "CURRENT CONTROL CARD"
        currentCardTitle.textSize = 18f

        val currentCard = TextView(context)
        currentCard.text = buildControlCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        root.addView(title)
        root.addView(plannerSummary)
        root.addView(timelineTitle)
        root.addView(timelineCard)
        root.addView(phaseTitle)
        root.addView(phaseCard)
        root.addView(curveTitle)
        root.addView(curveCard)
        root.addView(decisionTitle)
        root.addView(decisionCard)
        root.addView(currentCardTitle)
        root.addView(currentCard)

        // Turning
        val turningTitle = TextView(context)
        turningTitle.text = "Turning Event"

        val actualTurningInput = EditText(context)
        actualTurningInput.hint = "Actual Turning sec"
        actualTurningInput.inputType = InputType.TYPE_CLASS_NUMBER
        AppState.liveActualTurningSec?.let {
            actualTurningInput.setText(it.toString())
        } ?: actualTurningInput.setText(predTurning.toString())

        val turningBtn = Button(context)
        turningBtn.text = "Run Turning Assist"

        val turningResult = TextView(context)

        root.addView(turningTitle)
        root.addView(actualTurningInput)
        root.addView(turningBtn)
        root.addView(turningResult)

        // Yellow
        val yellowTitle = TextView(context)
        yellowTitle.text = "Yellow Event"

        val actualYellowInput = EditText(context)
        actualYellowInput.hint = "Actual Yellow sec"
        actualYellowInput.inputType = InputType.TYPE_CLASS_NUMBER
        AppState.liveActualYellowSec?.let {
            actualYellowInput.setText(it.toString())
        } ?: actualYellowInput.setText(predYellow.toString())

        val yellowRorInput = EditText(context)
        yellowRorInput.hint = "Current ROR"
        yellowRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        yellowRorInput.setText(
            AppState.liveActualPreFcRor?.toString() ?: "13.0"
        )

        val yellowBtn = Button(context)
        yellowBtn.text = "Run Yellow Assist"

        val yellowResult = TextView(context)

        root.addView(yellowTitle)
        root.addView(actualYellowInput)
        root.addView(yellowRorInput)
        root.addView(yellowBtn)
        root.addView(yellowResult)

        // FC + Drop
        val fcTitle = TextView(context)
        fcTitle.text = "First Crack / Development"

        val actualFcInput = EditText(context)
        actualFcInput.hint = "Actual FC sec"
        actualFcInput.inputType = InputType.TYPE_CLASS_NUMBER
        AppState.liveActualFcSec?.let {
            actualFcInput.setText(it.toString())
        } ?: actualFcInput.setText(predFc.toString())

        val preFcRorInput = EditText(context)
        preFcRorInput.hint = "Pre-FC ROR"
        preFcRorInput.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        AppState.liveActualPreFcRor?.let {
            preFcRorInput.setText(it.toString())
        } ?: preFcRorInput.setText("9.0")

        val actualDropInput = EditText(context)
        actualDropInput.hint = "Actual Drop sec"
        actualDropInput.inputType = InputType.TYPE_CLASS_NUMBER
        AppState.liveActualDropSec?.let {
            actualDropInput.setText(it.toString())
        } ?: actualDropInput.setText(predDrop.toString())

        val fcBtn = Button(context)
        fcBtn.text = "Run FC Assist"

        val fcResult = TextView(context)

        root.addView(fcTitle)
        root.addView(actualFcInput)
        root.addView(preFcRorInput)
        root.addView(actualDropInput)
        root.addView(fcBtn)
        root.addView(fcResult)

        container.addView(root)

        turningBtn.setOnClickListener {

            val actualTurning =
                actualTurningInput.text.toString().toIntOrNull() ?: predTurning

            AppState.liveActualTurningSec = actualTurning

            val advice = LiveAssistEngine.turningAssist(predTurning, actualTurning)
            val diff = actualTurning - predTurning

            val actionNow = """
Heat: ${advice.heat}
Air: ${advice.airflow}
            """.trimIndent()

            val targetNext = when {
                diff > 8 -> "Push Yellow back toward target window"
                diff < -8 -> "Delay Yellow slightly and reduce early push"
                else -> "Hold Yellow near current prediction"
            }

            val risk = when {
                diff > 8 -> "Front-end energy short risk"
                diff < -8 -> "Early acceleration risk"
                else -> "Low immediate Turning risk"
            }

            turningResult.text = """
Current State
Pred Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Actual Turning ${RoastEngine.toMMSS(actualTurning.toDouble())}

Deviation
${advice.deviation}

Action Now
$actionNow

Target Next
$targetNext

Risk
$risk
            """.trimIndent()

            refreshCards(
                timelineCard,
                phaseCard,
                curveCard,
                decisionCard,
                currentCard,
                predTurning,
                predYellow,
                predFc,
                predDrop
            )
        }

        yellowBtn.setOnClickListener {

            val actualYellow =
                actualYellowInput.text.toString().toIntOrNull() ?: predYellow

            val ror =
                yellowRorInput.text.toString().toDoubleOrNull() ?: 13.0

            AppState.liveActualYellowSec = actualYellow
            AppState.liveActualPreFcRor = ror

            val advice = LiveAssistEngine.yellowAssist(predYellow, actualYellow, ror)
            val diff = actualYellow - predYellow

            val actionNow = """
Heat: ${advice.heat}
Air: ${advice.airflow}
            """.trimIndent()

            val targetNext = when {
                diff > 15 -> "Recover FC timing with stronger middle push"
                diff < -15 -> "Slow momentum before crack"
                ror > 14.0 -> "Reduce ROR before pre-crack section"
                else -> "Keep FC on current path"
            }

            val risk = when {
                diff > 15 -> "Late crack / flat finish risk"
                diff < -15 -> "Pre-FC overshoot risk"
                ror > 14.0 -> "High ROR spike risk"
                else -> "Middle phase stable"
            }

            yellowResult.text = """
Current State
Pred Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
Actual Yellow ${RoastEngine.toMMSS(actualYellow.toDouble())}
Current ROR ${"%.1f".format(ror)}

Deviation
${advice.deviation}

Action Now
$actionNow

Target Next
$targetNext

Risk
$risk
            """.trimIndent()

            refreshCards(
                timelineCard,
                phaseCard,
                curveCard,
                decisionCard,
                currentCard,
                predTurning,
                predYellow,
                predFc,
                predDrop
            )
        }

        fcBtn.setOnClickListener {

            val actualFc =
                actualFcInput.text.toString().toIntOrNull() ?: predFc

            val ror =
                preFcRorInput.text.toString().toDoubleOrNull() ?: 9.0

            val actualDrop =
                actualDropInput.text.toString().toIntOrNull() ?: predDrop

            AppState.liveActualFcSec = actualFc
            AppState.liveActualPreFcRor = ror
            AppState.liveActualDropSec = actualDrop

            val advice = LiveAssistEngine.fcAssist(predFc, actualFc, ror)
            val diff = actualFc - predFc

            val actionNow = """
Heat: ${advice.heat}
Air: ${advice.airflow}
            """.trimIndent()

            val targetNext = when {
                ror > 10.0 -> "Stabilize development and prevent overshoot"
                ror < 7.0 -> "Preserve energy and avoid crash"
                diff > 15 -> "Avoid dragging development too long"
                diff < -15 -> "Protect sweetness and avoid harsh finish"
                else -> "Hold development in controlled window"
            }

            val risk = when {
                ror > 10.0 -> "Development overshoot risk"
                ror < 7.0 -> "Development crash risk"
                diff > 15 -> "Late crack / flat finish risk"
                diff < -15 -> "Fast crack / sharp finish risk"
                else -> "Moderate development risk"
            }

            fcResult.text = """
Current State
Pred FC ${RoastEngine.toMMSS(predFc.toDouble())}
Actual FC ${RoastEngine.toMMSS(actualFc.toDouble())}
Actual Drop ${RoastEngine.toMMSS(actualDrop.toDouble())}
Pre-FC ROR ${"%.1f".format(ror)}

Deviation
${advice.deviation}

Action Now
$actionNow

Target Next
$targetNext

Risk
$risk
            """.trimIndent()

            refreshCards(
                timelineCard,
                phaseCard,
                curveCard,
                decisionCard,
                currentCard,
                predTurning,
                predYellow,
                predFc,
                predDrop
            )
        }
    }

    private fun refreshCards(
        timelineCard: TextView,
        phaseCard: TextView,
        curveCard: TextView,
        decisionCard: TextView,
        currentCard: TextView,
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ) {
        timelineCard.text = buildTimelineCard(predTurning, predYellow, predFc, predDrop)
        phaseCard.text = buildPhaseCard(predTurning, predYellow, predFc, predDrop)
        curveCard.text = buildCurvePredictionCard(predTurning, predYellow, predFc, predDrop)
        decisionCard.text = buildDecisionCard(predTurning, predYellow, predFc, predDrop)
        currentCard.text = buildControlCard(predTurning, predYellow, predFc, predDrop)
    }

    private fun buildTimelineCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val rows = TimelineEngine.build(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = AppState.liveActualTurningSec,
            actualYellow = AppState.liveActualYellowSec,
            actualFc = AppState.liveActualFcSec,
            actualDrop = AppState.liveActualDropSec
        )

        return buildString {
            appendLine("Timeline")
            appendLine()
            rows.forEach { row ->
                appendLine(row.label)
                appendLine("Pred ${RoastEngine.toMMSS(row.predictedSec.toDouble())}")
                appendLine("Actual ${row.actualSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}")
                appendLine("Status ${row.status}")
                appendLine()
            }
        }.trim()
    }

    private fun buildPhaseCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val phase = PhaseEngine.detect(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = AppState.liveActualTurningSec,
            actualYellow = AppState.liveActualYellowSec,
            actualFc = AppState.liveActualFcSec,
            actualDrop = AppState.liveActualDropSec,
            actualPreFcRor = AppState.liveActualPreFcRor
        )

        return """
Current Phase
${phase.currentPhase}

Next Target
${phase.nextTargetLabel} ${RoastEngine.toMMSS(phase.nextTargetSec.toDouble())}

Phase Summary
${phase.phaseSummary}

Risk Hint
${phase.riskHint}
        """.trimIndent()
    }

    private fun buildCurvePredictionCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val prediction = CurveEngine.predict(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = AppState.liveActualTurningSec,
            actualYellow = AppState.liveActualYellowSec,
            actualFc = AppState.liveActualFcSec,
            currentRor = AppState.liveActualPreFcRor
        )

        return """
Predicted Curve

Yellow ${RoastEngine.toMMSS(prediction.predictedYellowSec.toDouble())}
FC ${RoastEngine.toMMSS(prediction.predictedFcSec.toDouble())}
Drop ${RoastEngine.toMMSS(prediction.predictedDropSec.toDouble())}
Dev ${prediction.predictedDevSec}s

Confidence
${prediction.confidence}

Logic
${prediction.summary}
        """.trimIndent()
    }

    private fun buildDecisionCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val plannerInput = AppState.lastPlannerInput
        if (plannerInput == null) {
            return "No planner input available."
        }

        val decision = DecisionEngine.decide(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = AppState.liveActualTurningSec,
            actualYellow = AppState.liveActualYellowSec,
            actualFc = AppState.liveActualFcSec,
            actualDrop = AppState.liveActualDropSec,
            currentRor = AppState.liveActualPreFcRor,
            envTemp = plannerInput.envTemp,
            humidity = plannerInput.envRH,
            pressureKpa = 101.3,
            density = plannerInput.density,
            moisture = plannerInput.moisture,
            aw = plannerInput.aw,
            heatLevelW = 1320,
            airflowPa = 16,
            drumRpm = 7
        )

        return """
Current Phase
${decision.currentPhase}

Action Now
${decision.actionNow}

Heat Command
${decision.heatCommand}

Air Command
${decision.airCommand}

Target Window
${decision.targetWindow}

Risk Level
${decision.riskLevel}

Reason
${decision.reason}

Physics
${decision.physicsSummary}
        """.trimIndent()
    }

    private fun buildControlCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val actualTurning = AppState.liveActualTurningSec
        val actualYellow = AppState.liveActualYellowSec
        val actualFc = AppState.liveActualFcSec
        val actualDrop = AppState.liveActualDropSec
        val actualRor = AppState.liveActualPreFcRor

        val currentStage = when {
            actualDrop != null -> "Finished"
            actualFc != null -> "Development / Drop"
            actualYellow != null -> "Maillard / Pre-FC"
            actualTurning != null -> "Drying / To Yellow"
            else -> "Pre-Turning"
        }

        val nextAction = when {
            actualDrop != null -> "Review roast and move to Correction"
            actualFc != null && actualRor != null && actualRor > 10.0 -> "Reduce heat, add air, protect development"
            actualFc != null && actualRor != null && actualRor < 7.0 -> "Preserve heat, avoid collapse"
            actualYellow != null -> "Control middle momentum toward FC"
            actualTurning != null -> "Shape drying so Yellow stays on plan"
            else -> "Watch first anchor point"
        }

        val biggestRisk = when {
            actualDrop != null -> "Batch complete"
            actualFc != null && actualRor != null && actualRor > 10.0 -> "Overshoot in development"
            actualFc != null && actualRor != null && actualRor < 7.0 -> "Development crash"
            actualYellow != null && actualYellow - predYellow > 15 -> "Late crack / flat cup"
            actualYellow != null && actualYellow - predYellow < -15 -> "Pre-FC spike"
            actualTurning != null && actualTurning - predTurning > 8 -> "Front-end energy short"
            actualTurning != null && actualTurning - predTurning < -8 -> "Early push too strong"
            else -> "No dominant risk yet"
        }

        return """
Current Stage
$currentStage

Predicted Anchors
Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}

Actual Anchors
Turning ${actualTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${actualYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${actualFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${actualDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Pre-FC ROR ${actualRor?.let { "%.1f".format(it) } ?: "-"}

Next Action
$nextAction

Biggest Risk
$biggestRisk
        """.trimIndent()
    }
}
