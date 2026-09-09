/*
 * Copyright 2026 Open Health Stack Foundation
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

package dev.ohs.fhir.fhirpath.operators

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import dev.ohs.fhir.fhirpath.createSecondBigDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val CALENDAR_DATE_UNITS =
  setOf("year", "years", "month", "months", "week", "weeks", "day", "days")
private val UCUM_DATE_UNITS = setOf("wk", "d")
private val DATE_ARITHMETIC_UNITS = CALENDAR_DATE_UNITS + UCUM_DATE_UNITS

private val CALENDAR_TIME_UNITS =
  setOf("hour", "hours", "minute", "minutes", "second", "seconds", "millisecond", "milliseconds")
private val UCUM_TIME_UNITS = setOf("h", "min", "s", "ms")
private val TIME_ARITHMETIC_UNITS = CALENDAR_TIME_UNITS + UCUM_TIME_UNITS

private val DATETIME_ARITHMETIC_UNITS = DATE_ARITHMETIC_UNITS + TIME_ARITHMETIC_UNITS

private const val MONTHS_PER_YEAR = 12
private const val DAYS_PER_YEAR = 365
private const val DAYS_PER_MONTH = 30
private const val DAYS_PER_WEEK = 7
private const val HOURS_PER_DAY = 24
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR
private const val NANOSECONDS_PER_MILLISECOND = 1_000_000L
private const val NANOSECONDS_PER_SECOND = 1_000_000_000L
private const val NANOSECONDS_PER_MINUTE = 60L * NANOSECONDS_PER_SECOND
private const val NANOSECONDS_PER_HOUR = MINUTES_PER_HOUR * NANOSECONDS_PER_MINUTE
private const val NANOSECONDS_PER_DAY = HOURS_PER_DAY * NANOSECONDS_PER_HOUR

/**
 * Adds a date-based duration ([DATE_ARITHMETIC_UNITS]) to a [FhirPathDate].
 *
 * Per the [specification](https://hl7.org/fhirpath/STU3/en/#datetime-arithmetic), addition of
 * time-valued duration units (hours, minutes, seconds, milliseconds) to a [FhirPathDate] is not
 * permitted and will result in an empty collection `{}` / error.
 */
