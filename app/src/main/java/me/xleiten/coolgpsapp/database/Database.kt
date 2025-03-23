package me.xleiten.coolgpsapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.xleiten.coolgpsapp.area.Position
import java.sql.Timestamp
import java.util.Date

open class Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Measurements"
        private const val DATABASE_VERSION = 1

        fun dateToTimestamp(date: Date): String {
            return Timestamp(date.time).toString()
        }
    }

    val resultsTable: ResultsTable = ResultsTable()
    val pointsTable: PointsTable = PointsTable()

    override fun onCreate(db: SQLiteDatabase) {
        this.resultsTable.createTable(db)
        this.pointsTable.createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        this.resultsTable.dropTable(db)
        this.pointsTable.dropTable(db)
        this.onCreate(db)
    }

    fun getSavedPointsAmount(): Int {
        return this.pointsTable.getAmountOfPoints(this.readableDatabase)
    }

    fun clearPoints() {
        this.pointsTable.clear(this.writableDatabase)
    }

    fun saveResult(areaName: String, area: Double, date: String) {
        this.resultsTable.saveResult(this.writableDatabase, areaName, area, date)
    }

    fun savePoint(latitude: Double, longitude: Double, date: String) {
        this.pointsTable.savePoint(this.writableDatabase, latitude, longitude, date)
    }

    fun getAllPoints(): List<Position> {
        return this.pointsTable.getPoints(this.readableDatabase)
    }

}