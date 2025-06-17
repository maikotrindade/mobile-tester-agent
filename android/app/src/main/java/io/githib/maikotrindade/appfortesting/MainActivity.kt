package io.githib.maikotrindade.appfortesting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.githib.maikotrindade.appfortesting.ui.screen.feed.FeedScreen
import io.githib.maikotrindade.appfortesting.ui.screen.notification.NotificationScreen
import io.githib.maikotrindade.appfortesting.ui.screen.profile.ProfileScreen
import io.githib.maikotrindade.appfortesting.ui.screen.search.SearchScreen
import io.githib.maikotrindade.appfortesting.ui.theme.AppForTestingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppForTestingTheme {
                MainContent()
            }
        }
    }
}

@Composable
private fun MainContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    var showSnackbarMessage by remember { mutableStateOf<String?>(null) }
    val accountDeletedSuccess = stringResource(id = R.string.account_deleted_success)

    Scaffold(
        topBar = { InstagramTopBar(navController) },
        bottomBar = { InstagramBottomBar(navController) },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        showSnackbarMessage?.let { message ->
            LaunchedEffect(message) {
                snackbarHostState.showSnackbar(message)
                showSnackbarMessage = null
            }
        }
        NavHost(
            navController = navController,
            startDestination = "feed",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable("feed") { FeedScreen() }
            composable("search") { SearchScreen() }
            composable("notification") { NotificationScreen() }
            composable("profile") {
                ProfileScreen(
                    onSettingClick = {},
                    onDeleteAccount = {
                        showSnackbarMessage = accountDeletedSuccess
                        navController.navigate("feed") {
                            popUpTo("feed") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramTopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontFamily = FontFamily.Cursive,
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = { navController.navigate("notification") }) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Likes",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
            Box {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun InstagramBottomBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = currentRoute == "feed",
            onClick = { navController.navigate("feed") {
                popUpTo("feed") { inclusive = true }
                launchSingleTop = true
            } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = currentRoute == "search",
            onClick = { navController.navigate("search") {
                popUpTo("feed")
                launchSingleTop = true
            } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = R.string.reels),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = stringResource(id = R.string.profile),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") {
                popUpTo("feed")
                launchSingleTop = true
            } },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
    }
}