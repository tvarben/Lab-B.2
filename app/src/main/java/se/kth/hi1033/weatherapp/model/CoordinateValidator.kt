package se.kth.hi1033.weatherapp.model

/**
 * Pure Kotlin validator for user-entered latitude and longitude strings.
 */
object CoordinateValidator {

    sealed class ValidationResult {
        data class Valid(val latitude: Double, val longitude: Double) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(latInput: String, lonInput: String): ValidationResult {
        val normalizedLat = latInput.trim().replace(',', '.')
        val normalizedLon = lonInput.trim().replace(',', '.')

        if (normalizedLat.isEmpty() || normalizedLon.isEmpty()) {
            return ValidationResult.Invalid("Enter both latitude and longitude.")
        }

        val lat = normalizedLat.toDoubleOrNull()
        val lon = normalizedLon.toDoubleOrNull()

        if (lat == null || !lat.isFinite()) {
            return ValidationResult.Invalid("Latitude must be a valid number.")
        }
        if (lon == null || !lon.isFinite()) {
            return ValidationResult.Invalid("Longitude must be a valid number.")
        }
        if (lat < -90.0 || lat > 90.0) {
            return ValidationResult.Invalid("Latitude must be between -90 and 90.")
        }
        if (lon < -180.0 || lon > 180.0) {
            return ValidationResult.Invalid("Longitude must be between -180 and 180.")
        }

        return ValidationResult.Valid(lat, lon)
    }
}
