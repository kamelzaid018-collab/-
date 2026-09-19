package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.media.SoundHelper
import com.example.ui.components.MosqueEmeraldDark
import com.example.ui.components.MosqueEmeraldPanel
import com.example.ui.components.MosqueGold
import com.example.ui.language.AppStrings
import com.example.ui.language.LocaleManager
import com.example.ui.navigation.AppScreen
import com.example.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAppContainer(viewModel: PrayerViewModel) {
    val lang by viewModel.settingsRepo.language.collectAsState()
    val layoutDirection = LocaleManager.getLayoutDirection(lang)

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        SoundHelper.ensureDefaultSounds(context)
    }

    // Handle back button: if not on HOME, return to HOME, else close app
    BackHandler(enabled = currentScreen != AppScreen.HOME || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentScreen = AppScreen.HOME
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MosqueEmeraldDark,
                    modifier = Modifier.width(310.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MosqueEmeraldPanel)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "🕌 " + AppStrings.bismillah(lang),
                            color = MosqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang.isRtl) "مواقيت الصلاة والأذان" else "Prayer Times & Azan",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    HorizontalDivider(color = MosqueGold.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    DrawerNavigationItem(
                        icon = Icons.Default.Home,
                        label = AppStrings.tabMain(lang),
                        isSelected = currentScreen == AppScreen.HOME,
                        onClick = {
                            currentScreen = AppScreen.HOME
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Place,
                        label = AppStrings.tabLocationAndCalc(lang),
                        isSelected = currentScreen == AppScreen.LOCATION_CALC,
                        onClick = {
                            currentScreen = AppScreen.LOCATION_CALC
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Notifications,
                        label = AppStrings.tabAlerts(lang),
                        isSelected = currentScreen == AppScreen.ALERTS,
                        onClick = {
                            currentScreen = AppScreen.ALERTS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.VolumeUp,
                        label = AppStrings.tabAzan(lang),
                        isSelected = currentScreen == AppScreen.AZAN_SETTINGS,
                        onClick = {
                            currentScreen = AppScreen.AZAN_SETTINGS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Star,
                        label = AppStrings.tabRamadan(lang),
                        isSelected = currentScreen == AppScreen.RAMADAN,
                        onClick = {
                            currentScreen = AppScreen.RAMADAN
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Favorite,
                        label = AppStrings.tabSalawat(lang),
                        isSelected = currentScreen == AppScreen.SALAWAT,
                        onClick = {
                            currentScreen = AppScreen.SALAWAT
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Widgets,
                        label = AppStrings.tabNotificationAndWidgets(lang),
                        isSelected = currentScreen == AppScreen.NOTIFICATIONS_AND_WIDGETS,
                        onClick = {
                            currentScreen = AppScreen.NOTIFICATIONS_AND_WIDGETS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Language,
                        label = AppStrings.tabLanguage(lang),
                        isSelected = currentScreen == AppScreen.LANGUAGE,
                        onClick = {
                            currentScreen = AppScreen.LANGUAGE
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerNavigationItem(
                        icon = Icons.Default.Security,
                        label = AppStrings.tabPermissions(lang),
                        isSelected = currentScreen == AppScreen.PERMISSIONS,
                        onClick = {
                            currentScreen = AppScreen.PERMISSIONS
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            when (currentScreen) {
                AppScreen.HOME -> MainMosqueClockScreen(
                    viewModel = viewModel,
                    onOpenMenu = { coroutineScope.launch { drawerState.open() } },
                    onOpenLocation = { currentScreen = AppScreen.LOCATION_CALC },
                    onOpenAlerts = { currentScreen = AppScreen.ALERTS },
                    onOpenAzan = { currentScreen = AppScreen.AZAN_SETTINGS },
                    onOpenRamadan = { currentScreen = AppScreen.RAMADAN },
                    onOpenSalawat = { currentScreen = AppScreen.SALAWAT },
                    onSwitchToMosqueClockView = { currentScreen = AppScreen.MOSQUE_CLOCK }
                )
                AppScreen.MOSQUE_CLOCK -> MosqueElectronicClockScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.LOCATION_CALC -> LocationAndCalcScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.ALERTS -> AlertsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.AZAN_SETTINGS -> AzanSettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.RAMADAN -> RamadanScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.SALAWAT -> SalawatScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.NOTIFICATIONS_AND_WIDGETS -> NotificationAndWidgetsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.LANGUAGE -> LanguageScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.PERMISSIONS -> PermissionsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                )
            }
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = if (isSelected) MosqueGold else Color.LightGray) },
        label = {
            Text(
                text = label,
                color = if (isSelected) MosqueGold else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFF0C3A32),
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
