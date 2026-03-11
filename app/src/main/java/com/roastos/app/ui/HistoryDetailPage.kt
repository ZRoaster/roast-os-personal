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

        val title = UiKit.pageTitle(context, "ROAST DETAIL")
        val subtitle = UiKit.pageSubtitle(context, "Detailed roast history")

        root.addView(title)
        root.addView(subtitle)
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")

        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        if (entry == null) {

            val emptyCard = UiKit.card(context)

            emptyCard.addView(
                UiKit.cardTitle(context, "NO DATA")
            )

            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No roast history entry selected."
                )
            )

            root.addView(emptyCard)

            backBtn.setOnClickListener {
                RoastStudioPage.show(context, container)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val batchCard = UiKit.card(context)

        batchCard.addView(
            UiKit.cardTitle(context, "BATCH")
        )

        batchCard.addView(
            UiKit.bodyText(
                context,
                """
Batch ID
${entry.batchId}

Title
${entry.title}

Status
${entry.batchStatus}

Created
${entry.createdAtMillis}
                """.trimIndent()
            )
        )

        root.addView(batchCard)
        root.addView(UiKit.spacer(context))

        val healthCard = UiKit.card(context)

        healthCard.addView(
            UiKit.cardTitle(context, "ROAST HEALTH")
        )

        healthCard.addView(
            UiKit.bodyText(
                context,
                """
Headline
${entry.roastHealthHeadline}

Detail
${entry.roastHealthDetail}
                """.trimIndent()
            )
        )

        root.addView(healthCard)
        root.addView(UiKit.spacer(context))

        val beanCard = UiKit.card(context)

        beanCard.addView(
            UiKit.cardTitle(context, "BEAN / ENVIRONMENT")
        )

        beanCard.addView(
            UiKit.bodyText(
                context,
                """
Process
${entry.process}

Density
${entry.density}

Moisture
${entry.moisture}

Aw
${entry.aw}

Environment Temp
${entry.envTemp}

Environment RH
${entry.envRh}
                """.trimIndent()
            )
        )

        root.addView(beanCard)
        root.addView(UiKit.spacer(context))

        val timelineCard = UiKit.card(context)

        timelineCard.addView(
            UiKit.cardTitle(context, "TIMELINE")
        )

        timelineCard.addView(
            UiKit.bodyText(
                context,
                """
Predicted Turning
${formatSec(entry.predictedTurningSec)}

Predicted Yellow
${formatSec(entry.predictedYellowSec)}

Predicted First Crack
${formatSec(entry.predictedFcSec)}

Predicted Drop
${formatSec(entry.predictedDropSec)}

Actual Turning
${formatSec(entry.actualTurningSec)}

Actual Yellow
${formatSec(entry.actualYellowSec)}

Actual First Crack
${formatSec(entry.actualFcSec)}

Actual Drop
${formatSec(entry.actualDropSec)}

Actual Pre-FC RoR
${entry.actualPreFcRor ?: "-"}
                """.trimIndent()
            )
        )

        root.addView(timelineCard)
        root.addView(UiKit.spacer(context))

        val reportCard = UiKit.card(context)

        reportCard.addView(
            UiKit.cardTitle(context, "REPORT")
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
            UiKit.cardTitle(context, "DIAGNOSIS")
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
            UiKit.cardTitle(context, "CORRECTION")
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
            UiKit.cardTitle(context, "EVALUATION")
        )

        evaluationCard.addView(
            UiKit.bodyText(
                context,
                buildEvaluation(entry)
            )
        )

        root.addView(evaluationCard)
        root.addView(UiKit.spacer(context))

        val baselineCard = UiKit.card(context)

        baselineCard.addView(
            UiKit.cardTitle(context, "BASELINE")
        )

        baselineCard.addView(
            UiKit.bodyText(
                context,
                """
Baseline Source
${entry.baselineSource ?: "-"}

Baseline Label
${entry.baselineLabel ?: "-"}

Match Grade
${entry.baselineMatchGrade ?: "-"}

Source Profile ID
${entry.baselineSourceProfileId ?: "-"}

Source Batch ID
${entry.baselineSourceBatchId ?: "-"}
                """.trimIndent()
            )
        )

        root.addView(baselineCard)

        backBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun formatSec(value: Int?): String {
        if (value == null) return "-"
        val m = value / 60
        val s = value % 60
        return "%d:%02d".format(m, s)
    }

    private fun buildEvaluation(entry: RoastHistoryEntry): String {

        val e = entry.evaluation ?: return "No evaluation saved."

        return """
Bean Color
${e.beanColor ?: "-"}

Ground Color
${e.groundColor ?: "-"}

Roasted Aw
${e.roastedAw ?: "-"}

Sweetness
${e.sweetness ?: "-"}

Acidity
${e.acidity ?: "-"}

Body
${e.body ?: "-"}

Flavor Clarity
${e.flavorClarity ?: "-"}

Balance
${e.balance ?: "-"}

Notes
${e.notes}
        """.trimIndent()
    }
}
