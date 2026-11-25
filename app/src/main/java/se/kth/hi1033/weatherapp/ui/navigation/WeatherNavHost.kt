package se.kth.hi1033.weatherapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.kth.hi1033.weatherapp.ui.screens.DetailScreen
import se.kth.hi1033.weatherapp.ui.screens.FavouritesScreen
import se.kth.hi1033.weatherapp.ui.screens.HomeScreen
import se.kth.hi1033.weatherapp.ui.screens.SettingsScreen
import se.kth.hi1033.weatherapp.ui.state.WeatherUiState
import se.kth.hi1033.weatherapp.ui.viewmodel.WeatherViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector? = null) {
    data object Home : Screen("home", "Forecast", Icons.Default.Home)
    data object Favourites : Screen("favourites", "Favourites", Icons.Default.Favorite)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Detail : Screen("detail/{dayIndex}", "Day Detail") {
        fun createRoute(dayIndex: Int) = "detail/$dayIndex"
    }
}

private val bottomNavItems = listOf(Screen.Home, Screen.Favourites, Screen.Settings)

@Composable
fun WeatherNavHost(
    viewModel: WeatherViewModel,
    uiState: WeatherUiState
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let { Icon(it, contentDescription = screen.label) }
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onSearchModeChange = viewModel::setSearchMode,
                    onCityQueryChange = viewModel::onCityQueryChange,
                    onLatInputChange = viewModel::onLatInputChange,
                    onLonInputChange = viewModel::onLonInputChange,
                    onSubmitSearch = viewModel::submitSearch,
                    onSelectPlace = viewModel::selectPlace,
                    onRefresh = viewModel::refreshCurrentForecast,
                    onToggleFavourite = viewModel::toggleCurrentFavourite,
                    onDayClick = { dayIndex ->
                        navController.navigate(Screen.Detail.createRoute(dayIndex))
                    }
                )
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("dayIndex") { type = NavType.IntType })
            ) { entry ->
                val dayIndex = entry.arguments?.getInt("dayIndex") ?: 0
                val day = uiState.forecast?.dailyForecasts?.getOrNull(dayIndex)
                DetailScreen(
                    day = day,
                    locationName = uiState.forecast?.locationName.orEmpty(),
                    unit = uiState.settings.temperatureUnit,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Favourites.route) {
                FavouritesScreen(
                    favourites = uiState.favourites,
                    onSelectPlace = { place ->
                        viewModel.loadFavourite(place)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onRemovePlace = viewModel::removeFavourite
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    onRefreshIntervalChange = viewModel::updateRefreshInterval,
                    onTemperatureUnitChange = viewModel::updateTemperatureUnit,
                    onDarkThemeChange = viewModel::updateDarkTheme
                )
            }
        }
    }
}
