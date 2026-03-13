package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastCalibrationHistoryEngine
import com.roastos.app.UiKit

class RoastCalibrationHistoryPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val history = RoastCalibrationHistoryEngine.all()

        if (history.isEmpty()) {
            textView.text = """
Calibration History

No calibration history yet.
            """.trimIndent()
            return
        }

        textView.text = history.take(5).joinToString("\n\n────────\n\n") { profile ->
            """
Machine
${profile.machineName}

Calibrated
${profile.calibratedAtMillis}

Environment
Temp ${profile.calibrationEnvironment.ambientTempC ?: "-"} °C
Humidity ${profile.calibrationEnvironment.ambientHumidityRh ?: "-"} %RH
Altitude ${profile.calibrationEnvironment.altitudeMeters ?: "-"} m

Delays
Heat Up ${profile.delays.heatUpDelaySec}s
Heat Down ${profile.delays.heatDownDelaySec}s
Airflow ${profile.delays.airflowDelaySec}s
Drum ${profile.delays.drumSpeedDelaySec}s
Cooling ${profile.delays.coolingResponseDelaySec}s

Inertia
Thermal ${profile.inertia.thermalInertiaScore}
Airflow ${profile.inertia.airflowInertiaScore}
Drum ${profile.inertia.drumInertiaScore}

Note
${profile.note}
            """.trimIndent()
        }
    }
}
