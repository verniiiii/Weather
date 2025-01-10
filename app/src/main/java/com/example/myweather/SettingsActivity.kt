package com.example.myweather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myweather.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(viewModel: WeatherViewModel){
    var cityName by remember { mutableStateOf("") }
    var cityType by remember { mutableStateOf("") }
    var juneTemp by remember { mutableStateOf("") }
    var julyTemp by remember { mutableStateOf("") }
    var augustTemp by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Настройки города:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = cityName,
            onValueChange = {cityName = it},
            label = { Text("Название города") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cityType,
            onValueChange = { cityType = it },
            label = { Text("Тип города") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = juneTemp,
            onValueChange = { juneTemp = it },
            label = { Text("Температура в июне") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = julyTemp,
            onValueChange = { julyTemp = it },
            label = { Text("Температура в июле") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = augustTemp,
            onValueChange = { augustTemp = it },
            label = { Text("Температура в августе") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            viewModel.saveCitySettings(cityName, cityType, julyTemp.toDoubleOrNull(), julyTemp.toDoubleOrNull(), augustTemp.toDoubleOrNull())
        }) {
            Text("Сохранить")
        }
    }
}