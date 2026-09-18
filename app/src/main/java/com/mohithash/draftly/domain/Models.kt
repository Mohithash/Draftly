package com.mohithash.draftly.domain

import kotlinx.serialization.Serializable

enum class Intent(val label: String, val emoji: String, val hint: String) {
    REPLY("Reply", "↩️", "Paste the message you received"),
    REWRITE("Rewrite", "✍️", "Paste your draft"),
    SUMMARIZE("Summarise", "🧾", "Paste a long thread, article or notes"),
    TRANSLATE("Translate", "🌐", "Paste text in any language"),
    EXPAND("Expand", "🧱", "Bullet points or a rough idea"),
    SHORTEN("Shorten", "✂️", "Paste something too long"),
}

val TONES = listOf("friendly", "professional", "casual", "assertive", "apologetic", "enthusiastic", "neutral")
val LENGTHS = listOf("short", "medium", "long")

@Serializable data class Variant(val label: String, val text: String)
@Serializable data class DraftResult(val variants: List<Variant> = emptyList(), val note: String = "")

@Serializable data class Settings(val name: String = "", val signoff: String = "", val language: String = "English", val onboarded: Boolean = false)
