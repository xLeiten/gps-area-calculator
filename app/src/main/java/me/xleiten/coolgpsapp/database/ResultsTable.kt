package me.xleiten.coolgpsapp.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class ResultsTable : DBTable("Results") {
    val columnName: String = this.addColumn("name", "VARCHAR(255)")
    val columnDate: String = this.addColumn("date", "DATETIME")
    val columnArea: String = this.addColumn("area", "REAL")

    fun saveResult(db: SQLiteDatabase, name: String, area: Float, date: String) {
        val values = ContentValues();
        values.put(columnName, name)
        values.put(columnArea, area)
        values.put(columnDate, date)
        db.insert(this.name, null, values)
    }
}