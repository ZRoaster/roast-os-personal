package com.roastos.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roastos.app.RoastAiRealtimeContextBuilder
import com.roastos.app.RoastAiPromptBuilder

@Composable
fun RoastAiPromptPreviewPanel() {

    val context = RoastAiRealtimeContextBuilder.build()

    val prompt = RoastAiPromptBuilder.buildFullPrompt(context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors()
    ) {

        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "AI Prompt Preview",
            )

            Text(
                text = prompt,
                modifier = Modifier.padding(top = 8.dp)
            )

        }
    }
}
