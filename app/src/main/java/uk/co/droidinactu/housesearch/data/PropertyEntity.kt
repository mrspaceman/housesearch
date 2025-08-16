package uk.co.droidinactu.housesearch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val rightmoveNumber: String? = null,
    val zooplaNumber: String? = null,
    val onthemarketNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val price: Long? = null,
    val category: String? = null,
)