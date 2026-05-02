package com.misw4203.vinilos.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// matchParentSize not available across all compose versions; avoid importing it
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

private val CreateAlbumGenreSuggestions = listOf("Classical", "Salsa", "Rock", "Folk")
private val CreateAlbumRecordLabelSuggestions = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")

private fun formatReleaseDateForDisplay(value: String): String =
    runCatching { value.substringBefore('T') }
        .getOrElse { value }

@Composable
fun CreateAlbumScreen(
    viewModel: CreateAlbumViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coverUrlErrorText = coverUrlError(state.cover)
    val releaseDateErrorText = releaseDateError(state.releaseDate)
    val descriptionErrorText = descriptionError(state.description)
    val formIsValid = validateCreateAlbumForm(state) == null

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CreateAlbumUiEffect.Created -> onCreated()
                is CreateAlbumUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.clickable(onClick = onBack),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Create Album",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Collector access only · POST /albums",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        ) {
                            // Image picker launcher (gallery)
                            val context = LocalContext.current
                            val pickImageLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent(),
                                onResult = { uri: Uri? ->
                                    uri?.let { picked ->
                                        viewModel.onPickImageUri(context.contentResolver, picked)
                                    }
                                },
                            )
                            val coverUrl = state.cover.trim()
                            if (coverUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = state.name.ifBlank { "Album cover preview" },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                                ),
                                            ),
                                        ),
                                )
                            }
                            // Picker action on top-right of the artwork preview
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clickable { pickImageLauncher.launch("image/*") }
                                    .semantics { contentDescription = "Choose album cover image" },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = "Choose cover",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhotoLibrary,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "ALBUM ARTWORK",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = if (state.name.isBlank()) "Your release will appear here" else state.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                )
                                Text(
                                    text = if (state.description.isBlank()) "Write a description to preview it here." else state.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.78f),
                                )
                            }
                        }

                        SectionLabel(text = "Album details")

                        EditorialTextField(
                            value = state.name,
                            onValueChange = viewModel::onNameChange,
                            label = "Album name",
                            placeholder = "Buscando América",
                        )

                        val context = LocalContext.current

                        Text(
                            text = "Choose a genre from the options below",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        GenreSuggestionRow(
                            selectedGenre = state.genre,
                            onSelectGenre = viewModel::onGenreChange,
                        )
                        Text(
                            text = genreError(state.genre) ?: "Select one genre",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.genre.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            DatePickerField(
                                value = state.releaseDate,
                                onValueChange = viewModel::onReleaseDateChange,
                                label = "Release date",
                                placeholder = "YYYY-MM-DD",
                                supportingText = releaseDateErrorText ?: "Required",
                                isError = releaseDateErrorText != null,
                                modifier = Modifier.weight(1f),
                                context = context,
                            )
                        }

                        Text(
                            text = "Choose a record label from the options below",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        RecordLabelSuggestionRow(
                            selectedRecordLabel = state.recordLabel,
                            onSelectRecordLabel = viewModel::onRecordLabelChange,
                        )
                        Text(
                            text = recordLabelError(state.recordLabel) ?: "Select one record label",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.recordLabel.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        )

                        EditorialTextField(
                            value = state.cover,
                            onValueChange = viewModel::onCoverChange,
                            label = "Cover URL",
                            placeholder = "https://...",
                            keyboardType = KeyboardType.Uri,
                            supportingText = coverUrlErrorText ?: "Use a valid http or https image URL",
                            isError = coverUrlErrorText != null,
                        )

                        EditorialTextField(
                            value = state.description,
                            onValueChange = viewModel::onDescriptionChange,
                            label = "Description",
                            placeholder = "Tell collectors about the album, its sound and story...",
                            modifier = Modifier.height(140.dp),
                            singleLine = false,
                            isMultiline = true,
                            supportingText = descriptionErrorText ?: "Minimum $MIN_DESCRIPTION_LENGTH characters",
                            isError = descriptionErrorText != null,
                        )

                        Text(
                            text = "The API expects the release date in ISO-8601 format.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Button(
                    onClick = viewModel::submit,
                    enabled = !state.isSubmitting && formIsValid,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text = "Create Album",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                Text(
                    text = "Collector mode only · Your album will be published after the request succeeds.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun GenreSuggestionRow(
    selectedGenre: String,
    onSelectGenre: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    ) {
        CreateAlbumGenreSuggestions.forEach { genre ->
            val selected = selectedGenre.equals(genre, ignoreCase = true)
            FilterChip(
                selected = selected,
                onClick = { onSelectGenre(genre) },
                label = { Text(genre) },
            )
        }
    }
}

@Composable
private fun RecordLabelSuggestionRow(
    selectedRecordLabel: String,
    onSelectRecordLabel: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    ) {
        CreateAlbumRecordLabelSuggestions.forEach { label ->
            val selected = selectedRecordLabel.equals(label, ignoreCase = true)
            FilterChip(
                selected = selected,
                onClick = { onSelectRecordLabel(label) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun EditorialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isMultiline: Boolean = false,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    val shape = if (isMultiline) RoundedCornerShape(16.dp) else RoundedCornerShape(999.dp)
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = shape,
        isError = isError,
        supportingText = {
            if (!supportingText.isNullOrBlank()) {
                Text(supportingText)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
private fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    context: android.content.Context,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    // Parse existing value to preselect date if possible
    val initDate = runCatching {
        if (value.isBlank()) LocalDate.now() else LocalDate.parse(value.substringBefore('T'))
    }.getOrElse { LocalDate.now() }

    val year = initDate.year
    val month = initDate.monthValue - 1
    val day = initDate.dayOfMonth

    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    val picker = DatePickerDialog(context, { _, y, m, d ->
        val selected = LocalDate.of(y, m + 1, d)
        val zdt = ZonedDateTime.of(selected.atStartOfDay(), ZoneId.systemDefault())
        val formatted = zdt.format(formatter)
        onValueChange(formatted)
    }, year, month, day)

    // Read-only field that opens the native date picker. We overlay a transparent clickable box to ensure
    // the DatePicker opens even when the TextField is inside scrollable parents.
    TextField(
        value = formatReleaseDateForDisplay(value),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(999.dp),
        isError = isError,
        supportingText = {
            if (!supportingText.isNullOrBlank()) {
                Text(supportingText)
            }
        },
        trailingIcon = {
            Text(
                text = "📅",
                modifier = Modifier
                    .clickable { picker.show() }
                    .semantics { contentDescription = "Open date picker" },
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}


