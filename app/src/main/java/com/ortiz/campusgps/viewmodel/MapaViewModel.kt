package com.ortiz.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ortiz.campusgps.data.LugaresRepository
import com.ortiz.campusgps.data.UbicacionNoDisponibleException
import com.ortiz.campusgps.data.UbicacionRepository
import com.ortiz.campusgps.model.Lugar
import com.ortiz.campusgps.model.LugarConDistancia
import com.ortiz.campusgps.model.Posicion
import com.ortiz.campusgps.model.TipoError
import com.ortiz.campusgps.model.UbicacionUi
import com.ortiz.campusgps.util.distanciaMetros
import com.ortiz.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    // Mejora IA 1: filtro por categoría (null = todas)
    val categorias: List<String> = lugaresRepo.obtenerLugares().map { it.categoria }.distinct()

    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    // Lugares que se muestran en el mapa y en la lista según la categoría elegida
    val lugaresVisibles: StateFlow<List<LugarConDistancia>> =
        combine(_lugares, _categoria) { lista, cat ->
            if (cat == null) lista else lista.filter { it.lugar.categoria == cat }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _lugares.value)

    // Paso 7: el primero de la lista con distancia calculada; null si aún no hay ubicación
    // (Mejora IA 1: se calcula sobre los lugares visibles del filtro)
    val masCercano: StateFlow<Lugar?> = lugaresVisibles.map { lista ->
        lista.firstOrNull { it.metros != null }?.lugar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun buscar() {
        // Paso 8: sin Google Play Services no se puede buscar
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    // Paso 5: calcula la distancia a cada lugar y ordena del más cercano al más lejano
    private fun actualizarDistancias(pos: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(pos, it)) }
            .sortedBy { it.metros }
        // Comprobación en Logcat (filtra por "CampusGPS")
        _lugares.value.forEach {
            Log.d("CampusGPS", "${it.lugar.nombre}: ${formatearDistancia(it.metros ?: 0f)}")
        }
    }

    // Mejora IA 1: cambia la categoría; si el lugar seleccionado queda oculto, se deselecciona
    fun filtrarCategoria(cat: String?) {
        _categoria.value = cat
        val seleccionadoOculto = cat != null && _lugares.value.any {
            it.lugar.id == _seleccionado.value && it.lugar.categoria != cat
        }
        if (seleccionadoOculto) _seleccionado.value = null
    }

    // Paso 6: guarda qué lugar tocó el usuario (null = ninguno)
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    // Paso 8: convierte cada excepción en un Error con su tipo
    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException ->
            UbicacionUi.Error("Falta el permiso de ubicación", TipoError.SIN_PERMISO)
        is UbicacionNoDisponibleException ->
            UbicacionUi.Error("Activa el GPS e intenta de nuevo", TipoError.GPS_APAGADO)
        else ->
            UbicacionUi.Error(e.message ?: "No se pudo obtener la ubicación", TipoError.OTRO)
    }
}
