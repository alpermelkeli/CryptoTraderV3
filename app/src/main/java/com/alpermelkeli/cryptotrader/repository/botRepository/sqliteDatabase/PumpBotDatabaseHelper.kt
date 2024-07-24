package com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PumpBotDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "pump_bot.db"
        private const val DATABASE_VERSION = 2 // Versiyonu 2 olarak güncelledik
        private const val TABLE_NAME = "pumpBot"
        private const val COLUMN_ID = "id"
        private const val COLUMN_LIMIT = "botLimit"
        private const val COLUMN_OPEN_POSITION = "openPosition"
        private const val COLUMN_PAIR_NAME = "pairName"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_ACTIVE = "active"
        private const val COLUMN_INTERVAL = "interval"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LIMIT REAL,
                $COLUMN_OPEN_POSITION INTEGER,
                $COLUMN_PAIR_NAME TEXT,
                $COLUMN_AMOUNT REAL,
                $COLUMN_ACTIVE INTEGER,
                $COLUMN_INTERVAL TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun addPumpBot(bot: PumpBotEntity) {
        val db = writableDatabase
        val insertQuery = """
            INSERT INTO $TABLE_NAME ($COLUMN_LIMIT, $COLUMN_OPEN_POSITION, $COLUMN_PAIR_NAME, $COLUMN_AMOUNT, $COLUMN_ACTIVE,$COLUMN_INTERVAL)
            VALUES (${bot.limit}, ${if (bot.openPosition) 1 else 0}, '${bot.pairName}', ${bot.amount}, ${if(bot.active) 1 else 0}, '${bot.interval}')
        """
        db.execSQL(insertQuery)
        db.close()
    }

    fun getPumpBot(): PumpBotEntity? {
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME LIMIT 1"
        val cursor = db.rawQuery(selectQuery, null)
        var bot: PumpBotEntity? = null
        if (cursor.moveToFirst()) {
            val limit = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LIMIT))
            val openPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OPEN_POSITION)) == 1
            val pairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAIR_NAME))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
            val active = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTIVE)) == 1
            val interval = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTERVAL))
            bot = PumpBotEntity(limit, openPosition, pairName, amount,active,interval)
        }
        cursor.close()
        db.close()
        return bot
    }

    private fun removePumpBot() {
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $TABLE_NAME"
        db.execSQL(deleteQuery)
        db.close()
    }

    fun updatePumpBot(bot:PumpBotEntity){
        removePumpBot()
        addPumpBot(bot)
    }
}
