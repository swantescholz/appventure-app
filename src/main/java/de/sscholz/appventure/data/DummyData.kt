package de.sscholz.appventure.data

import de.sscholz.appventure.util.pickRandom
import de.sscholz.appventure.util.random
import de.sscholz.appventure.util.randomLatLngWithinRadius

object DummyData {
    fun createDummyTour(id: Int): Tour {
        return Tour("Tour " + "AB ".repeat(id), "Description: " + "tour story text ".repeat((5..50).random()),
                listOf("dummy1.jpg", "dummy2.jpg", "dummy3.jpg", "dummy4.jpg").pickRandom(),
                createRandomStations())
    }

    fun createRandomStations(): List<Station> {
        val center = Globals.paderbornPosition
        val stationCenter = center.randomLatLngWithinRadius(10000.0)
        return (1..((5..10).random())).map {
            val pos = stationCenter.randomLatLngWithinRadius(100.0)
            Station("Halt $it", pos, "station story text ".repeat((50..350).random()),
                    listOf("dummy1.jpg", "dummy2.jpg", "dummy3.jpg", "dummy4.jpg").pickRandom(),
                    puzzleSolutions = listOf(""))
        }.toList()
    }
}