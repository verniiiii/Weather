package com.example.myweather.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.myweather.network.GeocoderRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityDialog(
    onDismiss: () -> Unit,
    onSave: (cityName: String, cityType: String) -> Unit
) {
    var cityName by remember { mutableStateOf("") }
    var cityType by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isCityTypeMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val geocoderRepository = GeocoderRepository()

    val cityTypes = listOf("Большой", "Средний", "Маленький")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                getCurrentLocation(fusedLocationClient) { location ->
                    location?.let {
                        coroutineScope.launch {
                            val cityNameResult = geocoderRepository.fetchCityInfo(it.latitude, it.longitude)
                            cityName = cityNameResult
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить город",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cityName,
                    onValueChange = {
                        if (it.all { char -> char.isLetter() || char.isWhitespace() }) {
                            cityName = it
                        }
                    },
                    label = { Text("Название города") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && cityName.isEmpty(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    )
                )
                if (showError && cityName.isEmpty()) {
                    Text(
                        text = "Пожалуйста, введите название города",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = isCityTypeMenuExpanded,
                    onExpandedChange = { isCityTypeMenuExpanded = !isCityTypeMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = cityType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип города") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = isCityTypeMenuExpanded
                            )
                        },
                        isError = showError && cityType.isEmpty()
                    )
                    ExposedDropdownMenu(
                        expanded = isCityTypeMenuExpanded,
                        onDismissRequest = { isCityTypeMenuExpanded = false }
                    ) {
                        cityTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    cityType = type
                                    isCityTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                if (showError && cityType.isEmpty()) {
                    Text(
                        text = "Пожалуйста, выберите тип города",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Button(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    onClick = {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            coroutineScope.launch {
                                getCurrentLocation(fusedLocationClient) { location ->
                                    location?.let {
                                        coroutineScope.launch {
                                            val cityNameResult = geocoderRepository.fetchCityInfo(it.latitude, it.longitude)
                                            cityName = cityNameResult
                                        }
                                    }
                                }
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Использовать текущую геолокацию")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (cityName.isEmpty() || cityType.isEmpty()) {
                    showError = true
                } else {
                    onSave(cityName, cityType)
                    onDismiss()
                }
            }) {
                Text("Сохранить", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.error)
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
