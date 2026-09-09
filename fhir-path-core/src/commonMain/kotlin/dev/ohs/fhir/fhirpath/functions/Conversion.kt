/*
 * Copyright 2025-2026 Open Health Stack Foundation
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

package dev.ohs.fhir.fhirpath.functions

import dev.ohs.fhir.fhirpath.isValidUcumUnit
import dev.ohs.fhir.fhirpath.toEqualCanonicalized
import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import dev.ohs.fhir.fhirpath.types.toFhirPathDecimal
import kotlinx.datetime.LocalTime

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#toquantityunit--string--quantity).
 *
 * NB: The regular expression is slightly modified from the original in order for it to work in
 * Kotlin.
 */
private val QUANTITY_REGEX =
  """(?<value>[+-]?\d+(\.\d+)?)\s*('(?<unit>[^']+)'|(?<time>[a-zA-Z]+))?""".toRegex()

/** See [specification](https://hl7.org/fhirpath/STU3/en/#toquantityunit--string--quantity). */
const val DEFAULT_UNIT = "'1'"

private val SINGULAR_CALENDAR_DURATION_LIST =
  listOf("year", "month", "week", "day", "hour", "minute", "second", "millisecond")
private val PLURAL_CALENDAR_DURATION_LIST =
  listOf("years", "months", "weeks", "days", "hours", "minutes", "seconds", "milliseconds")
private val CALENDAR_DURATION_LIST = SINGULAR_CALENDAR_DURATION_LIST + PLURAL_CALENDAR_DURATION_LIST

