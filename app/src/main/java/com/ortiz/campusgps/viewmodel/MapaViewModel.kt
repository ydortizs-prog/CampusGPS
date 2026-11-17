package com.ortiz.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ortiz.campusgps.data.LugaresRepository
import com.ortiz.campusgps.data.UbicacionRepository
import com.ortiz.campusgps.model.LugarConDistancia
import com.ortiz.campusgps.model.Posicion
import com.ortiz.campusgps.model.UbicacionUi
import com.ortiz.campusgps.util.distanciaMetros
import com.ortiz.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun buscar() {
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = UbicacionUi.Error(mensajeDe(it)) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error("Sin permiso de ubicación no puedo mostrar dónde estás")
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

    // Paso 6: guarda qué lugar tocó el usuario (null = ninguno)
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }
    // TODO Paso 7: val masCercano: StateFlow<Lugar?>

    private fun mensajeDe(e: Throwable): String = when (e) {
        is SecurityException -> "Falta el permiso de ubicación"
        else -> e.message ?: "No se pudo obtener la ubicación"
    }
}
