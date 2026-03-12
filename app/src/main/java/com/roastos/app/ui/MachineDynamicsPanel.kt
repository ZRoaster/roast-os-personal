package com.roastos.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.text.BasicText

import androidx.compose.material3.Card

import com.roastos.app.MachineDynamicsEngine

@Composable
fun MachineDynamicsPanel() {

    val profile = remember { MachineDynamicsEngine.current() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            BasicText("Machine Dynamics")

            Spacer(modifier = Modifier.height(12.dp))

            BasicText("Machine: ${profile.machineName}")
            BasicText("Calibration ID: ${profile.calibrationId}")

            Spacer(modifier = Modifier.height(12.dp))

            BasicText("Environment")

            BasicText("Altitude: ${profile.calibrationEnvironment.altitudeMeters ?: "-"} m")
            BasicText("Temp: ${profile.calibrationEnvironment.ambientTempC ?: "-"} °C")
            BasicText("Humidity: ${profile.calibrationEnvironment.ambientHumidityRh ?: "-"} %")

            Spacer(modifier = Modifier.height(12.dp))

            BasicText("Delays")

            BasicText("Heat Up Delay: ${profile.delays.heatUpDelaySec ?: "-"} s")
            BasicText("Heat Down Delay: ${profile.delays.heatDownDelaySec ?: "-"} s")
            BasicText("Airflow Delay: ${profile.delays.airflowDelaySec ?: "-"} s")
            BasicText("Drum Delay: ${profile.delays.drumSpeedDelaySec ?: "-"} s")
            BasicText("Cooling Delay: ${profile.delays.coolingResponseDelaySec ?: "-"} s")

            Spacer(modifier = Modifier.height(12.dp))

            BasicText("Inertia")

            BasicText("Thermal: ${profile.inertia.thermalInertiaScore ?: "-"}")
            BasicText("Airflow: ${profile.inertia.airflowInertiaScore ?: "-"}")
            BasicText("Drum: ${profile.inertia.drumInertiaScore ?: "-"}")
        }
    }
}
