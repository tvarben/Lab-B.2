package se.kth.hi1033.weatherapp.model

/**
 * Pure Kotlin validator for user-entered latitude and longitude strings.
 */
object CoordinateValidator {
    //Q21 our domain is not big so enforcing business invariants is not that difficult. This object was ai generated and shouldn't be a separate file.
    //Personally, this type of invariant should already bee enforced inside the domain model as a private method, not as a separate object and file.


    //Q23 Section 3 & 4 -> D-09 resulted in this file
    sealed class ValidationResult {
        data class Valid(val latitude: Double, val longitude: Double) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    //Q25
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
