package com.ortiz.campusgps.data

import com.ortiz.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>

    // Paso 8: el ViewModel lo consulta antes de buscar (así no necesita un Context)
    fun playServicesDisponible(): Boolean
}

// Paso 8: el GPS está apagado o no se pudo determinar la ubicación
class UbicacionNoDisponibleException : Exception("Activa el GPS e intenta de nuevo")
