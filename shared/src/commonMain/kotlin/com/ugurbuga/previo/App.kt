package com.ugurbuga.previo

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.ugurbuga.previo.runtime.Previo
import com.ugurbuga.previo.runtime.PrevioOverlay
import com.ugurbuga.previo.runtime.PrevioConfig
import com.ugurbuga.previo.runtime.PrevioColors
import com.ugurbuga.previo.runtime.PrevioColorConstants
import com.ugurbuga.previo.runtime.PrevioDefaults
import com.ugurbuga.previo.theme.AppTheme

sealed interface AppScreen {
    data object Home : AppScreen
    data object Settings : AppScreen
    data object Profile : AppScreen
    data object Checkout : AppScreen
    data object Crypto : AppScreen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode")
fun App() {
    AppTheme {
        val isDark = isSystemInDarkTheme()
        val colorScheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography

        val defaultLightConfig = remember(colorScheme, typography) {
            PrevioConfig(
                title = "Previo Component Catalog",
                colorScheme = colorScheme,
                typography = typography,
                isDarkTheme = false
            )
        }

        var activeConfig by remember { mutableStateOf(defaultLightConfig) }
        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

        Scaffold(
            topBar = {
                if (currentScreen != AppScreen.Home) {
                    TopAppBar(
                        title = {
                            Text(
                                when (currentScreen) {
                                    AppScreen.Settings -> "Settings Screen"
                                    AppScreen.Profile -> "Profile Screen"
                                    AppScreen.Checkout -> "Checkout Screen"
                                    AppScreen.Crypto -> "Crypto Dashboard"
                                    else -> ""
                                }
                            )
                        },
                        navigationIcon = {
                            TextButton(onClick = { currentScreen = AppScreen.Home }) {
                                Text("← Back")
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.Home -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Previo Demo & Navigator",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // 3 Previo Launch Buttons using PrevioColorConstants
                            Button(
                                onClick = {
                                    activeConfig = PrevioConfig(
                                        title = "Previo (Custom Color)",
                                        colors = PrevioColors(
                                            screenBackground = PrevioColorConstants.CustomDarkBg,
                                            cardBackground = PrevioColorConstants.CustomDarkBg,
                                            primaryText = PrevioColorConstants.CustomLightText,
                                            secondaryText = PrevioColorConstants.CustomSubText,
                                            dividerOrBorder = PrevioColorConstants.CustomLightText,
                                            chipSelectedBackground = PrevioColorConstants.CustomAccent,
                                            chipSelectedText = PrevioColorConstants.CustomLightText,
                                            iconTint = PrevioColorConstants.CustomLightText,
                                            badgeBackground = PrevioColorConstants.CustomAccent,
                                            badgeText = PrevioColorConstants.CustomLightText
                                        ),
                                        colorScheme = colorScheme,
                                        typography = typography,
                                        isDarkTheme = true
                                    )
                                    Previo.launch()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Previo (Custom Color)")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    activeConfig = PrevioConfig(
                                        title = "Previo (Default Dark Color)",
                                        colors = PrevioDefaults.DarkColors,
                                        colorScheme = colorScheme,
                                        typography = typography,
                                        isDarkTheme = true
                                    )
                                    Previo.launch()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Previo (Default Dark Color)")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    activeConfig = PrevioConfig(
                                        title = "Previo (Default Light Color)",
                                        colors = PrevioDefaults.LightColors,
                                        colorScheme = colorScheme,
                                        typography = typography,
                                        isDarkTheme = false
                                    )
                                    Previo.launch()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Previo (Default Light Color)")
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Direct Individual Screen Navigation",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Button(
                                        onClick = { currentScreen = AppScreen.Settings },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Open Settings Screen Preview")
                                    }
                                }
                                item {
                                    Button(
                                        onClick = { currentScreen = AppScreen.Profile },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Open Profile Screen Preview")
                                    }
                                }
                                item {
                                    Button(
                                        onClick = { currentScreen = AppScreen.Checkout },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Open Checkout Screen Preview")
                                    }
                                }
                                item {
                                    Button(
                                        onClick = { currentScreen = AppScreen.Crypto },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Open Crypto Dashboard Preview")
                                    }
                                }
                            }
                        }
                    }

                    AppScreen.Settings -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            LongSettingsScreenPreview()
                        }
                    }

                    AppScreen.Profile -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            DetailedProfileScreenPreview()
                        }
                    }

                    AppScreen.Checkout -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            EcommerceCheckoutScreenPreview()
                        }
                    }

                    AppScreen.Crypto -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            CryptoDashboardScreenPreview()
                        }
                    }
                }
            }
            PrevioOverlay(config = activeConfig) {}
        }
    }
}
