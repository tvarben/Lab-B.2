package se.kth.hi1033.weatherapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import se.kth.hi1033.weatherapp.model.CoordinateValidator
import se.kth.hi1033.weatherapp.model.CoordinateValidator.ValidationResult

//Q28
class CoordinateValidatorTest {

    @Test
    fun validCoordinates_returnsValid() {
        val result = CoordinateValidator.validate("59.33", "18.07")
        assertTrue(result is ValidationResult.Valid)
        val valid = result as ValidationResult.Valid
        assertEquals(59.33, valid.latitude, 0.0001)
        assertEquals(18.07, valid.longitude, 0.0001)
    }

    @Test
    fun commaDecimalSeparator_isNormalizedAndValid() {
        val result = CoordinateValidator.validate("-33,86", "151,20")
        assertTrue(result is ValidationResult.Valid)
        val valid = result as ValidationResult.Valid
        assertEquals(-33.86, valid.latitude, 0.0001)
        assertEquals(151.20, valid.longitude, 0.0001)
    }

    @Test
    fun outOfRangeLatitude_returnsInvalid() {
        val result = CoordinateValidator.validate("90.5", "18.0")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun outOfRangeLongitude_returnsInvalid() {
        val result = CoordinateValidator.validate("59.3", "-181.0")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun nanOrNonNumericInput_returnsInvalid() {
        assertTrue(CoordinateValidator.validate("NaN", "18.0") is ValidationResult.Invalid)
        assertTrue(CoordinateValidator.validate("abc", "18.0") is ValidationResult.Invalid)
        assertTrue(CoordinateValidator.validate("", "18.0") is ValidationResult.Invalid)
    }
}
