package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.feature.home.domain.model.HomeItem

enum class HomeFilter(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    CLASSICAL("Classical"),
    SALSA("Salsa"),
    ROCK("Rock"),
    FOLK("Folk"),
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

    val availableFilters: List<HomeFilter>
        get() = HomeFilter.entries.filter { filter ->
            when (filter) {
                HomeFilter.RECENTLY_ADDED -> items.isNotEmpty()
                HomeFilter.CLASSICAL -> hasGenre("Classical")
                HomeFilter.SALSA -> hasGenre("Salsa")
                HomeFilter.ROCK -> hasGenre("Rock")
                HomeFilter.FOLK -> hasGenre("Folk")
            }
        }

    val activeFilter: HomeFilter
        get() = if (availableFilters.contains(selectedFilter)) {
            selectedFilter
        } else {
            availableFilters.firstOrNull() ?: HomeFilter.RECENTLY_ADDED
        }

    val filteredItems: List<HomeItem>
        get() = when (activeFilter) {
            HomeFilter.RECENTLY_ADDED -> items.sortedByDescending(HomeItem::year)
            HomeFilter.CLASSICAL -> items
                .filter { it.genre.equals("Classical", ignoreCase = true) }
                .sortedByDescending(HomeItem::year)
            HomeFilter.SALSA -> items
                .filter { it.genre.equals("Salsa", ignoreCase = true) }
                .sortedByDescending(HomeItem::year)
            HomeFilter.ROCK -> items
                .filter { it.genre.equals("Rock", ignoreCase = true) }
                .sortedByDescending(HomeItem::year)
            HomeFilter.FOLK -> items
                .filter { it.genre.equals("Folk", ignoreCase = true) }
                .sortedByDescending(HomeItem::year)
        }

    val featuredItem: HomeItem?
        get() = filteredItems.firstOrNull()

    val gridItems: List<HomeItem>
        get() = filteredItems.drop(1)

    val totalCount: Int
        get() = filteredItems.size

    private fun hasGenre(genre: String): Boolean {
        return items.any { it.genre.equals(genre, ignoreCase = true) }
    }
}
