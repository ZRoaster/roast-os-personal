package com.roastos.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roastos.app.MachineDynamicsEngine

@Composable
fun MachineDynamicsPanel() {

    val profile = remember {
        MachineDynamicsEngine.current()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Machine Dynamics",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Machine: ${profile.machineName}")
            Text("Calibration ID: ${profile.calibrationId}")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Environment")
            Text("Altitude: ${profile.calibrationEnvironment.altitudeMeters ?: "-"} m")
            Text("Temp: ${profile.calibrationEnvironment.ambientTempC ?: "-"} °C")
            Text("Humidity: ${profile.calibrationEnvironment.ambientHumidityRh ?: "-"} %")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Delays")
            Text("Heat Up Delay: ${profile.delays.heatUpDelaySec ?: "-"} s")
            Text("Heat Down Delay: ${profile.delays.heatDownDelaySec ?: "-"} s")
            Text("Airflow Delay: ${profile.delays.airflowDelaySec ?: "-"} s")
            Text("Drum Delay: ${profile.delays.drumSpeedDelaySec ?: "-"} s")
            Text("Cooling Delay: ${profile.delays.coolingResponseDelaySec ?: "-"} s")

            Spacer(modifier = Modifier.height(12.dp))

            Text("Inertia")
            Text("Thermal: ${profile.inertia.thermalInertiaScore ?: "-"}")
            Text("Airflow: ${profile.inertia.airflowInertiaScore ?: "-"}")
            Text("Drum: ${profile.inertia.drumInertiaScore ?: "-"}")

        }
    }
}
