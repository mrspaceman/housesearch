package uk.co.droidinactu.housesearch.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PropertyEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
}