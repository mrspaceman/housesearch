package uk.co.droidinactu.housesearch.model

// Data classes defined according to Requirements.md
// Property fields: id, name, rightmoveNumber, zooplaNumber, onthemarketNumber, latitude, longitude, price, category
// BudgetItem fields: id, name, category, cost
// RequiredFeature fields: id, name, priority

data class Property(
    val id: Long,
    val name: String,
    val rightmoveNumber: String? = null,
    val zooplaNumber: String? = null,
    val onthemarketNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val price: Long? = null,
    val category: String? = null,
)

data class BudgetItem(
    val id: Long,
    val name: String,
    val category: String? = null,
    val cost: Long = 0L,
)

data class RequiredFeature(
    val id: Long,
    val name: String,
    val priority: Int = 0,
)

data class FavoritePlace(
    val id: Long,
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

// Additional helper class to satisfy requirement of marking properties without altering Property definition
// This keeps statuses separate from the Property fields listed in Requirements.md

data class PropertyStatus(
    val propertyId: Long,
    val discounted: Boolean = false,
    val missed: Boolean = false,
    val viewed: Boolean = false,
)
