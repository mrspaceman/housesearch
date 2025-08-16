package uk.co.droidinactu.housesearch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import uk.co.droidinactu.housesearch.model.BudgetItem
import uk.co.droidinactu.housesearch.model.Property
import uk.co.droidinactu.housesearch.model.PropertyStatus
import uk.co.droidinactu.housesearch.model.RequiredFeature

class MainViewModel : ViewModel() {
    // In-memory lists to satisfy requirements (no persistence for minimal change)
    private val _properties = mutableStateListOf<Property>()
    val properties: List<Property> = _properties

    private val _propertyStatuses = mutableStateListOf<PropertyStatus>()
    val propertyStatuses: List<PropertyStatus> = _propertyStatuses

    private val _budgetItems = mutableStateListOf<BudgetItem>()
    val budgetItems: List<BudgetItem> = _budgetItems

    private val _requiredItems = mutableStateListOf<RequiredFeature>()
    val requiredItems: List<RequiredFeature> = _requiredItems

    var selectedPropertyId: Long? by mutableStateOf(null)

    init {
        // seed with a couple of demo items
//        addProperty(
//            Property(
//                id = 1, name = "Sample House 1", rightmoveNumber = "12345678",
//                latitude = 51.5074, longitude = -0.1278, price = 650_000, category = "Detached"
//            )
//        )
//        addProperty(
//            Property(
//                id = 2, name = "Sample House 2", zooplaNumber = "87654321",
//                latitude = 51.5007, longitude = -0.1246, price = 550_000, category = "Semi"
//            )
//        )

        _budgetItems.addAll(
            listOf(
                BudgetItem(1, "Survey", "Buying", 600),
                BudgetItem(2, "Stamp Duty", "Buying", 15000),
                BudgetItem(3, "Moving", "Buying", 1200),
            )
        )

        _requiredItems.addAll(
            listOf(
                RequiredFeature(1, "Garden", 3),
                RequiredFeature(2, "Parking", 2),
                RequiredFeature(3, "Near Station", 1),
            )
        )
    }

    fun addProperty(property: Property) {
        _properties.add(property)
        // Initialize status entry if not present
        if (_propertyStatuses.none { it.propertyId == property.id }) {
            _propertyStatuses.add(PropertyStatus(propertyId = property.id))
        }
        if (selectedPropertyId == null) selectedPropertyId = property.id
    }

    fun toggleDiscounted(id: Long) = updateStatus(id) { it.copy(discounted = !it.discounted) }
    fun toggleMissed(id: Long) = updateStatus(id) { it.copy(missed = !it.missed) }
    fun toggleViewed(id: Long) = updateStatus(id) { it.copy(viewed = !it.viewed) }

    private fun updateStatus(propertyId: Long, update: (PropertyStatus) -> PropertyStatus) {
        val idx = _propertyStatuses.indexOfFirst { it.propertyId == propertyId }
        if (idx >= 0) {
            _propertyStatuses[idx] = update(_propertyStatuses[idx])
        } else {
            _propertyStatuses.add(update(PropertyStatus(propertyId)))
        }
    }

    fun setSelectedProperty(id: Long?) {
        selectedPropertyId = id
    }

    // Budget checklist support
    private val _checkedBudgetIds = mutableStateListOf<Long>()
    val checkedBudgetIds: List<Long> = _checkedBudgetIds

    fun toggleBudgetChecked(id: Long) {
        if (_checkedBudgetIds.contains(id)) _checkedBudgetIds.remove(id) else _checkedBudgetIds.add(id)
    }

    fun totalCheckedBudget(): Long = _budgetItems.filter { _checkedBudgetIds.contains(it.id) }.sumOf { it.cost }

    // Required items checklist
    private val _checkedRequiredIds = mutableStateListOf<Long>()
    val checkedRequiredIds: List<Long> = _checkedRequiredIds
    fun toggleRequiredChecked(id: Long) {
        if (_checkedRequiredIds.contains(id)) _checkedRequiredIds.remove(id) else _checkedRequiredIds.add(id)
    }
}
