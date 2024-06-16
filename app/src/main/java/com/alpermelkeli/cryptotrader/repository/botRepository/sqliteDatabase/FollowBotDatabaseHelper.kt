package com.alpermelkeli.cryptotrader.repository.botRepository.sqliteDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.alpermelkeli.cryptotrader.model.FollowBotManager

class FollowBotDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), DataBaseHelper<FollowBotEntity> {
    companion object {
        private const val DATABASE_NAME = "follow_bots.db"
        private const val DATABASE_VERSION = 1  // Versiyon numarasını burada başlatabilirsiniz
        private const val TABLE_NAME = "followBots"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FIRST_PAIR_NAME = "firstPairName"
        private const val COLUMN_SECOND_PAIR_NAME = "secondPairName"
        private const val COLUMN_PAIR_NAME = "pairName"
        private const val COLUMN_THRESHOLD = "threshold"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_EXCHANGE_MARKET = "exchangeMarket"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_API_KEY = "apiKey"
        private const val COLUMN_SECRET_KEY = "secretKey"
        private const val COLUMN_OPEN_POSITION = "openPosition"
        private const val COLUMN_DISTANCE_INTERVAL = "distanceInterval"
        private const val COLUMN_FOLLOW_INTERVAL = "followInterval"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_FIRST_PAIR_NAME TEXT,
            $COLUMN_SECOND_PAIR_NAME TEXT,
            $COLUMN_PAIR_NAME TEXT,
            $COLUMN_THRESHOLD REAL,
            $COLUMN_AMOUNT REAL,
            $COLUMN_EXCHANGE_MARKET TEXT,
            $COLUMN_STATUS TEXT,
            $COLUMN_API_KEY TEXT,
            $COLUMN_SECRET_KEY TEXT,
            $COLUMN_OPEN_POSITION INTEGER,
            $COLUMN_DISTANCE_INTERVAL REAL,
            $COLUMN_FOLLOW_INTERVAL REAL
        )
    """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versiyon güncellemeleri için gerekli kodlar buraya eklenebilir
    }

    override fun insertBot(bot: FollowBotEntity) {
        val db = writableDatabase
        val insertQuery = """
        INSERT OR REPLACE INTO $TABLE_NAME ($COLUMN_ID, $COLUMN_FIRST_PAIR_NAME, $COLUMN_SECOND_PAIR_NAME, $COLUMN_PAIR_NAME, $COLUMN_THRESHOLD, $COLUMN_AMOUNT, $COLUMN_EXCHANGE_MARKET, $COLUMN_STATUS, $COLUMN_API_KEY, $COLUMN_SECRET_KEY, $COLUMN_OPEN_POSITION, $COLUMN_DISTANCE_INTERVAL, $COLUMN_FOLLOW_INTERVAL)
        VALUES ('${bot.id}', '${bot.firstPairName}', '${bot.secondPairName}', '${bot.pairName}', ${bot.threshold}, ${bot.amount}, '${bot.exchangeMarket}', '${bot.status}', '${bot.apiKey}', '${bot.secretKey}', ${if (bot.openPosition) 1 else 0}, ${bot.distanceInterval}, ${bot.followInterval})
    """
        db.execSQL(insertQuery)
        db.close()
    }

    override fun getAllBots(): List<FollowBotEntity> {
        val bots = mutableListOf<FollowBotEntity>()
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val firstPairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_PAIR_NAME))
                val secondPairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SECOND_PAIR_NAME))
                val pairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAIR_NAME))
                val threshold = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_THRESHOLD))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
                val exchangeMarket = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXCHANGE_MARKET))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                val apiKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_KEY))
                val secretKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SECRET_KEY))
                val openPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OPEN_POSITION)) == 1
                val distanceInterval = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE_INTERVAL))
                val followInterval = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOLLOW_INTERVAL))
                val bot = FollowBotEntity(
                    id,
                    firstPairName,
                    secondPairName,
                    pairName,
                    threshold,
                    amount,
                    exchangeMarket,
                    status,
                    apiKey,
                    secretKey,
                    openPosition,
                    distanceInterval,
                    followInterval
                )
                bots.add(bot)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bots
    }

    override fun getBotById(id: String): FollowBotEntity? {
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = '$id'"
        val cursor = db.rawQuery(selectQuery, null)
        var bot: FollowBotEntity? = null
        if (cursor.moveToFirst()) {
            val firstPairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_PAIR_NAME))
            val secondPairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SECOND_PAIR_NAME))
            val pairName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAIR_NAME))
            val threshold = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_THRESHOLD))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
            val exchangeMarket = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXCHANGE_MARKET))
            val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
            val apiKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_KEY))
            val secretKey = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SECRET_KEY))
            val openPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OPEN_POSITION)) == 1
            val distanceInterval = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE_INTERVAL))
            val followInterval = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FOLLOW_INTERVAL))
            bot = FollowBotEntity(
                id,
                firstPairName,
                secondPairName,
                pairName,
                threshold,
                amount,
                exchangeMarket,
                status,
                apiKey,
                secretKey,
                openPosition,
                distanceInterval,
                followInterval
            )
        }
        cursor.close()
        db.close()
        return bot
    }

    override fun removeBotById(id: String) {
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val statement = db.compileStatement(deleteQuery)
        statement.bindString(1, id)
        statement.executeUpdateDelete()
        db.close()
    }

    override fun deleteAllBots() {
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $TABLE_NAME"
        db.execSQL(deleteQuery)
        db.close()
    }
}
