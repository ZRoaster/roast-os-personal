package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.RoastHistoryEntry
import com.roastos.app.RoastStyleFromBatchEngine
import com.roastos.app.UiKit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryDetailPage {

    fun show(
        context: Context,
        container: LinearLayout,
        entry: RoastHistoryEntry?,
        onBack: (() -> Unit)? = null
    ) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST HISTORY DETAIL"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        if (entry == null) {

            val emptyCard = UiKit.card(context)

            emptyCard.addView(UiKit.cardTitle(context, "NO DATA"))
            emptyCard.addView(
                UiKit.bodyText(
                    context,
                    "No roast history entry found."
                )
            )

            root.addView(emptyCard)

            backBtn.setOnClickListener {
                onBack?.invoke() ?: RoastStudioPage.show(context, container)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val batchCard = UiKit.card(context)

        batchCard.addView(UiKit.cardTitle(context, "BATCH"))
        batchCard.addView(
            UiKit.bodyText(
                context,
                """
Batch ID
${entry.batchId}

Title
${entry.title}

Created
${formatDateTime(entry.createdAtMillis)}

Status
${entry.batchStatus}

Process
${entry.process}

Density
${entry.density}

Moisture
${entry.moisture}

AW
${entry.aw}

Environment
${entry.envTemp} ℃ / ${entry.envRh} %
                """.trimIndent()
            )
        )

        root.addView(batchCard)
        root.addView(UiKit.spacer(context))

        val timelineCard = UiKit.card(context)

        timelineCard.addView(UiKit.cardTitle(context, "TIMELINE"))
        timelineCard.addView(
            UiKit.bodyText(
                context,
                """
Turning
${formatSec(entry.actualTurningSec ?: entry.predictedTurningSec)}

Yellow
${formatSec(entry.actualYellowSec ?: entry.predictedYellowSec)}

First Crack
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)}

Drop
${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}
                """.trimIndent()
            )
        )

        root.addView(timelineCard)
        root.addView(UiKit.spacer(context))

        val reportCard = UiKit.card(context)

        reportCard.addView(UiKit.cardTitle(context, "REPORT"))
        reportCard.addView(
            UiKit.bodyText(
                context,
                entry.reportText
            )
        )

        root.addView(reportCard)
        root.addView(UiKit.spacer(context))

        val diagnosisCard = UiKit.card(context)

        diagnosisCard.addView(UiKit.cardTitle(context, "DIAGNOSIS"))
        diagnosisCard.addView(
            UiKit.bodyText(
                context,
                entry.diagnosisText
            )
        )

        root.addView(diagnosisCard)
        root.addView(UiKit.spacer(context))

        val correctionCard = UiKit.card(context)

        correctionCard.addView(UiKit.cardTitle(context, "CORRECTION"))
        correctionCard.addView(
            UiKit.bodyText(
                context,
                entry.correctionText
            )
        )

        root.addView(correctionCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val createStyleBtn = UiKit.primaryButton(context, "CREATE MY STYLE")

        styleCard.addView(UiKit.cardTitle(context, "STYLE"))
        styleCard.addView(createStyleBtn)

        root.addView(styleCard)

        createStyleBtn.setOnClickListener {
            val suggestedName =
                RoastStyleFromBatchEngine.suggestStyleName(entry.batchId)

            val result =
                RoastStyleFromBatchEngine.createFromBatch(
                    entry.batchId,
                    suggestedName
                )

            Toast.makeText(
                context,
                result.message,
                Toast.LENGTH_LONG
            ).show()
        }

        backBtn.setOnClickListener {
            onBack?.invoke() ?: RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun formatSec(sec: Int?): String {
        if (sec == null) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
