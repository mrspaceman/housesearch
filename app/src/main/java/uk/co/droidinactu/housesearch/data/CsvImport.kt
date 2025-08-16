package uk.co.droidinactu.housesearch.data

import android.content.Context
import uk.co.droidinactu.housesearch.model.Property

object CsvImport {
    private const val ASSET_NAME = "properties.csv"

    fun parsePropertiesFromAssets(context: Context): List<Property> {
        val am = context.assets
        val input = am.open(ASSET_NAME)
        input.bufferedReader().use { reader ->
            val result = mutableListOf<Property>()
            var lineNumber = 0
            reader.forEachLine { raw ->
                lineNumber++
                val line = raw.trim()
                if (line.isBlank()) return@forEachLine
                if (line.startsWith("//")) return@forEachLine
                if (lineNumber == 1 && line.contains("latitude") && line.contains("property site")) {
                    // header row
                    return@forEachLine
                }
                // Split by comma but keep quoted segments; simple CSV handling
                val tokens = splitCsv(line)
                if (tokens.size < 7) return@forEachLine
                val lat = tokens[0].trim().trim(',').toDoubleOrNull()
                val lon = tokens[1].trim().trim(',').toDoubleOrNull()
                val id = tokens[2].trim().trim(',').toLongOrNull() ?: return@forEachLine
                val name = tokens[3].trim().trim(',').trim('"')
                val site = tokens[4].trim().trim(',').lowercase()
                val number = tokens[5].trim().trim(',').trim('"')
                val price = tokens[6].trim().trim(',').toLongOrNull()
                val category = tokens.getOrNull(8)?.trim()?.trim(',')?.trim('"')?.ifBlank { null }

                val (rm, zp, otm) = when (site) {
                    "rightmove" -> Triple(number.ifBlank { null }, null, null)
                    "zoopla" -> Triple(null, number.ifBlank { null }, null)
                    "onthemarket" -> Triple(null, null, number.ifBlank { null })
                    else -> Triple(null, null, null)
                }

                result.add(
                    Property(
                        id = id,
                        name = if (name.isBlank()) "Property $id" else name,
                        rightmoveNumber = rm,
                        zooplaNumber = zp,
                        onthemarketNumber = otm,
                        latitude = lat,
                        longitude = lon,
                        price = price,
                        category = category
                    )
                )
            }
            return result
        }
    }

    private fun splitCsv(line: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        line.forEach { ch ->
            when (ch) {
                '"' -> { inQuotes = !inQuotes; /* keep quotes for later trimming */ sb.append(ch) }
                ',' -> if (inQuotes) sb.append(ch) else { out.add(sb.toString()); sb.setLength(0) }
                else -> sb.append(ch)
            }
        }
        out.add(sb.toString())
        return out
    }
}