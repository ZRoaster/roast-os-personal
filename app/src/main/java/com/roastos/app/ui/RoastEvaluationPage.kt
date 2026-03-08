package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.RoastEvaluation
import com.roastos.app.RoastHistoryEngine

object RoastEvaluationPage {

    fun show(
        context: Context,
        container: LinearLayout,
        batchId: String
    ) {
        container.removeAllViews()

        val entry = RoastHistoryEngine.findByBatchId(batchId)

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST EVALUATION"))
        root.addView(UiKit.pageSubtitle(context, "Roasted bean data and cup evaluation for a single batch"))
        root.addView(UiKit.spacer(context))

        val navCard = UiKit.card(context)
        navCard.addView(UiKit.cardTitle(context, "NAVIGATION"))

        val backBtn = Button(context)
        backBtn.text = "Back to Batch Detail"
        backBtn.setOnClickListener {
            BatchDetailPage.show(
                context = context,
                container = container,
                batchId = batchId
            )
        }

        navCard.addView(backBtn)
        root.addView(navCard)
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
                "BATCH TARGET",
                """
Batch ID
${entry.batchId}

Process
${entry.process.ifBlank { "-" }}

Status
${entry.batchStatus}
                """.trimIndent()
            )
        )
        root.addView(UiKit.spacer(context))

        val currentEvaluationCard = UiKit.card(context)
        currentEvaluationCard.addView(UiKit.cardTitle(context, "CURRENT EVALUATION"))
        val currentEvaluationBody = UiKit.bodyText(context, buildEvaluationSummary(entry.evaluation))
        currentEvaluationCard.addView(currentEvaluationBody)
        root.addView(currentEvaluationCard)
        root.addView(UiKit.spacer(context))

        val inputCard = UiKit.card(context)
        inputCard.addView(UiKit.cardTitle(context, "EVALUATION INPUT"))

        val existing = entry.evaluation

        val beanColorInput = decimalInput(context, "Bean Color", existing?.beanColor)
        val groundColorInput = decimalInput(context, "Ground Color", existing?.groundColor)
        val roastedAwInput = decimalInput(context, "Roasted aw", existing?.roastedAw)

        val sweetnessInput = intInput(context, "Sweetness (1-10)", existing?.sweetness)
        val acidityInput = intInput(context, "Acidity (1-10)", existing?.acidity)
        val bodyInput = intInput(context, "Body (1-10)", existing?.body)
        val clarityInput = intInput(context, "Flavor Clarity (1-10)", existing?.flavorClarity)
        val balanceInput = intInput(context, "Balance (1-10)", existing?.balance)

        val notesInput = EditText(context)
        notesInput.hint = "Notes"
        notesInput.setText(existing?.notes ?: "")

        val saveBtn = Button(context)
        saveBtn.text = "Save Evaluation"

        val clearBtn = Button(context)
        clearBtn.text = "Clear Evaluation"

        val statusText = UiKit.bodyText(context, "")

        inputCard.addView(beanColorInput)
        inputCard.addView(groundColorInput)
        inputCard.addView(roastedAwInput)
        inputCard.addView(sweetnessInput)
        inputCard.addView(acidityInput)
        inputCard.addView(bodyInput)
        inputCard.addView(clarityInput)
        inputCard.addView(balanceInput)
        inputCard.addView(notesInput)
        inputCard.addView(saveBtn)
        inputCard.addView(clearBtn)
        inputCard.addView(statusText)

        root.addView(inputCard)

        fun refreshCurrentEvaluation() {
            val latestEntry = RoastHistoryEngine.findByBatchId(batchId)
            currentEvaluationBody.text = buildEvaluationSummary(latestEntry?.evaluation)
        }

        saveBtn.setOnClickListener {
            val evaluation = RoastEvaluation(
                beanColor = beanColorInput.text.toString().toDoubleOrNull(),
                groundColor = groundColorInput.text.toString().toDoubleOrNull(),
                roastedAw = roastedAwInput.text.toString().toDoubleOrNull(),
                sweetness = clampScore(sweetnessInput.text.toString().toIntOrNull()),
                acidity = clampScore(acidityInput.text.toString().toIntOrNull()),
                body = clampScore(bodyInput.text.toString().toIntOrNull()),
                flavorClarity = clampScore(clarityInput.text.toString().toIntOrNull()),
                balance = clampScore(balanceInput.text.toString().toIntOrNull()),
                notes = notesInput.text.toString().trim()
            )

            val result = RoastHistoryEngine.saveEvaluation(batchId, evaluation)
            statusText.text = result.message
            refreshCurrentEvaluation()
        }

        clearBtn.setOnClickListener {
            val result = RoastHistoryEngine.clearEvaluation(batchId)
            beanColorInput.setText("")
            groundColorInput.setText("")
            roastedAwInput.setText("")
            sweetnessInput.setText("")
            acidityInput.setText("")
            bodyInput.setText("")
            clarityInput.setText("")
            balanceInput.setText("")
            notesInput.setText("")
            statusText.text = result.message
            refreshCurrentEvaluation()
        }

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun decimalInput(
        context: Context,
        hint: String,
        value: Double?
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(value?.toString() ?: "")
        return input
    }

    private fun intInput(
        context: Context,
        hint: String,
        value: Int?
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(value?.toString() ?: "")
        return input
    }

    private fun clampScore(value: Int?): Int? {
        return value?.coerceIn(1, 10)
    }

    private fun buildEvaluationSummary(evaluation: RoastEvaluation?): String {
        if (evaluation == null) {
            return """
Status
Not saved

Bean Color
-

Ground Color
-

Roasted aw
-

Cup Scores
Sweetness -
Acidity -
Body -
Flavor Clarity -
Balance -

Notes
-
            """.trimIndent()
        }

        return """
Status
Saved

Bean Color
${evaluation.beanColor?.let { "%.2f".format(it) } ?: "-"}

Ground Color
${evaluation.groundColor?.let { "%.2f".format(it) } ?: "-"}

Roasted aw
${evaluation.roastedAw?.let { "%.3f".format(it) } ?: "-"}

Cup Scores
Sweetness ${evaluation.sweetness ?: "-"}
Acidity ${evaluation.acidity ?: "-"}
Body ${evaluation.body ?: "-"}
Flavor Clarity ${evaluation.flavorClarity ?: "-"}
Balance ${evaluation.balance ?: "-"}

Notes
${evaluation.notes.ifBlank { "-" }}
        """.trimIndent()
    }
}
