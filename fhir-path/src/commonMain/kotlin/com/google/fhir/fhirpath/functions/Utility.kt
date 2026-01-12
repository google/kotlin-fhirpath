/*
 * Copyright 2025-2026 Google LLC
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

import com.google.fhir.fhirpath.types.FhirPathDate
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.FhirDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.offsetAt
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** See [specification](https://hl7.org/fhirpath/N1/#now-datetime). */
@OptIn(ExperimentalTime::class)
internal fun now(now: Instant): Collection<FhirPathDateTime> {
  val systemTimeZone = TimeZone.currentSystemDefault()
  val localDateTime = now.toLocalDateTime(systemTimeZone)
  val utcOffset = systemTimeZone.offsetAt(now)
  return listOf(
    FhirPathDateTime(
      year = localDateTime.year,
      month = localDateTime.monthNumber,
      day = localDateTime.dayOfMonth,
      hour = localDateTime.hour,
      minute = localDateTime.minute,
      second = localDateTime.second + localDateTime.nanosecond / 1_000_000_000.0,
      utcOffset = utcOffset,
    )
  )
}

/** See [specification](https://hl7.org/fhirpath/N1/#timeofday-time). */
@OptIn(ExperimentalTime::class)
internal fun timeOfDay(now: Instant): Collection<Any> {
  val systemTimeZone = TimeZone.currentSystemDefault()
  val localDateTime = now.toLocalDateTime(systemTimeZone)
  return listOf(
    FhirPathTime(
      hour = localDateTime.hour,
      minute = localDateTime.minute,
      second = localDateTime.second + localDateTime.nanosecond / 1_000_000_000.0,
    )
  )
}

/** See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#today--date). */
@OptIn(ExperimentalTime::class)
internal fun today(now: Instant): Collection<FhirPathDate> {
  val systemTimeZone = TimeZone.currentSystemDefault()
  val localDateTime = now.toLocalDateTime(systemTimeZone)
  return listOf(FhirPathDate.fromString(localDateTime.date.toString()))
}

/**
 * See
 * [specification](https://build.fhir.org/ig/HL7/FHIRPath/#lowboundaryprecision-integer-decimal-date-datetime-time).
 * The least possible value of the input to the specified precision.
 */
internal fun Collection<Any>.lowBoundary(params: List<Any>): Collection<Any> {
  check(size <= 1) { "lowBoundary() cannot be called on a collection with more than 1 item" }
  val value = singleOrNull() ?: return emptyList()
  val precision = params.singleOrNull() as? Int

  return when (value) {
    is Double -> {
      // TODO: Implement lowBoundary for Decimal
      listOf(value)
    }
    is FhirDate -> {
      when (precision) {
        4 ->
          listOf(
            FhirDate.Year(
              when (value) {
                is FhirDate.Year -> value.value
                is FhirDate.YearMonth -> value.value.year
                is FhirDate.Date -> value.date.year
              }
            )
          )
        6 ->
          when (value) {
            is FhirDate.Year ->
              listOf(FhirDate.YearMonth(YearMonth(value.value, month = Month.JANUARY)))
            is FhirDate.YearMonth -> listOf(value)
            is FhirDate.Date ->
              listOf(FhirDate.YearMonth(YearMonth(value.date.year, value.date.month.number)))
          }
        8 ->
          when (value) {
            is FhirDate.Year -> listOf(FhirDate.Date(LocalDate(value.value, 1, 1)))
            is FhirDate.YearMonth ->
              listOf(FhirDate.Date(LocalDate(value.value.year, value.value.month, 1)))
            is FhirDate.Date -> listOf(value)
          }
        else -> error("Invalid precision value: $precision")
      }
    }
    is FhirPathDateTime -> {
      val targetPrecision = getDateTimePrecision(precision)
      listOf(
        FhirPathDateTime(
          year = value.year,
          month =
            if (targetPrecision >= FhirPathDateTime.Precision.MONTH) value.month ?: 1 else null,
          day = if (targetPrecision >= FhirPathDateTime.Precision.DAY) value.day ?: 1 else null,
          hour = if (targetPrecision >= FhirPathDateTime.Precision.HOUR) value.hour ?: 0 else null,
          minute =
            if (targetPrecision >= FhirPathDateTime.Precision.MINUTE) value.minute ?: 0 else null,
          second =
            if (targetPrecision >= FhirPathDateTime.Precision.SECOND) value.second ?: 0.0 else null,
          utcOffset = value.utcOffset,
        )
      )
    }
    is FhirPathTime -> {
      val targetPrecision = getTimePrecision(precision)
      listOf(
        FhirPathTime(
          hour = value.hour,
          minute =
            if (targetPrecision >= FhirPathTime.Precision.MINUTE) value.minute ?: 0 else null,
          second =
            if (targetPrecision >= FhirPathTime.Precision.SECOND) value.second ?: 0.0 else null,
        )
      )
    }
    else -> error("lowBoundary() can only be applied to Decimal, Date, DateTime, and Time values")
  }
}

