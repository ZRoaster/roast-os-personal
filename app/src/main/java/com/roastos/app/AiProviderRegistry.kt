package com.roastos.app

data class AiProviderDescriptor(
    val type: RoastAiProviderType,
    val displayName: String,
    val description: String,
    val supportsVision: Boolean,
    val supportsAudio: Boolean,
    val supportsRemoteApi: Boolean,
    val isEnabled: Boolean = true
) {
    fun summary(): String {
        return """
AI Provider

Type
$type

Name
$displayName

Description
$description

Vision
${if (supportsVision) "Yes" else "No"}

Audio
${if (supportsAudio) "Yes" else "No"}

Remote API
${if (supportsRemoteApi) "Yes" else "No"}

Enabled
${if (isEnabled) "Yes" else "No"}
        """.trimIndent()
    }
}

object AiProviderRegistry {

    private val descriptors = linkedMapOf(
        RoastAiProviderType.MOCK to AiProviderDescriptor(
            type = RoastAiProviderType.MOCK,
            displayName = "Mock AI",
            description = "Local mock provider for testing AI flows without external API.",
            supportsVision = false,
            supportsAudio = false,
            supportsRemoteApi = false,
            isEnabled = true
        ),
        RoastAiProviderType.REMOTE_API to AiProviderDescriptor(
            type = RoastAiProviderType.REMOTE_API,
            displayName = "Remote API",
            description = "Generic remote provider entry for OpenAI and future external AI services.",
            supportsVision = true,
            supportsAudio = true,
            supportsRemoteApi = true,
            isEnabled = true
        )
    )

    fun all(): List<AiProviderDescriptor> {
        return descriptors.values.toList()
    }

    fun enabled(): List<AiProviderDescriptor> {
        return descriptors.values.filter { it.isEnabled }
    }

    fun get(type: RoastAiProviderType): AiProviderDescriptor? {
        return descriptors[type]
    }

    fun exists(type: RoastAiProviderType): Boolean {
        return descriptors.containsKey(type)
    }

    fun supportsVision(type: RoastAiProviderType): Boolean {
        return descriptors[type]?.supportsVision == true
    }

    fun supportsAudio(type: RoastAiProviderType): Boolean {
        return descriptors[type]?.supportsAudio == true
    }

    fun supportsRemoteApi(type: RoastAiProviderType): Boolean {
        return descriptors[type]?.supportsRemoteApi == true
    }

    fun displayName(type: RoastAiProviderType): String {
        return descriptors[type]?.displayName ?: type.name
    }

    fun defaultProvider(): RoastAiProviderType {
        return RoastAiProviderType.MOCK
    }

    fun summary(): String {
        return buildString {
            appendLine("AI Provider Registry")
            appendLine()
            enabled().forEachIndexed { index, descriptor ->
                appendLine("Provider ${index + 1}")
                appendLine(descriptor.summary())
                if (index != enabled().lastIndex) {
                    appendLine()
                }
            }
        }.trim()
    }
}
