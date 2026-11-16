package com.ortiz.campusgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ortiz.campusgps.ui.PantallaMapa
import com.ortiz.campusgps.ui.theme.CampusGPSTheme
import com.ortiz.campusgps.viewmodel.CampusViewModelFactory
import com.ortiz.campusgps.viewmodel.MapaViewModel

class MainActivity : ComponentActivity() {

    private val vm: MapaViewModel by viewModels { CampusViewModelFactory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusGPSTheme {
                PantallaMapa(vm)
            }
        }
    }
}
