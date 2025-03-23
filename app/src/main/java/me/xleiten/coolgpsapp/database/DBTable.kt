package me.xleiten.coolgpsapp.database

import android.database.sqlite.SQLiteDatabase

abstract class DBTable(val name: String) {

    companion object {
        const val COLUMN_ID = "ID"
    }

    private val columns: MutableMap<String, TableColumn> = mutableMapOf(
        Pair(
            COLUMN_ID, TableColumn(
                COLUMN_ID, "INTEGER", "PRIMARY KEY AUTOINCREMENT"
            )
        )
    )

    fun createTable(db: SQLiteDatabase) {
        val columns =
            this.columns.map { it -> "${it.key} ${it.value.type} ${it.value.params ?: ""}" }
                .joinToString(", ")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${this.name} (${columns})")
    }

    fun dropTable(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS ${this.name}")
    }

    fun clear(db: SQLiteDatabase) {
        db.delete(this.name, null, null)
    }

    final fun addColumn(name: String, type: String, params: String? = null): String {
        val column = this.columns[name]
        if (column != null) {
            throw IllegalArgumentException("Column $name already exist")
        } else {
            this.columns[name] = TableColumn(name, type, params)
        }
        return name
    }

}