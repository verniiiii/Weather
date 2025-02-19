@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myweather.screens

import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myweather.data.City
import com.example.myweather.strategy.CelsiusFormatStrategy
import com.example.myweather.strategy.FahrenheitFormatStrategy
import com.example.myweather.strategy.KelvinFormatStrategy
import com.example.myweather.viewmodel.WeatherViewModel
import com.example.myweather.constants.Month
import com.example.myweather.constants.Season


import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.annotation.SuppressLint

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.navigation.NavController
import com.example.myweather.R

import com.example.myweather.network.GeocoderRepository
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import org.tensorflow.lite.support.label.Category
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeatherViewModel = viewModel(), owner: LifecycleOwner, navController: NavController, onThemeChanged: (Boolean) -> Unit) {
    val cities by viewModel.cities.observeAsState(emptyList())
    var showAddCityDialog by remember { mutableStateOf(false) }
    var showAddTemperatureDialog by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var cityToDelete by remember { mutableStateOf<City?>(null) }
    val temperatureFormat by viewModel.temperatureFormat.observeAsState()

    val selectedFormat by viewModel.selectedTemperatureFormat.observeAsState("Цельсий")

    val temperatureFormats = listOf("Цельсий", "Фаренгейт", "Кельвин")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val seasons = listOf("Весна", "Лето", "Осень", "Зима")
    val averageTemperatures = remember { mutableStateMapOf<String, Double>() }

    // Отслеживание изменений состояния pagerState
    LaunchedEffect(pagerState.currentPage) {
        val format = temperatureFormats[pagerState.currentPage]
        when (format) {
            "Цельсий" -> viewModel.setTemperatureFormat(CelsiusFormatStrategy())
            "Фаренгейт" -> viewModel.setTemperatureFormat(FahrenheitFormatStrategy())
            "Кельвин" -> viewModel.setTemperatureFormat(KelvinFormatStrategy())
        }
    }

    LaunchedEffect(selectedCity, temperatureFormat) {
        if (selectedCity != null) {
            seasons.forEach { season ->
                val avgTemp = viewModel.getAverageTemperatureBySeason(selectedCity!!.id, season)
                averageTemperatures[season] = avgTemp
            }
        }
    }

    AppScaffold(
        navController = navController,
        title = "Настройки",
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
                    text = "Управление городами:",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(cities) { index, city ->
                        val visibleState = remember { MutableTransitionState(false) }
                        LaunchedEffect(Unit) {
                            delay(100 * index.toLong()) // Задержка для анимации
                            visibleState.targetState = true
                        }

                        AnimatedVisibility(
                            visibleState = visibleState,
                            enter = slideInHorizontally(animationSpec = tween(durationMillis = 500)) +
                                    fadeIn(animationSpec = tween(durationMillis = 500)),
                            exit = slideOutHorizontally(animationSpec = tween(durationMillis = 500)) +
                                    fadeOut(animationSpec = tween(durationMillis = 500))
                        ) {
                            CityCard(
                                city = city,
                                onDelete = {
                                    cityToDelete = city
                                    showDeleteConfirmation = true
                                },
                                onEdit = {
                                    selectedCity = city
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { showAddCityDialog = true }) {
                    Text("Добавить город", fontSize = 18.sp)
                }

                if (showBottomSheet && selectedCity != null) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Город: ${selectedCity!!.name}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Горизонтальный список для выбора формата температуры
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
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
                                    val offset =
                                        ((pagerState.currentPage - page) + pagerState.currentPageOffset).absoluteValue
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
                                                    "Цельсий" -> viewModel.setTemperatureFormat(
                                                        CelsiusFormatStrategy()
                                                    )

                                                    "Фаренгейт" -> viewModel.setTemperatureFormat(
                                                        FahrenheitFormatStrategy()
                                                    )

                                                    "Кельвин" -> viewModel.setTemperatureFormat(
                                                        KelvinFormatStrategy()
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Средняя температура по сезонам:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Column {
                                seasons.forEach { season ->
                                    val avgTemp = averageTemperatures[season] ?: 0.0
                                    val formattedTemp = temperatureFormat?.formatTemperature(avgTemp) ?: "Н/Д"
                                    Text(
                                        text = "$season: $formattedTemp",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showAddTemperatureDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Добавить температуру")
                            }
                        }
                    }
                }




                if (showAddCityDialog) {
                    AddCityDialog(
                        onDismiss = { showAddCityDialog = false },
                        onSave = { cityName, cityType ->
                            viewModel.addCity(cityName, cityType)
                        }
                    )
                }

                if (showAddTemperatureDialog && selectedCity != null) {
                    AddTemperatureDialog(
                        onDismiss = { showAddTemperatureDialog = false
                            showBottomSheet = false
                            selectedCity = null},
                        onSave = { season, temperatures ->
                            // Передаем имя города, сезон и карту температур для сохранения
                            temperatures.forEach { (month, temperature) ->
                                viewModel.addTemperature(selectedCity!!.name, month, temperature)

                            }

                        },
                    )


                }



                if (showDeleteConfirmation && cityToDelete != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteConfirmation = false
                            cityToDelete = null
                        },
                        title = { Text("Подтверждение удаления") },
                        text = { Text("Вы действительно хотите удалить город ${cityToDelete!!.name}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteCity(cityToDelete!!)
                                showDeleteConfirmation = false
                                cityToDelete = null
                            }) {
                                Text("Удалить")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDeleteConfirmation = false
                                cityToDelete = null
                            }) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }
        }
    }
}









