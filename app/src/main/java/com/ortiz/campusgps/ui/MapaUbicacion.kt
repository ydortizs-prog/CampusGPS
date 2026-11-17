package com.ortiz.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.graphics.Color
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.ortiz.campusgps.model.Lugar
import com.ortiz.campusgps.model.LugarConDistancia
import com.ortiz.campusgps.model.UbicacionUi

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier,
    masCercano: Lugar? = null,
    radio: Int? = null,
    permisoConcedido: Boolean = false
) {
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 12f)
    }

    // TODO 1: cuando llega la ubicación, la cámara viaja a tu posición con zoom 16
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val destino = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            camara.animate(CameraUpdateFactory.newLatLngZoom(destino, 16f))
        }
    }

    // Paso 6: al seleccionar un lugar, la cámara viaja hasta él con zoom 17
    LaunchedEffect(seleccionado) {
        val lugar = lugares.firstOrNull { it.lugar.id == seleccionado }?.lugar
        if (lugar != null) {
            camara.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lugar.latitud, lugar.longitud), 17f)
            )
        }
    }

    // Mejora IA 3: punto azul nativo y botón "mi ubicación", solo si hay permiso
    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara,
        properties = MapProperties(isMyLocationEnabled = permisoConcedido),
        uiSettings = MapUiSettings(myLocationButtonEnabled = permisoConcedido)
    ) {
        // TODO 2: marcador "Estás aquí" solo si el estado es Lista
        if (estado is UbicacionUi.Lista) {
            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(estado.posicion.latitud, estado.posicion.longitud)
                ),
                title = "Estás aquí"
            )
        }
        // Mejora IA 2: círculo del radio de cercanía alrededor de tu posición
        if (estado is UbicacionUi.Lista && radio != null) {
            Circle(
                center = LatLng(estado.posicion.latitud, estado.posicion.longitud),
                radius = radio.toDouble(),
                strokeColor = Color(0xFF1E88E5),
                strokeWidth = 4f,
                fillColor = Color(0x331E88E5)
            )
        }
        // TODO 3 (Paso 4): un marcador azul por cada lugar del campus
        lugares.forEach { item ->
            key(item.lugar.id) {
                val marcador = rememberUpdatedMarkerState(
                    position = LatLng(item.lugar.latitud, item.lugar.longitud)
                )
                // El lugar seleccionado muestra su nombre en el mapa
                LaunchedEffect(seleccionado) {
                    if (seleccionado == item.lugar.id) marcador.showInfoWindow()
                }
                Marker(
                    state = marcador,
                    title = item.lugar.nombre,
                    snippet = item.lugar.categoria,
                    // Paso 7: el más cercano en verde; los demás siguen en azul
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (item.lugar.id == masCercano?.id) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
        }
    }
}
