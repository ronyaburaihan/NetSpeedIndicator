package com.englesoft.netspeedindicator.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.englesoft.netspeedindicator.R
import com.englesoft.netspeedindicator.presentation.navigation.ScreenRoute


@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    containerColor: Color = MaterialTheme.colorScheme.background,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unSelectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val items = listOf(
        AppBottomNavItem.Home,
        AppBottomNavItem.History,
        AppBottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    Surface(
        color = containerColor,
        shadowElevation = 4.dp
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route::class.qualifiedName
                NavigationBarItem(
                    modifier = Modifier.weight(1f),
                    selected = isSelected,
                    label = {
                        Text(text = stringResource(item.label))
                    },
                    icon = {
                        Icon(
                            if (currentRoute == item.route::class.qualifiedName) {
                                item.selectedIcon
                            } else {
                                item.unselectedIcon
                            },
                            contentDescription = stringResource(item.label),
                        )
                    },
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigationItem(
    modifier: Modifier = Modifier,
    item: AppBottomNavItem,
    isSelected: Boolean,
    unSelectedContentColor: Color,
    selectedContentColor: Color,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.10f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    val textStyle = if (isSelected) MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Medium, color = selectedContentColor
    ) else MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Normal, color = unSelectedContentColor
    )

    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Icon(
            imageVector = (if (isSelected) item.selectedIcon else item.unselectedIcon),
            contentDescription = stringResource(item.label),
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(item.label),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = textStyle
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

sealed class AppBottomNavItem(
    val label: Int,
    val route: ScreenRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    object Home : AppBottomNavItem(
        label = R.string.home,
        route = ScreenRoute.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    object History : AppBottomNavItem(
        label = R.string.history,
        route = ScreenRoute.History,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
    )

    object Settings : AppBottomNavItem(
        label = R.string.settings,
        route = ScreenRoute.Settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    )
}