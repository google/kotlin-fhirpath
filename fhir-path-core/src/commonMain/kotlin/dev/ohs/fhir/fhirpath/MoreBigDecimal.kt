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

package dev.ohs.fhir.fhirpath

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Returns the number of decimal places (digits to the right of the decimal point, including
 * trailing zeros) of this [BigDecimal].
 *
 * Uses the internal [precision] and [exponent] to calculate the unnormalized decimal places (since
 * standard formatting functions like `toPlainString()` normalize the value and discard trailing
 * zeros).
 */
val BigDecimal.decimalPlaces: Long
  get() = kotlin.math.max(0L, precision - (exponent + 1))

/**
 * Converts this [BigDecimal] to a plain string representation, formatting or padding it to have at
 * least the specified number of [decimalPlaces].
 *
 * If this number already has more decimal places than requested, the plain string is returned
 * unmodified without truncating or rounding.
 *
 * E.g.:
 * - `BigDecimal.fromDouble(12.5).toPlainString(3)` -> `"12.500"`
 * - `BigDecimal.fromDouble(12.0).toPlainString(0)` -> `"12.0"`
 * - `BigDecimal.fromDouble(12.3456).toPlainString(2)` -> `"12.3456"`
 */
fun BigDecimal.toPlainStringWithMinDecimalPlaces(decimalPlaces: Long): String {
  val plain = toPlainString()
  val parts = plain.split('.')
  val integerPart = parts[0]
  val fractionPart = parts.getOrNull(1) ?: ""

  if (fractionPart.length >= decimalPlaces) {
    return plain
  }
  val paddedFraction = fractionPart.padEnd(decimalPlaces.toInt(), '0')
  return "$integerPart.$paddedFraction"
}

/**
 * Converts this [BigDecimal] to a plain string representation while preserving its decimal places
 * (including trailing zeros).
 *
 * For example, if this [BigDecimal] represents `1.500`, it will return `"1.500"` (retaining the
 * trailing zeros) rather than the standard library's `toPlainString()` which normalizes it to
 * `"1.5"`.
 */
fun BigDecimal.toPlainStringPreservingDecimalPlaces(): String {
  return toPlainStringWithMinDecimalPlaces(decimalPlaces)
}

private val DECIMAL_REGEX = Regex("""^[+-]?\d+(\.\d+)?$""")

/**
 * Parses a string representation of a decimal number into a [BigDecimal] while preserving its
 * decimal places.
 *
 * For example:
 * - "1.500" will be parsed with 3 decimal places (trailing zeros are preserved).
 * - "120" will be parsed with 0 decimal places.
 *
 * This is crucial for FHIRPath precision and boundary calculations where trailing zeros in decimals
 * carry significant semantic meaning. The underlying ionspin library's `BigDecimal.parseString()`
 * normalizes numbers and strips trailing zeros (so `"1.500"` becomes `1.5`, losing scale). It also
 * accepts scientific notation (e.g. `"1e5"`), which is not part of FHIRPath decimal syntax.
 */
fun String.toBigDecimalPreservingScale(): BigDecimal {
  val decimalIndex = indexOf('.')
  if (decimalIndex == -1) {
    return BigDecimal.fromBigInteger(BigInteger.parseString(this))
  }
  val decimalPlaces = length - 1 - decimalIndex

  val significand = BigInteger.parseString(replace(".", ""))
  val significandDigits = significand.abs().toString().length
  val exponent = significandDigits - 1 - decimalPlaces
  return BigDecimal.fromBigIntegerWithExponent(significand, exponent.toLong())
}

/**
 * Parses a string representation of a decimal number into a [BigDecimal] while preserving its
 * decimal places, or returns `null` if the string is not a valid FHIRPath decimal representation.
 *
 * This validates against valid decimal string format (`^[+-]?\d+(\.\d+)?$`) before parsing to
 * reject malformed strings (e.g., multiple dots, trailing dots, scientific notation).
 *
 * Note: The underlying ionspin library's `BigDecimal.parseString()` normalizes numbers and strips
 * trailing zeros (so `"1.500"` becomes `1.5`, losing scale). It also accepts scientific notation
 * (e.g., `"1e5"`), which is not part of FHIRPath decimal syntax.
 */
fun String.toBigDecimalOrNullPreservingScale(): BigDecimal? {
  if (!DECIMAL_REGEX.matches(this)) {
    return null
  }
  return try {
    toBigDecimalPreservingScale()
  } catch (_: Exception) {
    null
  }
}

/**
 * Creates a [BigDecimal] representing seconds from the given whole [second] and [nanosecond]
 * integer values.
 *
 * Note: Because [nanosecond] is an integer (e.g. `500_000_000` for 0.5s), original string-level
 * trailing zeros (such as `"15.5000000"`) cannot be distinguished from `"15.5"`. Trailing zeros are
 * stripped so the returned [BigDecimal] has the minimal decimal scale necessary to represent the
 * non-zero nanosecond fraction.
 *
 * For example:
 * - `createSecondBigDecimal(15, 0)` -> `BigDecimal("15")` (0 decimal places)
 * - `createSecondBigDecimal(15, 500_000_000)` -> `BigDecimal("15.5")` (1 decimal place; cannot
 *   produce `"15.5000000"`)
 * - `createSecondBigDecimal(15, 123_000_000)` -> `BigDecimal("15.123")` (3 decimal places)
 * - `createSecondBigDecimal(15, 123_456_789)` -> `BigDecimal("15.123456789")` (9 decimal places)
 *
 * @param second The integer count of seconds.
 * @param nanosecond The integer count of nanoseconds (0..999_999_999).
 * @return A [BigDecimal] representing the combined second and fractional second value.
 */
fun createSecondBigDecimal(second: Int, nanosecond: Int): BigDecimal {
  val nanoStr = nanosecond.toString().padStart(9, '0').trimEnd('0')
  val secondStr = if (nanoStr.isEmpty()) "$second" else "$second.$nanoStr"
  return secondStr.toBigDecimalPreservingScale()
}