/** See [specification](https://hl7.org/fhirpath/STU3/en/#toboolean--boolean). */
internal fun Collection<Any>.toBoolean(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "toBoolean() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is Boolean -> listOf(value)
    is Int ->
      when (value) {
        1 -> listOf(true)
        0 -> listOf(false)
        else -> emptyList()
      }
    is FhirPathDecimal ->
      when (value) {
        FhirPathDecimal.ONE -> listOf(true)
        FhirPathDecimal.ZERO -> listOf(false)
        else -> emptyList()
      }
    is String ->
      when (value.lowercase()) {
        "true",
        "t",
        "yes",
        "y",
        "1",
        "1.0" -> listOf(true)
        "false",
        "f",
        "no",
        "n",
        "0",
        "0.0" -> listOf(false)
        else -> emptyList()
      }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#convertstoboolean--boolean). */
internal fun Collection<Any>.convertsToBoolean(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToBoolean() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toBoolean(fhirPathTypeResolver).isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#tointeger--integer). */
internal fun Collection<Any>.toInteger(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Int> {
  check(size <= 1) { "toInteger() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is Int -> listOf(value)
    is String -> {
      value.toIntOrNull()?.let { listOf(it) } ?: emptyList()
    }
    is Boolean -> if (value) listOf(1) else listOf(0)
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#convertstointeger--boolean). */
internal fun Collection<Any>.convertsToInteger(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToInteger() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toInteger(fhirPathTypeResolver).isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#tolong--long). */
internal fun Collection<Any>.toLong(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Long> {
  check(size <= 1) { "toLong() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is Long -> listOf(value)
    is Int -> listOf(value.toLong())
    is String -> {
      value.toLongOrNull()?.let { listOf(it) } ?: emptyList()
    }
    is Boolean -> if (value) listOf(1L) else listOf(0L)
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#convertstolong--boolean). */
internal fun Collection<Any>.convertsToLong(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToLong() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toLong(fhirPathTypeResolver).isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#todateformat--string--date). */
internal fun Collection<Any>.toDate(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<FhirPathDate> {
  check(size <= 1) { "toDate() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is FhirPathDate -> listOf(value)
    // N.B. The specification does not explicitly specify how timezone offsets affect the conversion
    // (e.g., whether to convert to UTC or a specific timezone first). We simply extract the year,
    // month, and day components directly.
    is FhirPathDateTime -> listOf(FhirPathDate(value.year, value.month, value.day))
    is String ->
      try {
        listOf(FhirPathDate.fromString(value))
      } catch (_: Exception) {
        emptyList()
      }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#convertstodateformat--string--boolean). */
internal fun Collection<Any>.convertsToDate(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToDate() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is FhirPathDate -> listOf(true)
    is FhirPathDateTime -> listOf(true)
    is String ->
      try {
        FhirPathDate.fromString(value)
        listOf(true)
      } catch (_: Exception) {
        listOf(false)
      }
    else -> listOf(false)
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#todatetimeformat--string--datetime). */
internal fun Collection<Any>.toDateTime(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<FhirPathDateTime> {
  check(size <= 1) { "toDateTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is FhirPathDateTime -> listOf(value)
    is FhirPathDate -> listOf(FhirPathDateTime.fromString(value.toString()))
    is String ->
      try {
        listOf(FhirPathDateTime.fromString(value))
      } catch (_: Exception) {
        emptyList()
      }
    else -> emptyList()
  }
}

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#convertstodatetimeformat--string--boolean).
 */
internal fun Collection<Any>.convertsToDateTime(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToDateTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is FhirPathDateTime -> listOf(true)
    is FhirPathDate -> listOf(true)
    is String ->
      try {
        FhirPathDateTime.fromString(value)
        listOf(true)
      } catch (_: Exception) {
        listOf(false)
      }
    else -> listOf(false)
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#todecimal--decimal). */
internal fun Collection<Any>.toDecimal(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<FhirPathDecimal> {
  check(size <= 1) { "toDecimal() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single().toFhirPathType(fhirPathTypeResolver)) {
    is FhirPathDecimal -> listOf(value)
    is Int -> listOf(value.toFhirPathDecimal())
    is Boolean -> listOf(if (value) FhirPathDecimal.ONE else FhirPathDecimal.ZERO)
    is String -> {
      value.toDoubleOrNull()?.let { listOf(it.toFhirPathDecimal()) } ?: emptyList()
    }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#convertstodecimal--boolean). */
internal fun Collection<Any>.convertsToDecimal(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToDecimal() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toDecimal(fhirPathTypeResolver).isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#toquantityunit--string--quantity). */
internal fun Collection<Any>.toQuantity(
  targetUnit: String?,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<FhirPathQuantity> {
  check(size <= 1) { "toQuantity() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single().toFhirPathType(fhirPathTypeResolver)) {
    is Int -> {
      val pair = (item.toFhirPathDecimal() to DEFAULT_UNIT)
      listOf(FhirPathQuantity(value = pair.first, unit = pair.second))
    }
    is Long -> {
      val pair1 = (item.toFhirPathDecimal() to DEFAULT_UNIT)
      listOf(FhirPathQuantity(value = pair1.first, unit = pair1.second))
    }
    is FhirPathDecimal -> {
      val pair1 = (item to DEFAULT_UNIT)
      listOf(FhirPathQuantity(value = pair1.first, unit = pair1.second))
    }
    is FhirPathQuantity -> {
      if (targetUnit != null) {
        convertQuantityUnit(item, targetUnit)
      } else {
        listOf(item)
      }
    }
    is String -> {
      val match = QUANTITY_REGEX.matchEntire(item.trim()) ?: return emptyList()
      val value = FhirPathDecimal.fromString(match.groups["value"]?.value!!)
      val unit = match.groups["unit"]?.value?.trim()
      val calendarDuration = match.groups["time"]?.value

      val parsedUnit: String =
        when {
          unit != null -> {
            if (!isValidUcumUnit(unit)) {
              return emptyList()
            }
            "'$unit'"
          }
          calendarDuration != null -> {
            if (calendarDuration !in CALENDAR_DURATION_LIST) {
              return emptyList()
            }
            calendarDuration
          }
          else -> DEFAULT_UNIT
        }

      val parsedQuantity = FhirPathQuantity(value = value, unit = parsedUnit)
      if (targetUnit != null) {
        convertQuantityUnit(parsedQuantity, targetUnit)
      } else {
        listOf(parsedQuantity)
      }
    }
    is Boolean -> {
      val pair1 = ((if (item) FhirPathDecimal.ONE else FhirPathDecimal.ZERO) to DEFAULT_UNIT)
      listOf(FhirPathQuantity(value = pair1.first, unit = pair1.second))
    }
    else -> emptyList()
  }
}

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#convertstoquantityunit--string--boolean).
 */
internal fun Collection<Any>.convertsToQuantity(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToQuantity() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toQuantity(targetUnit = null, fhirPathTypeResolver).isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#tostring--string). */
internal fun Collection<Any>.toStringFun(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<String> {
  check(size <= 1) { "toString() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single().toFhirPathType(fhirPathTypeResolver)) {
    is String -> listOf(item)
    is Int -> listOf(item.toString())
    is Long -> listOf(item.toString())
    is FhirPathDecimal -> listOf(item.toString())
    is FhirPathDate -> listOf(item.toString())
    is FhirPathDateTime -> listOf(item.toString())
    is FhirPathTime -> listOf(item.toString())
    is Boolean -> listOf(item.toString())
    is FhirPathQuantity -> {
      listOf("${item.value} ${item.unit}")
    }
    else -> emptyList()
  }
}

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#convertstostring--boolean).
 *
 * NB: The URL is inconsistent with other functions due to a function signature error in the
 * documentation.
 *
 * TODO: Correct URL once https://jira.hl7.org/browse/FHIR-52051 is addressed.
 */
internal fun Collection<Any>.convertsToString(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToString() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  val isConvertible =
    when (single().toFhirPathType(fhirPathTypeResolver)) {
      is String,
      is Int,
      is Long,
      is FhirPathDecimal,
      is FhirPathDate,
      is FhirPathDateTime,
      is FhirPathTime,
      is Boolean,
      is FhirPathQuantity -> true
      else -> false
    }

  return listOf(isConvertible)
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#totime--time). */
internal fun Collection<Any>.toTime(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<FhirPathTime> {
  check(size <= 1) { "toTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single().toFhirPathType(fhirPathTypeResolver)) {
    is LocalTime -> listOf(FhirPathTime.fromLocalTime(item))
    is FhirPathTime -> listOf(item)
    is String ->
      try {
        listOf(FhirPathTime.fromString(item))
      } catch (_: Exception) {
        emptyList()
      }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#convertstotime--boolean). */
internal fun Collection<Any>.convertsToTime(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> {
  check(size <= 1) { "convertsToTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single().toFhirPathType(fhirPathTypeResolver)) {
    is LocalTime -> listOf(true)
    is FhirPathTime -> listOf(true)
    is String ->
      try {
        FhirPathTime.fromString(item)
        listOf(true)
      } catch (_: Exception) {
        listOf(false)
      }

    else -> listOf(false)
  }
}

/**
 * Converts a [quantity] to the specified [unit] (which can be a UCUM unit or a FHIRPath calendar
 * duration).
 *
 * Both [quantity] and a unit quantity of 1 [unit] are canonicalized to their base UCUM
 * representations using [toEqualCanonicalized]. If their base units match, the scaled value
 * (`sourceCanonicalValue / targetCanonicalScale`) is computed and returned with [unit]. Returns an
 * empty collection if the units are incompatible or if exact conversion is not possible.
 */
private fun convertQuantityUnit(
  quantity: FhirPathQuantity,
  unit: String,
): Collection<FhirPathQuantity> {
  val unquotedUnit = unit.trim('\'')
  val targetUnit = if (unquotedUnit in CALENDAR_DURATION_LIST) unquotedUnit else "'$unquotedUnit'"

  if (quantity.unit == targetUnit) return listOf(quantity)

  val sourceCanonical = quantity.toEqualCanonicalized()
  val targetCanonical =
    FhirPathQuantity(value = FhirPathDecimal.ONE, unit = targetUnit).toEqualCanonicalized()

  val sourceCanonicalUnit = sourceCanonical.unit ?: return emptyList()
  val targetCanonicalUnit = targetCanonical.unit ?: return emptyList()
  val sourceCanonicalValue = sourceCanonical.value ?: return emptyList()
  val targetCanonicalScale = targetCanonical.value ?: return emptyList()

  if (sourceCanonicalUnit != targetCanonicalUnit || targetCanonicalScale.isZero()) {
    return emptyList()
  }
  val convertedValue = sourceCanonicalValue / targetCanonicalScale
  return listOf(FhirPathQuantity(value = convertedValue, unit = targetUnit))
}
