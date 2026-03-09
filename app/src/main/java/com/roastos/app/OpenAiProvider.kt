package com.roastos.app

class OpenAiProvider : RoastAiProvider {

    override suspend fun generate(
        request: RoastAiRequest
    ): RoastAiResponse {

        val ctx = request.context

        val machine = ctx.machineName ?: "Unknown machine"
        val bt = ctx.btC ?: 0.0
        val ror = ctx.rorCPerMin ?: 0.0

        val model = request.model ?: "gpt-default"

        val message = """
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

        return RoastAiResponse(
            message = message,
            confidence = 0.7,
            actions = emptyList()
        )
    }
}
