/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.fhir.fhirpath.types

import com.google.fhir.model.r4.FhirDateTime
import kotlin.text.get
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant

internal data class FhirPathDateTime(
  val year: Int,
  val month: Int? = null,
  val day: Int? = null,
  val hour: Int? = null,
  val minute: Int? = null,
  val second: Double? = null,
  val utcOffset: UtcOffset? = null,
) {

  enum class Precision {
    YEAR,
    MONTH,
    DAY,
    HOUR,
    MINUTE,
    SECOND,
  }

  val precision =
    when {
      second != null -> Precision.SECOND
      minute != null -> Precision.MINUTE
      hour != null -> Precision.HOUR
      day != null -> Precision.DAY
      month != null -> Precision.MONTH
      else -> Precision.YEAR
    }

  fun compareTo(other: FhirPathDateTime): Int? {
    // A value with a timezone offset cannot be compared to a value without a timezone offset
    if ((utcOffset == null) != (other.utcOffset == null)) {
      return null
    }

    if (precision == Precision.SECOND && other.precision == Precision.SECOND) {
      return toInstant().compareTo(other.toInstant())
    }

    // Partial date times with timezone offsets cannot be compared
    // See
    // https://github.com/jingtang10/kotlin-fhirpath/tree/main?tab=readme-ov-file#timezone-offset-in-date-time-values
    if (utcOffset != null) return null

    year.compareTo(other.year).let { if (it != 0) return it }

    if ((month == null) != (other.month == null)) return null
    month?.compareTo(other.month!!).let { if (it != 0) return it }

    if ((day == null) != (other.day == null)) return null
    day?.compareTo(other.day!!).let { if (it != 0) return it }

    if ((hour == null) != (other.hour == null)) return null
    hour?.compareTo(other.hour!!).let { if (it != 0) return it }

    if ((minute == null) != (other.minute == null)) return null
    minute?.compareTo(other.minute!!).let { if (it != 0) return it }

    return 0 // Precision.SECOND is already covered above
  }

  /**
   * Converts the DateTime to an Instant, assuming a zero timezone offset.
   *
   * This functions is used to compare two values with explicit timezone offsets, or two values
   * without timezone offsets. In the former case, no timezone offset will be assumed in this
   * function; in the latter case, the same timezone offset will be assumed, producing correct
   * comparison result.
   */
  private fun toInstant(): Instant {
    return LocalDateTime(
        year,
        month!!,
        day!!,
        hour!!,
        minute!!,
        second!!.toInt(),
        second.rem(1).times(1_000_000_000.0).toInt(),
      )
      .toInstant(utcOffset ?: UtcOffset.ZERO)
  }

  companion object {
    fun fromString(string: String): FhirPathDateTime {
      val regex =
        Regex(
          "^" +
            "(?<year>\\d{4})" +
            "(-(?<month>\\d{2})" +
            "(-(?<day>\\d{2})" +
            "(T(?<hour>\\d{2})" +
            "(:(?<minute>\\d{2})" +
            "(:(?<second>\\d{2}(?:\\.\\d+)?))?)?" +
            "(?<offset>Z|[+\\-]\\d{2}:\\d{2})?)?)?)?" +
            "$"
        )

      val match =
        regex.find(string)
          ?: throw IllegalArgumentException("Invalid FHIRPath DateTime format: $string")
      val groups = match.groups

      val year = groups["year"]!!.value.toInt()
      val month = groups["month"]?.value?.toInt()
      val day = groups["day"]?.value?.toInt()
      val hour = groups["hour"]?.value?.toInt()
      val minute = groups["minute"]?.value?.toInt()
      val second = groups["second"]?.value?.toDouble()
      val offset = groups["offset"]?.value?.let { UtcOffset.Companion.parse(it) }

      // Use kotlinx.datetime for robust validation of date and time components
      try {
        if (hour != null) {
          LocalDateTime(year, month!!, day!!, hour, minute ?: 0, second?.toInt() ?: 0)
        } else if (day != null) {
          LocalDate(year, month!!, day)
        }
        // TODO: Validate YearMonth using the new kotlinx.datetime.YearMonth class
      } catch (e: Exception) {
        throw IllegalArgumentException("Invalid date or time component in literal: $string", e)
      }

      return FhirPathDateTime(year, month, day, hour, minute, second, offset)
    }

    fun fromFhirDateTime(fhirDateTime: FhirDateTime): FhirPathDateTime {
      return fromString(fhirDateTime.toString())
    }
  }
}
