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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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


