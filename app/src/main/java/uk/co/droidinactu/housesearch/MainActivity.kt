package uk.co.droidinactu.housesearch

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import uk.co.droidinactu.housesearch.model.Property
import uk.co.droidinactu.housesearch.ui.theme.HouseSearchTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            HouseSearchTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(vm: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Properties", "Map", "Budget", "Required", "Web")

    Scaffold(topBar = {
        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(top = 8.dp)) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> PropertiesScreen(vm)
                1 -> MapScreen(vm)
                2 -> BudgetScreen(vm)
                3 -> RequiredScreen(vm)
                4 -> PropertyWebScreen(vm)
            }
        }
    }
}

@Composable
fun PropertiesScreen(vm: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    try {
                        val items = uk.co.droidinactu.housesearch.data.CsvImport.parsePropertiesFromAssets(context)
                        // Insert into DB
                        val db = uk.co.droidinactu.housesearch.data.DatabaseProvider.get(context)
                        val entities = items.map { p ->
                            uk.co.droidinactu.housesearch.data.PropertyEntity(
                                id = p.id,
                                name = p.name,
                                rightmoveNumber = p.rightmoveNumber,
                                zooplaNumber = p.zooplaNumber,
                                onthemarketNumber = p.onthemarketNumber,
                                latitude = p.latitude,
                                longitude = p.longitude,
                                price = p.price,
                                category = p.category,
                            )
                        }
                        db.propertyDao().insertAll(entities)
                        // Update in-memory list avoiding duplicates
                        val existingIds = vm.properties.map { it.id }.toSet()
                        items.filter { it.id !in existingIds }.forEach { vm.addProperty(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }) { Text("Import CSV") }
        }
        Spacer(Modifier.height(8.dp))
        AddPropertyForm(onAdd = { vm.addProperty(it) })
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(vm.properties) { prop ->
                val status = vm.propertyStatuses.find { it.propertyId == prop.id }
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = prop.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Price: " + (prop.price?.toString() ?: "N/A"))
                        Text(text = "Category: " + (prop.category ?: ""))
                        if (prop.rightmoveNumber != null) {
                            Text(text = "Site: Rightmove")
                        } else if (prop.zooplaNumber != null) {
                            Text(text = "Site: Zoopla")
                        } else if (prop.onthemarketNumber != null) {
                            Text(text = "Site: OnTheMarket")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                label = { Text("Discounted") },
                                selected = status?.discounted == true,
                                onClick = { vm.toggleDiscounted(prop.id) })
                            FilterChip(
                                label = { Text("Missed") },
                                selected = status?.missed == true,
                                onClick = { vm.toggleMissed(prop.id) })
                            FilterChip(
                                label = { Text("Viewed") },
                                selected = status?.viewed == true,
                                onClick = { vm.toggleViewed(prop.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPropertyForm(onAdd: (Property) -> Unit) {
    var idText by remember { mutableStateOf(TextFieldValue("")) }
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf(TextFieldValue("")) }
    var priceText by remember { mutableStateOf(TextFieldValue("")) }
    var latText by remember { mutableStateOf(TextFieldValue("")) }
    var lonText by remember { mutableStateOf(TextFieldValue("")) }
    var rightmove by remember { mutableStateOf(TextFieldValue("")) }
    var zoopla by remember { mutableStateOf(TextFieldValue("")) }
    var otm by remember { mutableStateOf(TextFieldValue("")) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Add Property", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(idText, onValueChange = { idText = it }, label = { Text("ID (number)") })
        OutlinedTextField(name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(category, onValueChange = { category = it }, label = { Text("Category") })
        OutlinedTextField(priceText, onValueChange = { priceText = it }, label = { Text("Price (number)") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                latText,
                onValueChange = { latText = it },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                lonText,
                onValueChange = { lonText = it },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                rightmove,
                onValueChange = { rightmove = it },
                label = { Text("Rightmove #") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                zoopla,
                onValueChange = { zoopla = it },
                label = { Text("Zoopla #") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                otm,
                onValueChange = { otm = it },
                label = { Text("OnTheMarket #") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(onClick = {
            val id = idText.text.toLongOrNull() ?: System.currentTimeMillis()
            val price = priceText.text.toLongOrNull()
            val lat = latText.text.toDoubleOrNull()
            val lon = lonText.text.toDoubleOrNull()
            onAdd(
                Property(
                    id = id,
                    name = name.text.ifBlank { "Property $id" },
                    rightmoveNumber = rightmove.text.ifBlank { null },
                    zooplaNumber = zoopla.text.ifBlank { null },
                    onthemarketNumber = otm.text.ifBlank { null },
                    latitude = lat,
                    longitude = lon,
                    price = price,
                    category = category.text.ifBlank { null }
                )
            )
            idText = TextFieldValue("")
            name = TextFieldValue("")
            category = TextFieldValue("")
            priceText = TextFieldValue("")
            latText = TextFieldValue("")
            lonText = TextFieldValue("")
            rightmove = TextFieldValue("")
            zoopla = TextFieldValue("")
            otm = TextFieldValue("")
        }) { Text("Add") }
    }
}

@Composable
fun MapScreen(vm: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val properties = vm.properties
    AndroidView(factory = { ctx ->
        // Configure osmdroid
        Configuration.getInstance().userAgentValue = ctx.packageName
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(9.0)
            setBuiltInZoomControls(true);
            setMultiTouchControls(true);
            val first = properties.firstOrNull { it.latitude != null && it.longitude != null }
            val center = if (first != null) GeoPoint(first.latitude!!, first.longitude!!) else GeoPoint(
                51.63073619219499,
                -3.9562151983634304
            )
            controller.setCenter(center)

            // Add markers
            overlays.clear()
            properties.filter { it.latitude != null && it.longitude != null && it.category != "discounted" }
                .forEach { p ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(p.latitude!!, p.longitude!!)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.setTextIcon("${p.id}")
                    marker.title = "${p.name}"
                    overlays.add(marker)
                }
        }
    }, update = { map ->
        // Update overlays when properties change
        map.overlays.removeAll { it is Marker }
        properties.filter { it.latitude != null && it.longitude != null && it.category != "discounted" }.forEach { p ->
            val markerBitmap =
                drawTextToBitmap(context, R.drawable.mapmarker1, "${p.id}")
                    .toDrawable(context.getResources())

            val marker = Marker(map)
            marker.position = GeoPoint(p.latitude!!, p.longitude!!)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.setIcon(markerBitmap)
            marker.title = "${p.name}"
            map.overlays.add(marker)
        }
        map.invalidate()
    }, modifier = Modifier.fillMaxSize())
}

fun drawTextToBitmap(
    gContext: Context,
    gResId: Int,
    gText: String
): Bitmap {
    val resources = gContext.getResources()
    val scale = resources.getDisplayMetrics().density
    var bitmap: Bitmap =
        BitmapFactory.decodeResource(resources, gResId)

    var bitmapConfig =
        bitmap.getConfig()
    // set default bitmap config if none
    if (bitmapConfig == null) {
        bitmapConfig = Bitmap.Config.ARGB_8888
    }
    // resource bitmaps are immutable,
    // so we need to convert it to mutable one
    bitmap = bitmap.copy(bitmapConfig, true)

    val canvas: Canvas = Canvas(bitmap)
    // new antialised Paint
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // text color - #3D3D3D
    paint.setColor(Color.rgb(61, 61, 61))
    // text size in pixels
    paint.setTextSize((14 * scale).toInt().toFloat())
    // text shadow
    paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

    // draw text to the Canvas center
    val bounds: Rect = Rect()
    paint.getTextBounds(gText, 0, gText.length, bounds)
    val x: Float = ((bitmap.getWidth() - bounds.width()) / 2).toFloat()
    val y: Float = ((bitmap.getHeight() + bounds.height()) / 2).toFloat()

    canvas.drawText(gText, x, y, paint)

    return bitmap
}

@Composable
fun BudgetScreen(vm: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Budget Checklist", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(2.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(vm.budgetItems) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = vm.checkedBudgetIds.contains(item.id),
                        onCheckedChange = { vm.toggleBudgetChecked(item.id) })
                    Row(Modifier.padding(start = 2.dp)) {
                        Text(item.category ?: "General")
                        Text(" : ")
                        Text(item.name)
                        Text(" : ")
                        Text("Cost: ${item.cost}")
                    }
                }
            }
        }
        Text("Total: ${vm.totalCheckedBudget()}", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun RequiredScreen(vm: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Required Features", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(2.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(vm.requiredItems) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = vm.checkedRequiredIds.contains(item.id),
                        onCheckedChange = { vm.toggleRequiredChecked(item.id) })
                    Row(Modifier.padding(start = 2.dp)) {
                        Text(item.name)
                        Text(" : ")
                        Text("Priority: ${item.priority}")
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyWebScreen(vm: MainViewModel) {
    var selectedSite by remember { mutableStateOf("Rightmove") }
    val selected = vm.properties.firstOrNull { it.id == vm.selectedPropertyId }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Property:")
            Spacer(Modifier.width(8.dp))
            var expanded by remember { mutableStateOf(false) }
            val currentName = selected?.name ?: "Select"
            Box {
                Button(onClick = { expanded = true }) { Text(currentName) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    vm.properties.forEach { p ->
                        DropdownMenuItem(text = { Text(p.name) }, onClick = {
                            vm.setSelectedProperty(p.id)
                            expanded = false
                        })
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            var siteExpanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { siteExpanded = true }) { Text(selectedSite) }
                DropdownMenu(expanded = siteExpanded, onDismissRequest = { siteExpanded = false }) {
                    listOf("Rightmove", "Zoopla", "OnTheMarket").forEach { site ->
                        DropdownMenuItem(text = { Text(site) }, onClick = { selectedSite = site; siteExpanded = false })
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val url = remember(selected, selectedSite) { buildSiteUrl(selected, selectedSite) }
        if (url != null) {
            AndroidView(factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            }, update = { it.loadUrl(url) }, modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No URL available for selection")
            }
        }
    }
}

private fun buildSiteUrl(p: Property?, site: String): String? {
    if (p == null) return null
    return when (site) {
        "Rightmove" -> p.rightmoveNumber?.let { "https://www.rightmove.co.uk/properties/$it" }
        "Zoopla" -> p.zooplaNumber?.let { "https://www.zoopla.co.uk/for-sale/details/$it/" }
        "OnTheMarket" -> p.onthemarketNumber?.let { "https://www.onthemarket.com/details/$it/" }
        else -> null
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    HouseSearchTheme {
        MainApp()
    }
}