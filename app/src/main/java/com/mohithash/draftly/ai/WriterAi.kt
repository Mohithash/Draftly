package com.mohithash.draftly.ai

import com.mohithash.draftly.domain.DraftResult
import com.mohithash.draftly.domain.Intent
import com.mohithash.draftly.domain.Settings

class WriterAi(private val client: AiClient) {
    private val schema = Schema.obj("variants" to Schema.arr(Schema.obj("label" to Schema.str, "text" to Schema.str)), "note" to Schema.str)

    suspend fun draft(ai: AiSettings, s: Settings, intent: Intent, tone: String, length: String, input: String, image: String?, extra: String, targetLang: String): DraftResult {
        val who = (if (s.name.isNotBlank()) "The user's name is ${s.name}. " else "") + (if (s.signoff.isNotBlank()) "Sign off with: \"${s.signoff}\". " else "") + "Write in ${if (intent == Intent.TRANSLATE) targetLang else s.language}. "
        val task = when (intent) {
            Intent.REPLY -> "Write a reply to the message the user received. Answer every question in it; keep commitments realistic. Produce 3 variants with different angles (e.g. warm, concise, firm) — label each."
            Intent.REWRITE -> "Rewrite the user's draft in the requested tone, keeping meaning and facts. Produce 3 variants — label each with what changed (e.g. 'clearer', 'warmer', 'tighter')."
            Intent.SUMMARIZE -> "Summarise the text. Variant 1 'TL;DR': 1-2 sentences. Variant 2 'Key points': 3-7 bullets. Variant 3 'Actions': who needs to do what, or 'None' if nothing."
            Intent.TRANSLATE -> "Translate faithfully into $targetLang. Variant 1 'Natural': idiomatic. Variant 2 'Literal': close to source structure. Variant 3 'Formal': polite register."
            Intent.EXPAND -> "Turn the rough notes into a well-structured full text (message, email or paragraph as appropriate). Produce 2 variants — label them."
            Intent.SHORTEN -> "Cut the text to about half its length without losing meaning. Variant 1 'Half'. Variant 2 'One paragraph'. Variant 3 'One line'."
        }
        val system = "You are a sharp writing assistant. $who Tone: $tone. Length: $length. $task If a photo is attached, treat its visible text as the input. note: one short remark only if something is missing or ambiguous, else empty. Never add preambles inside variant text."
        val user = input.ifBlank { "(see attached image)" } + if (extra.isNotBlank()) "\n\nExtra instructions: $extra" else ""
        return client.ask(ai, system, user, schema, image, 4000)
    }
}
