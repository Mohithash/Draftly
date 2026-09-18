package com.mohithash.draftly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.draftly.App
import com.mohithash.draftly.ai.AiSettings
import com.mohithash.draftly.data.Draft
import com.mohithash.draftly.domain.DraftResult
import com.mohithash.draftly.domain.Intent
import com.mohithash.draftly.domain.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    val client get() = app.client
    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val settings: StateFlow<Settings> = app.store.flow("settings", Settings.serializer(), Settings())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveSettings(s: Settings) = app.store.set("settings", Settings.serializer(), s.copy(onboarded = true))

    val history = db.drafts().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _result = MutableStateFlow<Job<DraftResult>>(Job.Idle)
    val result: StateFlow<Job<DraftResult>> = _result

    fun draft(intent: Intent, tone: String, length: String, input: String, image: String?, extra: String, targetLang: String) {
        _result.value = Job.Loading
        viewModelScope.launch {
            _result.value = runCatching { app.writer.draft(ai.value, settings.value, intent, tone, length, input, image, extra, targetLang) }.fold({ r ->
                r.variants.firstOrNull()?.let { db.drafts().insert(Draft(intent = intent.name, tone = tone, input = input.take(500), output = it.text)) }
                Job.Done(r)
            }, { Job.Failed(it.message ?: "Failed") })
        }
    }
    fun clear() { _result.value = Job.Idle }
    fun delete(id: Long) = viewModelScope.launch { db.drafts().delete(id) }
    fun clearHistory() = viewModelScope.launch { db.drafts().clear() }
}
