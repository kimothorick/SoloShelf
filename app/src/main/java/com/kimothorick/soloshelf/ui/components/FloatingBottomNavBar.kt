package com.kimothorick.soloshelf.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kimothorick.soloshelf.R
import com.kimothorick.soloshelf.navigation.MainDestination
import com.kimothorick.soloshelf.navigation.Route
import com.kimothorick.soloshelf.ui.theme.SoloShelfTheme
import com.kimothorick.soloshelf.ui.tooling.ComponentPreviews

@Composable
fun FloatingBottomNavBar(
    destinations: List<MainDestination>,
    currentRoute: Route,
    onDestinationSelected: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ButtonDefaults.filledTonalButtonColors()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        SoloShelfTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                        SoloShelfTheme.colorScheme.surfaceContainer,
                    ),
                ),
            ).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = colors.containerColor,
            modifier = Modifier.wrapContentSize(),
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = currentRoute == destination.route
                    val backgroundColor by animateColorAsState(
                        targetValue = if (selected) colors.contentColor else Color.Transparent,
                        label = "NavPillBackground",
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) colors.containerColor else colors.contentColor,
                        label = "NavPillContentColor",
                    )

                    Surface(
                        onClick = { onDestinationSelected(destination.route) },
                        shape = CircleShape,
                        color = backgroundColor,
                        modifier = Modifier.height(48.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = if (selected) 16.dp else 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.labelRes),
                                tint = contentColor,
                                modifier = Modifier.size(24.dp),
                            )
                            AnimatedVisibility(
                                visible = selected,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally(),
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(destination.labelRes),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = contentColor,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ComponentPreviews
@Composable
private fun FloatingBottomNavBarPreview() {
    SoloShelfTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoloShelfTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val destinations = listOf(
                MainDestination(Route.Library, R.string.library, Icons.AutoMirrored.Filled.MenuBook),
                MainDestination(Route.Search, R.string.search, Icons.Filled.Search),
                MainDestination(Route.Bookshelf, R.string.bookshelf, Icons.Filled.BarChart),
            )
            FloatingBottomNavBar(
                destinations = destinations,
                currentRoute = Route.Library,
                onDestinationSelected = {},
            )
        }
    }
}
