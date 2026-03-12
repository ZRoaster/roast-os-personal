package com.roastos.app

data class RoastAiSessionSnapshot(
    val context: RoastAiContext,
    val prompt: String,
    val assistantOutput: RoastAiAssistantOutput
)

object RoastAiSessionEngine {

    fun build(
        userPrompt: String = "",
        operatorNote: String = ""
    ): RoastAiSessionSnapshot {

        val context = RoastAiRealtimeContextBuilder.build(
            intent = RoastAiIntentType.REALTIME_COACHING,
            userPrompt = userPrompt,
            operatorNote = operatorNote
        )

        val prompt = RoastAiPromptBuilder.buildFullPrompt(context)

        val assistantOutput = RoastAiAssistantEngine.generate(context)

        return RoastAiSessionSnapshot(
            context = context,
            prompt = prompt,
            assistantOutput = assistantOutput
        )
    }
}
