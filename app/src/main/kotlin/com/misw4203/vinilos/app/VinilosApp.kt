package com.misw4203.vinilos.app

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.misw4203.vinilos.core.navigation.AppRoute
import com.misw4203.vinilos.core.ui.theme.VinilosTheme
import com.misw4203.vinilos.feature.home.ui.AlbumDetailScreen
import com.misw4203.vinilos.feature.home.ui.AlbumDetailViewModel
import com.misw4203.vinilos.feature.auth.ui.AuthScreen
import com.misw4203.vinilos.feature.auth.ui.AuthViewModel
import com.misw4203.vinilos.feature.home.ui.ArtistDetailScreen
import com.misw4203.vinilos.feature.home.ui.ArtistDetailViewModel
import com.misw4203.vinilos.feature.home.ui.ArtistsScreen
import com.misw4203.vinilos.feature.home.ui.ArtistsViewModel
import com.misw4203.vinilos.feature.home.ui.HomeScreen
import com.misw4203.vinilos.feature.home.ui.HomeViewModel

@Composable
fun VinilosApp(appContainer: AppContainer) {
    VinilosTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            VinilosNavHost(appContainer = appContainer)
        }
    }
}

@Composable
private fun VinilosNavHost(appContainer: AppContainer) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.Bootstrap,
    ) {
        composable(AppRoute.Bootstrap) {
            val viewModel: BootstrapViewModel = viewModel(factory = appContainer.bootstrapViewModelFactory())
            BootstrapRoute(
                viewModel = viewModel,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(AppRoute.Bootstrap) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoute.Auth) {
            val viewModel: AuthViewModel = viewModel(factory = appContainer.authViewModelFactory())
            AuthScreen(
                viewModel = viewModel,
                onContinue = {
                    navController.navigate(AppRoute.Home) {
                        launchSingleTop = true
                        popUpTo(AppRoute.Auth) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoute.Home) {
            val viewModel: HomeViewModel = viewModel(factory = appContainer.homeViewModelFactory())
            val artistsViewModel: ArtistsViewModel = viewModel(factory = appContainer.artistsViewModelFactory())
            HomeScreen(
                viewModel = viewModel,
                onBackToAuth = {
                    navController.navigate(AppRoute.Auth) {
                        popUpTo(AppRoute.Home) { inclusive = true }
                    }
                },
                onAlbumClick = { item ->
                    navController.navigate("${AppRoute.AlbumDetail}/${Uri.encode(item.id)}")
                },
                onCreateAlbum = {
                    navController.navigate(AppRoute.CreateAlbum)
                },
                content = {
                    ArtistsScreen(
                        viewModel = artistsViewModel,
                        onArtistClick = { artist ->
                            navController.navigate("${AppRoute.ArtistDetail}/${artist.id}")
                        },
                    )
                },
            )
        }

        composable(
            route = "${AppRoute.AlbumDetail}/{${AppRoute.AlbumDetailArg}}",
            arguments = listOf(
                navArgument(AppRoute.AlbumDetailArg) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString(AppRoute.AlbumDetailArg).orEmpty()
            val viewModel: AlbumDetailViewModel = viewModel(
                factory = appContainer.albumDetailViewModelFactory(albumId),
            )
            AlbumDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "${AppRoute.ArtistDetail}/{${AppRoute.ArtistDetailArg}}",
            arguments = listOf(
                navArgument(AppRoute.ArtistDetailArg) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getLong(AppRoute.ArtistDetailArg) ?: 0L
            val viewModel: ArtistDetailViewModel = viewModel(
                factory = appContainer.artistDetailViewModelFactory(artistId),
            )
            ArtistDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAlbumClick = { albumId ->
                    navController.navigate("${AppRoute.AlbumDetail}/${Uri.encode(albumId.toString())}")
                },
            )
        }

        composable(AppRoute.CreateAlbum) {
            val viewModel: com.misw4203.vinilos.feature.home.ui.CreateAlbumViewModel = viewModel(factory = appContainer.createAlbumViewModelFactory())
            com.misw4203.vinilos.feature.home.ui.CreateAlbumScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCreated = {
                    // Ensure Home is refreshed: pop back then navigate to Home to recreate the route
                    navController.popBackStack()
                    navController.navigate(AppRoute.Home) {
                        launchSingleTop = true
                        popUpTo(AppRoute.Home) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun BootstrapRoute(
    viewModel: BootstrapViewModel,
    onNavigate: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.targetRoute) {
        state.targetRoute?.let(onNavigate)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
