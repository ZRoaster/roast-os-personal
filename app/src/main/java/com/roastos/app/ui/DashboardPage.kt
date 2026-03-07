package com.roastos.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DashboardPage() {

    Text(
        """
Roast OS

Machine
HB M2SE
Batch 200g

Charge 204°C
Max Power 1450W

Planner → RoastEngine
Correction → CorrectionEngine
Live Assist → LiveAssistEngine
""".trimIndent()
    )

}
