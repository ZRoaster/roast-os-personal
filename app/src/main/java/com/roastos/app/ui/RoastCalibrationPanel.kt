package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import com.roastos.app.MachineStateStorage
import com.roastos.app.RoastCalibrationSessionEngine
import com.roastos.app.RoastStateModel
import com.roastos.app.UiKit

class RoastCalibrationPanel(
    context: Context
) : LinearLayout(context) {

    private val summaryView = UiKit.bodyText(context, "")

    private val heatUpInput = EditText(context).apply {
        hint = "Heat Up Delay (sec)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val heatDownInput = EditText(context).apply {
        hint = "Heat Down Delay (sec)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val airflowInput = EditText(context).apply {
        hint = "Airflow Delay (sec)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val drumInput = EditText(context).apply {
        hint = "Drum Delay (sec)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val coolingInput = EditText(context).apply {
        hint = "Cooling Delay (sec)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val thermalInertiaInput = EditText(context).apply {
        hint = "Thermal Inertia (0.0 - 1.0)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val airflowInertiaInput = EditText(context).apply {
        hint = "Airflow Inertia (0.0 - 1.0)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val drumInertiaInput = EditText(context).apply {
        hint = "Drum Inertia (0.0 - 1.0)"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private val noteInput = EditText(context).apply {
        hint = "Calibration Note"
    }

    init {
        orientation = VERTICAL

        addView(UiKit.cardTitle(context, "CALIBRATION SESSION"))
        addView(summaryView)
        addView(UiKit.spacer(context))

        addView(heatUpInput)
        addView(heatDownInput)
        addView(airflowInput)
        addView(drumInput)
        addView(coolingInput)
        addView(thermalInertiaInput)
        addView(airflowInertiaInput)
        addView(drumInertiaInput)
        addView(noteInput)
        addView(UiKit.spacer(context))

        val startBtn = UiKit.secondaryButton(context, "START CALIBRATION")
        val saveBtn = UiKit.primaryButton(context, "SAVE CALIBRATION")
        val cancelBtn = UiKit.secondaryButton(context, "CANCEL")

        addView(startBtn)
        addView(saveBtn)
        addView(cancelBtn)

        startBtn.setOnClickListener {
            RoastCalibrationSessionEngine.start()
            refreshFromDraft()
        }

        saveBtn.setOnClickListener {
            RoastCalibrationSessionEngine.update(
                heatUpDelaySec = heatUpInput.text.toString().toDoubleOrNull(),
                heatDownDelaySec = heatDownInput.text.toString().toDoubleOrNull(),
                airflowDelaySec = airflowInput.text.toString().toDoubleOrNull(),
                drumSpeedDelaySec = drumInput.text.toString().toDoubleOrNull(),
                coolingResponseDelaySec = coolingInput.text.toString().toDoubleOrNull(),
                thermalInertiaScore = thermalInertiaInput.text.toString().toDoubleOrNull(),
                airflowInertiaScore = airflowInertiaInput.text.toString().toDoubleOrNull(),
                drumInertiaScore = drumInertiaInput.text.toString().toDoubleOrNull(),
                note = noteInput.text.toString()
            )

            val profile = RoastCalibrationSessionEngine.commit()

            RoastStateModel.machine = RoastStateModel.MachineState(
                thermalMass = profile.inertia.thermalInertiaScore ?: 1.0,
                drumMass = profile.inertia.drumInertiaScore ?: 1.0,
                heatRetention = profile.inertia.airflowInertiaScore ?: 1.0,
                maxPowerW = RoastStateModel.machine.maxPowerW,
                maxAirPa = RoastStateModel.machine.maxAirPa,
                maxRpm = RoastStateModel.machine.maxRpm,
                powerResponseDelay = profile.delays.heatUpDelaySec ?: 6.0,
                airflowResponseDelay = profile.delays.airflowDelaySec ?: 3.0,
                rpmResponseDelay = profile.delays.drumSpeedDelaySec ?: 2.0
            )

            MachineStateStorage.save(context, RoastStateModel.machine)

            refreshFromDraft()
            summaryView.text = "Calibration saved to MachineDynamicsEngine and local storage."
        }

        cancelBtn.setOnClickListener {
            RoastCalibrationSessionEngine.cancel()
            clearInputs()
            refreshFromDraft()
        }

        refreshFromDraft()
    }

    fun update() {
        refreshFromDraft()
    }

    private fun refreshFromDraft() {
        val draft = RoastCalibrationSessionEngine.current()

        if (draft != null) {
            heatUpInput.setText(draft.heatUpDelaySec?.toString() ?: "")
            heatDownInput.setText(draft.heatDownDelaySec?.toString() ?: "")
            airflowInput.setText(draft.airflowDelaySec?.toString() ?: "")
            drumInput.setText(draft.drumSpeedDelaySec?.toString() ?: "")
            coolingInput.setText(draft.coolingResponseDelaySec?.toString() ?: "")
            thermalInertiaInput.setText(draft.thermalInertiaScore?.toString() ?: "")
            airflowInertiaInput.setText(draft.airflowInertiaScore?.toString() ?: "")
            drumInertiaInput.setText(draft.drumInertiaScore?.toString() ?: "")
            noteInput.setText(draft.note)

            summaryView.text = draft.summaryText()
            return
        }

        val machine = RoastStateModel.machine

        heatUpInput.setText(machine.powerResponseDelay.toString())
        heatDownInput.setText("")
        airflowInput.setText(machine.airflowResponseDelay.toString())
        drumInput.setText(machine.rpmResponseDelay.toString())
        coolingInput.setText("")
        thermalInertiaInput.setText(machine.thermalMass.toString())
        airflowInertiaInput.setText(machine.heatRetention.toString())
        drumInertiaInput.setText(machine.drumMass.toString())
        noteInput.setText("")

        summaryView.text = """
Current machine state loaded from storage/runtime.

Power Response Delay
${machine.powerResponseDelay}s

Airflow Response Delay
${machine.airflowResponseDelay}s

RPM Response Delay
${machine.rpmResponseDelay}s

Thermal Mass
${machine.thermalMass}

Heat Retention
${machine.heatRetention}

Drum Mass
${machine.drumMass}
        """.trimIndent()
    }

    private fun clearInputs() {
        heatUpInput.setText("")
        heatDownInput.setText("")
        airflowInput.setText("")
        drumInput.setText("")
        coolingInput.setText("")
        thermalInertiaInput.setText("")
        airflowInertiaInput.setText("")
        drumInertiaInput.setText("")
        noteInput.setText("")
    }
}
