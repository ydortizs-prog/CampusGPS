package com.ortiz.campusgps.data

import com.ortiz.campusgps.model.Lugar

interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
