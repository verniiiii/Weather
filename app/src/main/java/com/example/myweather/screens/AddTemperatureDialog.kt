package com.example.myweather.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myweather.R
import com.example.myweather.constants.Month
import com.example.myweather.constants.Season

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun AddTemperatureDialog(
    onDismiss: () -> Unit,
    onSave: (season: String, temperatures: Map<String, Double>) -> Unit
) {
    var selectedSeason by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val seasons = listOf(Season.ВЕСНА, Season.ЛЕТО, Season.ОСЕНЬ, Season.ЗИМА)
    val seasonMonths = mapOf(
        Season.ВЕСНА to listOf(Month.МАРТ, Month.АПРЕЛЬ, Month.МАЙ),
        Season.ЛЕТО to listOf(Month.ИЮНЬ, Month.ИЮЛЬ, Month.АВГУСТ),
        Season.ОСЕНЬ to listOf(Month.СЕНТЯБРЬ, Month.ОКТЯБРЬ, Month.НОЯБРЬ),
        Season.ЗИМА to listOf(Month.ДЕКАБРЬ, Month.ЯНВАРЬ, Month.ФЕВРАЛЬ)
    )
    val temperatures = remember { mutableStateMapOf<String, Double>() }
    val isError by derivedStateOf {
        temperatures.values.any { it == null } || selectedSeason.isEmpty()
    }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Добавить температуру",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Column {
                    // Выбор сезона
                    ExposedDropdownMenuBox(
                        expanded = isMenuExpanded,
                        onExpandedChange = { isMenuExpanded = !isMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSeason,
                            onValueChange = { selectedSeason = it },
                            readOnly = true,
                            label = { Text("Выберите сезон") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = isMenuExpanded
                                )
                            },
                            isError = showError && selectedSeason.isEmpty(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            seasons.forEach { season ->
                                DropdownMenuItem(
                                    text = { Text(season) },
                                    onClick = {
                                        selectedSeason = season
                                        isMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Поля для ввода температуры по месяцам
                    selectedSeason.takeIf { it.isNotEmpty() }?.let { season ->
                        seasonMonths[season]?.forEach { month ->
                            var temp by remember(month) { mutableStateOf("") }
                            OutlinedTextField(
                                value = temp,
                                onValueChange = {
                                    if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                        temp = it
                                        temperatures[month] = it.toDoubleOrNull() ?: 0.0
                                    }
                                },
                                label = { Text(month) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                isError = showError && temperatures[month] == 0.0,
                                leadingIcon = {
                                    val iconRes = when (month) {
                                        Month.ДЕКАБРЬ, Month.ЯНВАРЬ, Month.ФЕВРАЛЬ -> R.drawable.winter
                                        Month.МАРТ, Month.АПРЕЛЬ, Month.МАЙ -> R.drawable.spring
                                        Month.ИЮНЬ, Month.ИЮЛЬ, Month.АВГУСТ -> R.drawable.summer
                                        else -> R.drawable.autumn
                                    }
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = "Иконка месяца",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    if (showError) {
                        Text(
                            text = "Пожалуйста, заполните все поля и выберите сезон",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isError) {
                        onSave(selectedSeason, temperatures)
                        onDismiss()
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Отмена")
            }
        }
    )
}