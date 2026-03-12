package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.EnvironmentProfileEngine
import com.roastos.app.UiKit

object EnvironmentInputPage {

    fun show(
        context: Context,
        container: LinearLayout
    ) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ENVIRONMENT INPUT"))
        root.addView(UiKit.pageSubtitle(context, "Set today's roasting environment"))
        root.addView(UiKit.spacer(context))

        val backBtn = UiKit.secondaryButton(context, "BACK TO STUDIO")
        root.addView(backBtn)
        root.addView(UiKit.spacer(context))

        val current = EnvironmentProfileEngine.current()

        val altitudeInput = EditText(context).apply {
            hint = "Altitude (m)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            setText(current.altitudeMeters?.toString() ?: "")
        }

        val tempInput = EditText(context).apply {
            hint = "Ambient Temp (°C)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            setText(current.ambientTempC?.toString() ?: "")
        }

        val humidityInput = EditText(context).apply {
            hint = "Humidity (%RH)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(current.ambientHumidityRh?.toString() ?: "")
        }

        val noteInput = EditText(context).apply {
            hint = "Note"
            setText(current.note ?: "")
        }

        val saveBtn = UiKit.primaryButton(context, "SAVE ENVIRONMENT")
        val resultText = UiKit.bodyText(context, "")

        val card = UiKit.card(context)
        card.addView(UiKit.cardTitle(context, "TODAY ENVIRONMENT"))
        card.addView(altitudeInput)
        card.addView(tempInput)
        card.addView(humidityInput)
        card.addView(noteInput)
        card.addView(UiKit.spacer(context))
        card.addView(saveBtn)
        card.addView(UiKit.spacer(context))
        card.addView(resultText)

        root.addView(card)

        saveBtn.setOnClickListener {
            val updated = EnvironmentProfileEngine.updateTodayEnvironment(
                ambientTempC = tempInput.text.toString().toDoubleOrNull(),
                ambientHumidityRh = humidityInput.text.toString().toDoubleOrNull(),
                altitudeMeters = altitudeInput.text.toString().toIntOrNull(),
                note = noteInput.text.toString()
            )

            resultText.text = """
Saved

Altitude
${updated.altitudeMeters ?: "-"}

Ambient Temp
${updated.ambientTempC ?: "-"}

Humidity
${updated.ambientHumidityRh ?: "-"}

Note
${updated.note ?: "-"}
            """.trimIndent()
        }

        backBtn.setOnClickListener {
            RoastStudioPage.show(context, container)
        }

        scroll.addView(root)
        container.addView(scroll)
    }
}
