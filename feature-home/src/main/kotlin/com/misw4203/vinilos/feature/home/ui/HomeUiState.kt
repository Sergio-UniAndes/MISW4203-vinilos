package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.feature.home.domain.model.HomeItem

enum class HomeFilter(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    ROCK("Rock"),
    JAZZ("Jazz"),
}

enum class HomeTab(val label: String) {
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    COLLECTORS("Collectors"),
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val session: UserSession? = null,
    val items: List<HomeItem> = emptyList(),
    val selectedFilter: HomeFilter = HomeFilter.RECENTLY_ADDED,
    val selectedTab: HomeTab = HomeTab.ALBUMS,
) {
    val permissions: RolePermissions
        get() = session?.permissions ?: RolePermissions()

    val filteredItems: List<HomeItem>
        get() = when (selectedFilter) {
            HomeFilter.RECENTLY_ADDED -> items.sortedByDescending(HomeItem::year)
            HomeFilter.ROCK -> items
                .filter { it.genre.equals("Rock", ignoreCase = true) }
                .sortedByDescending(HomeItem::year)
            HomeFilter.JAZZ -> items
                .filter { it.genre.equals("Jazz", ignoreCase = true) }
                .sortedByDescending(HomeItem::year)
        }

    val featuredItem: HomeItem?
        get() = filteredItems.firstOrNull()

    val gridItems: List<HomeItem>
        get() = filteredItems.drop(1)

    val totalCount: Int
        get() = filteredItems.size
}
