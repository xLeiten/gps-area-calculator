package test.app.areacalculator.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import test.app.areacalculator.figures.Position
import java.sql.Timestamp
import java.util.Date

open class Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AreasDatabase"
        private const val DATABASE_VERSION = 1
        private const val COLUMN_ID = "ID"
        private const val SAVED_POINTS_TABLE = "saved_point"

        fun dateToTimestamp(date: Date): String {
            return Timestamp(date.time).toString()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS $SAVED_POINTS_TABLE (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude REAL," +
                "longitude REAL," +
                "date DATETIME)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $SAVED_POINTS_TABLE")
        this.onCreate(db)
    }

    fun getSavedPointsAmount(): Int {
        val result = this.readableDatabase.rawQuery("SELECT $COLUMN_ID FROM $SAVED_POINTS_TABLE", null)
        val rows = result.count
        result.close()
        return rows
    }

    fun clearPoints() {
        this.writableDatabase.delete(SAVED_POINTS_TABLE, null, null)
    }

    fun savePoint(latitude: Float, longitude: Float, date: String) {
        val values = ContentValues();
        values.put("latitude", latitude)
        values.put("longitude", longitude)
        values.put("date", date)
        this.writableDatabase.insert(SAVED_POINTS_TABLE, null, values)
    }

    fun getSavedPoints(): List<Position> {
        val mutableList: MutableList<Position> = mutableListOf()
        val result = this.readableDatabase.rawQuery("SELECT * FROM $SAVED_POINTS_TABLE", null)
        result.moveToFirst()
        do {
            val latitude = result.getDouble(result.getColumnIndexOrThrow("latitude"))
            val longitude = result.getDouble(result.getColumnIndexOrThrow("longitude"))
            mutableList.add(Position(latitude, longitude))
        } while (result.moveToNext())
        result.close()
        return mutableList.toList()
    }

}