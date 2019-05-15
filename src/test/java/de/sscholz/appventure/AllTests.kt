package de.sscholz.appventure

import com.google.android.gms.maps.model.LatLng
import de.sscholz.appventure.util.*
import org.junit.Assert.*
import org.junit.Test


class AllTests {

    @Test
    fun yamlToJson() {
        val yaml = """
            foo: |
              Einen wahnsinnigen Plan hat sich der verrückte Professor Maniax ausgedacht. Er will die Stadt mit seiner Photonenkanone vernichten. Schaffst du es, die Stadt zu retten, bevor es zu spät ist? Das Schicksal von Paderborn liegt nun in deinen Händen. Rund um den Domplatz hat Professor Maniax seine Spuren hinterlassen. Nur wenn du es schnell genug schaffst, den Geheimcode zum Abschalten seiner Maschine zu entschlüsseln, gibt es Hoffnung. Du solltest besser keine Zeit verlieren...

              Weitere Infos zur Tour:

              Bewertung 4,5/5 Sterne
              Rätselstationen 6
              Dauer Durchschnitt 00:47
              Dauer Schnellster 00:18
              Länge 387 m
              City-tour
              Geeignet für
              - Wandern
              - Fahrrad
              - Inliner
              - Rollstuhl
              - Kinderwagen
              - Hund
              Schwierigkeitsgrad Terrain 0/5
              Schwierigkeitsgrad Rätsel 2/5
              Mindestalter 10 Jahre
              Öffnungszeiten 24h/7
              Monate 12/12
              Thema Schience Fiction meets Sigths
              Benötigt
              - Zettel & Stift / Handynotizen
              - kein besonderes Schuhwerk
              - keine besondere Ausdauer
              - keine Kletteraktivität
              Shopping in direkter Umgebung möglich
              Alleine & im Team möglich
              - Integration Sehenswürdigkeiten 3
              Kategorie:
              x Sightseeing
                Citywalk
                Activity
                Distance
              Feedback anderer venturer
        """.trimIndent()
        println(yaml)
        println(convertYamlToJson(yaml).replace("}", "}\n"))
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun testRandomCoordinates() {
        val center = LatLng(51.7189, 8.7575)
        (1..100).forEach {
            val radius = 50.0
            val p = center.randomLatLngWithinRadius(radius)
            val distance = p.distanceTo(center)
            de.sscholz.appventure.util.println(p, distance)
            assertLess(distance, radius)
        }
    }

    @Test
    fun testLevensthein() {
        assertEquals(1, levenshteinDistance("abcde", "abcdef"))
        assertEquals(1, levenshteinDistance("abcde", "abcae"))
        assertEquals(2, levenshteinDistance("abcde", "abdef"))
        assertEquals(3, levenshteinDistance("abc", "xyz"))
    }

    @Test
    fun testSolutionAcceptable() {
        assertNull(findAcceptableSolution(listOf("123"), "122"))
        assertEquals("122", findAcceptableSolution(listOf("123", "122"), "122"))
        assertEquals("42", findAcceptableSolution(listOf("123"), "42"))
        assertEquals("ABC", findAcceptableSolution(listOf("ABC"), "abc"))
        assertEquals("Handball", findAcceptableSolution(listOf("Handball"), "hamdtall"))
        assertNull(findAcceptableSolution(listOf("Handball"), "hadtalt"))
    }




}
