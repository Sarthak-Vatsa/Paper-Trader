package com.papertrader.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.papertrader.app.presentation.account.AccountScreen
import com.papertrader.app.presentation.auth.AuthViewModel
import com.papertrader.app.presentation.auth.LoginScreen
import com.papertrader.app.presentation.auth.RegisterScreen
import com.papertrader.app.presentation.order.OrderScreen
import com.papertrader.app.presentation.portfolio.PortfolioScreen
import com.papertrader.app.presentation.watchlist.WatchlistScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Watchlist : Screen("watchlist")
    object Portfolio : Screen("portfolio")
    object Account : Screen("account")
    object Order : Screen("order/{ticker}") {
        fun createRoute(ticker: String) = "order/$ticker"
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun PaperTraderNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavScreens = listOf(Screen.Watchlist.route, Screen.Portfolio.route, Screen.Account.route)
    val showBottomBar = currentDestination?.route in bottomNavScreens

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Watchlist, "Watchlist", Icons.AutoMirrored.Filled.List),
        BottomNavItem(Screen.Portfolio, "Portfolio", Icons.Default.PieChart),
        BottomNavItem(Screen.Account, "Account", Icons.Default.AccountCircle),
    )

    val startDestination = if (authViewModel.uiState.value.isAuthenticated) {
        Screen.Watchlist.route
    } else {
        Screen.Login.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Watchlist.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.navigateUp() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Watchlist.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    onNavigateToOrder = { ticker ->
                        navController.navigate(Screen.Order.createRoute(ticker))
                    }
                )
            }
            composable(Screen.Portfolio.route) {
                PortfolioScreen(
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToOrder = { ticker ->
                        navController.navigate(Screen.Order.createRoute(ticker))
                    }
                )
            }
            composable(Screen.Account.route) {
                AccountScreen(
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Order.route) { backStackEntry ->
                val ticker = backStackEntry.arguments?.getString("ticker") ?: return@composable
                OrderScreen(
                    ticker = ticker,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
