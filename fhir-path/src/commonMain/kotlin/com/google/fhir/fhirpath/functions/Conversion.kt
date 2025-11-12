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

package com.google.fhir.fhirpath.functions

import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.fhirpath.ucum.Unit
import com.google.fhir.model.r4.Date
import com.google.fhir.model.r4.DateTime
import com.google.fhir.model.r4.Decimal
import com.google.fhir.model.r4.FhirDate
import com.google.fhir.model.r4.FhirDateTime
import com.google.fhir.model.r4.Quantity
import com.google.fhir.model.r4.Time
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.LocalTime

/**
 * See [specification](https://hl7.org/fhirpath/N1/#toquantityunit-string-quantity).
 *
 * NB: The regular expression is slightly modified from the original in order for it to work in
 * Kotlin.
 */
val QUANTITY_REGEX =
  """(?<value>[+-]?\d+(\.\d+)?)\s*('(?<unit>[^']+)'|(?<time>[a-zA-Z]+))?""".toRegex()

/** See [specification](https://hl7.org/fhirpath/N1/#toquantityunit-string-quantity). */
const val DEFAULT_UNIT = "'1'"

val SINGULAR_CALENDAR_DURATION_LIST =
  listOf("year", "month", "week", "day", "hour", "minute", "second", "millisecond")
val PLURAL_CALENDAR_DURATION_LIST =
  listOf("years", "months", "weeks", "days", "hours", "minutes", "seconds", "milliseconds")
val CALENDAR_DURATION_LIST = SINGULAR_CALENDAR_DURATION_LIST + PLURAL_CALENDAR_DURATION_LIST

