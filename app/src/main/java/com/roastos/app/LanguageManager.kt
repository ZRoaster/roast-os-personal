package com.roastos.app

enum class AppLanguage(
    val code: String,
    val label: String
) {
    ENGLISH("en", "English"),
    CHINESE_SIMPLIFIED("zh", "中文")
}

object LanguageManager {

    private var currentLanguage: AppLanguage =
        AppLanguage.CHINESE_SIMPLIFIED

    fun current(): AppLanguage {
        return currentLanguage
    }

    fun currentCode(): String {
        return currentLanguage.code
    }

    fun currentLabel(): String {
        return currentLanguage.label
    }

    fun isChinese(): Boolean {
        return currentLanguage == AppLanguage.CHINESE_SIMPLIFIED
    }

    fun isEnglish(): Boolean {
        return currentLanguage == AppLanguage.ENGLISH
    }

    fun set(language: AppLanguage) {
        currentLanguage = language
    }

    fun setByCode(code: String?): Boolean {
        val normalized = code?.trim()?.lowercase() ?: return false

        val match = AppLanguage.entries.firstOrNull { it.code == normalized }
            ?: return false

        currentLanguage = match
        return true
    }

    fun toggle(): AppLanguage {
        currentLanguage =
            when (currentLanguage) {
                AppLanguage.ENGLISH ->
                    AppLanguage.CHINESE_SIMPLIFIED

                AppLanguage.CHINESE_SIMPLIFIED ->
                    AppLanguage.ENGLISH
            }

        return currentLanguage
    }

    fun all(): List<AppLanguage> {
        return AppLanguage.entries.toList()
    }

    fun summary(): String {
        return buildString {
            appendLine("Language")
            appendLine(currentLabel())
            appendLine()
            appendLine("Code")
            append(currentCode())
        }
    }

    /**
     * 通用二选一文案函数
     */
    fun text(
        zh: String,
        en: String
    ): String {
        return when (currentLanguage) {
            AppLanguage.CHINESE_SIMPLIFIED -> zh
            AppLanguage.ENGLISH -> en
        }
    }

    /**
     * 给未来多语言扩展留口子
     */
    fun text(
        values: Map<AppLanguage, String>,
        fallback: String = ""
    ): String {
        return values[currentLanguage]
            ?: values[AppLanguage.ENGLISH]
            ?: values[AppLanguage.CHINESE_SIMPLIFIED]
            ?: fallback
    }
}
