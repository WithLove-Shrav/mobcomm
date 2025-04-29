package com.example.modpract

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest



class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var getLocationBtn: Button
    private lateinit var locationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val nameInput = findViewById<EditText>(R.id.editName)
        val emailInput = findViewById<EditText>(R.id.editEmail)
        val submitBtn = findViewById<Button>(R.id.submitBtn)
        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)
        submitBtn.setOnClickListener {
            //popupmenu
            val pop=PopupMenu(this,submitBtn)
            pop.menuInflater.inflate(R.menu.popup_menu,pop.menu)
            pop.setOnMenuItemClickListener {
                item->
                when(item.itemId){
                    R.id.popup_edit -> {
                        Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.popup_delete -> {
                        Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false

                }


            }
            pop.show()
        }

        val myTextView = findViewById<TextView>(R.id.myTextView)
        registerForContextMenu(myTextView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocationBtn = findViewById(R.id.getLocationBtn)
        locationText = findViewById(R.id.locationText)

        getLocationBtn.setOnClickListener {
            getLocation()
        }
        
        // Add navigation to MainActivity3 (Map page)
        val btnGoToMap = findViewById<Button>(R.id.btnGoToMap)
        btnGoToMap.setOnClickListener {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
    }
    //optionsmenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.menu_about->{
                Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_settings -> {
                val i=Intent(this,MainActivity2::class.java)
                startActivity(i)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //contextmenu
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu,menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when(item.itemId)
        {
            R.id.menu_edit -> {
                Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_delete -> {
                Toast.makeText(this, "Delete clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)

        }

    }
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        locationText.text = "Latitude: $lat\nLongitude: $lon"
                    } else {
                        locationText.text = "Location not available"
                    }
                }
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}