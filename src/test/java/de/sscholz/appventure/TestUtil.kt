package de.sscholz.appventure

import org.junit.Assert

fun <T : Comparable<T>> assertLess(expectedSmallerValue: T, expectedGreaterValue: T) {
    Assert.assertTrue("$expectedGreaterValue is unexpectedly smaller than $expectedSmallerValue", expectedSmallerValue < expectedGreaterValue)
}