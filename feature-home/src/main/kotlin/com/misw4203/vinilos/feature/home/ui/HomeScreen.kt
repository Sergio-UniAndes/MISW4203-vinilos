package com.misw4203.vinilos.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misw4203.vinilos.core.ui.components.VinylItemCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBackToAuth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                HomeUiEffect.NavigateAuth -> onBackToAuth()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (state.permissions.canCreate) {
                FloatingActionButton(onClick = viewModel::onCreateItem) {
                    Text("+")
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Role: ${state.session?.role?.displayName ?: "Unknown"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (state.permissions.canCreate) "Collector mode" else "Visitor mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::onChangeProfile,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Change profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    VinylItemCard(
                        title = item.title,
                        subtitle = "${item.artist} • ${item.year}",
                        canEdit = state.permissions.canEdit,
                        canDelete = state.permissions.canDelete,
                        onEdit = { viewModel.onEditItem(item) },
                        onDelete = { viewModel.onDeleteItem(item) },
                    )
                }
            }
        }
    }
}

