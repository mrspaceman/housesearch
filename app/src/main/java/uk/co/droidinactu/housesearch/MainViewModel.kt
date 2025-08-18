package uk.co.droidinactu.housesearch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import uk.co.droidinactu.housesearch.model.*

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

    private val _favoritePlace = mutableStateListOf<FavoritePlace>()
    val favoritePlace: List<FavoritePlace> = _favoritePlace

    var selectedPropertyId: Long? by mutableStateOf(null)

    init {
        _favoritePlace.addAll(
            listOf(
                FavoritePlace(1, "Wendy", 51.63073619219499, -3.9562151983634304),
                FavoritePlace(1, "Rob", 51.664432908385415, -4.040572853936915),
            )
        )
        _budgetItems.addAll(
            listOf(
                BudgetItem(1, "Survey", "Buying", 600),
                BudgetItem(2, "Stamp Duty", "Buying", 15000),
                BudgetItem(3, "Moving", "Buying", 1200),
                BudgetItem(1, "decorating", "general", 1500),
                BudgetItem(2, "fitted wardrobes", "general", 7500),
                BudgetItem(3, "carpets", "general", 7500),
                BudgetItem(4, "New", "kitchen", 10000),
                BudgetItem(5, "doors", "kitchen", 2500),
                BudgetItem(6, "oven", "kitchen", 500),
                BudgetItem(7, "new", "bathroom", 10000),
                BudgetItem(8, "double glazing", "general", 15000),
                BudgetItem(9, "rewire", "electrics", 10000),
                BudgetItem(10, "alterations", "electrics", 2000),
                BudgetItem(11, "boiler", "heating", 7000),
                BudgetItem(12, "full system", "heating", 12000),
                BudgetItem(13, "repair", "roof", 5000),
                BudgetItem(14, "new", "roof", 15000),
                BudgetItem(15, "garage", "general", 10000),
                BudgetItem(16, "workshop", "general", 10000),
                BudgetItem(17, "greenhouse", "general", 6000),
                BudgetItem(18, "conservatory", "general", 9000),
                BudgetItem(19, "septic tank", "general", 25000),
                BudgetItem(20, "bore hole", "general", 30000),
                BudgetItem(21, "air conditioning", "general", 3000),
            )
        )

        _requiredItems.addAll(
            listOf(
                RequiredFeature(1, "Broadband & data", 1),
                RequiredFeature(2, "Detached", 1),
                RequiredFeature(3, "3 bedrooms", 1),
                RequiredFeature(4, "Kitchen", 1),
                RequiredFeature(5, "Lounge (separate from kitchen)", 1),
                RequiredFeature(6, "Utility Room (to bring in muddy dogs)", 1),
                RequiredFeature(7, "Hobby Room", 1),
                RequiredFeature(8, "Office", 1),
                RequiredFeature(9, "Garage", 1),
                RequiredFeature(10, "Workshop", 1),
                RequiredFeature(11, "Reading Room", 1),
                RequiredFeature(12, "Music Room", 1),
                RequiredFeature(13, "Exercise Room", 1),
                RequiredFeature(14, "Not too near River or coast (flooding)", 1),
                RequiredFeature(15, "Large Garden", 1),
                RequiredFeature(16, "Dog walking area", 1),
                RequiredFeature(17, "room for fish tanks", 1),
                RequiredFeature(18, "running routes", 1),
                RequiredFeature(19, "cycling routes", 1),
                RequiredFeature(20, "in quiet village", 1),
                RequiredFeature(21, "on quiet road", 1),
                RequiredFeature(22, "Quiet location", 1),
                RequiredFeature(23, "Conservatory", 5),
                RequiredFeature(24, "Greenhouse", 5),
                RequiredFeature(25, "Workshop", 5),
                RequiredFeature(26, "Top of hill / views", 5),
                RequiredFeature(27, "Leisure Centre / Swimming Pool", 5),
                RequiredFeature(28, "Running / Triathlon clubs", 5),
                RequiredFeature(29, "Jigsaws", 5),
                RequiredFeature(30, "Sewing", 5),
                RequiredFeature(31, "Model Making", 5),
                RequiredFeature(32, "Reading / Music", 5),
                RequiredFeature(33, "Motorbike servicing", 5),
                RequiredFeature(34, "Woodwork", 5),
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
