# HouseSearch

## Requirements

1. allow adding a property
2. allow marking a property as discounted, missed, viewed
3. display a map which has markers for each property
4. a list of properties as cards
5. a list of budget items as a checklist that will calculate the sum of the total checked items
6. a list of required items where each item has a checkbox
7. display a web view of the associated property site (rightmove, zoopla or onthemarket)


## where:
a Property is an class that has the following fields:
* id
* name
* rightmoveNumber
* zooplaNumber
* onthemarketNumber
* latitude
* longitude
* price
* category

a BudgetItem is an class that has the following fields:
* id
* name
* category
* cost

a RequiredFeature is an class that has the following fields:
* id
* name
* priority


