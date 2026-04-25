package com.misw4203.vinilos.feature.home.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val brush = editorialBrush(seed = "${album.id}${album.title}${album.artist}${album.genre}")

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
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.62f),
                                ),
                            ),
                        ),
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

private fun editorialBrush(seed: String): Brush {
    val palette = listOf(
        listOf(Color(0xFF1F2A37), Color(0xFF0F1115), Color(0xFF7A5CFF)),
        listOf(Color(0xFF30204B), Color(0xFF111117), Color(0xFFB792FF)),
        listOf(Color(0xFF15282D), Color(0xFF0E1215), Color(0xFF47E0C8)),
        listOf(Color(0xFF34261B), Color(0xFF101012), Color(0xFFFFB38A)),
    )
    val index = kotlin.math.abs(seed.hashCode()) % palette.size
    return Brush.radialGradient(
        colors = palette[index],
        center = Offset(500f, 240f),
        radius = 1200f,
    )
}

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





