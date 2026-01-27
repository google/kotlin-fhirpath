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

package com.google.fhir.fhirpath

import com.google.fhir.fhirpath.types.FhirPathQuantity
import com.google.fhir.fhirpath.ucum.BaseUnit
import com.google.fhir.fhirpath.ucum.Prefix
import com.google.fhir.fhirpath.ucum.Unit
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.math.pow

internal fun FhirPathQuantity.toEqualCanonicalized() =
  toEqualUcumDefiniteDuration().stripUcumPrefix().toCanonicalizedUcumUnit()

internal fun FhirPathQuantity.toEquivalentCanonicalized() =
  toEquivalentUcumDefiniteDuration().stripUcumPrefix().toCanonicalizedUcumUnit()

/** Multiplies two quantities, combining their UCUM units. */
internal fun multiplyQuantities(
  left: FhirPathQuantity,
  right: FhirPathQuantity,
): FhirPathQuantity {
  val leftCanonical = left.toEqualCanonicalized()
  val rightCanonical = right.toEqualCanonicalized()

  val resultValue = leftCanonical.value!! * rightCanonical.value!!

  val leftUnits = parseUcumUnit(leftCanonical.unit ?: "")
  val rightUnits = parseUcumUnit(rightCanonical.unit ?: "")
  val combinedUnits = leftUnits * rightUnits
  val resultUnitString = formatUcumUnit(combinedUnits)

  return FhirPathQuantity(value = resultValue, unit = resultUnitString)
}

/** Divides two quantities, combining their UCUM units. Returns `null` if the divisor is zero. */
internal fun divideQuantities(
  left: FhirPathQuantity,
  right: FhirPathQuantity,
  decimalMode: DecimalMode,
): FhirPathQuantity? {
  val leftCanonical = left.toEqualCanonicalized()
  val rightCanonical = right.toEqualCanonicalized()

  if (rightCanonical.value!! == BigDecimal.ZERO) return null

  val resultValue = leftCanonical.value!!.divide(rightCanonical.value, decimalMode)

  val leftUnits = parseUcumUnit(leftCanonical.unit ?: "")
  val rightUnits = parseUcumUnit(rightCanonical.unit ?: "")
  val combinedUnits = leftUnits / rightUnits
  val resultUnitString = formatUcumUnit(combinedUnits)

  return FhirPathQuantity(value = resultValue, unit = resultUnitString)
}

/**
 * Converts a FHIRPath calendar duration to the equal UCUM definite unit if there is one. Returns
 * the original quantity, otherwise.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#time-valued-quantities).
 *
 * N.B. The equality relationship is maintained for up to `weeks` following discussions with Bryn
 * Rhodes. This change has not yet been made in the latest version of the specification.
 */
private fun FhirPathQuantity.toEqualUcumDefiniteDuration(): FhirPathQuantity {
  val calendarDurationCode = unit ?: return this
  val ucumDefinitionDurationCode =
    when (calendarDurationCode) {
      "week",
      "weeks" -> "'wk'"
      "day",
      "days" -> "'d'"
      "hour",
      "hours" -> "'h'"
      "minute",
      "minutes" -> "'min'"
      "second",
      "seconds" -> "'s'"
      "millisecond",
      "milliseconds" -> "'ms'"
      else -> return this
    }
  return FhirPathQuantity(value = value, unit = ucumDefinitionDurationCode)
}

/**
 * Converts a FHIRPath calendar duration to the equivalent UCUM definite unit if there is one.
 * Returns the original quantity, otherwise.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#time-valued-quantities).
 */
private fun FhirPathQuantity.toEquivalentUcumDefiniteDuration(): FhirPathQuantity {
  val calendarDurationCode = unit ?: return this
  val ucumDefinitionDurationCode =
    when (calendarDurationCode) {
      "year",
      "years" -> "'a'"
      "month",
      "months" -> "'mo'"
      "week",
      "weeks" -> "'wk'"
      "day",
      "days" -> "'d'"
      "hour",
      "hours" -> "'h'"
      "minute",
      "minutes" -> "'min'"
      "second",
      "seconds" -> "'s'"
      "millisecond",
      "milliseconds" -> "'ms'"
      else -> return this
    }
  return FhirPathQuantity(value = value, unit = ucumDefinitionDurationCode)
}

/**
 * Returns a new quantity value equal to the original value with any UCUM prefix removed.
 *
 * For example:
 * - 1.0 'kg' -> 1000.0 'g'
 */
private fun FhirPathQuantity.stripUcumPrefix(): FhirPathQuantity {
  // TODO: Handle more complex UCUM strings
  val code = unit?.stripSingleQuotes() ?: return this
  for (prefix in Prefix.entries) {
    if (!code.startsWith(prefix.code)) continue
    val codeWithoutPrefix = code.removePrefix(prefix.code)
    if (codeWithoutPrefix in (BaseUnit.entries.map { it.code } + Unit.entries.map { it.code })) {
      return FhirPathQuantity(
        value = value!! * 10.0.pow(prefix.power).toBigDecimal(),
        unit = "'$codeWithoutPrefix'",
      )
    }
  }
  return this
}

/**
 * Returns a new quantity value with a canonicalized unit string composed of base UCUM units.
 *
 * N.B. "1" will be appended to base units to make them comparable to the base unit strings of
 * derived units.
 *
 * For example:
 * - 1.0 'h' -> 3600.0 's1'
 * - 1.0 'kg' -> 1000.0 'g1'
 * - 1.0 'g' -> 1.0 'g1' (to be comparable to kg and other units derived from grams)
 */
private fun FhirPathQuantity.toCanonicalizedUcumUnit(): FhirPathQuantity {
  val unitCode = unit?.stripSingleQuotes() ?: return this

  // Process base units
  BaseUnit.fromString(unitCode)?.let {
    return FhirPathQuantity(value = value!!, unit = "'${it.code}1'")
  }

  // Process derived units
  Unit.fromString(unitCode)?.let {
    return FhirPathQuantity(value = value!! * it.scalar.toBigDecimal(), unit = "'${it.base}'")
  }

  return this
}

private fun String.stripSingleQuotes(): String {
  return if (startsWith("'") && endsWith("'")) trim('\'') else this
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
