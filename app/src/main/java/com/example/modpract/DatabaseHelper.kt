package com.example.modpract

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "LocationsDB"
        private const val TABLE_LOCATIONS = "locations"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
    }

    // Update a location
    fun updateLocation(id: Int, newName: String): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, newName)

        // Update row
        val rowsAffected = db.update(TABLE_LOCATIONS, values, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return rowsAffected
    }

    // Delete a location
    fun deleteLocation(id: Int): Int {
        val db = this.writableDatabase

        // Delete row
        val rowsDeleted = db.delete(TABLE_LOCATIONS, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return rowsDeleted
    }

    // Get all locations with IDs
    fun getAllLocationsWithIds(): List<Pair<Int, String>> {
        val locationsList = ArrayList<Pair<Int, String>>()
        val selectQuery = "SELECT * FROM $TABLE_LOCATIONS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(KEY_ID)
                val nameIndex = cursor.getColumnIndex(KEY_NAME)
                if (idIndex != -1 && nameIndex != -1) {
                    val id = cursor.getInt(idIndex)
                    val name = cursor.getString(nameIndex)
                    locationsList.add(Pair(id, name))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return locationsList
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_LOCATIONS ($KEY_ID INTEGER PRIMARY KEY, $KEY_NAME TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        onCreate(db)
    }

    // Add a new location
    fun addLocation(locationName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, locationName)
        
        // Insert row
        val id = db.insert(TABLE_LOCATIONS, null, values)
        db.close()
        return id
    }

    // Get all locations
    fun getAllLocations(): List<String> {
        val locationsList = ArrayList<String>()
        val selectQuery = "SELECT * FROM $TABLE_LOCATIONS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        
        if (cursor.moveToFirst()) {
            do {
                val nameIndex = cursor.getColumnIndex(KEY_NAME)
                if (nameIndex != -1) {
                    locationsList.add(cursor.getString(nameIndex))
                }
            } while (cursor.moveToNext())
        }
        
        cursor.close()
        db.close()
        return locationsList
    }
}
