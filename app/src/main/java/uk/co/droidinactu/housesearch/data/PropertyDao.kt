package uk.co.droidinactu.housesearch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PropertyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PropertyEntity>)

    @Query("SELECT * FROM properties")
    suspend fun getAll(): List<PropertyEntity>

    @Query("DELETE FROM properties")
    suspend fun clear()
}