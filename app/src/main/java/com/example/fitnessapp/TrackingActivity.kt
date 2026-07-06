package com.example.fitnessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TrackingActivity : AppCompatActivity(), SensorEventListener, OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var cameraExecutor: ExecutorService

    private var isTracking = false
    private var totalDistance = 0f
    private var totalSteps = 0
    private var startStepCount = -1
    private var lastLocation: Location? = null
    private val routePoints = mutableListOf<LatLng>()

    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvStepsDisplay: TextView
    private lateinit var tvHeartRate: TextView
    private lateinit var tvCalories: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnSave: Button
    private lateinit var btnOpenMaps: Button
    private lateinit var viewFinder: PreviewView
    private lateinit var mapView: MapView
    private lateinit var rgActivityType: android.widget.RadioGroup
    private var googleMap: GoogleMap? = null

    private val heartRateHandler = Handler(Looper.getMainLooper())
    private val heartRateRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                val bpm = (70..160).random()
                tvHeartRate.text = getString(R.string.heart_rate_format, bpm)
                heartRateHandler.postDelayed(this, 2000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        tvDistance = findViewById(R.id.tvDistance)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvStepsDisplay = findViewById(R.id.tvStepsCount)
        tvHeartRate = findViewById(R.id.tvHeartRate)
        tvCalories = findViewById(R.id.tvCalories)
        btnStartStop = findViewById(R.id.btnStartStop)
        btnSave = findViewById(R.id.btnSaveActivity)
        btnOpenMaps = findViewById(R.id.btnOpenMaps)
        viewFinder = findViewById(R.id.viewFinderTracking)
        mapView = findViewById(R.id.mapView)
        rgActivityType = findViewById(R.id.rgActivityType)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        cameraExecutor = Executors.newSingleThreadExecutor()

        btnStartStop.setOnClickListener {
            if (isTracking) stopTracking() else startTracking()
        }

        btnSave.setOnClickListener { saveActivityToFirestore() }
        btnOpenMaps.setOnClickListener { openExternalMaps() }

        setupLocationCallback()
        
        if (stepSensor == null) {
            Toast.makeText(this, "Step Counter Sensor not detected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.isCompassEnabled = true
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                }
            }
        }
    }

    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, 
                Manifest.permission.CAMERA,
                Manifest.permission.ACTIVITY_RECOGNITION
            ), 100)
            return
        }

        isTracking = true
        routePoints.clear()
        totalDistance = 0f
        startStepCount = -1
        googleMap?.clear()
        btnStartStop.text = "Stop Tracking"
        btnStartStop.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
        btnSave.visibility = View.GONE
        
        startCameraPreview()
        
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1500)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        heartRateHandler.post(heartRateRunnable)
    }

    private fun stopTracking() {
        isTracking = false
        btnStartStop.text = "Start Tracking"
        btnStartStop.backgroundTintList = getColorStateList(R.color.primaryColor)
        btnSave.visibility = View.VISIBLE
        
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
        heartRateHandler.removeCallbacks(heartRateRunnable)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    routePoints.add(currentLatLng)
                    
                    if (lastLocation != null) {
                        totalDistance += lastLocation!!.distanceTo(location)
                        tvDistance.text = String.format("%.2f km", totalDistance / 1000)
                        tvSpeed.text = String.format("%.1f km/h", location.speed * 3.6f)
                    }
                    
                    lastLocation = location
                    updateMapRoute()
                }
            }
        }
    }

    private fun updateMapRoute() {
        if (routePoints.size < 2) return
        googleMap?.addPolyline(PolylineOptions().addAll(routePoints).color(Color.BLUE).width(10f))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(routePoints.last(), 16f))
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {}
        }, ContextCompat.getMainExecutor(this))
    }

    private fun openExternalMaps() {
        val lat = lastLocation?.latitude ?: return
        val lng = lastLocation?.longitude ?: return
        val uri = "geo:$lat,$lng?q=$lat,$lng"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri)))
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isTracking && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (startStepCount == -1) startStepCount = event.values[0].toInt()
            totalSteps = event.values[0].toInt() - startStepCount
            tvStepsDisplay.text = totalSteps.toString()
            
            // 0.04 calories per step
            val calories = totalSteps * 0.04f
            tvCalories.text = getString(R.string.calories_format, calories)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveActivityToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val type = when(rgActivityType.checkedRadioButtonId) {
            R.id.rbRun -> "RUNNING"
            R.id.rbCycle -> "CYCLING"
            else -> "WALKING"
        }

        val data = mapOf(
            "distance" to String.format("%.2f km", totalDistance / 1000),
            "steps" to totalSteps,
            "points" to routePoints.map { mapOf("lat" to it.latitude, "lng" to it.longitude) },
            "timestamp" to System.currentTimeMillis(),
            "type" to type
        )

        db.collection("activity_history").document(userId).collection("entries").add(data)
            .addOnSuccessListener { 
                Toast.makeText(this, "Activity ($totalSteps steps) saved!", Toast.LENGTH_SHORT).show()
                finish() 
            }
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { 
        super.onDestroy()
        mapView.onDestroy()
        cameraExecutor.shutdown()
        sensorManager.unregisterListener(this)
        heartRateHandler.removeCallbacks(heartRateRunnable)
    }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}
