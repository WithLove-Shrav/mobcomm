package com.example.modpract

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
// Using fully qualified name in code instead of import to avoid conflicts
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

class MainActivity3 : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var btnShowMap: Button
    private lateinit var editLocation: EditText
    private lateinit var btnSaveLocation: Button
    private lateinit var btnUpdateLocation: Button
    private lateinit var btnDeleteLocation: Button
    private lateinit var savedLocationsText: TextView
    private lateinit var airplaneModeStatus: TextView
    private lateinit var videoView: VideoView
    private lateinit var btnPlayVideo: Button
    private lateinit var btnPlaySound: Button
    
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var airplaneModeReceiver: BroadcastReceiver
    private var mediaPlayer: MediaPlayer? = null
    
    private var selectedLocationId: Int = -1
    private var selectedLocationName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure OpenStreetMap
        Configuration.getInstance().load(applicationContext, 
            PreferenceManager.getDefaultSharedPreferences(applicationContext))
            
        setContentView(R.layout.activity_main3)
        
        // Initialize views
        mapView = findViewById(R.id.mapView)
        btnShowMap = findViewById(R.id.btnShowMap)
        editLocation = findViewById(R.id.editLocation)
        btnSaveLocation = findViewById(R.id.btnSaveLocation)
        btnUpdateLocation = findViewById(R.id.btnUpdateLocation)
        btnDeleteLocation = findViewById(R.id.btnDeleteLocation)
        savedLocationsText = findViewById(R.id.savedLocationsText)
        airplaneModeStatus = findViewById(R.id.airplaneModeStatus)
        videoView = findViewById(R.id.videoView)
        btnPlayVideo = findViewById(R.id.btnPlayVideo)
        btnPlaySound = findViewById(R.id.btnPlaySound)
        
        // Initialize database helper
        dbHelper = DatabaseHelper(this)
        
        // Apply entrance animation to the title
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        airplaneModeStatus.startAnimation(fadeInAnimation)
        
        // Set up button click listeners
        btnShowMap.setOnClickListener {
            // Show map when button is clicked with animation
            val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            mapView.startAnimation(slideUpAnimation)
            mapView.visibility = View.VISIBLE
            setupMap()
    }

    fun openCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivity(intent)
    }

    fun toggleWifi() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
        Toast.makeText(this, if (wifiManager.isWifiEnabled) "WiFi Enabled" else "WiFi Disabled", Toast.LENGTH_SHORT).show()
    }

    fun toggleBluetooth() {
        val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.disable()
                Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show()
            } else {
                bluetoothAdapter.enable()
                Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
        }
    }

    fun setupSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val sensorEventListener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event != null) {
                    val sensorDataText = "X: ${event.values[0]}, Y: ${event.values[1]}, Z: ${event.values[2]}"
                    findViewById<TextView>(R.id.sensorData).text = "Sensor Data: $sensorDataText"
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(sensorEventListener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
    }
        
        findViewById<Button>(R.id.btnOpenCamera).setOnClickListener {
            openCamera()
        }

        findViewById<Button>(R.id.btnToggleWifi).setOnClickListener {
            toggleWifi()
        }

        findViewById<Button>(R.id.btnToggleBluetooth).setOnClickListener {
            toggleBluetooth()
        }

        setupSensor()

        btnSaveLocation.setOnClickListener {
            saveLocation()
            // Apply button animation
            val buttonAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            btnSaveLocation.startAnimation(buttonAnim)
        }
        
        btnUpdateLocation.setOnClickListener {
            updateLocation()
            val buttonAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            btnUpdateLocation.startAnimation(buttonAnim)
        }
        
        btnDeleteLocation.setOnClickListener {
            deleteLocation()
            val buttonAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            btnDeleteLocation.startAnimation(buttonAnim)
        }
        
        // Make saved locations text clickable for selection
        savedLocationsText.setOnClickListener {
            selectLocation()
        }
        
        // Set up media buttons
        btnPlayVideo.setOnClickListener {
            playVideo()
        }
        
        btnPlaySound.setOnClickListener {
            playSound()
        }
        
        // Display saved locations
        updateLocationsList()
        
        // Initialize airplane mode receiver
        setupAirplaneModeReceiver()
    }
    
    private fun setupAirplaneModeReceiver() {
        airplaneModeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                    val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                    updateAirplaneModeStatus(isAirplaneModeOn)
                }
            }
        }
        
        // Register the receiver
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(airplaneModeReceiver, filter)
        
        // Check current airplane mode status
        val isAirplaneModeOn = android.provider.Settings.Global.getInt(
            contentResolver,
            android.provider.Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
        updateAirplaneModeStatus(isAirplaneModeOn)
    }
    
    private fun updateAirplaneModeStatus(isAirplaneModeOn: Boolean) {
        val status = if (isAirplaneModeOn) "ON" else "OFF"
        airplaneModeStatus.text = "Airplane Mode: $status"
        
        // Apply blinking animation if airplane mode is on
        if (isAirplaneModeOn) {
            val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
            airplaneModeStatus.startAnimation(blinkAnimation)
        }
    }
    
    private fun saveLocation() {
        val locationName = editLocation.text.toString().trim()
        
        if (locationName.isNotEmpty()) {
            // Save to database
            val id = dbHelper.addLocation(locationName)
            
            if (id > -1) {
                Toast.makeText(this, "Location saved successfully", Toast.LENGTH_SHORT).show()
                editLocation.text.clear()
                updateLocationsList()
                
                // Apply animation to the locations list
                val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                savedLocationsText.startAnimation(fadeInAnimation)
            } else {
                Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter a location name", Toast.LENGTH_SHORT).show()
            
            // Apply shake animation to the input field
val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
            editLocation.startAnimation(shakeAnimation)
        }
    }
    
    private fun updateLocation() {
        val newName = editLocation.text.toString().trim()
        
        if (selectedLocationId != -1 && newName.isNotEmpty()) {
            val result = dbHelper.updateLocation(selectedLocationId, newName)
            
            if (result > 0) {
                Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
                editLocation.text.clear()
                selectedLocationId = -1
                selectedLocationName = ""
                btnUpdateLocation.isEnabled = false
                btnDeleteLocation.isEnabled = false
                updateLocationsList()
                
                // Apply animation to the locations list
                val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                savedLocationsText.startAnimation(fadeInAnimation)
            } else {
                Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select a location and enter a new name", Toast.LENGTH_SHORT).show()
            
            // Apply shake animation to the input field
val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
            editLocation.startAnimation(shakeAnimation)
        }
    }
    
    private fun deleteLocation() {
        if (selectedLocationId != -1) {
            val result = dbHelper.deleteLocation(selectedLocationId)
            
            if (result > 0) {
                Toast.makeText(this, "Location deleted successfully", Toast.LENGTH_SHORT).show()
                editLocation.text.clear()
                selectedLocationId = -1
                selectedLocationName = ""
                btnUpdateLocation.isEnabled = false
                btnDeleteLocation.isEnabled = false
                updateLocationsList()
            } else {
                Toast.makeText(this, "Failed to delete location", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select a location to delete", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun selectLocation() {
        val locations = dbHelper.getAllLocationsWithIds()
        
        if (locations.isNotEmpty()) {
            // For simplicity, we'll just use the first location for now
            // In a real app, you would show a dialog to select from the list
            val location = locations[0]
            selectedLocationId = location.first
            selectedLocationName = location.second
            
            editLocation.setText(selectedLocationName)
            btnUpdateLocation.isEnabled = true
            btnDeleteLocation.isEnabled = true
            
            Toast.makeText(this, "Selected: $selectedLocationName", Toast.LENGTH_SHORT).show()
            
            // Apply selection animation
            val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)
            savedLocationsText.startAnimation(pulseAnimation)
        }
    }
    
    private fun updateLocationsList() {
        val locations = dbHelper.getAllLocations()
        if (locations.isEmpty()) {
            savedLocationsText.text = "No locations saved yet"
        } else {
            savedLocationsText.text = locations.joinToString("\n")
        }
    }
    
    private fun setupMap() {
        // Configure the map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        // Set default zoom and center point (New York City coordinates as example)
        val mapController = mapView.controller
        mapController.setZoom(12.0)
        val startPoint = org.osmdroid.util.GeoPoint(40.7128, -74.0060)
        mapController.setCenter(startPoint)
        
        // Add compass
        val compassOverlay = CompassOverlay(
            applicationContext,
            InternalCompassOrientationProvider(applicationContext),
            mapView
        )
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)
    }
    
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        updateLocationsList()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    
    private fun playVideo() {
        // For demonstration, we'll use a sample video URL
        // In a real app, you would include a video file in your resources
        try {
            // val videoPath = "android.resource://" + packageName + "/" + R.raw.sample_video
            // videoView.setVideoURI(Uri.parse(videoPath))
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = false
                videoView.start()
            }
            videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
                false
            }
            
            // Apply animation to video view
            val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            videoView.startAnimation(fadeInAnimation)
        } catch (e: Exception) {
            Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun playSound() {
        try {
            // Release any previous MediaPlayer instance
            mediaPlayer?.release()
            
            // Create a new MediaPlayer and set the sound file
            // mediaPlayer = MediaPlayer.create(this, R.raw.sample_sound)
            // mediaPlayer?.start()
            
            // Apply animation to the sound button
            val pulseAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            btnPlaySound.startAnimation(pulseAnimation)
        } catch (e: Exception) {
            Toast.makeText(this, "Sound file not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver
        try {
            unregisterReceiver(airplaneModeReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        
        // Release MediaPlayer resources
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
