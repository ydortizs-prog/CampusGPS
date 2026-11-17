package com.ortiz.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ortiz.campusgps.model.UbicacionUi
import com.ortiz.campusgps.util.formatearDistancia
import com.ortiz.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    // Mejora IA 1: el mapa y la lista muestran solo los lugares del filtro
    val lugares by vm.lugaresVisibles.collectAsState()
    val categoria by vm.categoria.collectAsState()
    val radio by vm.radio.collectAsState()
    val dentroDelRadio by vm.dentroDelRadio.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()

    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        // Si concedió FINE o COARSE, buscamos; si no, la app sigue abierta con el mensaje
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    fun alPulsar() {
        val tienePermiso = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).any {
            ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
        }
        if (tienePermiso) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
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
                is UbicacionUi.Error -> MensajeError(error = e, onReintentar = { alPulsar() })
            }

            // Mejora IA 1: chips para filtrar por categoría
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = categoria == null,
                    onClick = { vm.filtrarCategoria(null) },
                    label = { Text("Todos") }
                )
                vm.categorias.forEach { cat ->
                    FilterChip(
                        selected = categoria == cat,
                        onClick = { vm.filtrarCategoria(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            // Mejora IA 2: selector del radio de cercanía
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Radio:")
                vm.radios.forEach { r ->
                    FilterChip(
                        selected = radio == r,
                        onClick = { vm.cambiarRadio(r) },
                        label = { Text("$r m") }
                    )
                }
            }

            // Paso 3: el mapa ocupa el espacio libre
            MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                modifier = Modifier.weight(1f),
                masCercano = masCercano,
                radio = radio
            )

            // Paso 7: botón con borde para ir al lugar más cercano
            OutlinedButton(
                onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                enabled = masCercano != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir al más cercano")
            }
            masCercano?.let { lugar ->
                val metros = lugares.firstOrNull { it.lugar.id == lugar.id }?.metros
                Text("Más cercano: ${lugar.nombre} (${metros?.let { formatearDistancia(it) } ?: "Sin calcular"})")
            }

            // Paso 6: la lista abajo con altura fija
            ListaLugares(
                lugares = lugares,
                seleccionado = seleccionado,
                onClick = { vm.seleccionar(it) },
                dentroDelRadio = dentroDelRadio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}
