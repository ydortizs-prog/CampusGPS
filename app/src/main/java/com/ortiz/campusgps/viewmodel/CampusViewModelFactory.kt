package com.ortiz.campusgps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ortiz.campusgps.data.LugaresRepositoryLocal
import com.ortiz.campusgps.data.UbicacionRepositoryGoogle

// Crea el MapaViewModel con sus repositorios. El Context se queda aquí:
// el ViewModel solo recibe las interfaces.
class CampusViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {
            return MapaViewModel(
                ubicacionRepo = UbicacionRepositoryGoogle(context.applicationContext),
                lugaresRepo = LugaresRepositoryLocal()
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
