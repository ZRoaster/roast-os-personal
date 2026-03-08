package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.PreheatLiveInput
import com.roastos.app.RoastPreheatAssistEngine

object PreheatPage {

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "PREHEAT CENTER"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Semi-automatic preheat assist with target, window, hold, action, beep text, and charge countdown"
            )
        )
        root.addView(UiKit.spacer(context))

        val targetCard = UiKit.card(context)
        targetCard.addView(UiKit.cardTitle(context, "PREHEAT TARGET"))
        val targetBody = UiKit.bodyText(context, "")
        targetCard.addView(targetBody)
        root.addView(targetCard)
        root.addView(UiKit.spacer(context))

        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context, "LIVE INPUT"))

        val currentTempInput = decimalInput(context, "Current Temp ℃", "205.0")
        val riseRateInput = decimalInput(context, "Rise Rate ℃/s", "0.18")
        val powerInput = intInput(context, "Current Power W", "540")
        val elapsedInput = intInput(context, "Elapsed Sec", "0")
        val holdElapsedInput = intInput(context, "Hold Elapsed Sec", "0")
        val ambientTempInput = decimalInput(context, "Ambient Temp ℃", "")
        val ambientRhInput = decimalInput(context, "Ambient RH %", "")

        val refreshBtn = Button(context)
        refreshBtn.text = "Refresh Preheat Assist"

        inputCard.addView(currentTempInput)
        inputCard.addView(riseRateInput)
        inputCard.addView(powerInput)
        inputCard.addView(elapsedInput)
        inputCard.addView(holdElapsedInput)
        inputCard.addView(ambientTempInput)
        inputCard.addView(ambientRhInput)
        inputCard.addView(refreshBtn)

        root.addView(inputCard)
        root.addView(UiKit.spacer(context))

        val assistCard = UiKit.card(context)
        assistCard.addView(UiKit.cardTitle(context, "PREHEAT ASSIST"))
        val assistBody = UiKit.bodyText(context, "")
        assistCard.addView(assistBody)
        root.addView(assistCard)
        root.addView(UiKit.spacer(context))

        val statusCard = UiKit.card(context)
        statusCard.addView(UiKit.cardTitle(context, "STATUS"))
        val statusBody = UiKit.bodyText(context, "")
        statusCard.addView(statusBody)
        root.addView(statusCard)

        fun refreshAll() {
            val target = RoastPreheatAssistEngine.buildTargetFromCurrentState()

            val ambientTemp = ambientTempInput.text.toString().toDoubleOrNull()
                ?: target.targetTempC.let {  // fallback not used semantically; just avoid null
                    val plannerTemp = com.roastos.app.AppState.lastPlannerInput?.envTemp
                    plannerTemp ?: 25.0
                }

            val ambientRh = ambientRhInput.text.toString().toDoubleOrNull()
                ?: com.roastos.app.AppState.lastPlannerInput?.envRH
                ?: 50.0

            val live = PreheatLiveInput(
                currentTempC = currentTempInput.text.toString().toDoubleOrNull() ?: 0.0,
                riseRateCPerSec = riseRateInput.text.toString().toDoubleOrNull() ?: 0.0,
                currentPowerW = powerInput.text.toString().toIntOrNull() ?: 0,
                elapsedSec = elapsedInput.text.toString().toIntOrNull() ?: 0,
                holdElapsedSec = holdElapsedInput.text.toString().toIntOrNull() ?: 0,
                ambientTempC = ambientTemp,
                ambientRh = ambientRh
            )

            val result = RoastPreheatAssistEngine.assess(target, live)

            targetBody.text = """
Target
${"%.1f".format(target.targetTempC)}℃

Window
${"%.1f".format(target.windowLowC)}–${"%.1f".format(target.windowHighC)}℃

Hold
${target.holdSec}s

Intent
${target.intent}

Reason
${target.reason}
            """.trimIndent()

            assistBody.text = result.summary
            statusBody.text = result.statusText
        }

        refreshBtn.setOnClickListener {
            refreshAll()
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun decimalInput(
        context: Context,
        hint: String,
        defaultText: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        input.setText(defaultText)
        return input
    }

    private fun intInput(
        context: Context,
        hint: String,
        defaultText: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        input.setText(defaultText)
        return input
    }
}
