package com.roastos.app

data class RoastAiRequest(

    val provider: RoastAiProviderType = RoastAiProviderType.MOCK,

    val apiKey: String? = null,

    val model: String? = null,

    val context: RoastAiContext,

    val systemInstruction: String? = null,

    val userMessageOverride: String? = null,

    val enableVision: Boolean = false,

    val enableAudio: Boolean = false,

    val temperature: Double = 0.3,

    val maxTokens: Int = 800

)
