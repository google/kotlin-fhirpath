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

package dev.ohs.fhir.fhirpath.types

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import dev.ohs.fhir.fhirpath.decimalPlaces
import dev.ohs.fhir.fhirpath.operators.DECIMAL_MODE
import dev.ohs.fhir.fhirpath.toBigDecimalPreservingScale
import dev.ohs.fhir.fhirpath.toPlainStringPreservingDecimalPlaces

/**
 * The internal representation of the FHIRPath `System.Decimal` type.
 *
 * Like [FhirPathDate], [FhirPathDateTime] and [FhirPathTime], this is the engine's own type rather
 * than the model's: kotlin-fhir ships a separate `FhirDecimal` class per FHIR version, and the
 * version neutral engine converts each of them at the boundary through its wire string, exactly as
 * it does for dates. It mirrors `FhirDecimal`: a precision preserving number backed by an ionspin
 * [BigDecimal] that is kept internal, so trailing zeros survive (`1.50` has two decimal places).
 *
 * Division uses FHIRPath's [DECIMAL_MODE] so non-terminating quotients round instead of throwing.
 */
class FhirPathDecimal private constructor(internal val bigDecimal: BigDecimal) :
  Comparable<FhirPathDecimal> {

  /** Digits to the right of the decimal point, including trailing zeros. */
  val decimalPlaces: Long
    get() = bigDecimal.decimalPlaces

  operator fun plus(other: FhirPathDecimal) = FhirPathDecimal(bigDecimal + other.bigDecimal)

  operator fun minus(other: FhirPathDecimal) = FhirPathDecimal(bigDecimal - other.bigDecimal)

  operator fun times(other: FhirPathDecimal) = FhirPathDecimal(bigDecimal * other.bigDecimal)

  operator fun div(other: FhirPathDecimal) =
    FhirPathDecimal(bigDecimal.divide(other.bigDecimal, DECIMAL_MODE))

  operator fun unaryMinus() = FhirPathDecimal(-bigDecimal)

  fun divideAndRemainder(other: FhirPathDecimal): Pair<FhirPathDecimal, FhirPathDecimal> {
    val (quotient, remainder) = bigDecimal divrem other.bigDecimal
    return FhirPathDecimal(quotient) to FhirPathDecimal(remainder)
  }

  fun isZero() = bigDecimal.isZero()

  fun abs() = FhirPathDecimal(bigDecimal.abs())

  fun ceil() = FhirPathDecimal(bigDecimal.ceil())

  fun floor() = FhirPathDecimal(bigDecimal.floor())

  fun round(decimalPlaces: Long, roundingMode: RoundingMode) =
    FhirPathDecimal(bigDecimal.roundToDigitPositionAfterDecimalPoint(decimalPlaces, roundingMode))

  /** Throws if [exactRequired] and the value has a fractional part; truncates otherwise. */
  fun intValue(exactRequired: Boolean = true) = bigDecimal.intValue(exactRequired)

  fun longValue(exactRequired: Boolean = true) = bigDecimal.longValue(exactRequired)

  fun doubleValue(exactRequired: Boolean = true) = bigDecimal.doubleValue(exactRequired)

  /** Returns the underlying ionspin [BigDecimal]. */
  fun asBigDecimal() = bigDecimal

  override fun compareTo(other: FhirPathDecimal) = bigDecimal.compareTo(other.bigDecimal)

  /** Numeric equality, so `1.0 = 1.00` as the specification requires. */
  override fun equals(other: Any?) = other is FhirPathDecimal && bigDecimal == other.bigDecimal

  override fun hashCode() = bigDecimal.hashCode()

  /** The plain decimal text with its decimal places preserved, e.g. `1.50`. */
  override fun toString() = bigDecimal.toPlainStringPreservingDecimalPlaces()

  companion object {
    val ZERO = FhirPathDecimal(BigDecimal.ZERO)
    val ONE = FhirPathDecimal(BigDecimal.ONE)

    /** Parses a decimal string, preserving its decimal places (`"1.50"` keeps two). */
    fun fromString(string: String) = FhirPathDecimal(string.toBigDecimalPreservingScale())

    fun fromBigDecimal(bigDecimal: BigDecimal) = FhirPathDecimal(bigDecimal)

    fun fromInt(int: Int) = FhirPathDecimal(BigDecimal.fromInt(int))

    fun fromLong(long: Long) = FhirPathDecimal(BigDecimal.fromLong(long))

    fun fromDouble(double: Double) = FhirPathDecimal(BigDecimal.fromDouble(double))
  }
}

fun Int.toFhirPathDecimal() = FhirPathDecimal.fromInt(this)

fun Long.toFhirPathDecimal() = FhirPathDecimal.fromLong(this)

fun Double.toFhirPathDecimal() = FhirPathDecimal.fromDouble(this)
