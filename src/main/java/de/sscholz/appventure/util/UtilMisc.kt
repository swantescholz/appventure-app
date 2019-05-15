package de.sscholz.appventure.util

import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import de.sscholz.appventure.data.Globals
import java.util.*
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory




fun getString(resId: Int) = Globals.resources!!.getString(resId)

operator fun Int.plus(other: Boolean) = this + if (other) 1 else 0
fun Long.plus(other: Boolean) = this + if (other) 1 else 0
fun Boolean.plus(other: Int) = other + if (this) 1 else 0
fun Boolean.plus(other: Long) = other + if (this) 1 else 0

fun findAcceptableSolution(correctSolutions: List<String>, enteredSolution: String): String? {
    if (enteredSolution in Globals.SOLUTIONS_THAT_ALWAYS_WORK)
        return enteredSolution
    val lowerEntered = enteredSolution.toLowerCase()
    correctSolutions.forEach { upperCorrect ->
        val lowerCorrect = upperCorrect.toLowerCase()
        if (lowerCorrect.toIntOrNull() != null) {
            // correct is number, so it must be an exact match
            if (lowerCorrect == lowerEntered) {
                return upperCorrect
            }
        } else {
            val levenshteinDistance = levenshteinDistance(lowerCorrect, lowerEntered)
            if (lowerEntered.length >= 8 && levenshteinDistance <= 2) {
                return upperCorrect
            } else if (lowerEntered.length >= 4 && levenshteinDistance <= 1) {
                return upperCorrect
            } else if (levenshteinDistance == 0) {
                return upperCorrect
            }
        }
    }
    return null
}

fun levenshteinDistance(s: String, t: String): Int {
    // degenerate cases
    if (s == t) return 0
    if (s == "") return t.length
    if (t == "") return s.length

    // create two integer arrays of distances and initialize the first one
    val v0 = IntArray(t.length + 1) { it }  // previous
    val v1 = IntArray(t.length + 1)         // current

    var cost: Int
    for (i in 0 until s.length) {
        // calculate v1 from v0
        v1[0] = i + 1
        for (j in 0 until t.length) {
            cost = if (s[i] == t[j]) 0 else 1
            v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost))
        }
        // copy v1 to v0 for next iteration
        for (j in 0..t.length) v0[j] = v1[j]
    }
    return v1[t.length]
}

fun convertYamlToJson(yaml: String): String {
    val yamlReader = ObjectMapper(YAMLFactory())
    val obj = yamlReader.readValue(yaml, Any::class.java)

    val jsonWriter = ObjectMapper()
    return jsonWriter.writeValueAsString(obj)
}

// call e.g. with "subdir/my_image.jpg"
fun completeAssetUri(backPart: String) = Uri.parse("file:///android_asset/$backPart")

// call e.g. with "stories.yaml" or "subdir/that_story.json"
fun AssetManager.readAssetTextFile(backPart: String): String = this.open(backPart).bufferedReader().use { it.readText() }

fun println(vararg items: Any?) {
    kotlin.io.println(items.iterator().asSequence().map { it.toString() }.joinToString(" "))
}

fun loge(vararg items: Any?) {
    Log.e("my Log.e ==============", items.iterator().asSequence().map { it.toString() }.joinToString(" "))
}

fun <T> List<T>.pickRandom(): T = this[(0 until size).random()]

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start + 1) + start


fun <T> enumerate(items: Iterable<T>, startIndex: Int = 0): List<Pair<Int, T>> = (startIndex..Int.MAX_VALUE).zip(items)

fun formatEllipsis(text: String, maxLength: Int, ellipsisString: String = "..."): String {
    if (text.length <= maxLength) {
        return text
    }
    return text.substring(0, maxLength) + ellipsisString
}

fun formatDistance(distanceInMeters: Double): String {
    if (distanceInMeters < 1000) {
        return "${distanceInMeters.toInt()} m"
    }
    val distanceInKm = distanceInMeters / 1000.0
    if (distanceInKm < 100) {
        return "%.1f km".format(distanceInKm)
    }
    return "${distanceInKm.toInt()} km"
}

fun formatTime(timeInMinutes: Int): String {
    if (timeInMinutes < 60) {
        return "$timeInMinutes min"
    }
    return "${timeInMinutes / 60}:${timeInMinutes % 60} h"
}



