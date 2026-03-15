package com.roastos.app.ui

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.roastos.app.RoastEvaluation
import com.roastos.app.RoastHistoryEngine
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

        root.addView(UiKit.pageTitle(context, "ROAST DETAIL"))
        root.addView(UiKit.pageSubtitle(context, "Inspect result, evaluate, and reuse"))
        root.addView(UiKit.spacerS(context))
        root.addView(
            TopNavBar.create(
                context = context,
                container = container,
                current = TopNavBar.Section.REVIEW
            )
        )
        root.addView(UiKit.spacer(context))

        val accessCard = UiKit.card(context)
        val backBtn = UiKit.secondaryButton(context, "Back")
        accessCard.addView(UiKit.cardTitle(context, "ACCESS"))
        accessCard.addView(UiKit.helperText(context, "Return to the review flow."))
        accessCard.addView(UiKit.spacerM(context))
        accessCard.addView(backBtn)
        root.addView(accessCard)
        root.addView(UiKit.spacer(context))

        if (entry == null) {
            val emptyCard = UiKit.card(context)
            emptyCard.addView(UiKit.cardTitle(context, "NO DATA"))
            emptyCard.addView(UiKit.helperText(context, "No roast history entry found."))

            root.addView(emptyCard)

            backBtn.setOnClickListener {
                onBack?.invoke() ?: ReviewHubPage.show(context, container)
            }

            scroll.addView(root)
            container.addView(scroll)
            return
        }

        val summaryCard = UiKit.card(context)
        summaryCard.addView(UiKit.cardTitle(context, "RESULT SUMMARY"))
        summaryCard.addView(UiKit.spacerS(context))
        summaryCard.addView(
            UiKit.bodyText(
                context,
                buildSummaryStrip(entry)
            )
        )
        root.addView(summaryCard)
        root.addView(UiKit.spacer(context))

        val compareCard = UiKit.card(context)
        val compareWithLatestBtn = UiKit.primaryButton(context, "Compare With Latest")
        val compareWithPreviousBtn = UiKit.secondaryButton(context, "Compare With Previous")

        compareCard.addView(UiKit.cardTitle(context, "COMPARE"))
        compareCard.addView(UiKit.helperText(context, "Open this roast against a newer or older reference."))
        compareCard.addView(UiKit.spacerM(context))
        compareCard.addView(compareWithLatestBtn)
        compareCard.addView(UiKit.spacerS(context))
        compareCard.addView(compareWithPreviousBtn)
        root.addView(compareCard)
        root.addView(UiKit.spacer(context))

        val batchCard = UiKit.card(context)
        batchCard.addView(UiKit.cardTitle(context, "BATCH OVERVIEW"))
        batchCard.addView(UiKit.spacerS(context))
        batchCard.addView(
            UiKit.bodyText(
                context,
                """
批次
${entry.batchId}

标题 / 处理
${entry.title} / ${entry.process}

创建时间
${formatDateTime(entry.createdAtMillis)}

环境
${entry.envTemp} ℃ / ${entry.envRh} %

密度 / 水分 / AW
${entry.density} / ${entry.moisture} / ${entry.aw}
                """.trimIndent()
            )
        )
        root.addView(batchCard)
        root.addView(UiKit.spacer(context))

        val timelineCard = UiKit.card(context)
        timelineCard.addView(UiKit.cardTitle(context, "TIMELINE"))
        timelineCard.addView(UiKit.spacerS(context))
        timelineCard.addView(
            UiKit.bodyText(
                context,
                """
Turning / Yellow
${formatSec(entry.actualTurningSec ?: entry.predictedTurningSec)} / ${formatSec(entry.actualYellowSec ?: entry.predictedYellowSec)}

FC / Drop
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)} / ${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}

Pre-FC RoR
${formatRor(entry.actualPreFcRor)}
                """.trimIndent()
            )
        )
        root.addView(timelineCard)
        root.addView(UiKit.spacer(context))

        val insightCard = UiKit.card(context)
        insightCard.addView(UiKit.cardTitle(context, "INSIGHT"))
        insightCard.addView(UiKit.spacerS(context))
        insightCard.addView(
            UiKit.bodyText(
                context,
                """
Report
${entry.reportText.ifBlank { "-" }}

Diagnosis
${entry.diagnosisText.ifBlank { "-" }}

Correction
${entry.correctionText.ifBlank { "-" }}
                """.trimIndent()
            )
        )
        root.addView(insightCard)
        root.addView(UiKit.spacer(context))

        val evaluationCard = UiKit.card(context)

        val beanColorInput = UiKit.decimalField(context, "Bean Color")
        val groundColorInput = UiKit.decimalField(context, "Ground Color")
        val roastedAwInput = UiKit.decimalField(context, "Roasted AW")

        val sweetnessInput = UiKit.integerField(context, "Sweetness")
        val acidityInput = UiKit.integerField(context, "Acidity")
        val bodyInput = UiKit.integerField(context, "Body")
        val flavorClarityInput = UiKit.integerField(context, "Flavor Clarity")
        val balanceInput = UiKit.integerField(context, "Balance")

        val notesInput = UiKit.notesField(context, "Notes")

        fillEvaluation(
            entry.evaluation,
            beanColorInput,
            groundColorInput,
            roastedAwInput,
            sweetnessInput,
            acidityInput,
            bodyInput,
            flavorClarityInput,
            balanceInput,
            notesInput
        )

        val saveEvaluationBtn = UiKit.primaryButton(context, "Save Evaluation")
        val clearEvaluationBtn = UiKit.secondaryButton(context, "Clear Evaluation")

        evaluationCard.addView(UiKit.cardTitle(context, "EVALUATION"))
        evaluationCard.addView(UiKit.helperText(context, buildEvaluationIntro(entry.evaluation)))
        evaluationCard.addView(UiKit.spacerM(context))

        evaluationCard.addView(UiKit.sectionLabel(context, "COLOR / AW"))
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(beanColorInput)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(groundColorInput)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(roastedAwInput)
        evaluationCard.addView(UiKit.spacerM(context))

        evaluationCard.addView(UiKit.sectionLabel(context, "CUP SCORES"))
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(sweetnessInput)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(acidityInput)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(bodyInput)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(flavorClarityInput)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(balanceInput)
        evaluationCard.addView(UiKit.spacerM(context))

        evaluationCard.addView(UiKit.sectionLabel(context, "NOTES"))
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(notesInput)
        evaluationCard.addView(UiKit.spacerM(context))

        evaluationCard.addView(UiKit.sectionLabel(context, "SAVED SUMMARY"))
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(
            UiKit.bodyText(
                context,
                buildEvaluationSummary(entry.evaluation)
            )
        )
        evaluationCard.addView(UiKit.spacerM(context))
        evaluationCard.addView(saveEvaluationBtn)
        evaluationCard.addView(UiKit.spacerS(context))
        evaluationCard.addView(clearEvaluationBtn)

        root.addView(evaluationCard)
        root.addView(UiKit.spacer(context))

        val styleCard = UiKit.card(context)
        val createStyleBtn = UiKit.primaryButton(context, "Create My Style")

        styleCard.addView(UiKit.cardTitle(context, "REUSE"))
        styleCard.addView(UiKit.helperText(context, "Turn this batch into a reusable style reference."))
        styleCard.addView(UiKit.spacerM(context))
        styleCard.addView(createStyleBtn)

        root.addView(styleCard)
        root.addView(UiKit.spacer(context))

        val deleteCard = UiKit.card(context)
        val deleteBtn = UiKit.secondaryButton(context, "Delete This History")

        deleteCard.addView(UiKit.cardTitle(context, "DELETE"))
        deleteCard.addView(UiKit.dangerText(context, "Remove this batch from local history."))
        deleteCard.addView(UiKit.spacerM(context))
        deleteCard.addView(deleteBtn)

        root.addView(deleteCard)

        compareWithLatestBtn.setOnClickListener {
            val latest = RoastHistoryEngine.latest()

            if (latest == null) {
                Toast.makeText(context, "No latest roast history found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (latest.batchId == entry.batchId) {
                Toast.makeText(context, "Current entry is already the latest batch", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RoastComparePage.show(
                context = context,
                container = container,
                left = entry,
                right = latest,
                onBack = {
                    show(
                        context = context,
                        container = container,
                        entry = entry,
                        onBack = onBack
                    )
                }
            )
        }

        compareWithPreviousBtn.setOnClickListener {
            val allEntries = RoastHistoryEngine.all()
            val currentIndex = allEntries.indexOfFirst { it.batchId == entry.batchId }

            if (currentIndex < 0 || currentIndex + 1 >= allEntries.size) {
                Toast.makeText(context, "No previous roast history found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val previous = allEntries[currentIndex + 1]

            RoastComparePage.show(
                context = context,
                container = container,
                left = previous,
                right = entry,
                onBack = {
                    show(
                        context = context,
                        container = container,
                        entry = entry,
                        onBack = onBack
                    )
                }
            )
        }

        saveEvaluationBtn.setOnClickListener {
            val evaluation = RoastEvaluation(
                beanColor = parseDoubleOrNull(beanColorInput.text?.toString()),
                groundColor = parseDoubleOrNull(groundColorInput.text?.toString()),
                roastedAw = parseDoubleOrNull(roastedAwInput.text?.toString()),
                sweetness = parseIntOrNull(sweetnessInput.text?.toString()),
                acidity = parseIntOrNull(acidityInput.text?.toString()),
                body = parseIntOrNull(bodyInput.text?.toString()),
                flavorClarity = parseIntOrNull(flavorClarityInput.text?.toString()),
                balance = parseIntOrNull(balanceInput.text?.toString()),
                notes = notesInput.text?.toString()?.trim().orEmpty()
            )

            val result = RoastHistoryEngine.saveEvaluation(entry.batchId, evaluation)

            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

            val updatedEntry = RoastHistoryEngine.findByBatchId(entry.batchId)
            show(
                context = context,
                container = container,
                entry = updatedEntry,
                onBack = onBack
            )
        }

        clearEvaluationBtn.setOnClickListener {
            if (entry.evaluation == null) {
                Toast.makeText(context, "No evaluation to clear", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(context)
                .setTitle("Clear evaluation?")
                .setMessage("Saved evaluation data for this roast will be removed.")
                .setPositiveButton("CLEAR") { _, _ ->
                    val result = RoastHistoryEngine.clearEvaluation(entry.batchId)

                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

                    val updatedEntry = RoastHistoryEngine.findByBatchId(entry.batchId)
                    show(
                        context = context,
                        container = container,
                        entry = updatedEntry,
                        onBack = onBack
                    )
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        createStyleBtn.setOnClickListener {
            val suggestedName = RoastStyleFromBatchEngine.suggestStyleName(entry.batchId)

            val result = RoastStyleFromBatchEngine.createFromBatch(
                entry.batchId,
                suggestedName
            )

            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
        }

        deleteBtn.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete this history?")
                .setMessage("Batch ${entry.batchId} will be permanently removed from local history.")
                .setPositiveButton("DELETE") { _, _ ->
                    val result = RoastHistoryEngine.delete(entry.batchId)

                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()

                    if (result.deleted) {
                        onBack?.invoke() ?: ReviewHubPage.show(context, container)
                    }
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        backBtn.setOnClickListener {
            onBack?.invoke() ?: ReviewHubPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildSummaryStrip(
        entry: RoastHistoryEntry
    ): String {
        return """
批次
${entry.batchId}

创建时间
${formatDateTime(entry.createdAtMillis)}

结果 / 健康
${entry.batchStatus} / ${entry.roastHealthHeadline}

评测
${if (entry.evaluation != null) "已保存" else "未保存"}

环境
${entry.envTemp} ℃ / ${entry.envRh} %

Turning / Yellow
${formatSec(entry.actualTurningSec ?: entry.predictedTurningSec)} / ${formatSec(entry.actualYellowSec ?: entry.predictedYellowSec)}

FC / Drop
${formatSec(entry.actualFcSec ?: entry.predictedFcSec)} / ${formatSec(entry.actualDropSec ?: entry.predictedDropSec)}

Pre-FC RoR
${formatRor(entry.actualPreFcRor)}
        """.trimIndent()
    }

    private fun buildEvaluationIntro(
        evaluation: RoastEvaluation?
    ): String {
        return if (evaluation == null) {
            "No saved evaluation yet. Add cup feedback and roast result notes here."
        } else {
            "Saved evaluation detected. Update the values below to revise this batch review."
        }
    }

    private fun fillEvaluation(
        evaluation: RoastEvaluation?,
        beanColorInput: EditText,
        groundColorInput: EditText,
        roastedAwInput: EditText,
        sweetnessInput: EditText,
        acidityInput: EditText,
        bodyInput: EditText,
        flavorClarityInput: EditText,
        balanceInput: EditText,
        notesInput: EditText
    ) {
        beanColorInput.setText(evaluation?.beanColor?.toString().orEmpty())
        groundColorInput.setText(evaluation?.groundColor?.toString().orEmpty())
        roastedAwInput.setText(evaluation?.roastedAw?.toString().orEmpty())
        sweetnessInput.setText(evaluation?.sweetness?.toString().orEmpty())
        acidityInput.setText(evaluation?.acidity?.toString().orEmpty())
        bodyInput.setText(evaluation?.body?.toString().orEmpty())
        flavorClarityInput.setText(evaluation?.flavorClarity?.toString().orEmpty())
        balanceInput.setText(evaluation?.balance?.toString().orEmpty())
        notesInput.setText(evaluation?.notes.orEmpty())
    }

    private fun buildEvaluationSummary(
        evaluation: RoastEvaluation?
    ): String {
        if (evaluation == null) {
            return "No evaluation saved yet."
        }

        return """
Bean Color
${evaluation.beanColor ?: "-"}

Ground Color
${evaluation.groundColor ?: "-"}

Roasted AW
${evaluation.roastedAw ?: "-"}

Sweetness
${evaluation.sweetness ?: "-"}

Acidity
${evaluation.acidity ?: "-"}

Body
${evaluation.body ?: "-"}

Flavor Clarity
${evaluation.flavorClarity ?: "-"}

Balance
${evaluation.balance ?: "-"}

Notes
${evaluation.notes.ifBlank { "-" }}
        """.trimIndent()
    }

    private fun parseDoubleOrNull(value: String?): Double? {
        val text = value?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toDoubleOrNull()
    }

    private fun parseIntOrNull(value: String?): Int? {
        val text = value?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toIntOrNull()
    }

    private fun formatSec(sec: Int?): String {
        if (sec == null) return "-"
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatRor(value: Double?): String {
        if (value == null) return "-"
        return String.format(Locale.getDefault(), "%.1f ℃/min", value)
    }

    private fun formatDateTime(ms: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date(ms))
    }
}
