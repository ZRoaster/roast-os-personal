package com.roastos.app

data class RoastAiRequest(

    val provider: RoastAiProviderType,

    val apiKey: String? = null,

    val model: String? = null,

    val context: RoastAiContext,

    val enableVision: Boolean = false,

    val enableAudio: Boolean = false,

    val temperature: Double = 0.3,

    val maxTokens: Int = 800

)
