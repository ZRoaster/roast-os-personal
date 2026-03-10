package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.UiKit

object HistoryDetailPage {

    fun show(
        context: Context,
        container: LinearLayout,
        entry: RoastHistoryEntry?
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(
            UiKit.pageTitle(
                context,
                "ROAST DETAIL"
            )
        )

        root.addView(
            UiKit.pageSubtitle(
                context,
                "Detailed roast history view"
            )
        )

        root.addView(UiKit.spacer(context))

        if (entry == null) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(
                UiKit.cardTitle(
                    context,
                    "NO DATA"
                )
            )
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No roast history entry selected."
                )
            )
            root.addView(emptyCard)

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val batchCard = UiKit.card(context)
        batchCard.addView(
            UiKit.cardTitle(
                context,
                "BATCH"
            )
        )
        batchCard.addView(
            UiKit.bodyText(
                context,
                buildString {
                    append("Batch ID\n")
                    append(entry.batchId)
                    append("\n\n")

                    append("Title\n")
                    append(entry.title)
                    append("\n\n")

                    append("Status\n")
                    append(entry.batchStatus)
                    append("\n\n")

                    append("Created At\n")
                    append(entry.createdAtMillis)
                }
            )
        )
        root.addView(batchCard)

        root.addView(UiKit.spacer(context))

        val beanCard = UiKit.card(context)
        beanCard.addView(
            UiKit.cardTitle(
                context,
                "BEAN / ENVIRONMENT"
            )
        )
        beanCard.addView(
            UiKit.bodyText(
                context,
                buildString {
                    append("Process\n")
                    append(entry.process)
                    append("\n\n")

                    append("Density\n")
                    append(entry.density)
                    append("\n\n")

                    append("Moisture\n")
                    append(entry.moisture)
                    append("\n\n")

                    append("Aw\n")
                    append(entry.aw)
                    append("\n\n")

                    append("Environment Temp\n")
                    append(entry.envTemp)
                    append("\n\n")

                    append("Environment RH\n")
                    append(entry.envRh)
                }
            )
        )
        root.addView(beanCard)

        root.addView(UiKit.spacer(context))

        val predictionCard = UiKit.card(context)
        predictionCard.addView(
            UiKit.cardTitle(
                context,
                "PREDICTION / ACTUAL"
            )
        )
        predictionCard.addView(
            UiKit.bodyText(
                context,
                buildString {
                    append("Predicted Turning\n")
                    append(formatSec(entry.predictedTurningSec))
                    append("\n\n")

                    append("Predicted Yellow\n")
                    append(formatSec(entry.predictedYellowSec))
                    append("\n\n")

                    append("Predicted FC\n")
                    append(formatSec(entry.predictedFcSec))
                    append("\n\n")

                    append("Predicted Drop\n")
                    append(formatSec(entry.predictedDropSec))
                    append("\n\n")

                    append("Actual Turning\n")
                    append(formatSec(entry.actualTurningSec))
                    append("\n\n")

                    append("Actual Yellow\n")
                    append(formatSec(entry.actualYellowSec))
                    append("\n\n")

                    append("Actual FC\n")
                    append(formatSec(entry.actualFcSec))
                    append("\n\n")

                    append("Actual Drop\n")
                    append(formatSec(entry.actualDropSec))
                    append("\n\n")

                    append("Actual Pre-FC RoR\n")
                    append(formatDouble(entry.actualPreFcRor))
                }
            )
        )
        root.addView(predictionCard)

        root.addView(UiKit.spacer(context))

        val reportCard = UiKit.card(context)
        reportCard.addView(
            UiKit.cardTitle(
                context,
                "REPORT"
            )
        )
        reportCard.addView(
            UiKit.bodyText(
                context,
                entry.reportText.ifBlank { "-" }
            )
        )
        root.addView(reportCard)

        root.addView(UiKit.spacer(context))

        val diagnosisCard = UiKit.card(context)
        diagnosisCard.addView(
            UiKit.cardTitle(
                context,
                "DIAGNOSIS"
            )
        )
        diagnosisCard.addView(
            UiKit.bodyText(
                context,
                entry.diagnosisText.ifBlank { "-" }
            )
        )
        root.addView(diagnosisCard)

        root.addView(UiKit.spacer(context))

        val correctionCard = UiKit.card(context)
        correctionCard.addView(
            UiKit.cardTitle(
                context,
                "CORRECTION"
            )
        )
        correctionCard.addView(
            UiKit.bodyText(
                context,
                entry.correctionText.ifBlank { "-" }
            )
        )
        root.addView(correctionCard)

        root.addView(UiKit.spacer(context))

        val evaluationCard = UiKit.card(context)
        evaluationCard.addView(
            UiKit.cardTitle(
                context,
                "EVALUATION"
            )
        )
        evaluationCard.addView(
            UiKit.bodyText(
                context,
                buildEvaluationText(entry)
            )
        )
        root.addView(evaluationCard)

        root.addView(UiKit.spacer(context))

        val baselineCard = UiKit.card(context)
        baselineCard.addView(
            UiKit.cardTitle(
                context,
                "BASELINE"
            )
        )
        baselineCard.addView(
            UiKit.bodyText(
                context,
                buildString {
                    append("Baseline Source\n")
                    append(entry.baselineSource ?: "-")
                    append("\n\n")

                    append("Baseline Label\n")
                    append(entry.baselineLabel ?: "-")
                    append("\n\n")

                    append("Match Grade\n")
                    append(entry.baselineMatchGrade ?: "-")
                    append("\n\n")

                    append("Source Profile ID\n")
                    append(entry.baselineSourceProfileId ?: "-")
                    append("\n\n")

                    append("Source Batch ID\n")
                    append(entry.baselineSourceBatchId ?: "-")
                }
            )
        )
        root.addView(baselineCard)

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildEvaluationText(entry: RoastHistoryEntry): String {
        val evaluation = entry.evaluation ?: return "No evaluation saved."

        return buildString {
            append("Bean Color\n")
            append(formatDouble(evaluation.beanColor))
            append("\n\n")

            append("Ground Color\n")
            append(formatDouble(evaluation.groundColor))
            append("\n\n")

            append("Roasted Aw\n")
            append(formatDouble(evaluation.roastedAw))
            append("\n\n")

            append("Sweetness\n")
            append(formatInt(evaluation.sweetness))
            append("\n\n")

            append("Acidity\n")
            append(formatInt(evaluation.acidity))
            append("\n\n")

            append("Body\n")
            append(formatInt(evaluation.body))
            append("\n\n")

            append("Flavor Clarity\n")
            append(formatInt(evaluation.flavorClarity))
            append("\n\n")

            append("Balance\n")
            append(formatInt(evaluation.balance))
            append("\n\n")

            append("Notes\n")
            append(evaluation.notes.ifBlank { "-" })
        }
    }

    private fun formatSec(value: Int?): String {
        if (value == null) return "-"
        val minutes = value / 60
        val seconds = value % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private fun formatDouble(value: Double?): String {
        return value?.toString() ?: "-"
    }

    private fun formatInt(value: Int?): String {
        return value?.toString() ?: "-"
    }
}
