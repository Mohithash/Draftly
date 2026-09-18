@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.draftly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.mohithash.draftly.ui.AppViewModel
import com.mohithash.draftly.ui.EmptyState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(vm: AppViewModel, onBack: () -> Unit) {
    val history by vm.history.collectAsState()
    val cs = MaterialTheme.colorScheme
    val clip = LocalClipboardManager.current
    val fmt = DateTimeFormatter.ofPattern("d MMM, HH:mm")
    Scaffold(topBar = { TopAppBar(title = { Text("History") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }, actions = { if (history.isNotEmpty()) TextButton(vm::clearHistory) { Text("Clear") } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (history.isEmpty()) item { EmptyState(Icons.Default.History, "Nothing yet", "Your drafts are kept here, on‑device.") }
            items(history, key = { it.id }) { d ->
                ListItem(headlineContent = { Text(d.output, maxLines = 3) }, overlineContent = { Text("${d.intent.lowercase().replaceFirstChar { it.uppercase() }} · ${d.tone} · ${Instant.ofEpochMilli(d.createdAt).atZone(ZoneId.systemDefault()).format(fmt)}") },
                    supportingContent = { Text(d.input, maxLines = 1, color = cs.onSurfaceVariant) },
                    trailingContent = { IconButton({ clip.setText(AnnotatedString(d.output)) }) { Icon(Icons.Default.ContentCopy, "Copy") } },
                    colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow), modifier = Modifier.clip(MaterialTheme.shapes.large))
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
