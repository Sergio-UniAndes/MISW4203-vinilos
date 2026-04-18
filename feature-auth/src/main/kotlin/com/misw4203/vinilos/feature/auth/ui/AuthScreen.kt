package com.misw4203.vinilos.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misw4203.vinilos.core.ui.components.SelectableOptionCard
import com.misw4203.vinilos.core.utils.model.UserRole

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthUiEffect.NavigateHome -> onContinue()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AuthBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            VinilosHeroHeader()

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Select Your Profile",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.2.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(14.dp))

            SelectableOptionCard(
                title = UserRole.VISITOR.displayName,
                subtitle = "Browse the catalog and discover music from around the world.",
                icon = Icons.Outlined.Explore,
                selected = state.selectedRole == UserRole.VISITOR,
                onClick = { viewModel.onRoleSelected(UserRole.VISITOR) },
            )

            SelectableOptionCard(
                title = UserRole.COLLECTOR.displayName,
                subtitle = "Manage your collection, edit records, and track your library.",
                icon = Icons.Outlined.Inventory2,
                selected = state.selectedRole == UserRole.COLLECTOR,
                onClick = { viewModel.onRoleSelected(UserRole.COLLECTOR) },
            )

            Spacer(modifier = Modifier.height(4.dp))

            GradientActionButton(
                text = "Get Started",
                enabled = state.canContinue,
                onClick = viewModel::onGetStarted,
            )

            Text(
                text = "HI-FIDELITY AUDIO EXPERIENCE",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.2.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.34f),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun AuthBackdrop() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A1620),
                        MaterialTheme.colorScheme.background,
                    ),
                    center = Offset(540f, 260f),
                    radius = 1300f,
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp)
                .size(520.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
                .size(420.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
        )
    }
}

@Composable
private fun VinilosHeroHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2231)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                )
            }
        }

        Text(
            text = "VINILOS",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "THE DIGITAL CURATOR",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 4.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
        )
    }
}

@Composable
private fun GradientActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(50)
    val backgroundBrush = if (enabled) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFCEB7FF),
                Color(0xFFB38AFF),
                Color(0xFF6A18D8),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4C415C),
                Color(0xFF332B3E),
            ),
        )
    }
    val contentColor = if (enabled) Color(0xFF2B1486) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(buttonShape)
            .background(backgroundBrush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}
