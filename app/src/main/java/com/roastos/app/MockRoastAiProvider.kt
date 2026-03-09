package com.roastos.app

class MockRoastAiProvider : RoastAiProvider {

    override suspend fun generate(
        request: RoastAiRequest
    ): RoastAiResponse {

        val ctx = request.context

        val message = buildString {

            appendLine("AI Roast Assistant (Mock)")
            appendLine()
            appendLine("Machine: ${ctx.machineName}")
            appendLine("BT: ${ctx.btC}")
            appendLine("RoR: ${ctx.rorCPerMin}")
            appendLine()
            appendLine("Suggestion:")
            appendLine("Maintain current power and monitor RoR stability.")
        }

        return RoastAiResponse(
            message = message,
            confidence = 0.5,
            actions = emptyList()
        )
    }
}
