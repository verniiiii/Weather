package com.example.myweather.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    title: String,
    onThemeChanged: (Boolean) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    var darkTheme by remember { mutableStateOf(true) }
    var expandedMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Получаем контекст
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                Divider()

                // Пункт меню для перехода на экран "Погода"
                NavigationDrawerItem(
                    label = { Text("Погода") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("weather")
                    }
                )

                // Пункт меню для перехода на экран "Настройки"
                NavigationDrawerItem(
                    label = { Text("Настройки") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    }
                )

                // Пункт меню для выхода из приложения
                NavigationDrawerItem(
                    label = { Text("Выйти из приложения") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (context is ComponentActivity) {
                            context.finish() // Завершение активности
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Меню")
                        }
                    },
                    actions = {
                        // Кнопка трёх точек
                        IconButton(onClick = { expandedMenu = !expandedMenu }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                        }

                        // Выпадающее меню
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            // Переключение между светлой и тёмной темой
                            DropdownMenuItem(
                                text = { Text("Переключить тему") },
                                onClick = {
                                    darkTheme = !darkTheme
                                    onThemeChanged(darkTheme)  // Обновляем состояние темы
                                    expandedMenu = false
                                }
                            )
                        }
                    }
                )
            },
            content = content // Передаем содержимое экрана
        )
    }
}

