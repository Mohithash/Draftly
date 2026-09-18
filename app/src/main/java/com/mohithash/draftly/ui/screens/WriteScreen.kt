@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.draftly.ui.screens

import android.content.Intent as AndroidIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.mohithash.draftly.domain.Intent
import com.mohithash.draftly.domain.LENGTHS
import com.mohithash.draftly.domain.TONES
import com.mohithash.draftly.ui.AppViewModel
import com.mohithash.draftly.ui.Job
import com.mohithash.draftly.ui.Label
import com.mohithash.draftly.ui.MealPhoto
import com.mohithash.draftly.ui.Photo
import com.mohithash.draftly.ui.ShapeIcon
import com.mohithash.draftly.ui.StatCard
import kotlinx.coroutines.launch

@Composable
fun WriteScreen(vm: AppViewModel, onHistory: () -> Unit, onSettings: () -> Unit) {
    val ai by vm.ai.collectAsState()
    val result by vm.result.collectAsState()
    val cs = MaterialTheme.colorScheme
    var intent by remember { mutableStateOf(Intent.REPLY) }
    var tone by remember { mutableStateOf("friendly") }
    var length by remember { mutableStateOf("medium") }
    var input by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }
    var lang by remember { mutableStateOf("English") }
    var photo by remember { mutableStateOf<MealPhoto?>(null) }
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { b -> b?.let { photo = Photo.fromBitmap(it) } }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u -> u?.let { photo = Photo.fromUri(ctx, it) } }

    Scaffold(topBar = { TopAppBar(title = { Text("Draftly") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onHistory) { Icon(Icons.Default.History, null) }; IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }) },
        snackbarHost = { SnackbarHost(snack) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Intent.entries) { i -> FilterChip(selected = intent == i, onClick = { intent = i; vm.clear() }, label = { Text("${i.emoji} ${i.label}") }) }
            }
            StatCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShapeIcon(Icons.Default.ContentPaste, cs.primaryContainer, cs.onPrimaryContainer, MaterialShapes.Cookie7Sided)
                    Column(Modifier.weight(1f)) { Text(intent.label, style = MaterialTheme.typography.titleMedium); Label(intent.hint) }
                    FilledTonalIconButton({ clip.getText()?.text?.let { input = it } }) { Icon(Icons.Default.ContentPaste, "Paste") }
                }
                photo?.let { p -> Box(Modifier.fillMaxWidth()) { Image(p.bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp).clip(MaterialTheme.shapes.large)); FilledTonalIconButton({ photo = null }, Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Default.Close, null) } } }
                OutlinedTextField(input, { input = it }, modifier = Modifier.fillMaxWidth(), minLines = 5, shape = MaterialTheme.shapes.large, placeholder = { Text(intent.hint + ", or attach a screenshot") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Text(" Camera") }
                    OutlinedButton({ gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Text(" Screenshot") }
                }
            }
            StatCard {
                if (intent == Intent.TRANSLATE) { Label("Into"); FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("English", "Spanish", "French", "German", "Hindi", "Japanese", "Chinese", "Arabic", "Portuguese").forEach { l -> FilterChip(selected = lang == l, onClick = { lang = l }, label = { Text(l) }) } } }
                else { Label("Tone"); FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { TONES.forEach { t -> FilterChip(selected = tone == t, onClick = { tone = t }, label = { Text(t) }) } } }
                Label("Length"); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { LENGTHS.forEach { l -> FilterChip(selected = length == l, onClick = { length = l }, label = { Text(l) }) } }
                OutlinedTextField(extra, { extra = it }, label = { Text("Extra instructions (optional)") }, placeholder = { Text("mention I'm away next week; don't promise a date") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
            }
            if (!ai.configured) Text("Add an API key in Settings to draft.", color = cs.error, style = MaterialTheme.typography.bodySmall)
            Button({ vm.draft(intent, tone, length, input, photo?.base64, extra, lang) }, enabled = ai.configured && (input.isNotBlank() || photo != null) && result != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                if (result == Job.Loading) { LoadingIndicator(Modifier.size(22.dp)); Spacer(Modifier.size(10.dp)); Text("Drafting…") } else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Draft it", style = MaterialTheme.typography.titleMedium) }
            }
            when (val r = result) {
                is Job.Failed -> StatCard(container = cs.errorContainer) { Text(r.message, color = cs.onErrorContainer) }
                is Job.Done -> {
                    if (r.value.note.isNotBlank()) Text("ℹ️ ${r.value.note}", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    r.value.variants.forEach { v ->
                        StatCard(container = cs.surfaceContainerHigh) {
                            Label(v.label, cs.primary)
                            Text(v.text, style = MaterialTheme.typography.bodyLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                FilledTonalButton({ clip.setText(AnnotatedString(v.text)); scope.launch { snack.showSnackbar("Copied") } }, shapes = ButtonDefaults.shapes()) { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)); Text(" Copy") }
                                FilledTonalButton({ ctx.startActivity(AndroidIntent.createChooser(AndroidIntent(AndroidIntent.ACTION_SEND).apply { type = "text/plain"; putExtra(AndroidIntent.EXTRA_TEXT, v.text) }, "Share")) }, shapes = ButtonDefaults.shapes()) { Icon(Icons.Default.Share, null, Modifier.size(16.dp)); Text(" Share") }
                            }
                        }
                    }
                }
                else -> {}
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
