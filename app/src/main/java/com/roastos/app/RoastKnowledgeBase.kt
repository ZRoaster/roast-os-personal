package com.roastos.app

data class RoastKnowledgeEntry(
    val tag: String,
    val type: String,
    val description: String,
    val sourceBatchId: String?,
    val confidence: String
)

object RoastKnowledgeBase {

    private val entries = mutableListOf<RoastKnowledgeEntry>()

    fun all(): List<RoastKnowledgeEntry> {
        return entries
    }

    fun add(entry: RoastKnowledgeEntry) {
        entries.add(entry)
    }

    fun count(): Int {
        return entries.size
    }

    fun summary(): String {

        if (entries.isEmpty()) {
            return """
Knowledge Base

Entries
0

Status
Empty
            """.trimIndent()
        }

        return """
Knowledge Base

Entries
${entries.size}

Latest
${entries.last().tag}
        """.trimIndent()
    }

    fun latest(): RoastKnowledgeEntry? {
        return entries.lastOrNull()
    }
}
