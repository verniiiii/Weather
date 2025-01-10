package com.example.myweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myweather.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navHostController = rememberNavController()
            WeatherNavHost(navHostController, this, viewModel)
        }
    }
}

@Composable
fun WeatherNavHost(navHostController: NavHostController, owner: LifecycleOwner, viewModel: WeatherViewModel){
    NavHost(navHostController, startDestination = "weather") {
        composable("weather") { WeatherScreen(viewModel, owner, navHostController) }
        composable("settings") { SettingsScreen(viewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel, owner: LifecycleOwner, navHostController: NavHostController) {
    val cities = listOf("Минск", "Москва", "Казань")
    val seasons = listOf("Весна", "Лето", "Осень", "Зима")

    var selectedCity by remember { mutableStateOf(cities[0]) }
    var selectedSeason by remember { mutableStateOf(seasons[0]) }
    var isCityExpanded by remember { mutableStateOf(false) }
    var isSeasonExpanded by remember { mutableStateOf(false) }

    //Переменные состояния для хранения значений из ViewModel
    var cityType by remember { mutableStateOf("Тип города") }
    var averageTemperature by remember { mutableStateOf("Средняя температура") }

    //Устанавливает наблюдателей при запуске приложения
    LaunchedEffect(Unit) {
        viewModel.cityType.observe(owner, Observer { type ->
            cityType = type
        })
        viewModel.averageTemperature.observe(owner, Observer { temp ->
            averageTemperature = temp
        })
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Выберите город и сезон:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ){
            ExposedDropdownMenuBox(
                expanded = isCityExpanded,
                onExpandedChange = {isCityExpanded = !isCityExpanded}
            ) {
                TextField(
                    value = selectedCity,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityExpanded)},
                    modifier = Modifier.menuAnchor() //привязываем меню к этому текстовому полю
                )

                ExposedDropdownMenu(
                    expanded = isCityExpanded,
                    onDismissRequest = {isCityExpanded = false}
                ) {
                    cities.forEach{city ->
                        DropdownMenuItem(
                            text = { Text(text = city) },
                            onClick = {
                                selectedCity = city
                                viewModel.selectCity(city) //Обновляем выбранный город в ViewModel
                                isCityExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ){
            ExposedDropdownMenuBox(
                expanded = isSeasonExpanded,
                onExpandedChange = {isSeasonExpanded = !isSeasonExpanded}
            ) {
                TextField(
                    value = selectedSeason,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSeasonExpanded)},
                    modifier = Modifier.menuAnchor() //привязываем меню к этому текстовому полю
                )

                ExposedDropdownMenu(
                    expanded = isSeasonExpanded,
                    onDismissRequest = {isSeasonExpanded = false}
                ) {
                    seasons.forEach{season ->
                        DropdownMenuItem(
                            text = { Text(text = season) },
                            onClick = {
                                selectedSeason = season
                                viewModel.selectSeason(season) //Обновляем выбор сезона в ViewModel
                                isSeasonExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        //Отображение средней температуры и типа города в пользовательском интерфейсе
        Text(text = "Тип города: $cityType", fontSize = 18.sp)
        Text(text = "Средняя температура: $averageTemperature", fontSize = 18.sp)

        TextButton(onClick = { navHostController.navigate("settings") }) {
            Text("Настройки")
        }
    }
}

