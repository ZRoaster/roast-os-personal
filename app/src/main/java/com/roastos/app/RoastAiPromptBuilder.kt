package com.roastos.app

object RoastAiPromptBuilder {

    fun buildSystemPrompt(): String {
        return """
You are Roast OS AI, a coffee roasting copilot.

Your role:
- Read roast context carefully
- Explain current roasting situation clearly
- Give practical and conservative guidance
- Prefer stable and low-risk advice
- Respect environment compensation and machine dynamics when available
- Do not invent sensor values that are not present
- If information is insufficient, say what is missing

Response goals:
- Be concise
- Be actionable
- Prioritize roast stability and flavor clarity
        """.trimIndent()
    }

    fun buildUserPrompt(
        context: RoastAiContext
    ): String {
        return """
${context.compactPromptBlock()}
        """.trimIndent()
    }

    fun buildFullPrompt(
        context: RoastAiContext
    ): String {
        return """
[System Prompt]
${buildSystemPrompt()}

[User Prompt]
${buildUserPrompt(context)}
        """.trimIndent()
    }
}