internal operator fun FhirPathDate.plus(duration: FhirPathQuantity): FhirPathDate {
  val durationInfo = DurationInfo.from(duration, DATE_ARITHMETIC_UNITS)

  return when (precision) {
    FhirPathDate.Precision.YEAR -> FhirPathDate(year = year + durationInfo.toYears())
    FhirPathDate.Precision.MONTH -> addMonthsToYearMonth(year, month!!, durationInfo.toMonths())
    FhirPathDate.Precision.DAY -> addDurationToLocalDate(year, month!!, day!!, durationInfo)
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#datetime-arithmetic). */
internal operator fun FhirPathDate.minus(duration: FhirPathQuantity): FhirPathDate =
  this + duration.copy(value = -duration.value!!)

/** See [specification](https://hl7.org/fhirpath/STU3/en/#datetime-arithmetic). */
@OptIn(ExperimentalTime::class)
internal operator fun FhirPathDateTime.plus(duration: FhirPathQuantity): FhirPathDateTime {
  val durationInfo = DurationInfo.from(duration, DATETIME_ARITHMETIC_UNITS)

  // For partial date time values, the result retains the partial date time precision.
  return when (precision) {
    FhirPathDateTime.Precision.YEAR ->
      FhirPathDateTime(year = year + durationInfo.toYears(), utcOffset = utcOffset)

    FhirPathDateTime.Precision.MONTH -> {
      val dateResult = addMonthsToYearMonth(year, month!!, durationInfo.toMonths())
      FhirPathDateTime(year = dateResult.year, month = dateResult.month, utcOffset = utcOffset)
    }

    FhirPathDateTime.Precision.DAY -> {
      val dateResult = addDurationToLocalDate(year, month!!, day!!, durationInfo)
      FhirPathDateTime(
        year = dateResult.year,
        month = dateResult.month,
        day = dateResult.day,
        utcOffset = utcOffset,
      )
    }

    FhirPathDateTime.Precision.HOUR,
    FhirPathDateTime.Precision.MINUTE,
    FhirPathDateTime.Precision.SECOND -> {
      val (wholeSecond, nanosecond) = second?.toSecondAndNanosecond() ?: (0 to 0)
      val initialDateTime =
        LocalDateTime(year, month!!, day!!, hour ?: 0, minute ?: 0, wholeSecond, nanosecond)
      val resultDateTime =
        initialDateTime
          .toInstant(TimeZone.UTC)
          .plus(durationInfo.toDateTimePeriod(), TimeZone.UTC)
          .toLocalDateTime(TimeZone.UTC)

      FhirPathDateTime(
        year = resultDateTime.year,
        month = resultDateTime.month.number,
        day = resultDateTime.day,
        hour = resultDateTime.hour.takeIf { precision >= FhirPathDateTime.Precision.HOUR },
        minute = resultDateTime.minute.takeIf { precision >= FhirPathDateTime.Precision.MINUTE },
        second =
          if (precision == FhirPathDateTime.Precision.SECOND) {
            createSecondBigDecimal(resultDateTime.second, resultDateTime.nanosecond)
          } else null,
        utcOffset = utcOffset,
      )
    }
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#datetime-arithmetic). */
internal operator fun FhirPathDateTime.minus(duration: FhirPathQuantity): FhirPathDateTime =
  this + duration.copy(value = -duration.value!!)

/**
 * Adds a time-based duration ([TIME_ARITHMETIC_UNITS]) to a [FhirPathTime], wrapping around the
 * 24-hour clock cycle.
 *
 * Per the [specification](https://hl7.org/fhirpath/STU3/en/#datetime-arithmetic), addition of
 * date-based duration units (years, months, weeks, days) to a [FhirPathTime] is not permitted and
 * will result in an empty collection `{}` / error.
 */
internal operator fun FhirPathTime.plus(duration: FhirPathQuantity): FhirPathTime {
  val durationInfo = DurationInfo.from(duration, TIME_ARITHMETIC_UNITS)

  return when (precision) {
    FhirPathTime.Precision.HOUR -> {
      val newHour = (hour + durationInfo.toHours()).mod(HOURS_PER_DAY)
      FhirPathTime(hour = newHour)
    }

    FhirPathTime.Precision.MINUTE -> {
      val totalMinutes = hour * MINUTES_PER_HOUR + minute!!
      val newTotalMinutes = (totalMinutes + durationInfo.toMinutes()).mod(MINUTES_PER_DAY)
      FhirPathTime(
        hour = newTotalMinutes / MINUTES_PER_HOUR,
        minute = newTotalMinutes % MINUTES_PER_HOUR,
      )
    }

    FhirPathTime.Precision.SECOND -> {
      val totalNanoseconds =
        second!!.toSecondAndNanosecond().let { (wholeSecond, nanosecond) ->
          LocalTime(hour, minute!!, wholeSecond, nanosecond).toNanosecondOfDay()
        }
      val nanosecondsToAdd =
        (durationInfo.value * BigDecimal.fromLong(durationInfo.unit.nanoseconds)).toBigInteger()
      val newTotalNanoseconds =
        (BigInteger.fromLong(totalNanoseconds) + nanosecondsToAdd)
          .mod(BigInteger.fromLong(NANOSECONDS_PER_DAY))
          .longValue()
      FhirPathTime.fromLocalTime(LocalTime.fromNanosecondOfDay(newTotalNanoseconds))
    }
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#datetime-arithmetic). */
internal operator fun FhirPathTime.minus(duration: FhirPathQuantity): FhirPathTime =
  this + duration.copy(value = -duration.value!!)

private fun addDurationToLocalDate(
  year: Int,
  month: Int,
  day: Int,
  durationInfo: DurationInfo,
): FhirPathDate {
  val resultDate =
    when (durationInfo.unit) {
      DurationUnit.DateBased.YEAR ->
        LocalDate(year, month, day).plus(durationInfo.toYears(), DateTimeUnit.YEAR)
      DurationUnit.DateBased.MONTH ->
        LocalDate(year, month, day).plus(durationInfo.toMonths(), DateTimeUnit.MONTH)
      else -> LocalDate(year, month, day).plus(durationInfo.toDays(), DateTimeUnit.DAY)
    }
  return FhirPathDate(year = resultDate.year, month = resultDate.month.number, day = resultDate.day)
}

private fun addMonthsToYearMonth(year: Int, month: Int, monthsToAdd: Int): FhirPathDate {
  val resultDate = LocalDate(year, month, 1).plus(monthsToAdd, DateTimeUnit.MONTH)
  return FhirPathDate(year = resultDate.year, month = resultDate.month.number)
}

private fun BigDecimal.toSecondAndNanosecond(): Pair<Int, Int> {
  val wholeSecond = toBigInteger()
  val nanosecond =
    ((this - BigDecimal.fromBigInteger(wholeSecond)) * NANOSECONDS_PER_SECOND.toBigDecimal())
      .toBigInteger()
      .intValue()
  return wholeSecond.intValue() to nanosecond
}

/**
 * FHIRPath duration units partitioned into [DateBased] (years, months, weeks, days) and [TimeBased]
 * (hours, minutes, seconds, milliseconds) to enforce date/time arithmetic scope rules.
 *
 * Supports FHIRPath calendar unit names and UCUM abbreviations (`wk`, `d`, `h`, `min`, `s`, `ms`).
 */
private sealed interface DurationUnit {
  val names: Set<String>
  val nanoseconds: Long

  fun toPeriod(value: BigDecimal): DateTimePeriod

  enum class DateBased(override val names: Set<String>, override val nanoseconds: Long) :
    DurationUnit {
    YEAR(setOf("year", "years"), DAYS_PER_YEAR * NANOSECONDS_PER_DAY) {
      override fun toPeriod(value: BigDecimal) =
        DateTimePeriod(years = value.toBigInteger().intValue())
    },
    MONTH(setOf("month", "months"), DAYS_PER_MONTH * NANOSECONDS_PER_DAY) {
      override fun toPeriod(value: BigDecimal) =
        DateTimePeriod(months = value.toBigInteger().intValue())
    },
    WEEK(setOf("week", "weeks", "wk"), DAYS_PER_WEEK * NANOSECONDS_PER_DAY) {
      override fun toPeriod(value: BigDecimal) =
        DateTimePeriod(days = (value.toBigInteger().intValue() * DAYS_PER_WEEK))
    },
    DAY(setOf("day", "days", "d"), NANOSECONDS_PER_DAY) {
      override fun toPeriod(value: BigDecimal) =
        DateTimePeriod(days = value.toBigInteger().intValue())
    },
  }

  enum class TimeBased(override val names: Set<String>, override val nanoseconds: Long) :
    DurationUnit {
    HOUR(setOf("hour", "hours", "h"), NANOSECONDS_PER_HOUR) {
      override fun toPeriod(value: BigDecimal) =
        DateTimePeriod(hours = value.toBigInteger().intValue())
    },
    MINUTE(setOf("minute", "minutes", "min"), NANOSECONDS_PER_MINUTE) {
      override fun toPeriod(value: BigDecimal) =
        DateTimePeriod(minutes = value.toBigInteger().intValue())
    },
    SECOND(setOf("second", "seconds", "s"), NANOSECONDS_PER_SECOND) {
      override fun toPeriod(value: BigDecimal): DateTimePeriod {
        val totalNanos =
          (value * BigDecimal.fromLong(NANOSECONDS_PER_SECOND)).toBigInteger().longValue()
        return DateTimePeriod(nanoseconds = totalNanos)
      }
    },
    MILLISECOND(setOf("millisecond", "milliseconds", "ms"), NANOSECONDS_PER_MILLISECOND) {
      override fun toPeriod(value: BigDecimal): DateTimePeriod {
        val totalNanos =
          (value * BigDecimal.fromLong(NANOSECONDS_PER_MILLISECOND)).toBigInteger().longValue()
        return DateTimePeriod(nanoseconds = totalNanos)
      }
    },
  }

  companion object {
    private val BY_NAME: Map<String, DurationUnit> =
      (DateBased.entries + TimeBased.entries)
        .flatMap { unit -> unit.names.map { it to unit } }
        .toMap()

    fun from(name: String): DurationUnit? = BY_NAME[name]
  }
}

/**
 * A normalized duration container that converts quantities between FHIRPath unit boundaries.
 *
 * When converting from a smaller unit to a larger precision (such as months to years or minutes to
 * hours), conversion uses integer division over fixed scale ratios (`12 months = 1 year`, `30 days
 * = 1 month`, `60 minutes = 1 hour`) and discards any fractional remainder:
 * - `23 months` -> `1 year` (`23 / 12`, remainder ignored)
 * - `5 weeks` -> `1 month` (`35 days / 30`, remainder ignored)
 * - `90 minutes` -> `1 hour` (`90 / 60`, remainder ignored)
 *
 * When converting from a larger unit to a smaller precision (such as years to days or hours to
 * nanoseconds), conversion uses exact multiplication:
 * - `2 years` -> `730 days` (`2 * 365`)
 * - `2 hours` -> `7,200,000,000,000 nanoseconds` (`2 * 3.6e12`)
 *
 * This class implements the calendar duration conversion factors defined in the
 * [specification](https://hl7.org/fhirpath/STU3/en/#time-valued-unit-conversions).
 *
 * @property unit The resolved enum [DurationUnit] (normalizing calendar names and UCUM
 *   abbreviations like `'s'`, `second`, and `seconds` to a single enum instance).
 * @property value The decimal magnitude of the duration as specified in the [FhirPathQuantity]
 *   (used for `second` and `millisecond` units to apply sub-second fractions).
 * @property wholeValue The integer magnitude of the duration, discarding any decimal remainder
 *   (used for all other units—years, months, weeks, days, hours, minutes—per calendar duration
 *   semantics).
 * @property originalUnit The raw unit string from [FhirPathQuantity.unit] (e.g. `'s'` vs `second`),
 *   preserved for descriptive error messages when unit conversion fails.
 */
private class DurationInfo(
  val unit: DurationUnit,
  val value: BigDecimal,
  val originalUnit: String,
) {
  val wholeValue: Long
    get() = value.toBigInteger().longValue()

  fun toYears(): Int =
    when (unit) {
      DurationUnit.DateBased.YEAR -> wholeValue.toInt()
      DurationUnit.DateBased.MONTH -> (wholeValue / MONTHS_PER_YEAR).toInt()
      else -> toDays() / DAYS_PER_YEAR
    }

  fun toMonths(): Int =
    when (unit) {
      DurationUnit.DateBased.YEAR -> (wholeValue * MONTHS_PER_YEAR).toInt()
      DurationUnit.DateBased.MONTH -> wholeValue.toInt()
      else -> toDays() / DAYS_PER_MONTH
    }

  fun toDays(): Int =
    when (unit) {
      DurationUnit.DateBased.YEAR -> (wholeValue * DAYS_PER_YEAR).toInt()
      DurationUnit.DateBased.MONTH -> (wholeValue * DAYS_PER_MONTH).toInt()
      DurationUnit.DateBased.WEEK -> (wholeValue * DAYS_PER_WEEK).toInt()
      DurationUnit.DateBased.DAY -> wholeValue.toInt()
      is DurationUnit.TimeBased -> (wholeValue * unit.nanoseconds / NANOSECONDS_PER_DAY).toInt()
    }

  fun toHours(): Int = (wholeValue * unit.nanoseconds / NANOSECONDS_PER_HOUR).toInt()

  fun toMinutes(): Int = (wholeValue * unit.nanoseconds / NANOSECONDS_PER_MINUTE).toInt()

  fun toDateTimePeriod(): DateTimePeriod = unit.toPeriod(value)

  companion object {
    fun from(duration: FhirPathQuantity, allowedUnits: Set<String>): DurationInfo {
      val rawValue =
        checkNotNull(duration.value) { "Duration quantity must have a value" }.asBigDecimal()
      val unquotedUnit = duration.unit?.removeSurrounding("'")?.removeSurrounding("\"") ?: ""
      check(unquotedUnit in allowedUnits) {
        "Unsupported or invalid unit '${duration.unit}' for date/time arithmetic"
      }
      val unit = checkNotNull(DurationUnit.from(unquotedUnit)) { "Invalid unit '${duration.unit}'" }
      // Preserve decimals only for seconds/milliseconds per spec
      val effectiveValue =
        when (unit) {
          DurationUnit.TimeBased.SECOND,
          DurationUnit.TimeBased.MILLISECOND -> rawValue
          else -> BigDecimal.fromBigInteger(rawValue.toBigInteger())
        }
      return DurationInfo(unit = unit, value = effectiveValue, originalUnit = duration.unit ?: "")
    }
  }
}
