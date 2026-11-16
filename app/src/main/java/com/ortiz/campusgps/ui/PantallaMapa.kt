package com.ortiz.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ortiz.campusgps.model.UbicacionUi
import com.ortiz.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()

    // TODO Paso 2: pedir el permiso con rememberLauncherForActivityResult(RequestMultiplePermissions())
    fun alPulsar() {
        vm.buscar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { alPulsar() },
                enabled = estado !is UbicacionUi.Buscando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar mi ubicación")
            }

            when (val e = estado) {
                UbicacionUi.Inicial -> Text("Pulsa el botón para buscar tu ubicación")
                UbicacionUi.Buscando -> Text("Buscando...")
                is UbicacionUi.Lista -> Text(
                    "Latitud: ${e.posicion.latitud}\nLongitud: ${e.posicion.longitud}"
                )
                is UbicacionUi.Error -> Text(e.mensaje, color = MaterialTheme.colorScheme.error)
            }

            // TODO Paso 3: MapaUbicacion(..., modifier = Modifier.weight(1f))
        }
    }
}
