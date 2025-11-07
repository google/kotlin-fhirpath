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

import kotlin.text.get
import kotlinx.datetime.LocalTime

internal data class FhirPathTime(
  val hour: Int,
  val minute: Int? = null,
  val second: Double? = null,
) {

  enum class Precision {
    HOUR,
    MINUTE,
    SECOND,
  }

  val precision =
    when {
      second != null -> Precision.SECOND
      minute != null -> Precision.MINUTE
      else -> Precision.HOUR
    }

  fun compareTo(other: FhirPathTime): Int? {
    if (precision != other.precision) {
      return null
    }
    hour.compareTo(other.hour).let { if (it != 0) return it }
    minute?.compareTo(other.minute!!).let { if (it != 0) return it }
    second?.compareTo(other.second!!).let { if (it != 0) return it }
    return 0
  }

  companion object {
    fun fromString(string: String): FhirPathTime {
      val regex =
        Regex(
          "^" +
            "(?<hour>\\d{2})" +
            "(:(?<minute>\\d{2}))?" +
            "(:(?<second>\\d{2}(\\.\\d+)?))?" +
            "$"
        )

      val match =
        regex.find(string)
          ?: throw IllegalArgumentException("Invalid FHIRPath Time format: $string")
      val groups = match.groups

      val hour = groups["hour"]!!.value.toInt()
      val minute = groups["minute"]?.value?.toInt()
      val second = groups["second"]?.value?.toDouble()

      // Use kotlinx.LocalTime to validate the time components
      try {
        LocalTime(hour, minute ?: 0, second?.toInt() ?: 0)
      } catch (e: Exception) {
        throw IllegalArgumentException("Invalid time component in literal: $string", e)
      }
      return FhirPathTime(hour, minute, second)
    }

    fun fromLocalTime(localTime: LocalTime): FhirPathTime =
      FhirPathTime(
        hour = localTime.hour,
        minute = localTime.minute,
        second = localTime.second + (localTime.nanosecond / 1_000_000_000.0),
      )
  }
}
