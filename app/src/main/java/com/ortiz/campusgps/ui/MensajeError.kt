package com.ortiz.campusgps.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ortiz.campusgps.model.TipoError
import com.ortiz.campusgps.model.UbicacionUi

// Recuadro de error con el texto y, debajo, el botón que corresponde al tipo
@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val colores = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(error.mensaje, color = MaterialTheme.colorScheme.onErrorContainer)

        when (error.tipo) {
            TipoError.SIN_PERMISO -> Button(
                onClick = {
                    // Abre la pantalla de ajustes de esta app
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", contexto.packageName, null)
                    )
                    contexto.startActivity(intent)
                },
                colors = colores
            ) { Text("Abrir ajustes") }

            TipoError.GPS_APAGADO, TipoError.OTRO -> Button(
                onClick = onReintentar,
                colors = colores
            ) { Text("Reintentar") }

            TipoError.SIN_PLAY_SERVICES -> Unit // sin botón de reintentar
        }
    }
}
