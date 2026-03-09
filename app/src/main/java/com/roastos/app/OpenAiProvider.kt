package com.roastos.app

class OpenAiProvider : RoastAiProvider {

    override suspend fun generate(
        request: RoastAiRequest
    ): RoastAiResponse {

        val ctx = request.context

        val machine = ctx.machineProfile?.name ?: "Unknown machine"
        val bt = ctx.machineState?.beanTemp ?: 0.0
        val ror = ctx.machineState?.ror ?: 0.0
        val model = request.model ?: "gpt-default"

        val text = """
OpenAI Provider (stub)

Model
$model

Machine
$machine

BT
$bt

RoR
$ror

AI Suggestion
Maintain RoR stability and avoid late-stage stall.
""".trimIndent()

        return RoastAiResponses.textExplanation(
            text = text,
            reasoning = "OpenAI provider stub response"
        )
    }
}
