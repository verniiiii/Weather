package com.example.myweather.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myweather.strategy.CelsiusFormatStrategy
import com.example.myweather.strategy.FahrenheitFormatStrategy
import com.example.myweather.strategy.KelvinFormatStrategy
import com.example.myweather.ui.theme.MyWeatherTheme
import com.example.myweather.viewmodel.SettingsViewModel
import com.example.myweather.viewmodel.WeatherViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.absoluteValue

val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController provided") }

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Используем состояние для переключения темы
            var darkTheme by remember { mutableStateOf(true) }

            MyWeatherTheme(darkTheme = darkTheme) {
                WeatherNavHost(owner = this, viewModel = weatherViewModel, onThemeChanged = { darkTheme = it })
            }
        }

        // Предварительное заполнение базы данных
        weatherViewModel.prefillDatabase()
    }
}


@Composable
fun WeatherNavHost(owner: LifecycleOwner, viewModel: WeatherViewModel, onThemeChanged: (Boolean) -> Unit) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(navController, startDestination = "weather") {
            composable("weather") {
                WeatherScreen(viewModel, owner, navController, onThemeChanged)
            }
            composable("settings") {
                SettingsScreen(viewModel, owner, navController, onThemeChanged)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    title: String,
    onThemeChanged: (Boolean) -> Unit, // Добавлен параметр для переключения темы
    content: @Composable (PaddingValues) -> Unit
) {
    // Состояние для переключения темы
    var darkTheme by remember { mutableStateOf(true) }
    var expandedMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                Divider()
                NavigationDrawerItem(
                    label = { Text("Погода") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("weather")
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Настройки") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, owner: LifecycleOwner, navController: NavController, onThemeChanged: (Boolean) -> Unit) {
    val cities by viewModel.cities.observeAsState(emptyList())
    val seasons by viewModel.seasons.observeAsState(emptyList())
    val selectedSeason by viewModel.selectedSeason.observeAsState("")
    val selectedCity by viewModel.selectedCity.observeAsState(null)

    var isCityExpanded by remember { mutableStateOf(false) }
    var isSeasonExpanded by remember { mutableStateOf(false) }
    var visibleSeason = remember { MutableTransitionState(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    val cityType by viewModel.cityType.observeAsState("Тип города")
    val averageTemperature by viewModel.averageTemperature.observeAsState("Средняя температура")
    val selectedFormat by viewModel.selectedTemperatureFormat.observeAsState("Цельсий")

    val temperatureFormats = listOf("Цельсий", "Фаренгейт", "Кельвин")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    // Состояние для переключения темы
    var darkTheme by remember { mutableStateOf(true) }

    // Отслеживание изменений состояния pagerState
    LaunchedEffect(pagerState.currentPage) {
        val format = temperatureFormats[pagerState.currentPage]
        when (format) {
            "Цельсий" -> viewModel.setTemperatureFormat(CelsiusFormatStrategy())
            "Фаренгейт" -> viewModel.setTemperatureFormat(FahrenheitFormatStrategy())
            "Кельвин" -> viewModel.setTemperatureFormat(KelvinFormatStrategy())
        }
    }
    AppScaffold(
        navController = navController,
        title = "Погода",
        onThemeChanged = onThemeChanged
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = "Выберите город и сезон:",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Выбор города с анимацией
                AnimatedVisibility(
                    visible = true
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isCityExpanded,
                            onExpandedChange = { isCityExpanded = !isCityExpanded }
                        ) {
                            TextField(
                                value = selectedCity?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Выберите город") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = isCityExpanded,
                                onDismissRequest = { isCityExpanded = false }
                            ) {
                                cities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(text = city.name) },
                                        onClick = {
                                            viewModel.selectCity(city.name)
                                            isCityExpanded = false
                                            visibleSeason.targetState = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Выбор сезона с анимацией
                AnimatedVisibility(
                    visibleState = visibleSeason,
                    enter = slideInHorizontally() + expandHorizontally(expandFrom = Alignment.End)
                            + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth })
                            + shrinkHorizontally() + fadeOut(),
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isSeasonExpanded,
                            onExpandedChange = { isSeasonExpanded = !isSeasonExpanded }
                        ) {
                            TextField(
                                value = selectedSeason,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Выберите сезон") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSeasonExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = isSeasonExpanded,
                                onDismissRequest = { isSeasonExpanded = false }
                            ) {
                                seasons.forEach { season ->
                                    DropdownMenuItem(
                                        text = { Text(text = season) },
                                        onClick = {
                                            viewModel.selectSeason(season)
                                            isSeasonExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Отображение типа города
                if (selectedCity != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Тип города: $cityType",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Отображение средней температуры
                if (selectedCity != null && selectedSeason.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Средняя температура: $averageTemperature",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else if (selectedCity != null && selectedSeason.isBlank()) {
                    Text(
                        text = "Пожалуйста, выберите сезон",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Горизонтальный список для выбора формата температуры
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        count = temperatureFormats.size,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 64.dp),
                        itemSpacing = (-128).dp // Overlap items
                    ) { page ->
                        val format = temperatureFormats[page]
                        val isSelected = format == selectedFormat
                        val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffset).absoluteValue
                        val scale = 1f - (0.2f * offset)
                        val alpha = 1f - (0.5f * offset)

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                        ) {
                            TemperatureButton(
                                label = format,
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page)
                                    }
                                    when (format) {
                                        "Цельсий" -> viewModel.setTemperatureFormat(CelsiusFormatStrategy())
                                        "Фаренгейт" -> viewModel.setTemperatureFormat(FahrenheitFormatStrategy())
                                        "Кельвин" -> viewModel.setTemperatureFormat(KelvinFormatStrategy())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
