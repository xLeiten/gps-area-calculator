package me.xleiten.coolgpsapp.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import me.xleiten.coolgpsapp.area.Position

class PointsTable : DBTable("Results") {
    val columnLatitude: String = this.addColumn("name", "REAL")
    val columnLongitude: String = this.addColumn("area", "REAL")
    val columnDate: String = this.addColumn("date", "DATETIME")

    fun savePoint(db: SQLiteDatabase, latitude: Float, longitude: Float, date: String) {
        val values = ContentValues();
        values.put(columnLatitude, latitude)
        values.put(columnLongitude, longitude)
        values.put(columnDate, date)
        db.insert(this.name, null, values)
    }

    fun getAmountOfPoints(db: SQLiteDatabase): Int {
        val result = db.rawQuery("SELECT $COLUMN_ID FROM ${this.name}", null)
        val rows = result.count
        result.close()
        return rows
    }

    fun getPoints(db: SQLiteDatabase): List<Position> {
        val mutableList: MutableList<Position> = mutableListOf()
        val result = db.rawQuery("SELECT * FROM ${this.name}", null)
        result.moveToFirst()
        do {
            val latitude = result.getDouble(result.getColumnIndexOrThrow(this.columnLatitude))
            val longitude = result.getDouble(result.getColumnIndexOrThrow(this.columnLongitude))
            mutableList.add(Position(latitude, longitude))
        } while (result.moveToNext())
        result.close()
        return mutableList.toList()
    }
}