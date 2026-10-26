package com.example.contador.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var contadorPasos by mutableIntStateOf(0)

    // Estado del permiso (controla si el botón se puede presionar)
    private var permisoConcedido by mutableStateOf(false)

    // true si el dispositivo NO tiene sensor de pasos (emulador)
    private var sinSensor by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permisoConcedido = isGranted
        if (isGranted) {
            inicializarSensor()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permisoConcedido = tienePermiso()
        if (permisoConcedido) {
            inicializarSensor()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        setContent {
            PantallaPasos(
                contador = contadorPasos,
                // El botón se muestra si no hay sensor real o si falta el permiso
                mostrarBoton = sinSensor || !permisoConcedido,
                botonHabilitado = permisoConcedido,
                permisoDenegado = !permisoConcedido,
                onSimularPaso = {
                    if (permisoConcedido) contadorPasos++
                }
            )
        }
    }

    private fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

    private fun inicializarSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sinSensor = stepSensor == null

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onResume() {
        super.onResume()
        // Por si el usuario concedió el permiso desde Ajustes
        val concedido = tienePermiso()
        permisoConcedido = concedido
        if (concedido && !::sensorManager.isInitialized) {
            inicializarSensor()
        } else if (::sensorManager.isInitialized) {
            stepSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            contadorPasos = event.values[0].toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun PantallaPasos(
    contador: Int,
    mostrarBoton: Boolean,
    botonHabilitado: Boolean,
    permisoDenegado: Boolean,
    onSimularPaso: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pasos:",
            style = MaterialTheme.typography.caption1,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "$contador",
            style = MaterialTheme.typography.display1,
            fontWeight = FontWeight.Bold
        )
        if (permisoDenegado) {
            Text(
                text = "Permiso denegado",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.error
            )
        }
        if (mostrarBoton) {
            Button(
                onClick = onSimularPaso,
                enabled = botonHabilitado,
                modifier = Modifier.size(40.dp)
            ) {
                Text("+1")
            }
        }
    }
}