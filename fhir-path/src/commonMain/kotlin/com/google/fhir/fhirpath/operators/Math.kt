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

package com.google.fhir.fhirpath.operators

import com.google.fhir.fhirpath.toEqualCanonicalized
import com.google.fhir.fhirpath.toFhirPathType
import com.google.fhir.fhirpath.types.FhirPathDate
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathQuantity
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.fhirpath.types.FhirPathTypeResolver
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

val DECIMAL_MODE =
  DecimalMode(
    decimalPrecision = 15, // TODO: clarify this with the specification
    roundingMode =
      RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, // See https://jira.hl7.org/browse/FHIR-53159
  )

val DATE_ARITHMETIC_UNITS =
  listOf("year", "years", "month", "months", "week", "weeks", "day", "days")
val TIME_ARITHMETIC_UNITS =
  listOf("hour", "hours", "minute", "minutes", "second", "seconds", "millisecond", "milliseconds")
val DATETIME_ARITHMETIC_UNITS = DATE_ARITHMETIC_UNITS + TIME_ARITHMETIC_UNITS

/** See [specification](https://hl7.org/fhirpath/N1/#multiplication). */
internal fun multiplication(
  left: Collection<Any>,
  right: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val leftItem = left.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()
  val rightItem = right.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()

  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(rightItem * leftItem)
    leftItem is Int && rightItem is FhirPathQuantity -> listOf(rightItem * leftItem.toBigDecimal())
    leftItem is Long && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(rightItem * leftItem)
    leftItem is Long && rightItem is FhirPathQuantity -> listOf(rightItem * leftItem.toBigDecimal())
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is FhirPathQuantity -> listOf(rightItem * leftItem)
    leftItem is FhirPathQuantity && rightItem is Int -> {
      listOf(leftItem * rightItem.toBigDecimal())
    }
    leftItem is FhirPathQuantity && rightItem is Long -> {
      listOf(leftItem * rightItem.toBigDecimal())
    }
    leftItem is FhirPathQuantity && rightItem is BigDecimal -> {
      listOf(leftItem * rightItem)
    }
    leftItem is FhirPathQuantity && rightItem is FhirPathQuantity -> {
      val leftCanonical = leftItem.toEqualCanonicalized()
      val rightCanonical = rightItem.toEqualCanonicalized()

      val resultValue = leftCanonical.value!! * rightCanonical.value!!

      val leftUnits = parseUcumUnit(leftCanonical.unit ?: "")
      val rightUnits = parseUcumUnit(rightCanonical.unit ?: "")
      val combinedUnits = leftUnits * rightUnits
      val resultUnitString = formatUcumUnit(combinedUnits)

      listOf(FhirPathQuantity(value = resultValue, unit = resultUnitString))
    }
    else -> error("Cannot multiply $leftItem and $rightItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#division). */
internal fun division(
  left: Collection<Any>,
  right: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val leftItem = left.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()
  val rightItem = right.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()

  if (leftItem is FhirPathQuantity && rightItem is FhirPathQuantity) {
    val leftCanonical = leftItem.toEqualCanonicalized()
    val rightCanonical = rightItem.toEqualCanonicalized()

    if (rightCanonical.value!! == BigDecimal.ZERO) return emptyList()

    val resultValue = leftCanonical.value!!.divide(rightCanonical.value, DECIMAL_MODE)

    val leftUnits = parseUcumUnit(leftCanonical.unit ?: "")
    val rightUnits = parseUcumUnit(rightCanonical.unit ?: "")
    val combinedUnits = leftUnits / rightUnits
    val resultUnitString = formatUcumUnit(combinedUnits)

    return listOf(FhirPathQuantity(value = resultValue, unit = resultUnitString))
  }

  val leftBigDecimal =
    when (leftItem) {
      is Int -> leftItem.toBigDecimal()
      is Long -> leftItem.toBigDecimal()
      is BigDecimal -> leftItem
      else -> error("Operand of division must be a number")
    }
  val rightBigDecimal =
    when (rightItem) {
      is Int -> rightItem.toBigDecimal()
      is Long -> rightItem.toBigDecimal()
      is BigDecimal -> rightItem
      else -> error("Operand of division must be a number")
    }

  if (rightBigDecimal == BigDecimal.ZERO) return emptyList()
  return listOf(leftBigDecimal.divide(rightBigDecimal, DECIMAL_MODE))
}

/** See [specification](https://hl7.org/fhirpath/N1/#addition). */
@OptIn(ExperimentalTime::class)
internal fun addition(
  left: Collection<Any>,
  right: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val leftItem = left.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()
  val rightItem = right.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()

  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem + rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem + rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(rightItem + leftItem)
    leftItem is Long && rightItem is Int -> listOf(leftItem + rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem + rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(rightItem + leftItem)
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem + rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem + rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem + rightItem)
    leftItem is String && rightItem is String -> listOf(leftItem + rightItem)
    leftItem is FhirPathQuantity && rightItem is FhirPathQuantity ->
      TODO("Implement adding two quantities")
    leftItem is FhirPathDate && rightItem is FhirPathQuantity -> listOf(leftItem + rightItem)
    leftItem is FhirPathDateTime && rightItem is FhirPathQuantity -> listOf(leftItem + rightItem)
    leftItem is FhirPathTime && rightItem is FhirPathQuantity -> listOf(leftItem + rightItem)
    else -> error("Cannot add $leftItem and $rightItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#subtraction). */
internal fun subtraction(
  left: Collection<Any>,
  right: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val leftItem = left.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()
  val rightItem = right.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()
  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem - rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem - rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(-rightItem + leftItem)
    leftItem is Long && rightItem is Int -> listOf(leftItem - rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem - rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(-rightItem + leftItem)
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem - rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem - rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem - rightItem)
    leftItem is FhirPathQuantity && rightItem is FhirPathQuantity ->
      TODO("Implement subtracting two quantities")
    leftItem is FhirPathDate && rightItem is FhirPathQuantity -> listOf(leftItem - rightItem)
    leftItem is FhirPathDateTime && rightItem is FhirPathQuantity -> listOf(leftItem - rightItem)
    leftItem is FhirPathTime && rightItem is FhirPathQuantity -> listOf(leftItem - rightItem)
    else -> error("Cannot subtract $rightItem from $leftItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#div). */
internal fun div(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftBigDecimal =
    when (val leftItem = left.singleOrNull() ?: return emptyList()) {
      is Int -> leftItem.toBigDecimal()
      is Long -> leftItem.toBigDecimal()
      is BigDecimal -> leftItem
      else -> error("Operand of div must be a number")
    }
  val rightBigDecimal =
    when (val rightItem = right.singleOrNull() ?: return emptyList()) {
      is Int -> rightItem.toBigDecimal()
      is Long -> rightItem.toBigDecimal()
      is BigDecimal -> rightItem
      else -> error("Operand of div must be a number")
    }
  if (rightBigDecimal == BigDecimal.ZERO) return emptyList()

  val (quotient, _) = leftBigDecimal divrem rightBigDecimal
  return listOf(quotient.intValue())
}

/** See [specification](https://hl7.org/fhirpath/N1/#mod). */
internal fun mod(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val leftBigDecimal =
    when (leftItem) {
      is Int -> leftItem.toBigDecimal()
      is Long -> leftItem.toBigDecimal()
      is BigDecimal -> leftItem
      else -> error("Operand of mod must be a number")
    }
  val rightItem = right.singleOrNull() ?: return emptyList()
  val rightBigDecimal =
    when (rightItem) {
      is Int -> rightItem.toBigDecimal()
      is Long -> rightItem.toBigDecimal()
      is BigDecimal -> rightItem
      else -> error("Operand of mod must be a number")
    }
  if (rightBigDecimal.isZero()) return emptyList()

  val (_, remainder) = leftBigDecimal divrem rightBigDecimal
  if (leftItem is Int && rightItem is Int) return listOf(remainder.intValue())
  if (
    (leftItem is Long && rightItem is Long) ||
      (leftItem is Int && rightItem is Long) ||
      (leftItem is Long && rightItem is Int)
  ) {
    // N.B. the specification does not specify what to do if the result is out of range for Integer.
    return listOf(remainder.longValue())
  }
  return listOf(remainder)
}

/** See [specification](https://hl7.org/fhirpath/N1/#string-concatenation) */
internal fun concat(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  check(left.size <= 1) { "& cannot be called on a collection with more than 1 item" }
  check(right.size <= 1) { "& cannot be called on a collection with more than 1 item" }

  val leftString = (left.singleOrNull() as String?) ?: ""
  val rightString: String = (right.singleOrNull() as String?) ?: ""
  return listOf(leftString + rightString)
}

private operator fun FhirPathQuantity.times(multiplier: BigDecimal): FhirPathQuantity {
  return FhirPathQuantity(value = this.value!! * multiplier, unit = this.unit)
}

/**
 * Splits a UCUM unit string into components, preserving the separator (`.` or `/`) with each
 * component.
 *
 * Examples:
 * - `"m"` → `["m"]`
 * - `"m/s"` → `["m", "/s"]`
 * - `"m.s-2"` → `["m", ".s-2"]`
 * - `"m/s.kg"` → `["m", "/s", ".kg"]`
 *
 * Uses lookahead regex to split before separators without consuming them.
 */
private fun splitUcumComponents(unitString: String): List<String> {
  return unitString.split(Regex("(?=[./])"))
}

/**
 * Parses a unit component (e.g., "m2", "s-1") into unit name and exponent. Defaults to exponent 1
 * if not specified.
 *
 * Examples:
 * - `"m"` → `Pair("m", 1)`
 * - `"m2"` → `Pair("m", 2)`
 * - `"s-1"` → `Pair("s", -1)`
 * - `"kg-2"` → `Pair("kg", -2)`
 * - `"123"` → `null` (no unit letters)
 */
private fun parseUnitAndExponent(component: String): Pair<String, Int>? {
  val match = Regex("([a-zA-Z]+)(-?\\d*)").matchEntire(component) ?: return null
  val unit = match.groupValues[1]
  val exponentStr = match.groupValues[2]
  val exponent = if (exponentStr.isEmpty()) 1 else exponentStr.toInt()
  return Pair(unit, exponent)
}

/**
 * Parses a UCUM unit string into a map of unit names to exponents. Once `/` is encountered, all
 * subsequent units (even after `.`) become negative (denominator).
 *
 * Examples:
 * - `"'m'"` → `{m=1}`
 * - `"m2"` → `{m=2}`
 * - `"g/m"` → `{g=1, m=-1}`
 * - `"m2.s-2"` → `{m=2, s=-2}`
 * - `"m/s.kg"` → `{m=1, s=-1, kg=-1}` (both s and kg in denominator)
 * - `"'1'"` → `{}` (dimensionless)
 *
 * Throws error if duplicate units found (e.g., "m.m").
 */
internal fun parseUcumUnit(unitString: String): Map<String, Int> {
  // Strip single quotes if present
  val cleanString = unitString.trim('\'')
  if (cleanString.isEmpty() || cleanString == "1") return emptyMap()

  val result = mutableMapOf<String, Int>()
  val components = splitUcumComponents(cleanString)

  var inDenominator = false
  for (component in components) {
    if (component.startsWith("/")) {
      inDenominator = true
    }

    val cleanComponent = component.removePrefix("/").removePrefix(".")

    val parsed = parseUnitAndExponent(cleanComponent)
    if (parsed != null) {
      val (unit, exponent) = parsed
      val finalExponent = if (inDenominator) -exponent else exponent
      if (result.containsKey(unit))
        error("Duplicate unit '$unit' in UCUM unit string '$unitString'")
      result[unit] = finalExponent
    }
  }

  return result
}

/**
 * Multiplies two unit maps by adding exponents (a^m × a^n = a^(m+n)). Filters out units that cancel
 * to zero.
 *
 * UCUM units are handled naively without canonicalization in this operation. For example, `kg` and
 * `g` are considered separate units. Similarly, `W` is not handled as `J/s` (therefore cannot be
 * multiplied with `s` to get `J`).
 *
 * Examples:
 * - `{m=1} * {m=1}` → `{m=2}`
 * - `{m=2, s=-1} * {s=1}` → `{m=2}` (s cancels)
 * - `{g=1} * {m=1}` → `{g=1, m=1}`
 * - `{m=1} * {m=-1}` → `{}` (dimensionless)
 */
private operator fun Map<String, Int>.times(other: Map<String, Int>): Map<String, Int> {
  val result = this.toMutableMap()
  for ((unit, exponent) in other) {
    result[unit] = (result[unit] ?: 0) + exponent
  }
  return result.filterValues { it != 0 }
}

/**
 * Divides two unit maps by subtracting exponents (a^m ÷ a^n = a^(m-n)). Filters out units that
 * cancel to zero.
 *
 * UCUM units are handled naively without canonicalization in this operation. For example, `kg` and
 * `g` are considered separate units. Similarly, `W` is not handled as `J/s` (therefore cannot be
 * multiplied with `s` to get `J`).
 *
 * Examples:
 * - `{m=1} / {m=1}` → `{}` (dimensionless)
 * - `{m=2} / {m=1}` → `{m=1}`
 * - `{g=1, m=1} / {m=1}` → `{g=1}` (m cancels)
 * - `{m=1} / {s=1}` → `{m=1, s=-1}`
 * - `{}` / `{s=1}` → `{s=-1}`
 */
private operator fun Map<String, Int>.div(other: Map<String, Int>): Map<String, Int> {
  val result = this.toMutableMap()
  for ((unit, exponent) in other) {
    result[unit] = (result[unit] ?: 0) - exponent
  }
  return result.filterValues { it != 0 }
}

/**
 * Formats a unit map into a UCUM string with inline notation. Units sorted alphabetically, joined
 * with `.`. Omits exponent when it's 1.
 *
 * Examples:
 * - `{}` → `"'1'"` (dimensionless)
 * - `{m=1}` → `"'m'"`
 * - `{m=2}` → `"'m2'"`
 * - `{g=1, m=-1}` → `"'g.m-1'"`
 * - `{m=2, s=-2}` → `"'m2.s-2'"`
 * - `{kg=-1, m=1, s=-1}` → `"'kg-1.m.s-1'"`
 * - `{s=-1}` → `"'s-1'"` (Hz frequency)
 *
 * Throws error if any unit has exponent 0 (should never happen due to filtering).
 */
private fun formatUcumUnit(units: Map<String, Int>): String {
  if (units.isEmpty()) return "'1'"

  val unitString =
    units.entries
      .sortedBy { it.key }
      .joinToString(".") { (unit, exp) ->
        when {
          exp == 1 -> unit // m
          exp > 1 -> "$unit$exp" // m2
          exp < 0 -> "$unit$exp" // m-2
          exp == 0 -> error("Unit should not have zero exponent: $unit")
          else -> error("Unit must be an integer: $unit")
        }
      }

  return "'$unitString'"
}

private operator fun FhirPathDate.plus(duration: FhirPathQuantity): FhirPathDate {
  check(duration.unit!! in DATE_ARITHMETIC_UNITS)
  return when (precision) {
    FhirPathDate.Precision.YEAR -> {
      FhirPathDate(year + convertToYear(duration))
    }
    FhirPathDate.Precision.MONTH -> {
      val (increment, unit) = convertToMonth(duration)
      YearMonth(year = year, month = month!!).plus(increment, unit).let {
        FhirPathDate(year = it.year, month = it.month.number)
      }
    }
    FhirPathDate.Precision.DAY -> {
      val (increment, unit) = convertToDay(duration)
      LocalDate(year = year, month = month!!, day = day!!).plus(increment, unit).let {
        FhirPathDate(year = it.year, month = it.month.number, day = it.day)
      }
    }
  }
}

@OptIn(ExperimentalTime::class)
private operator fun FhirPathDateTime.plus(duration: FhirPathQuantity): FhirPathDateTime {
  check(duration.unit!! in DATETIME_ARITHMETIC_UNITS)
  return when (precision) {
    FhirPathDateTime.Precision.YEAR ->
      FhirPathDateTime(year = year + convertToYear(duration), utcOffset = utcOffset)
    FhirPathDateTime.Precision.MONTH -> {
      val (increment, unit) = convertToMonth(duration)
      YearMonth(year = year, month = month!!).plus(increment, unit).let {
        FhirPathDateTime(year = it.year, month = it.month.number, utcOffset = utcOffset)
      }
    }
    FhirPathDateTime.Precision.DAY -> {
      val (increment, unit) = convertToDay(duration)
      LocalDate(year = year, month = month!!, day = day!!).plus(increment, unit).let {
        FhirPathDateTime(
          year = it.year,
          month = it.month.number,
          day = it.day,
          utcOffset = utcOffset,
        )
      }
    }
    FhirPathDateTime.Precision.HOUR ->
      LocalDateTime(
          year = year,
          month = month!!,
          day = day!!,
          hour = hour!!,
          minute = 0, // Unused
        )
        .toInstant(TimeZone.UTC)
        .plus(convertToHour(duration), TimeZone.UTC)
        .toLocalDateTime(TimeZone.UTC)
        .let {
          FhirPathDateTime(
            year = it.year,
            month = it.month.number,
            day = it.day,
            hour = it.hour,
            utcOffset = utcOffset,
          )
        }
    FhirPathDateTime.Precision.MINUTE ->
      LocalDateTime(year = year, month = month!!, day = day!!, hour = hour!!, minute = minute!!)
        .toInstant(TimeZone.UTC)
        .plus(convertToMinute(duration), TimeZone.UTC)
        .toLocalDateTime(TimeZone.UTC)
        .let {
          FhirPathDateTime(
            year = it.year,
            month = it.month.number,
            day = it.day,
            hour = it.hour,
            minute = it.minute,
            utcOffset = utcOffset,
          )
        }
    FhirPathDateTime.Precision.SECOND ->
      LocalDateTime(
          year = year,
          month = month!!,
          day = day!!,
          hour = hour!!,
          minute = minute!!,
          second = second!!.toInt(),
          nanosecond = ((second % 1) * 1_000_000_000).toInt(),
        )
        .toInstant(TimeZone.UTC)
        .plus(convertToSecond(duration), TimeZone.UTC)
        .toLocalDateTime(TimeZone.UTC)
        .let {
          FhirPathDateTime(
            year = it.year,
            month = it.month.number,
            day = it.day,
            hour = it.hour,
            minute = it.minute,
            second = it.second.toDouble() + it.nanosecond.toDouble() / 1_000_000_000,
            utcOffset = utcOffset,
          )
        }
  }
}

@OptIn(ExperimentalTime::class)
private operator fun FhirPathTime.plus(duration: FhirPathQuantity): FhirPathTime {
  check(duration.unit!! in TIME_ARITHMETIC_UNITS)
  return when (precision) {
    FhirPathTime.Precision.HOUR ->
      LocalDateTime(
          year = 1900,
          month = 1,
          day = 1,
          hour = hour,
          minute = 0, // Unused
        )
        .toInstant(TimeZone.UTC)
        .plus(convertToHour(duration), TimeZone.UTC)
        .toLocalDateTime(TimeZone.UTC)
        .let { FhirPathTime(hour = it.hour) }
    FhirPathTime.Precision.MINUTE ->
      LocalDateTime(year = 1900, month = 1, day = 1, hour = hour, minute = minute!!)
        .toInstant(TimeZone.UTC)
        .plus(convertToMinute(duration), TimeZone.UTC)
        .toLocalDateTime(TimeZone.UTC)
        .let { FhirPathTime(hour = it.hour, minute = it.minute) }
    FhirPathTime.Precision.SECOND ->
      LocalDateTime(
          year = 1900,
          month = 1,
          day = 1,
          hour = hour,
          minute = minute!!,
          second = second!!.toInt(),
          nanosecond = ((second % 1) * 1_000_000_000).toInt(),
        )
        .toInstant(TimeZone.UTC)
        .plus(convertToSecond(duration), TimeZone.UTC)
        .toLocalDateTime(TimeZone.UTC)
        .let {
          FhirPathTime(
            hour = it.hour,
            minute = it.minute,
            second = it.second.toDouble() + it.nanosecond.toDouble() / 1_000_000_000,
          )
        }
  }
}

private operator fun FhirPathDate.minus(duration: FhirPathQuantity): FhirPathDate =
  this + duration.let { FhirPathQuantity(value = -it.value!!, unit = it.unit) }

private operator fun FhirPathDateTime.minus(duration: FhirPathQuantity): FhirPathDateTime =
  this + duration.let { FhirPathQuantity(value = -it.value!!, unit = it.unit) }

private operator fun FhirPathTime.minus(duration: FhirPathQuantity): FhirPathTime =
  this + duration.let { FhirPathQuantity(value = -it.value!!, unit = it.unit) }

/**
 * Returns the number of years in the calendar duration.
 *
 * Used for date/time arithmetic with precision of year.
 */
private fun convertToYear(quantity: FhirPathQuantity): Int {
  val unit = quantity.unit!!
  // TODO: Clarify how to handle decimal values e.g. 1.5 years
  val intValue = quantity.value!!.intValue(true)
  return when (unit) {
    "year",
    "years" -> intValue
    "month",
    "months" -> intValue / 12
    "week",
    "weeks" -> intValue * 7 / 365
    "day",
    "days" -> intValue / 365
    "hour",
    "hours" -> intValue / 24 / 365
    "minute",
    "minutes" -> intValue / 60 / 24 / 365
    "second",
    "seconds" -> intValue / 60 / 60 / 24 / 365
    "millisecond",
    "milliseconds" -> intValue / 1000 / 60 / 60 / 24 / 365
    else -> error("Unit cannot be used for date arithmetic: '$unit'")
  }
}

/**
 * Returns the value of the calendar duration with unit year or month.
 *
 * Used for date/time arithmetic with precision of month.
 */
private fun convertToMonth(quantity: FhirPathQuantity): Pair<Int, DateTimeUnit.MonthBased> {
  val unit = quantity.unit!!
  // TODO: Clarify how to handle decimal values e.g. 1.5 years
  val intValue = quantity.value!!.intValue(true)
  return when (unit) {
    "year",
    "years" -> intValue to DateTimeUnit.YEAR
    "month",
    "months" -> intValue to DateTimeUnit.MONTH
    "week",
    "weeks" -> intValue * 7 / 30 to DateTimeUnit.MONTH
    "day",
    "days" -> intValue / 30 to DateTimeUnit.MONTH
    "hour",
    "hours" -> intValue / 24 / 30 to DateTimeUnit.MONTH
    "minute",
    "minutes" -> intValue / 60 / 24 / 30 to DateTimeUnit.MONTH
    "second",
    "seconds" -> intValue / 60 / 60 / 24 / 30 to DateTimeUnit.MONTH
    "millisecond",
    "milliseconds" -> intValue / 1000 / 60 / 60 / 24 / 30 to DateTimeUnit.MONTH
    else -> error("Unit cannot be used for date arithmetic: '$unit'")
  }
}

/**
 * Returns the value of the calendar duration with unit year, month, or day.
 *
 * Used for date/time arithmetic with precision of day.
 */
private fun convertToDay(quantity: FhirPathQuantity): Pair<Int, DateTimeUnit.DateBased> {
  val unit = quantity.unit!!
  // TODO: Clarify how to handle decimal values e.g. 1.5 years
  val intValue = quantity.value!!.intValue(exactRequired = false)
  return when (unit) {
    "year",
    "years" -> intValue to DateTimeUnit.YEAR
    "month",
    "months" -> intValue to DateTimeUnit.MONTH
    "week",
    "weeks" -> intValue * 7 to DateTimeUnit.DAY
    "day",
    "days" -> intValue to DateTimeUnit.DAY
    "hour",
    "hours" -> intValue / 24 to DateTimeUnit.DAY
    "minute",
    "minutes" -> intValue / 60 / 24 to DateTimeUnit.DAY
    "second",
    "seconds" -> intValue / 60 / 60 / 24 to DateTimeUnit.DAY
    "millisecond",
    "milliseconds" -> intValue / 1000 / 60 / 60 / 24 to DateTimeUnit.DAY
    else -> error("Unit cannot be used for date arithmetic: '$unit'")
  }
}

/**
 * Returns a [DateTimePeriod] for the calendar duration with unit year, month, day, or hour.
 *
 * Used for date/time arithmetic with precision of hour.
 */
private fun convertToHour(quantity: FhirPathQuantity): DateTimePeriod {
  val unit = quantity.unit!!
  // TODO: Clarify how to handle decimal values e.g. 1.5 years
  val intValue = quantity.value!!.intValue(exactRequired = false)
  return when (unit) {
    "year",
    "years" -> DateTimePeriod(years = intValue)
    "month",
    "months" -> DateTimePeriod(months = intValue)
    "week",
    "weeks" -> DateTimePeriod(days = intValue * 7)
    "day",
    "days" -> DateTimePeriod(days = intValue)
    "hour",
    "hours" -> DateTimePeriod(hours = intValue)
    "minute",
    "minutes" -> DateTimePeriod(hours = intValue / 60)
    "second",
    "seconds" -> DateTimePeriod(hours = intValue / 60 / 60)
    "millisecond",
    "milliseconds" -> DateTimePeriod(hours = intValue / 1000 / 60 / 60)
    else -> error("Unit cannot be used for date arithmetic: '$unit'")
  }
}

/**
 * Returns a [DateTimePeriod] for the calendar duration with unit year, month, day, hour, or minute.
 *
 * Used for date/time arithmetic with precision of minute.
 */
private fun convertToMinute(quantity: FhirPathQuantity): DateTimePeriod {
  val unit = quantity.unit!!
  // TODO: Clarify how to handle decimal values e.g. 1.5 years
  val intValue = quantity.value!!.intValue(exactRequired = false)
  return when (unit) {
    "year",
    "years" -> DateTimePeriod(years = intValue)
    "month",
    "months" -> DateTimePeriod(months = intValue)
    "week",
    "weeks" -> DateTimePeriod(days = intValue * 7)
    "day",
    "days" -> DateTimePeriod(days = intValue)
    "hour",
    "hours" -> DateTimePeriod(hours = intValue)
    "minute",
    "minutes" -> DateTimePeriod(minutes = intValue)
    "second",
    "seconds" -> DateTimePeriod(minutes = intValue / 60)
    "millisecond",
    "milliseconds" -> DateTimePeriod(minutes = intValue / 1000 / 60)
    else -> error("Unit cannot be used for date arithmetic: '$unit'")
  }
}

/**
 * Returns a [DateTimePeriod] for the calendar duration with unit year, month, day, hour, minute,
 * second, or nanosecond.
 *
 * Used for date/time arithmetic with precision of second.
 */
private fun convertToSecond(quantity: FhirPathQuantity): DateTimePeriod {
  val unit = quantity.unit!!
  // TODO: Clarify how to handle decimal values e.g. 1.5 years
  val intValue = quantity.value!!.intValue(exactRequired = false)
  return when (unit) {
    "year",
    "years" -> DateTimePeriod(years = intValue)
    "month",
    "months" -> DateTimePeriod(months = intValue)
    "week",
    "weeks" -> DateTimePeriod(days = intValue * 7)
    "day",
    "days" -> DateTimePeriod(days = intValue)
    "hour",
    "hours" -> DateTimePeriod(hours = intValue)
    "minute",
    "minutes" -> DateTimePeriod(minutes = intValue)
    "second",
    "seconds" -> DateTimePeriod(seconds = intValue)
    "millisecond",
    "milliseconds" -> DateTimePeriod(nanoseconds = intValue * 1000_000L)
    else -> error("Unit cannot be used for date arithmetic: '$unit'")
  }
}
