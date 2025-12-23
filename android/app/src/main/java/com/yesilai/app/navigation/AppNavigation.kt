package com.yesilai.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.yesilai.app.ui.screens.*
import com.yesilai.app.ui.theme.YesilPrimary
import com.yesilai.app.ui.theme.YesilTextSecondary

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object MainTabs : Screen("main_tabs")
    object ProfileCompletion : Screen("profile_completion")
}

sealed class TabScreen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Chat : TabScreen("chat", "Sohbet", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Profile : TabScreen("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
    object Settings : TabScreen("settings", "Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) Screen.MainTabs.route else Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToProfileCompletion = {
                    navController.navigate(Screen.ProfileCompletion.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ProfileCompletion.route) {
            ProfileCompletionScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.ProfileCompletion.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.MainTabs.route) {
            MainTabsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(onLogout: () -> Unit) {
    val tabNavController = rememberNavController()
    val tabs = listOf(TabScreen.Chat, TabScreen.Profile, TabScreen.Settings)
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp
                            )
                        },
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = YesilPrimary,
                            selectedTextColor = YesilPrimary,
                            unselectedIconColor = YesilTextSecondary,
                            unselectedTextColor = YesilTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = TabScreen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TabScreen.Chat.route) {
                ChatScreen(onLogout = onLogout)
            }
            composable(TabScreen.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
            composable(TabScreen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
