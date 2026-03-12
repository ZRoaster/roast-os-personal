package com.roastos.app

data class RoastAiSessionSnapshot(
    val context: RoastAiContext,
    val prompt: String,
    val assistantOutput: RoastAiAssistantOutput
)

object RoastAiSessionEngine {

    private var currentSnapshot: RoastAiSessionSnapshot? = null

    fun current(): RoastAiSessionSnapshot {
        return currentSnapshot ?: refresh()
    }

    fun peek(): RoastAiSessionSnapshot? {
        return currentSnapshot
    }

    fun refresh(
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

        val snapshot = RoastAiSessionSnapshot(
            context = context,
            prompt = prompt,
            assistantOutput = assistantOutput
        )

        currentSnapshot = snapshot
        return snapshot
    }

    fun reset() {
        currentSnapshot = null
    }
}
