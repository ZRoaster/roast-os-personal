package com.roastos.app.ui

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastHistoryEngine
import com.roastos.app.RoastHistoryEntry

object BatchDetailPage {

    fun show(
        context: Context,
        container: LinearLayout,
        batchId: String
    ) {
        container.removeAllViews()

        val entry = RoastHistoryEngine.findByBatchId(batchId)

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "BATCH DETAIL"))
        root.addView(UiKit.pageSubtitle(context, "Single roast detail, diagnosis, correction, and report"))
        root.addView(UiKit.spacer(context))

        val backCard = UiKit.card(context)
        backCard.addView(UiKit.cardTitle(context, "NAVIGATION"))

        val backBtn = Button(context)
        backBtn.text = "Back to History"

        backBtn.setOnClickListener {
            HistoryPage.show(context, container)
        }

        backCard.addView(backBtn)
        root.addView(backCard)
        root.addView(UiKit.spacer(context))

        if (entry == null) {
            root.addView(
                UiKit.buildCard(
                    context,
                    "NOT FOUND",
                    "No roast history found for batchId = $batchId"
                )
            )
            scroll.addView(root)
            container.addView(scroll)
            return
        }

        root.addView(
            UiKit.buildCard(
                context,
                "BATCH OVERVIEW",
                buildOverview(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "BEAN / ENVIRONMENT",
                buildBeanEnv(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "PREDICTED PLAN",
                buildPredicted(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "ACTUAL TIMELINE",
                buildActual(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "DEVIATION SUMMARY",
                buildDeviation(entry)
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "DIAGNOSIS",
                entry.diagnosisText.ifBlank { "No diagnosis saved" }
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "CORRECTION BRIDGE",
                entry.correctionText.ifBlank { "No correction bridge saved" }
            )
        )
        root.addView(UiKit.spacer(context))

        root.addView(
            UiKit.buildCard(
                context,
                "ROAST REPORT",
                entry.reportText.ifBlank { "No roast report saved" }
            )
        )

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildOverview(entry: RoastHistoryEntry): String {
        return """
Batch ID
${entry.batchId}

Created
${entry.createdAtMillis}

Status
${entry.batchStatus}

Title
${entry.title}
        """.trimIndent()
    }

    private fun buildBeanEnv(entry: RoastHistoryEntry): String {
        return """
Bean
Process    ${entry.process.ifBlank { "-" }}
Density    ${"%.1f".format(entry.density)}
Moisture   ${"%.1f".format(entry.moisture)}
aw         ${"%.2f".format(entry.aw)}

Environment
Temp       ${"%.1f".format(entry.envTemp)}℃
RH         ${"%.1f".format(entry.envRh)}%
        """.trimIndent()
    }

    private fun buildPredicted(entry: RoastHistoryEntry): String {
        return """
Predicted Anchors
Turning   ${entry.predictedTurningSec?.toString() ?: "-"}
Yellow    ${entry.predictedYellowSec?.toString() ?: "-"}
FC        ${entry.predictedFcSec?.toString() ?: "-"}
Drop      ${entry.predictedDropSec?.toString() ?: "-"}
        """.trimIndent()
    }

    private fun buildActual(entry: RoastHistoryEntry): String {
        return """
Actual Anchors
Turning   ${entry.actualTurningSec?.toString() ?: "-"}
Yellow    ${entry.actualYellowSec?.toString() ?: "-"}
FC        ${entry.actualFcSec?.toString() ?: "-"}
Drop      ${entry.actualDropSec?.toString() ?: "-"}
Pre-FC ROR ${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()
    }

    private fun buildDeviation(entry: RoastHistoryEntry): String {
        return """
Turning   ${deviationLine(entry.predictedTurningSec, entry.actualTurningSec)}
Yellow    ${deviationLine(entry.predictedYellowSec, entry.actualYellowSec)}
FC        ${deviationLine(entry.predictedFcSec, entry.actualFcSec)}
Drop      ${deviationLine(entry.predictedDropSec, entry.actualDropSec)}
ROR       ${entry.actualPreFcRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()
    }

    private fun deviationLine(predicted: Int?, actual: Int?): String {
        return when {
            predicted == null && actual == null -> "Pred - / Actual - / Δ -"
            predicted == null -> "Pred - / Actual $actual / Δ -"
            actual == null -> "Pred $predicted / Actual - / Δ -"
            else -> {
                val delta = actual - predicted
                val deltaText = if (delta > 0) "+$delta" else delta.toString()
                "Pred $predicted / Actual $actual / Δ ${deltaText}s"
            }
        }
    }
}
