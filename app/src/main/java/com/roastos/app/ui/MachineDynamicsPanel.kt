package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.MachineDynamicsEngine
import com.roastos.app.UiKit

class MachineDynamicsPanel(
    context: Context
) : LinearLayout(context) {

    private val textView = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(textView)
        update()
    }

    fun update() {
        val profile = MachineDynamicsEngine.current()

        textView.text = """
Machine
${profile.machineName}

Calibration ID
${profile.calibrationId}

Environment
Altitude: ${profile.calibrationEnvironment.altitudeMeters ?: "-"} m
Temp: ${profile.calibrationEnvironment.ambientTempC ?: "-"} °C
Humidity: ${profile.calibrationEnvironment.ambientHumidityRh ?: "-"} %

Delays
Heat Up Delay: ${profile.delays.heatUpDelaySec ?: "-"} s
Heat Down Delay: ${profile.delays.heatDownDelaySec ?: "-"} s
Airflow Delay: ${profile.delays.airflowDelaySec ?: "-"} s
Drum Delay: ${profile.delays.drumSpeedDelaySec ?: "-"} s
Cooling Delay: ${profile.delays.coolingResponseDelaySec ?: "-"} s

Inertia
Thermal: ${profile.inertia.thermalInertiaScore ?: "-"}
Airflow: ${profile.inertia.airflowInertiaScore ?: "-"}
Drum: ${profile.inertia.drumInertiaScore ?: "-"}
        """.trimIndent()
    }
}
