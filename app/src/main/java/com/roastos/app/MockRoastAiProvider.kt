package com.roastos.app

class MockRoastAiProvider : RoastAiProvider {

    override suspend fun generate(
        request: RoastAiRequest
    ): RoastAiResponse {

        val ctx = request.context

        val machine = ctx.machineProfile?.name ?: "Unknown machine"
        val bt = ctx.machineState?.beanTemp ?: 0.0
        val ror = ctx.machineState?.ror ?: 0.0

        val text = """
AI Roast Assistant (Mock)

Machine
$machine

BT
$bt

RoR
$ror

Suggestion
Maintain current power and monitor RoR stability.
""".trimIndent()

        return RoastAiResponses.textExplanation(
            text = text,
            reasoning = "Mock provider response"
        )
    }
}
