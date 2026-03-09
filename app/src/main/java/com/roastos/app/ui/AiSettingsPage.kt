package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.AiProviderRegistry
import com.roastos.app.RoastAiProviderType
import com.roastos.app.RoastAiService
import com.roastos.app.RoastAiServiceConfig

object AiSettingsPage {

    private var selectedProvider: RoastAiProviderType = AiProviderRegistry.defaultProvider()

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "AI SETTINGS"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Configure AI provider, model, API key, and multimodal capabilities."
            )
        )
        root.addView(UiKit.spacer(context))

        val currentConfig = RoastAiService.currentConfig()

        selectedProvider = currentConfig.providerType

        val providerCard = UiKit.card(context)
        providerCard.addView(UiKit.cardTitle(context, "PROVIDER"))
        val providerBody = UiKit.bodyText(context, "")
        providerCard.addView(providerBody)

        val providerButtonsWrap = LinearLayout(context)
        providerButtonsWrap.orientation = LinearLayout.VERTICAL

        AiProviderRegistry.enabled().forEach { descriptor ->
            val btn = Button(context)
            btn.text = "Use ${descriptor.displayName}"
            btn.setOnClickListener {
                selectedProvider = descriptor.type
                refreshProviderSummary(providerBody)
            }
            providerButtonsWrap.addView(btn)
        }

        providerCard.addView(providerButtonsWrap)
        root.addView(providerCard)
        root.addView(UiKit.spacer(context))

        val configCard = UiKit.card(context)
        configCard.addView(UiKit.cardTitle(context, "CONFIGURATION"))

        val modelInput = textInput(
            context = context,
            hint = "Model Name",
            defaultText = currentConfig.modelName
        )

        val baseUrlInput = textInput(
            context = context,
            hint = "API Base URL",
            defaultText = currentConfig.apiBaseUrl
        )

        val apiKeyInput = textInput(
            context = context,
            hint = "API Key",
            defaultText = currentConfig.apiKeyHint
        )
        apiKeyInput.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val visionBtn = Button(context)
        val audioBtn = Button(context)
        val fileBtn = Button(context)

        var visionEnabled = currentConfig.enableVision
        var audioEnabled = currentConfig.enableAudio
        var fileEnabled = currentConfig.enableFileContext

        fun refreshToggleButtons() {
            visionBtn.text = "Vision: " + if (visionEnabled) "ON" else "OFF"
            audioBtn.text = "Audio: " + if (audioEnabled) "ON" else "OFF"
            fileBtn.text = "File Context: " + if (fileEnabled) "ON" else "OFF"
        }

        visionBtn.setOnClickListener {
            visionEnabled = !visionEnabled
            refreshToggleButtons()
        }

        audioBtn.setOnClickListener {
            audioEnabled = !audioEnabled
            refreshToggleButtons()
        }

        fileBtn.setOnClickListener {
            fileEnabled = !fileEnabled
            refreshToggleButtons()
        }

        refreshToggleButtons()

        val applyBtn = Button(context)
        applyBtn.text = "Apply AI Config"

        val resetBtn = Button(context)
        resetBtn.text = "Reset To Defaults"

        val configBody = UiKit.bodyText(context, "")
        val serviceBody = UiKit.bodyText(context, "")

        applyBtn.setOnClickListener {
            val newConfig = RoastAiServiceConfig(
                providerType = selectedProvider,
                modelName = modelInput.text.toString().ifBlank { "default" },
                apiBaseUrl = baseUrlInput.text.toString(),
                apiKeyHint = apiKeyInput.text.toString(),
                enableVision = visionEnabled,
                enableAudio = audioEnabled,
                enableFileContext = fileEnabled
            )

            RoastAiService.configure(newConfig)
            refreshProviderSummary(providerBody)
            refreshConfigSummary(configBody)
            refreshServiceSummary(serviceBody)
        }

        resetBtn.setOnClickListener {
            selectedProvider = AiProviderRegistry.defaultProvider()
            modelInput.setText("default")
            baseUrlInput.setText("")
            apiKeyInput.setText("")
            visionEnabled = false
            audioEnabled = false
            fileEnabled = false
            refreshToggleButtons()

            RoastAiService.configure(RoastAiServiceConfig())
            refreshProviderSummary(providerBody)
            refreshConfigSummary(configBody)
            refreshServiceSummary(serviceBody)
        }

        configCard.addView(modelInput)
        configCard.addView(baseUrlInput)
        configCard.addView(apiKeyInput)
        configCard.addView(visionBtn)
        configCard.addView(audioBtn)
        configCard.addView(fileBtn)
        configCard.addView(applyBtn)
        configCard.addView(resetBtn)
        configCard.addView(configBody)

        root.addView(configCard)
        root.addView(UiKit.spacer(context))

        val serviceCard = UiKit.card(context)
        serviceCard.addView(UiKit.cardTitle(context, "SERVICE STATUS"))
        serviceCard.addView(serviceBody)

        root.addView(serviceCard)
        root.addView(UiKit.spacer(context))

        val registryCard = UiKit.card(context)
        registryCard.addView(UiKit.cardTitle(context, "REGISTRY"))
        val registryBody = UiKit.bodyText(context, AiProviderRegistry.summary())
        registryCard.addView(registryBody)

        root.addView(registryCard)

        fun refreshAll() {
            refreshProviderSummary(providerBody)
            refreshConfigSummary(configBody)
            refreshServiceSummary(serviceBody)
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun refreshProviderSummary(body: android.widget.TextView) {
        val descriptor = AiProviderRegistry.get(selectedProvider)

        body.text = """
Selected Provider
${descriptor?.displayName ?: selectedProvider.name}

Type
$selectedProvider

Description
${descriptor?.description ?: "-"}

Vision Support
${if (descriptor?.supportsVision == true) "Yes" else "No"}

Audio Support
${if (descriptor?.supportsAudio == true) "Yes" else "No"}

Remote API
${if (descriptor?.supportsRemoteApi == true) "Yes" else "No"}
        """.trimIndent()
    }

    private fun refreshConfigSummary(body: android.widget.TextView) {
        body.text = RoastAiService.currentConfig().summary()
    }

    private fun refreshServiceSummary(body: android.widget.TextView) {
        body.text = RoastAiService.summary()
    }

    private fun textInput(
        context: Context,
        hint: String,
        defaultText: String
    ): EditText {
        val input = EditText(context)
        input.hint = hint
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(defaultText)
        return input
    }
}
