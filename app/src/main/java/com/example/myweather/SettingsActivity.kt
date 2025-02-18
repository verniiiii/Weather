@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myweather

import android.app.Activity
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
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
import com.example.myweather.data.GeocoderRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeatherViewModel = viewModel(), owner: LifecycleOwner) {
    val cities by viewModel.cities.observeAsState(emptyList())
    var showAddCityDialog by remember { mutableStateOf(false) }
    var showAddTemperatureDialog by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf<City?>(null) }
    val temperatureFormat by viewModel.temperatureFormat.observeAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val seasons = listOf("Весна", "Лето", "Осень", "Зима")
    val averageTemperatures = remember { mutableStateMapOf<String, Double>() }

    LaunchedEffect(selectedCity, temperatureFormat) {
        if (selectedCity != null) {
            seasons.forEach { season ->
                val avgTemp = viewModel.getAverageTemperatureBySeason(selectedCity!!.id, season)
                averageTemperatures[season] = avgTemp
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .animateContentSize()
    ) {
        Text(
            text = "Управление городами:",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(cities) { index, city ->
                val visibleState = remember { MutableTransitionState(false) }
                LaunchedEffect(Unit) {
                    delay(100 * index.toLong()) // Задержка для создания эффекта "лесенки"
                    visibleState.targetState = true
                }

                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = slideInHorizontally(animationSpec = tween(durationMillis = 500)) +
                            expandHorizontally(expandFrom = Alignment.Start) +
                            fadeIn(animationSpec = tween(durationMillis = 500)),
                    exit = slideOutHorizontally(animationSpec = tween(durationMillis = 500)) +
                            shrinkHorizontally(shrinkTowards = Alignment.Start) +
                            fadeOut(animationSpec = tween(durationMillis = 500))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = city.name, fontSize = 18.sp)
                            Text(text = city.type, fontSize = 18.sp)
                            IconButton(onClick = { viewModel.deleteCity(city) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                            IconButton(onClick = {
                                selectedCity = city
                                showBottomSheet = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
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
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Город: ${selectedCity!!.name}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TemperatureButton(
                            label = "Цельсий",
                            selected = temperatureFormat == CelsiusFormatStrategy(),
                            onClick = { viewModel.setTemperatureFormat(CelsiusFormatStrategy()) }
                        )
                        TemperatureButton(
                            label = "Фаренгейт",
                            selected = temperatureFormat == FahrenheitFormatStrategy(),
                            onClick = { viewModel.setTemperatureFormat(FahrenheitFormatStrategy()) }
                        )
                        TemperatureButton(
                            label = "Кельвин",
                            selected = temperatureFormat == KelvinFormatStrategy(),
                            onClick = { viewModel.setTemperatureFormat(KelvinFormatStrategy()) }
                        )
                    }
                    Text(
                        text = "Времена года:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column {
                        seasons.forEach { season ->
                            val avgTemp = averageTemperatures[season] ?: 0.0
                            val formattedTemp = temperatureFormat?.formatTemperature(avgTemp) ?: "N/A"
                            Text(text = "$season: Средняя температура - $formattedTemp")
                        }
                    }
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                        Text("Скрыть нижнюю панель")
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
                onDismiss = { showAddTemperatureDialog = false },
                onSave = { month, temperature ->
                    viewModel.addTemperature(selectedCity!!.name, month, temperature)
                }
            )
        }
    }
}

@Composable
fun AddTemperatureDialog(
    onDismiss: () -> Unit,
    onSave: (month: String, temperature: Double) -> Unit
) {
    var month by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить температуру") },
        text = {
            Column {
                TextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Месяц") }
                )
                TextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Температура") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(month, temperature.toDoubleOrNull() ?: 0.0)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AddCityDialog(
    onDismiss: () -> Unit,
    onSave: (cityName: String, cityType: String) -> Unit
) {
    var cityName by remember { mutableStateOf("") }
    var cityType by remember { mutableStateOf("") }
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val geocoderRepository = GeocoderRepository()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                getCurrentLocation(fusedLocationClient) { location ->
                    location?.let {
                        coroutineScope.launch {
                            val cityNameResult = geocoderRepository.fetchCityInfo(it.latitude, it.longitude)
                            cityName = cityNameResult // Обновляем переменную состояния напрямую
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить город") },
        text = {
            Column {
                TextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("Название города") }
                )
                TextField(
                    value = cityType,
                    onValueChange = { cityType = it },
                    label = { Text("Тип города") }
                )
                Button(onClick = {
                    Log.d("фигня", "нажали")
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d("фигня", "зашли")
                        coroutineScope.launch {
                            Log.d("фигня", "начало корутины")
                            getCurrentLocation(fusedLocationClient) { location ->
                                Log.d("фигня", "$location")
                                location?.let {
                                    coroutineScope.launch {
                                        val cityNameResult = geocoderRepository.fetchCityInfo(it.latitude, it.longitude)
                                        cityName = cityNameResult // Обновляем переменную состояния напрямую
                                    }
                                }
                            }
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }) {
                    Text("Использовать текущую геолокацию")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(cityName, cityType)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    withContext(Dispatchers.IO) {
        Log.d("фигня", "зашли2")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d("Location", "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
                }
                onLocationReceived(location)
            }
            .addOnFailureListener { e ->
                Log.e("Location", "Error getting location", e)
            }
    }
}