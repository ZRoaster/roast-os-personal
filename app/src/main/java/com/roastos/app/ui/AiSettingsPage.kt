package com.roastos.app.ui

import android.content.Context
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.roastos.app.AiProviderRegistry
import com.roastos.app.RoastAiProviderType
import com.roastos.app.RoastAiService
import com.roastos.app.RoastAiServiceConfig

object AiSettingsPage {

    private var selectedProvider: RoastAiProviderType =
        AiProviderRegistry.defaultProvider()

    private var currentConfig = RoastAiServiceConfig()

    fun show(context: Context, container: LinearLayout) {

        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "AI SETTINGS"))
        root.addView(
            UiKit.pageSubtitle(
                context,
                "Configure AI provider and runtime capabilities."
            )
        )

        root.addView(UiKit.spacer(context))

        val providerCard = UiKit.card(context)
        providerCard.addView(UiKit.cardTitle(context, "PROVIDER"))

        val providerSummary = UiKit.bodyText(context, "")
        providerCard.addView(providerSummary)

        val providerButtons = LinearLayout(context)
        providerButtons.orientation = LinearLayout.VERTICAL

        AiProviderRegistry.enabled().forEach { descriptor ->

            val btn = Button(context)
            btn.text = "Use ${descriptor.displayName}"

            btn.setOnClickListener {
                selectedProvider = descriptor.type
                refreshProvider(providerSummary)
            }

            providerButtons.addView(btn)
        }

        providerCard.addView(providerButtons)

        root.addView(providerCard)
        root.addView(UiKit.spacer(context))

        val configCard = UiKit.card(context)
        configCard.addView(UiKit.cardTitle(context, "CONFIG"))

        val modelInput = textInput(context, "Model", currentConfig.modelName)

        val baseUrlInput = textInput(context, "API Base URL", currentConfig.apiBaseUrl)

        val apiKeyInput = textInput(context, "API Key", currentConfig.apiKeyHint)

        apiKeyInput.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val visionButton = Button(context)
        val audioButton = Button(context)
        val fileButton = Button(context)

        var vision = currentConfig.enableVision
        var audio = currentConfig.enableAudio
        var file = currentConfig.enableFileContext

        fun refreshToggle() {

            visionButton.text =
                "Vision: " + if (vision) "ON" else "OFF"

            audioButton.text =
                "Audio: " + if (audio) "ON" else "OFF"

            fileButton.text =
                "File Context: " + if (file) "ON" else "OFF"
        }

        visionButton.setOnClickListener {
            vision = !vision
            refreshToggle()
        }

        audioButton.setOnClickListener {
            audio = !audio
            refreshToggle()
        }

        fileButton.setOnClickListener {
            file = !file
            refreshToggle()
        }

        refreshToggle()

        val applyButton = Button(context)
        applyButton.text = "Apply"

        val resetButton = Button(context)
        resetButton.text = "Reset"

        val configSummary = UiKit.bodyText(context, "")
        val serviceSummary = UiKit.bodyText(context, "")

        applyButton.setOnClickListener {

            currentConfig = RoastAiServiceConfig(
                providerType = selectedProvider,
                modelName = modelInput.text.toString(),
                apiBaseUrl = baseUrlInput.text.toString(),
                apiKeyHint = apiKeyInput.text.toString(),
                enableVision = vision,
                enableAudio = audio,
                enableFileContext = file
            )

            RoastAiService.configure(currentConfig)

            refreshProvider(providerSummary)
            refreshConfig(configSummary)
            refreshService(serviceSummary)
        }

        resetButton.setOnClickListener {

            selectedProvider = AiProviderRegistry.defaultProvider()
            currentConfig = RoastAiServiceConfig()

            modelInput.setText("")
            baseUrlInput.setText("")
            apiKeyInput.setText("")

            vision = false
            audio = false
            file = false

            refreshToggle()

            RoastAiService.configure(currentConfig)

            refreshProvider(providerSummary)
            refreshConfig(configSummary)
            refreshService(serviceSummary)
        }

        configCard.addView(modelInput)
        configCard.addView(baseUrlInput)
        configCard.addView(apiKeyInput)

        configCard.addView(visionButton)
        configCard.addView(audioButton)
        configCard.addView(fileButton)

        configCard.addView(applyButton)
        configCard.addView(resetButton)

        configCard.addView(configSummary)

        root.addView(configCard)
        root.addView(UiKit.spacer(context))

        val serviceCard = UiKit.card(context)
        serviceCard.addView(UiKit.cardTitle(context, "SERVICE"))
        serviceCard.addView(serviceSummary)

        root.addView(serviceCard)

        root.addView(UiKit.spacer(context))

        val registryCard = UiKit.card(context)
        registryCard.addView(UiKit.cardTitle(context, "REGISTRY"))

        val registrySummary =
            UiKit.bodyText(context, AiProviderRegistry.summary())

        registryCard.addView(registrySummary)

        root.addView(registryCard)

        fun refreshAll() {
            refreshProvider(providerSummary)
            refreshConfig(configSummary)
            refreshService(serviceSummary)
        }

        refreshAll()

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun refreshProvider(body: TextView) {

        val descriptor =
            AiProviderRegistry.get(selectedProvider)

        body.text =
            """
Selected Provider

${descriptor?.displayName ?: selectedProvider.name}

Type
$selectedProvider

Description
${descriptor?.description ?: "-"}

Vision
${if (descriptor?.supportsVision == true) "Yes" else "No"}

Audio
${if (descriptor?.supportsAudio == true) "Yes" else "No"}

Remote API
${if (descriptor?.supportsRemoteApi == true) "Yes" else "No"}
            """.trimIndent()
    }

    private fun refreshConfig(body: TextView) {

        body.text = currentConfig.summary()
    }

    private fun refreshService(body: TextView) {

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
