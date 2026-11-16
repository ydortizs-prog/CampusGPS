package com.ortiz.campusgps.data

import com.ortiz.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
}