/**
 * See
 * [specification](https://build.fhir.org/ig/HL7/FHIRPath/#highboundaryprecision-integer-decimal-date-datetime-time).
 * The greatest possible value of the input to the specified precision.
 */
internal fun Collection<Any>.highBoundary(params: List<Any>): Collection<Any> {
  check(size <= 1) { "highBoundary() cannot be called on a collection with more than 1 item" }
  val value = singleOrNull() ?: return emptyList()
  val precision = params.singleOrNull() as? Int

  return when (value) {
    is Double -> {
      // TODO: Implement highBoundary for Decimal
      listOf(value)
    }
    is FhirDate -> {
      TODO("Implement highBoundary for Date")
    }
    is FhirPathDateTime -> {
      val targetPrecision = getDateTimePrecision(precision)
      val year = value.year
      val month =
        if (targetPrecision >= FhirPathDateTime.Precision.MONTH) value.month ?: 12 else null
      val day =
        if (targetPrecision >= FhirPathDateTime.Precision.DAY) {
          // Find the last valid day of the month
          val lastDayOfMonth =
            LocalDate(year, month!!, 1)
              .plus(DatePeriod(months = 1))
              .minus(DatePeriod(days = 1))
              .dayOfMonth
          value.day ?: lastDayOfMonth
        } else null
      listOf(
        FhirPathDateTime(
          year = year,
          month = month,
          day = day,
          hour = if (targetPrecision >= FhirPathDateTime.Precision.HOUR) value.hour ?: 23 else null,
          minute =
            if (targetPrecision >= FhirPathDateTime.Precision.MINUTE) value.minute ?: 59 else null,
          second =
            if (targetPrecision >= FhirPathDateTime.Precision.SECOND) value.second ?: 59.999999999
            else null,
          utcOffset = value.utcOffset,
        )
      )
    }
    is FhirPathTime -> {
      val targetPrecision = getTimePrecision(precision)
      listOf(
        FhirPathTime(
          hour = value.hour,
          minute =
            if (targetPrecision >= FhirPathTime.Precision.MINUTE) value.minute ?: 59 else null,
          second =
            if (targetPrecision >= FhirPathTime.Precision.SECOND) value.second ?: 59.999999999
            else null,
        )
      )
    }
    else -> error("highBoundary() can only be applied to Decimal, Date, DateTime, and Time values")
  }
}

/** See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#precision--integer). */
internal fun Collection<Any>.precision(): Collection<Any> {
  check(size <= 1) { "precision() cannot be called on a collection with more than 1 item" }
  val value = singleOrNull() ?: return emptyList()

  val precisionValue =
    when (value) {
      is Double -> TODO("Implement precision for Decimal")
      is FhirPathDateTime -> {
        var precision = 4 // Year
        if (value.month != null) precision += 2
        if (value.day != null) precision += 2
        if (value.hour != null) precision += 2
        if (value.minute != null) precision += 2
        if (value.second != null) {
          precision += 2 // For 'ss' part
          // Add precision for fractional seconds, if they exist
          val fracPart = value.second.toString().substringAfter('.', "")
          precision += fracPart.length
        }
        precision
      }
      is FhirPathTime -> {
        var precision = 2 // Hour
        if (value.minute != null) precision += 2
        if (value.second != null) {
          precision += 2 // For 'ss' part
          val fracPart = value.second.toString().substringAfter('.', "")
          precision += fracPart.length
        }
        precision
      }
      else -> error("precision() can only be applied to Decimal, Date, DateTime, and Time values")
    }
  return listOf(precisionValue)
}

private fun getDateTimePrecision(precision: Int?): FhirPathDateTime.Precision {
  return when (precision) {
    4 -> FhirPathDateTime.Precision.YEAR
    6 -> FhirPathDateTime.Precision.MONTH
    8 -> FhirPathDateTime.Precision.DAY
    10 -> FhirPathDateTime.Precision.HOUR
    12 -> FhirPathDateTime.Precision.MINUTE
    14,
    15,
    16,
    17,
    null -> FhirPathDateTime.Precision.SECOND
    else -> error("Invalid precision value: $precision")
  }
}

private fun getTimePrecision(precision: Int?): FhirPathTime.Precision {
  return when (precision) {
    2 -> FhirPathTime.Precision.HOUR
    4 -> FhirPathTime.Precision.MINUTE
    6,
    7,
    8,
    9,
    null -> FhirPathTime.Precision.SECOND
    else -> error("Invalid precision value: $precision")
  }
}
