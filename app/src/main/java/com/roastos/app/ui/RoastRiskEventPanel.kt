package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastRiskEventEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastRiskEventPanel(context: Context) : LinearLayout(context) {

    private val bodyText = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(bodyText)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.current()

        if (snapshot == null) {
            bodyText.text = """
Risk Events

No active session snapshot.
            """.trimIndent()
            return
        }

        val batchId = snapshot.log.batchId
        val events = RoastRiskEventEngine.eventsForBatch(batchId)

        if (events.isEmpty()) {
            bodyText.text = """
Risk Events

当前批次未记录到风险事件。
            """.trimIndent()
            return
        }

        bodyText.text = events.joinToString("\n\n────────\n\n") { event ->
            """
Issue
${event.issueCode}

Phase
${event.phase}

Elapsed
${formatElapsed(event.elapsedSec)}

BT
${String.format("%.1f", event.beanTemp)} ℃

RoR
${String.format("%.1f", event.ror)} ℃/min

Heat
${event.suggestedHeatAction}

Airflow
${event.suggestedAirflowAction}

Continued
${if (event.operatorContinued) "Yes" else "No"}
            """.trimIndent()
        }
    }

    private fun formatElapsed(sec: Int): String {
        val minutes = sec / 60
        val seconds = sec % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