/** See [specification](https://hl7.org/fhirpath/N1/#toboolean-boolean). */
internal fun Collection<Any>.toBoolean(): Collection<Boolean> {
  check(size <= 1) { "toBoolean() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is Boolean -> listOf(value)
    is Int ->
      when (value) {
        1 -> listOf(true)
        0 -> listOf(false)
        else -> emptyList()
      }
    is BigDecimal ->
      when (value) {
        BigDecimal.ONE -> listOf(true)
        BigDecimal.ZERO -> listOf(false)
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

/** See [specification](https://hl7.org/fhirpath/N1/#convertstoboolean-boolean). */
internal fun Collection<Any>.convertsToBoolean(): Collection<Boolean> {
  check(size <= 1) { "convertsToBoolean() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toBoolean().isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/N1/#tointeger-integer). */
internal fun Collection<Any>.toInteger(): Collection<Int> {
  check(size <= 1) { "toInteger() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is Int -> listOf(value)
    is String -> {
      value.toIntOrNull()?.let { listOf(it) } ?: emptyList()
    }
    is Boolean -> if (value) listOf(1) else listOf(0)
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#convertstointeger-boolean). */
internal fun Collection<Any>.convertsToInteger(): Collection<Boolean> {
  check(size <= 1) { "convertsToInteger() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toInteger().isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/N1/#todate-date). */
internal fun Collection<Any>.toDate(): Collection<FhirDate> {
  check(size <= 1) { "toDate() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is FhirDate -> listOf(value)
    is FhirDateTime -> TODO("Clarify the requirement in the specification")
    is String ->
      try {
        listOf(FhirDate.fromString(value)!!)
      } catch (_: Exception) {
        emptyList()
      }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#convertstodate-boolean). */
internal fun Collection<Any>.convertsToDate(): Collection<Boolean> {
  check(size <= 1) { "convertsToDate() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is FhirDate -> listOf(true)
    is FhirDateTime -> listOf(true)
    is String ->
      try {
        FhirDate.fromString(value)
        listOf(true)
      } catch (_: Exception) {
        listOf(false)
      }
    else -> listOf(false)
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#todatetime-datetime). */
internal fun Collection<Any>.toDateTime(): Collection<FhirPathDateTime> {
  check(size <= 1) { "toDateTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is FhirPathDateTime -> listOf(value)
    is FhirDateTime -> listOf(FhirPathDateTime.fromFhirDateTime(value))
    is FhirDate -> listOf(FhirPathDateTime.fromString(value.toString()))
    is String ->
      try {
        listOf(FhirPathDateTime.fromString(value))
      } catch (_: Exception) {
        emptyList()
      }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#convertstodatetime-boolean). */
internal fun Collection<Any>.convertsToDateTime(): Collection<Boolean> {
  check(size <= 1) { "convertsToDateTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is FhirPathDateTime -> listOf(true)
    is FhirDateTime -> listOf(true)
    is FhirDate -> listOf(true)
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

/** See [specification](https://hl7.org/fhirpath/N1/#todecimal-decimal). */
internal fun Collection<Any>.toDecimal(): Collection<BigDecimal> {
  check(size <= 1) { "toDecimal() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val value = single()) {
    is BigDecimal -> listOf(value)
    is Int -> listOf(value.toBigDecimal())
    is Boolean -> listOf(if (value) BigDecimal.ONE else BigDecimal.ZERO)
    is String -> {
      value.toDoubleOrNull()?.let { listOf(it.toBigDecimal()) } ?: emptyList()
    }
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#convertstodecimal-boolean). */
internal fun Collection<Any>.convertsToDecimal(): Collection<Boolean> {
  check(size <= 1) { "convertsToDecimal() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toDecimal().isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/N1/#toquantityunit-string-quantity). */
internal fun Collection<Any>.toQuantity(targetUnit: String?): Collection<Quantity> {
  check(size <= 1) { "toQuantity() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single()) {
    is Int -> listOf((item.toBigDecimal() to DEFAULT_UNIT).toQuantity())
    is Long -> listOf((item.toBigDecimal() to DEFAULT_UNIT).toQuantity())
    is BigDecimal -> listOf((item to DEFAULT_UNIT).toQuantity())
    is Quantity -> listOf(item)
    is String -> {
      val match = QUANTITY_REGEX.matchEntire(item.trim()) ?: return emptyList()
      val value = match.groups["value"]?.value!!.toBigDecimal()
      val unit =
        match.groups["unit"]?.value?.also {
          if (Unit.fromString(it) == null) {
            return emptyList()
          }
        }
      val calendarDuration =
        match.groups["time"]?.value?.also {
          if (it !in CALENDAR_DURATION_LIST) {
            return emptyList()
          }
        }
      if (targetUnit != null) {
        if (unit != null && targetUnit != "'$unit'") {
          TODO("Handle unit conversion")
        }
        if (calendarDuration != null && targetUnit != calendarDuration) {
          TODO("Handle calendar duration conversion")
        }
      }
      listOf((value to (unit?.let { "'$it'" } ?: calendarDuration ?: DEFAULT_UNIT)).toQuantity())
    }
    is Boolean ->
      listOf(((if (item) BigDecimal.ONE else BigDecimal.ZERO) to DEFAULT_UNIT).toQuantity())
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#convertstoquantityunit-string-boolean). */
internal fun Collection<Any>.convertsToQuantity(): Collection<Boolean> {
  check(size <= 1) { "convertsToQuantity() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return listOf(toQuantity(targetUnit = null).isNotEmpty())
}

/** See [specification](https://hl7.org/fhirpath/N1/#tostring-string). */
internal fun Collection<Any>.toStringFun(): Collection<String> {
  check(size <= 1) { "toString() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single()) {
    is String -> listOf(item)
    is Int -> listOf(item.toString())
    is Long -> listOf(item.toString())
    is BigDecimal -> listOf(item.toString())
    is Date -> listOf(item.toString())
    is DateTime -> listOf(item.toString())
    is Time -> listOf(item.toString())
    is FhirDate -> listOf(item.toString())
    is FhirPathDateTime -> listOf(item.toString())
    is FhirPathTime -> listOf(item.toString())
    is Boolean -> listOf(item.toString())
    is Quantity -> listOf("${item.value?.value} ${item.unit?.value}")
    else -> emptyList()
  }
}

/**
 * See [specification](https://hl7.org/fhirpath/N1/#convertstostring-string).
 *
 * NB: The URL is inconsistent with other functions due to a function signature error in the
 * documentation.
 *
 * TODO: Correct URL once https://jira.hl7.org/browse/FHIR-52051 is addressed.
 */
internal fun Collection<Any>.convertsToString(): Collection<Boolean> {
  check(size <= 1) { "convertsToString() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  val isConvertible =
    when (single()) {
      is String,
      is Int,
      is Long,
      is BigDecimal,
      is Date,
      is DateTime,
      is Time,
      is FhirDate,
      is FhirPathDateTime,
      is FhirPathTime,
      is Boolean,
      is Quantity -> true
      else -> false
    }

  return listOf(isConvertible)
}

/** See [specification](https://hl7.org/fhirpath/N1/#totime-time). */
internal fun Collection<Any>.toTime(): Collection<FhirPathTime> {
  check(size <= 1) { "toTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single()) {
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

/** See [specification](https://hl7.org/fhirpath/N1/#convertstotime-boolean). */
internal fun Collection<Any>.convertsToTime(): Collection<Boolean> {
  check(size <= 1) { "convertsToTime() cannot be called on a collection with more than 1 item" }

  if (isEmpty()) return emptyList()

  return when (val item = single()) {
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

internal fun Pair<BigDecimal, String>.toQuantity(): Quantity {
  return Quantity(
    value = Decimal(value = first),
    unit = com.google.fhir.model.r4.String(value = second),
  )
}
