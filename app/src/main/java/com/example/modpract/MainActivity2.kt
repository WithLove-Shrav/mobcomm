package com.example.modpract

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val prog=findViewById<ProgressBar>(R.id.prog)
        //alertdialog
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("Yes")
        {
            dialog, which->
            Toast.makeText(this,"yesss",Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dialog, which ->
             finish()
        }
        builder.setNeutralButton("Cancel") { dialog, which ->
            dialog.dismiss()}

        val button = findViewById<Button>(R.id.alert)
        button.setOnClickListener {
            //progressbar
            prog.visibility=View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                prog.visibility=View.GONE
                Toast.makeText(this, "Finished Loading", Toast.LENGTH_SHORT).show()
            },3000)
            builder.show()
        }
        //datepicker
        val date=findViewById<Button>(R.id.dateBtn)
        date.setOnClickListener {
            val c=Calendar.getInstance()
            val year=c.get(Calendar.YEAR)
            val month=c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                Toast.makeText(this, "Date: $selectedDay/${selectedMonth + 1}/$selectedYear", Toast.LENGTH_SHORT).show()
            }, year, month, day)

            dpd.show()
        }
        //timepicker
        val timeBtn = findViewById<Button>(R.id.timeBtn)
        timeBtn.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            val tpd = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                Toast.makeText(this, "Time: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
            }, hour, minute, true) // true = 24-hour format

            tpd.show()
        }
        //notifications
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            val name="mychannel"
            val des="description"
            val imp=NotificationManager.IMPORTANCE_DEFAULT
            val channel=NotificationChannel("notify_id",name,imp)
            channel.description=des
            val notman=getSystemService(NotificationManager::class.java)

            notman.createNotificationChannel(channel)

        }
        val sendBtn = findViewById<Button>(R.id.sendBtn)
        sendBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        101
                    )
                    Toast.makeText(this, "Notification permission required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val builder = NotificationCompat.Builder(this, "notify_id")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Reminder Set!")
                .setContentText("Scheduled for  at ")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(1001, builder.build())
            }
        }
        //sharedpref
        val editName = findViewById<EditText>(R.id.editName)
        val saveBtn = findViewById<Button>(R.id.saveBtn)
       val  showName = findViewById<TextView>(R.id.showName)

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // Show saved name on launch
        val savedName = sharedPref.getString("username", "No name")
        showName.text = "Welcome back, $savedName"

        // Save name on click
        saveBtn.setOnClickListener {
            val name = editName.text.toString()
            val editor = sharedPref.edit()
            editor.putString("username", name)
            editor.apply()
            showName.text = "Saved! Hello, $name"
        }


    }



}