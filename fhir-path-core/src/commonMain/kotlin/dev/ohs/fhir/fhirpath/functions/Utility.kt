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

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import dev.ohs.fhir.fhirpath.coerceToType
import dev.ohs.fhir.fhirpath.createSecondBigDecimal
import dev.ohs.fhir.fhirpath.decimalPlaces
import dev.ohs.fhir.fhirpath.toBigDecimalPreservingScale
import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.toPlainStringWithMinDecimalPlaces
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathSystemType
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.offsetAt
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** See [specification](https://hl7.org/fhirpath/STU3/en/#now--datetime). */
@OptIn(ExperimentalTime::class)
internal fun now(now: Instant): Collection<FhirPathDateTime> {
  val systemTimeZone = TimeZone.currentSystemDefault()
  val localDateTime = now.toLocalDateTime(systemTimeZone)
  val utcOffset = systemTimeZone.offsetAt(now)
  return listOf(
    FhirPathDateTime(
      year = localDateTime.year,
      month = localDateTime.month.number,
      day = localDateTime.day,
      hour = localDateTime.hour,
      minute = localDateTime.minute,
      second = createSecondBigDecimal(localDateTime.second, localDateTime.nanosecond),
      utcOffset = utcOffset,
    )
  )
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#timeofday--time). */
@OptIn(ExperimentalTime::class)
internal fun timeOfDay(now: Instant): Collection<Any> {
  val systemTimeZone = TimeZone.currentSystemDefault()
  val localDateTime = now.toLocalDateTime(systemTimeZone)
  return listOf(
    FhirPathTime(
      hour = localDateTime.hour,
      minute = localDateTime.minute,
      second = createSecondBigDecimal(localDateTime.second, localDateTime.nanosecond),
    )
  )
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#today--date). */
@OptIn(ExperimentalTime::class)
internal fun today(now: Instant): Collection<FhirPathDate> {
  val systemTimeZone = TimeZone.currentSystemDefault()
  val localDateTime = now.toLocalDateTime(systemTimeZone)
  return listOf(
    FhirPathDate(
      year = localDateTime.year,
      month = localDateTime.month.number,
      day = localDateTime.day,
    )
  )
}

/**
 * Evaluates `lowBoundary([precision: Integer])`.
 *
 * Computes the least possible value of the input based on its implicit uncertainty interval. If an
 * optional `precision` parameter N is provided, the calculated lowest boundary is rounded and
 * formatted to N decimal places (for Decimal) or to the target date/time precision N (for
 * Date/DateTime/Time).
 *
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#lowboundaryprecision-integer-decimal--date--datetime--time).
 */
internal fun Collection<Any>.lowBoundary(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "lowBoundary() cannot be called on a collection with more than 1 item" }
  val value =
    singleOrNull()?.toFhirPathType(fhirPathTypeResolver)?.coerceToType(FhirPathSystemType.DECIMAL)
      ?: return emptyList()
  val precision = params.singleOrNull() as? Int

  return when (value) {
    is FhirPathDecimal -> computeDecimalLowBoundary(value, precision)
    is FhirPathDate -> {
      val targetPrecision = value.resolvePrecision(precision) ?: return emptyList()
      when (targetPrecision) {
        4 -> listOf(FhirPathDate(year = value.year))
        6 -> listOf(FhirPathDate(year = value.year, month = value.month ?: 1))
        8 -> listOf(FhirPathDate(year = value.year, month = value.month ?: 1, day = value.day ?: 1))
        else -> emptyList()
      }
    }
    is FhirPathDateTime -> {
      val targetPrecisionInt = value.resolvePrecision(precision) ?: return emptyList()
      val targetPrecision = FhirPathDateTime.Precision.fromIntegerPrecision(targetPrecisionInt)
      val targetScale = if (targetPrecisionInt >= 14) targetPrecisionInt - 14 else 0
      val year = value.year
      val month =
        if (targetPrecision >= FhirPathDateTime.Precision.MONTH) value.month ?: 1 else null
      val day = if (targetPrecision >= FhirPathDateTime.Precision.DAY) value.day ?: 1 else null
      if (targetPrecision <= FhirPathDateTime.Precision.DAY) {
        listOf(FhirPathDate(year = year, month = month ?: 1, day = day ?: 1))
      } else {
        listOf(
          FhirPathDateTime(
            year = year,
            month = month,
            day = day,
            hour =
              if (targetPrecision >= FhirPathDateTime.Precision.HOUR) value.hour ?: 0 else null,
            minute =
              if (targetPrecision >= FhirPathDateTime.Precision.MINUTE) value.minute ?: 0 else null,
            second =
              if (targetPrecision >= FhirPathDateTime.Precision.SECOND) {
                value.second?.let { truncateSecond(it, targetScale) } ?: 0.toBigDecimal()
              } else null,
            utcOffset = value.utcOffset,
          )
        )
      }
    }
    is FhirPathTime -> {
      val targetPrecisionInt = value.resolvePrecision(precision) ?: return emptyList()
      val targetPrecision = FhirPathTime.Precision.fromIntegerPrecision(targetPrecisionInt)
      val targetScale = if (targetPrecisionInt >= 6) targetPrecisionInt - 6 else 0
      listOf(
        FhirPathTime(
          hour = value.hour,
          minute =
            if (targetPrecision >= FhirPathTime.Precision.MINUTE) value.minute ?: 0 else null,
          second =
            if (targetPrecision >= FhirPathTime.Precision.SECOND) {
              value.second?.let { truncateSecond(it, targetScale) } ?: 0.toBigDecimal()
            } else null,
        )
      )
    }
    is FhirPathQuantity ->
      value.applyBoundary(params, fhirPathTypeResolver, Collection<Any>::lowBoundary)
    else ->
      error(
        "lowBoundary() can only be applied to Decimal, Date, DateTime, Time, and Quantity values"
      )
  }
}

/**
 * Evaluates `highBoundary([precision: Integer])`.
 *
 * Computes the greatest possible value of the input based on its implicit uncertainty interval. If
 * an optional `precision` parameter N is provided, the calculated highest boundary is rounded and
 * formatted to N decimal places (for Decimal) or to the target date/time precision N (for
 * Date/DateTime/Time).
 *
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#highboundaryprecision-integer-decimal--date--datetime--time).
 */
internal fun Collection<Any>.highBoundary(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "highBoundary() cannot be called on a collection with more than 1 item" }
  val value =
    singleOrNull()?.toFhirPathType(fhirPathTypeResolver)?.coerceToType(FhirPathSystemType.DECIMAL)
      ?: return emptyList()
  val precision = params.singleOrNull() as? Int

  return when (value) {
    is FhirPathDecimal -> computeDecimalHighBoundary(value, precision)
    is FhirPathDate -> {
      val targetPrecision = value.resolvePrecision(precision) ?: return emptyList()
      when (targetPrecision) {
        4 -> listOf(FhirPathDate(year = value.year))
        6 -> listOf(FhirPathDate(year = value.year, month = value.month ?: 12))
        8 -> {
          val month = value.month ?: 12
          listOf(
            FhirPathDate(
              year = value.year,
              month = month,
              day = value.day ?: lastDayOfMonth(value.year, month),
            )
          )
        }
        else -> emptyList()
      }
    }
    is FhirPathDateTime -> {
      val targetPrecisionInt = value.resolvePrecision(precision) ?: return emptyList()
      val targetPrecision = FhirPathDateTime.Precision.fromIntegerPrecision(targetPrecisionInt)
      val targetScale = if (targetPrecisionInt >= 14) targetPrecisionInt - 14 else 0
      val year = value.year
      val month =
        if (targetPrecision >= FhirPathDateTime.Precision.MONTH) value.month ?: 12 else null
      val day =
        if (targetPrecision >= FhirPathDateTime.Precision.DAY) {
          val m = month ?: 12
          value.day ?: lastDayOfMonth(year, m)
        } else null
      if (targetPrecision <= FhirPathDateTime.Precision.DAY) {
        listOf(FhirPathDate(year = year, month = month ?: 12, day = day ?: 31))
      } else {
        listOf(
          FhirPathDateTime(
            year = year,
            month = month,
            day = day,
            hour =
              if (targetPrecision >= FhirPathDateTime.Precision.HOUR) value.hour ?: 23 else null,
            minute =
              if (targetPrecision >= FhirPathDateTime.Precision.MINUTE) value.minute ?: 59
              else null,
            second =
              if (targetPrecision >= FhirPathDateTime.Precision.SECOND) {
                value.second?.let { truncateSecond(it, targetScale) }
                  ?: maxSecondAtScale(targetScale)
              } else null,
            utcOffset = value.utcOffset,
          )
        )
      }
    }
    is FhirPathTime -> {
      val targetPrecisionInt = value.resolvePrecision(precision) ?: return emptyList()
      val targetPrecision = FhirPathTime.Precision.fromIntegerPrecision(targetPrecisionInt)
      val targetScale = if (targetPrecisionInt >= 6) targetPrecisionInt - 6 else 0
      listOf(
        FhirPathTime(
          hour = value.hour,
          minute =
            if (targetPrecision >= FhirPathTime.Precision.MINUTE) value.minute ?: 59 else null,
          second =
            if (targetPrecision >= FhirPathTime.Precision.SECOND) {
              value.second?.let { truncateSecond(it, targetScale) } ?: maxSecondAtScale(targetScale)
            } else null,
        )
      )
    }
    is FhirPathQuantity ->
      value.applyBoundary(params, fhirPathTypeResolver, Collection<Any>::highBoundary)
    else ->
      error(
        "highBoundary() can only be applied to Decimal, Date, DateTime, Time, and Quantity values"
      )
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#precision--integer). */
internal fun Collection<Any>.precision(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Any> {
  check(size <= 1) { "precision() cannot be called on a collection with more than 1 item" }
  val value =
    singleOrNull()?.toFhirPathType(fhirPathTypeResolver)?.coerceToType(FhirPathSystemType.DECIMAL)
      ?: return emptyList()

  val precisionValue =
    when (value) {
      is FhirPathDecimal -> value.decimalPlaces.toInt()
      is FhirPathDate -> value.integerPrecision
      is FhirPathDateTime -> value.integerPrecision
      is FhirPathTime -> value.integerPrecision
      else -> error("precision() can only be applied to Decimal, Date, DateTime, and Time values")
    }
  return listOf(precisionValue)
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#yearof-integer). */
internal fun Collection<Any>.yearOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("yearOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDate -> it.year
      is FhirPathDateTime -> it.year
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#monthof-integer). */
internal fun Collection<Any>.monthOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("monthOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDate -> it.month
      is FhirPathDateTime -> it.month
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#dayof-integer). */
internal fun Collection<Any>.dayOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("dayOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDate -> it.day
      is FhirPathDateTime -> it.day
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#hourof-integer). */
internal fun Collection<Any>.hourOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("hourOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDateTime -> it.hour
      is FhirPathTime -> it.hour
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#minuteof-integer). */
internal fun Collection<Any>.minuteOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("minuteOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDateTime -> it.minute
      is FhirPathTime -> it.minute
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#secondof-integer). */
internal fun Collection<Any>.secondOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("secondOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDateTime -> it.second?.toBigInteger()?.intValue()
      is FhirPathTime -> it.second?.toBigInteger()?.intValue()
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#millisecondof-integer). */
internal fun Collection<Any>.millisecondOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("millisecondOf", fhirPathTypeResolver) {
    when (it) {
        is FhirPathDateTime -> it.second
        is FhirPathTime -> it.second
        else -> null
      }
      ?.takeIf { it.decimalPlaces > 0 }
      ?.let { sec -> (sec * BigDecimal.fromInt(1000)).toBigInteger().intValue() % 1000 }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#timezoneoffsetof-decimal). */
internal fun Collection<Any>.timezoneOffsetOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("timezoneOffsetOf", fhirPathTypeResolver) {
    (it as? FhirPathDateTime)?.utcOffset?.let { offset ->
      FhirPathDecimal.fromInt(offset.totalSeconds) / FhirPathDecimal.fromInt(3600)
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#dateof-date). */
internal fun Collection<Any>.dateOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("dateOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathDate -> it
      is FhirPathDateTime -> FhirPathDate(year = it.year, month = it.month, day = it.day)
      else -> null
    }
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#timeof-time). */
internal fun Collection<Any>.timeOf(fhirPathTypeResolver: FhirPathTypeResolver) =
  extractComponent("timeOf", fhirPathTypeResolver) {
    when (it) {
      is FhirPathTime -> it
      is FhirPathDateTime ->
        it.hour?.let { h -> FhirPathTime(hour = h, minute = it.minute, second = it.second) }
      else -> null
    }
  }

/** Get the last day of the given month in the given year. */
private fun lastDayOfMonth(year: Int, month: Int): Int =
  LocalDate(year, month, 1).plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day

/** Delegate boundary computation to the numeric value of a Quantity. */
private fun FhirPathQuantity.applyBoundary(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
  boundaryFn: Collection<Any>.(List<Any>, FhirPathTypeResolver) -> Collection<Any>,
): Collection<Any> {
  val v = value ?: return emptyList()
  val boundedVal =
    listOf(v).boundaryFn(params, fhirPathTypeResolver).singleOrNull() as? FhirPathDecimal
      ?: return emptyList()
  return listOf(FhirPathQuantity(value = boundedVal, unit = unit))
}

/**
 * Computes the lower boundary (`lowBoundary(precision)`) for a [FhirPathDecimal] value.
 *
 * In short: Finds the lowest boundary of the input's uncertainty interval, then rounds and formats
 * the answer to `targetPrecision` decimal places.
 *
 * ### FHIRPath Decimal Boundary Specification:
 * In FHIRPath, a Decimal value `v` with input scale `S_in` (number of decimal places) represents an
 * implicit uncertainty interval `[v - halfStep, v + halfStep)`.
 * - `halfStep` = `0.5 * 10^-S_in` = `5 * 10^-(S_in + 1)`.
 * - `exactLower` = `v - halfStep`.
 *
 * For example:
 * - `1.5` (scale 1) -> `halfStep = 0.05` -> `exactLower = 1.45`
 * - `1.50` (scale 2) -> `halfStep = 0.005` -> `exactLower = 1.495`
 *
 * ### Target Precision (`precision` parameter `S_target`):
 * The `precision` parameter `S_target` specifies the requested number of decimal places for the
 * result:
 * 1. **`S_target >= S_in` (Expanding or Preserving Precision)**: `exactLower` is rounded to
 *    `S_target` decimal places using `ROUND_HALF_FLOOR`.
 * 2. **`S_target < S_in` (Reducing Precision)**: `exactLower` is floored using `FLOOR` to
 *    `S_target` decimal places. If the result is zero, ensure it is returned as positive zero
 *    (`0.0`) rather than negative zero (`-0.0`).
 */
private fun computeDecimalLowBoundary(decimal: FhirPathDecimal, precision: Int?): Collection<Any> {
  val value = decimal.bigDecimal
  // Validate target precision parameter: must be in 0..8 if specified (defaults to 8)
  if (precision != null && precision !in 0..8) return emptyList()
  val targetPrecision = precision ?: 8
  val inputScale = value.decimalPlaces

  // Calculate half-step offset (0.5 * 10^-inputScale = 5 * 10^-(inputScale + 1))
  val halfStep = BigDecimal.fromIntWithExponent(5, -(inputScale + 1))

  // Calculate exact lower bound: value - halfStep
  val exactLower = value - halfStep

  // Round exactLower to targetPrecision depending on expanding vs reducing precision
  val rounded =
    if (targetPrecision >= inputScale) {
      exactLower.roundToDigitPositionAfterDecimalPoint(
        targetPrecision.toLong(),
        RoundingMode.ROUND_HALF_FLOOR,
      )
    } else {
      // If reducing precision would result in zero, return positive zero instead of negative zero
      // (-0.0)
      val targetHalfStep = BigDecimal.fromIntWithExponent(5, -(targetPrecision + 1).toLong())
      if (value < 0 && exactLower >= -targetHalfStep) {
        BigDecimal.fromBigIntegerWithExponent(BigInteger.ZERO, -targetPrecision.toLong())
      } else {
        exactLower.roundToDigitPositionAfterDecimalPoint(
          targetPrecision.toLong(),
          RoundingMode.FLOOR,
        )
      }
    }

  // Format BigDecimal preserving target scale
  return listOf(
    FhirPathDecimal.fromString(rounded.toPlainStringWithMinDecimalPlaces(targetPrecision.toLong()))
  )
}

/**
 * Computes the upper boundary (`highBoundary(precision)`) for a [FhirPathDecimal] value.
 *
 * In short: Finds the highest boundary of the input's uncertainty interval, then rounds and formats
 * the answer to `targetPrecision` decimal places.
 *
 * ### FHIRPath Decimal Boundary Specification:
 * In FHIRPath, a Decimal value `v` with input scale `S_in` (number of decimal places) represents an
 * implicit uncertainty interval `[v - halfStep, v + halfStep)`.
 * - `halfStep` = `0.5 * 10^-S_in` = `5 * 10^-(S_in + 1)`.
 * - `exactUpper` = `v + halfStep`.
 *
 * For example:
 * - `1.5` (scale 1) -> `halfStep = 0.05` -> `exactUpper = 1.55`
 * - `1.50` (scale 2) -> `halfStep = 0.005` -> `exactUpper = 1.505`
 *
 * ### Target Precision (`precision` parameter `S_target`):
 * `exactUpper` is rounded to `S_target` decimal places using `ROUND_HALF_CEILING` for positive
 * numbers and `CEILING` for negative numbers.
 */
private fun computeDecimalHighBoundary(decimal: FhirPathDecimal, precision: Int?): Collection<Any> {
  val value = decimal.bigDecimal
  // Validate target precision parameter: must be in 0..8 if specified (defaults to 8)
  if (precision != null && precision !in 0..8) return emptyList()
  val targetPrecision = precision ?: 8
  val inputScale = value.decimalPlaces

  // Calculate half-step offset (0.5 * 10^-inputScale = 5 * 10^-(inputScale + 1))
  val halfStep = BigDecimal.fromIntWithExponent(5, -(inputScale + 1))

  // Calculate exact upper bound: value + halfStep
  val exactUpper = value + halfStep

  // Round exactUpper to targetPrecision using ceiling rounding
  val roundingMode = if (value < 0) RoundingMode.CEILING else RoundingMode.ROUND_HALF_CEILING
  val rounded =
    exactUpper.roundToDigitPositionAfterDecimalPoint(targetPrecision.toLong(), roundingMode)

  // Format BigDecimal preserving target scale
  return listOf(
    FhirPathDecimal.fromString(rounded.toPlainStringWithMinDecimalPlaces(targetPrecision.toLong()))
  )
}

/**
 * Helper function for singleton date/time component extraction functions (`yearOf()`, `secondOf()`,
 * etc.).
 *
 * Validates that the input collection contains at most one item, resolves it to its FHIRPath system
 * type, applies the component [extractor], and returns a singleton collection (or empty collection
 * if the component is absent or type is unsupported).
 */
private inline fun <reified T : Any> Collection<Any>.extractComponent(
  name: String,
  fhirPathTypeResolver: FhirPathTypeResolver,
  extractor: (Any) -> T?,
): Collection<T> {
  check(size <= 1) { "$name() cannot be called on a collection with more than 1 item" }
  val value = singleOrNull()?.toFhirPathType(fhirPathTypeResolver) ?: return emptyList()
  return extractor(value)?.let { listOf(it) } ?: emptyList()
}

/**
 * Truncates an existing second value towards zero to the requested [targetScale] (number of decimal
 * digits after the decimal point), while preserving trailing zeros at that scale.
 *
 * Examples:
 * - Truncating `45.123456` to scale `0` (whole seconds) returns `45`.
 * - Truncating `45.123456` to scale `3` (milliseconds) returns `45.123`.
 * - Truncating `45.1` to scale `3` (milliseconds) returns `45.100`.
 */
private fun truncateSecond(second: BigDecimal, targetScale: Int): BigDecimal {
  val truncated =
    second.roundToDigitPositionAfterDecimalPoint(targetScale.toLong(), RoundingMode.TOWARDS_ZERO)
  return truncated
    .toPlainStringWithMinDecimalPlaces(targetScale.toLong())
    .toBigDecimalPreservingScale()
}

/**
 * Returns the maximum possible second value at [targetScale] decimal digits to fill the remainder
 * of a minute interval when calculating the highest boundary (`highBoundary`).
 *
 * Examples:
 * - For scale `0` (whole seconds), returns `59`.
 * - For scale `1` (tenths of a second), returns `59.9`.
 * - For scale `3` (milliseconds), returns `59.999`.
 */
private fun maxSecondAtScale(targetScale: Int): BigDecimal {
  val defaultSecStr = if (targetScale == 0) "59" else "59." + "9".repeat(targetScale)
  return defaultSecStr.toBigDecimalPreservingScale()
}
