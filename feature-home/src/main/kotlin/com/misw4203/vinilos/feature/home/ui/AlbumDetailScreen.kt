package com.misw4203.vinilos.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.misw4203.vinilos.core.ui.components.VinilosTopBar
import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.model.AlbumPerformer
import com.misw4203.vinilos.feature.home.domain.model.AlbumTrack
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AlbumDetailScreen(
    viewModel: AlbumDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val album = state.album

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AlbumDetailUiEffect.TrackAdded -> { /* track added — list will refresh on next open */ }
            }
        }
    }

    if (state.showAddTrackDialog) {
        AddTrackDialog(
            albumTitle = album?.title ?: "",
            trackName = state.trackName,
            trackDuration = state.trackDuration,
            isAdding = state.isAddingTrack,
            error = state.addTrackError,
            onTrackNameChange = viewModel::onTrackNameChange,
            onTrackDurationChange = viewModel::onTrackDurationChange,
            onDismiss = viewModel::onDismissAddTrack,
            onConfirm = viewModel::onConfirmAddTrack,
        )
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            VinilosTopBar(
                title = album?.title ?: "Album detail",
                onBackClick = onBack,
                onSearchClick = { },
            )
        },
        floatingActionButton = {
            if (state.canCreate) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::onAddTrackClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                        )
                    },
                    text = { Text(text = "Add Track") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag(ADD_TRACK_FAB_TAG),
                )
            }
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (album == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Album not found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Go back and try another record.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DetailHeroCard(album = album)
            AlbumMetaPanel(album = album)
            DescriptionSection(album = album)
            TracksSection(tracks = album.tracks)
            PerformersSection(performers = album.performers)
            CommentsSection(comments = album.comments)
            ShareAction()
        }
    }
}

@Composable
private fun DetailHeroCard(album: HomeItem) {
    val brush = remember(album.id) { editorialRadialBrush(seed = "${album.id}${album.title}${album.artist}${album.genre}") }
    val overlayBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.62f),
            ),
        )
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(brush),
            ) {
                album.coverUrl?.takeIf { it.isNotBlank() }?.let { coverUrl ->
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayBrush),
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(18.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.White,
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = album.genre.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailChip(text = album.recordLabel ?: "Collector's Edition")
                DetailChip(text = formatReleaseDate(album.releaseDate, album.year))
            }
        }
    }
}

@Composable
private fun AlbumMetaPanel(album: HomeItem) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetaCell(
                    label = "Released",
                    value = formatReleaseDate(album.releaseDate, album.year),
                    modifier = Modifier.fillMaxWidth(0.48f),
                )
                MetaCell(
                    label = "Genre",
                    value = album.genre,
                    modifier = Modifier.fillMaxWidth(0.48f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetaCell(
                    label = "Label",
                    value = album.recordLabel ?: "Unknown",
                    modifier = Modifier.fillMaxWidth(0.48f),
                )
                MetaCell(
                    label = "Format",
                    value = album.format ?: "Album",
                    modifier = Modifier.fillMaxWidth(0.48f),
                )
            }
        }
    }
}

@Composable
private fun MetaCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ShareAction() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(54.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun DescriptionSection(album: HomeItem) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "About this release",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = album.description ?: "No description available.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TracksSection(tracks: List<AlbumTrack>) {
    if (tracks.isEmpty()) return

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Tracks",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            tracks.forEachIndexed { index, track ->
                Text(
                    text = "${index + 1}. ${track.name} - ${track.duration}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PerformersSection(performers: List<AlbumPerformer>) {
    if (performers.isEmpty()) return

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Performers",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            performers.forEach { performer ->
                Text(
                    text = performer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CommentsSection(comments: List<AlbumComment>) {
    if (comments.isEmpty()) return

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            comments.forEach { comment ->
                Text(
                    text = "${comment.rating}/5 - ${comment.description}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
internal fun AddTrackDialog(
    albumTitle: String,
    trackName: String,
    trackDuration: String,
    isAdding: Boolean,
    error: String?,
    onTrackNameChange: (String) -> Unit,
    onTrackDurationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!isAdding) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Icon header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LibraryAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                // Titles
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Add Track to Album",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (albumTitle.isNotBlank()) {
                        Text(
                            text = albumTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Track name field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "SEARCH TRACKS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TrackInputPill(
                        value = trackName,
                        onValueChange = onTrackNameChange,
                        placeholder = "Search for a track...",
                        leadingIcon = true,
                        testTag = ADD_TRACK_NAME_TAG,
                    )

                    // Duration field
                    Text(
                        text = "DURATION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TrackInputPill(
                        value = trackDuration,
                        onValueChange = onTrackDurationChange,
                        placeholder = "e.g. 3:45",
                        leadingIcon = false,
                        testTag = ADD_TRACK_DURATION_TAG,
                    )

                    if (error != null) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isAdding,
                    ) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = !isAdding && trackName.isNotBlank(),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.testTag(ADD_TRACK_CONFIRM_TAG),
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(text = "Add Track")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackInputPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val containerColor = if (isFocused) {
        MaterialTheme.colorScheme.surfaceBright
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (leadingIcon) {
                androidx.compose.foundation.Image(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconColor),
                    modifier = Modifier.size(20.dp),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

const val ADD_TRACK_FAB_TAG = "add_track_fab"
const val ADD_TRACK_NAME_TAG = "add_track_name_field"
const val ADD_TRACK_DURATION_TAG = "add_track_duration_field"
const val ADD_TRACK_CONFIRM_TAG = "add_track_confirm_button"

private fun formatReleaseDate(releaseDate: String?, fallbackYear: Int): String {
    if (releaseDate.isNullOrBlank()) {
        return fallbackYear.takeIf { it > 0 }?.toString() ?: "Unknown"
    }

    return runCatching {
        Instant.parse(releaseDate)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()))
    }.getOrElse {
        fallbackYear.takeIf { it > 0 }?.toString() ?: releaseDate.take(10)
    }
}





