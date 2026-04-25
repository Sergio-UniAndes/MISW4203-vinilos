package com.misw4203.vinilos.feature.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misw4203.vinilos.core.ui.components.VinilosBottomNavItem
import com.misw4203.vinilos.core.ui.components.VinilosBottomNavigationBar
import com.misw4203.vinilos.core.ui.components.VinilosFilterChip
import com.misw4203.vinilos.core.ui.components.VinilosTopBar
import com.misw4203.vinilos.feature.home.domain.model.HomeItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBackToAuth: () -> Unit,
    onAlbumClick: (HomeItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomNavItems = remember {
        listOf(
            VinilosBottomNavItem(HomeTab.ALBUMS.name, HomeTab.ALBUMS.label, Icons.Outlined.Search),
            VinilosBottomNavItem(HomeTab.ARTISTS.name, HomeTab.ARTISTS.label, Icons.Outlined.Person),
            VinilosBottomNavItem(HomeTab.COLLECTORS.name, HomeTab.COLLECTORS.label, Icons.Outlined.Person),
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                HomeUiEffect.NavigateAuth -> onBackToAuth()
            }
        }
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            VinilosTopBar(
                title = "Vinilos",
                onBackClick = viewModel::onChangeProfile,
                onSearchClick = viewModel::onSearchClick,
            )
        },
        bottomBar = {
            VinilosBottomNavigationBar(
                items = bottomNavItems,
                selectedKey = state.selectedTab.name,
                onItemSelected = { selectedKey ->
                    viewModel.onTabSelected(HomeTab.valueOf(selectedKey))
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state.selectedTab) {
                HomeTab.ALBUMS -> AlbumsEditorialFeed(
                    state = state,
                    onFilterSelected = viewModel::onFilterSelected,
                    onEditItem = viewModel::onEditItem,
                    onDeleteItem = viewModel::onDeleteItem,
                    onAlbumClick = onAlbumClick,
                )

                HomeTab.ARTISTS -> ComingSoonSection(title = "Artists")
                HomeTab.COLLECTORS -> ComingSoonSection(title = "Collectors")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumsEditorialFeed(
    state: HomeUiState,
    onFilterSelected: (HomeFilter) -> Unit,
    onEditItem: (HomeItem) -> Unit,
    onDeleteItem: (HomeItem) -> Unit,
    onAlbumClick: (HomeItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Albums",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }

                    Text(
                        text = "${state.totalCount} TOTAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    )
                }

                if (state.permissions.canCreate) {
                    Text(
                        text = "Collector mode · Full management enabled",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = "Visitor mode · Browse the editorial catalog",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                HomeFilter.entries.forEach { filter ->
                    VinilosFilterChip(
                        text = filter.label,
                        selected = state.selectedFilter == filter,
                        onClick = { onFilterSelected(filter) },
                    )
                }
            }
        }

        state.featuredItem?.let { featuredItem ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeaturedAlbumCard(
                    modifier = Modifier.clickable { onAlbumClick(featuredItem) },
                    itemTitle = featuredItem.title,
                    artist = featuredItem.artist,
                    genre = featuredItem.genre,
                    year = featuredItem.year,
                    canEdit = state.permissions.canEdit,
                    canDelete = state.permissions.canDelete,
                    onEdit = { onEditItem(featuredItem) },
                    onDelete = { onDeleteItem(featuredItem) },
                )
            }
        }

        items(state.gridItems, key = { it.id }) { item ->
            AlbumTileCard(
                modifier = Modifier.clickable { onAlbumClick(item) },
                title = item.title,
                artist = item.artist,
                genre = item.genre,
                year = item.year,
                canEdit = state.permissions.canEdit,
                canDelete = state.permissions.canDelete,
                onEdit = { onEditItem(item) },
                onDelete = { onDeleteItem(item) },
            )
        }
    }
}

@Composable
private fun FeaturedAlbumCard(
    modifier: Modifier = Modifier,
    itemTitle: String,
    artist: String,
    genre: String,
    year: Int,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val brush = editorialBrush(seed = "$itemTitle$artist$genre$year")
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(312.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(brush),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "FEATURED RELEASE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = itemTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TagPill(text = genre)
                TagPill(text = year.toString())
            }

            if (canEdit || canDelete) {
                ActionRow(canEdit = canEdit, canDelete = canDelete, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun AlbumTileCard(
    modifier: Modifier = Modifier,
    title: String,
    artist: String,
    genre: String,
    year: Int,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val brush = editorialBrush(seed = "$title$artist$genre$year")
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(brush),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = genre.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (canEdit || canDelete) {
            ActionRow(canEdit = canEdit, canDelete = canDelete, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
private fun ActionRow(
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (canEdit) {
            MiniActionPill(text = "Edit", onClick = onEdit)
        }
        if (canDelete) {
            MiniActionPill(text = "Delete", onClick = onDelete)
        }
    }
}

@Composable
private fun MiniActionPill(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun TagPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ComingSoonSection(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "This section is ready for the next iteration.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
    val colors = palette[index]
    return Brush.linearGradient(colors = colors)
}
