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

package dev.ohs.fhir.fhirpath.codegen.ucum

import dev.ohs.fhir.fhirpath.codegen.ucum.schema.BaseUnit
import dev.ohs.fhir.fhirpath.codegen.ucum.schema.Root
import kotlin.math.pow

/**
 * Separates unit components in the unit expression.
 *
 * For example, `F/m` will be separated into `F` and `/m`.
 */
private val UNIT_COMPONENT_SEPARATOR = Regex("(?=[./])")

/**
 * Represents the original unit component in the UCUM specification. For example, the Newtonian
 * constant of gravitation `[G]` is defined in the UCUM specification with the unit string
 * `m3.kg-1.s-2` which includes 3 unit components:
 * - unit `m` with the scalar 1 and power 3
 * - unit `g` with the scalar 1000 and power -1
 * - unit `s` with the scalar 1 and power -2
 *
 * Notice that the unit `kg` is already deconstructed as the prefix k (generating the scalar 1000,
 * which is represented as power of 10 in the unit component) and the unit `g`.
 *
 * N.B. Unlike [CanonicalUnitRepresentation], the units used in this data class do not have to be
 * base units.
 */
data class UnitComponent(val scalarAsPowerOfTen: Int, val code: String, val power: Int)

/**
 * Represents a unit using a scalar and a list of base units (each with a power component).
 *
 * For example, the Newtonian constant of gravitation `[G]` is defined in the UCUM specification by
 * the scalar value 6.67430e-11 and the unit string `m3.kg-1.s-2`.
 *
 * Its canonical representation will have the scalar value 6.67430e-14 and the base unit string
 * `g-1.m3.s-2`. Notice that the unit string is canonicalized using the base unit (g in this case)
 * and ordered and the scalar value is adapted accordingly.
 *
 * This canonicalization makes quantity comparison possible. Two quantities with units that have the
 * same base unit string can be compared after applying the scalars to the quantity values
 * respectively.
 */
internal data class CanonicalUnitRepresentation(
  val baseUnits: Map<BaseUnit, Int>,
  val scalar: Double,
) {
  fun getBaseUnitString() =
    baseUnits.entries
      .sortedBy { it.key.Code } // Sort the units for deterministic output
      .joinToString(".") { (baseUnit, power) -> "${baseUnit.Code}$power" }
}

internal class UcumUnitCanonicalizer(root: Root) {
  private val prefixMap = root.prefixes.associateBy { it.Code }
  private val baseUnitMap = root.baseUnits.associateBy { it.Code }
  private val unitMap = root.units.associateBy { it.Code }

  init {
    // Parse the unit string to generate unit components (transient field)
    for (unit in unitMap.values) {
      unit.components =
        unit.value.Unit!!
          .split(UNIT_COMPONENT_SEPARATOR)
          .filterNot { it.isBlank() }
          .mapNotNull { it.toUnitComponent() }
    }
  }

  /** Used for dynamic programming memorization. */
  private val cache = mutableMapOf<String, CanonicalUnitRepresentation>()

  init {
    // Add all base units to the cache
    for (baseUnit in root.baseUnits) {
      cache[baseUnit.Code] = CanonicalUnitRepresentation(mapOf(baseUnit to 1), 1.0)
    }
  }

  fun canonicalize(code: String): CanonicalUnitRepresentation? {
    cache[code]?.let {
      return it
    }

    // We can safely return since base units are already in the cache
    val unit = unitMap[code] ?: return null

    var scalar = unit.value.value?.toDouble() ?: 1.0
    val baseUnits = mutableMapOf<BaseUnit, Int>()

    for (component in unit.components) {
      val canonicalUnitRepresentation = canonicalize(component.code) ?: return null
      for (baseUnit in canonicalUnitRepresentation.baseUnits.entries) {
        baseUnits[baseUnit.key] =
          baseUnits.getOrPut(baseUnit.key) { 0 } + baseUnit.value * component.power
      }

      // Apply the scalar of the component to the result
      scalar *= canonicalUnitRepresentation.scalar
      // Apply the prefix scalar to the result (e.g. the `k` in kg^2 results in a 10^6 scalar)
      scalar *= 10.0.pow(component.scalarAsPowerOfTen.toDouble()).pow(component.power)
    }

    val result = CanonicalUnitRepresentation(baseUnits.filterValues { it != 0 }, scalar)
    cache[code] = result
    return result
  }

  /**
   * Converts a unit component in the unit string to a [UnitComponent] if it is valid. Otherwise,
   * returns null.
   *
   * Examples of valid unit components:
   * - m (base unit)
   * - Hz (derived unit)
   * - [c] (unit code with [])
   * - .m (dot separator)
   * - /m (inverse)
   * - m2 (power)
   * - km (prefix k)
   * - .km2
   */
  private fun String.toUnitComponent(): UnitComponent? {
    val (sign, term) = stripOperator()

    prefixMap
      .filter { term.startsWith(it.key) }
      .forEach { prefix ->
        val termWithoutPrefix = term.removePrefix(prefix.key)
        val (code, power) = termWithoutPrefix.toUnitAndPower() ?: return@forEach
        return UnitComponent(prefix.value.power!!, code, power * sign)
      }
    val (code, power) = term.toUnitAndPower() ?: return null
    return UnitComponent(0, code, power * sign)
  }

  /**
   * Strips the prefix `.` or `/` and returns the sign (1 for `.` or no prefix, and -1 for `/`) and
   * the rest of the string.
   */
  private fun String.stripOperator(): Pair<Int, String> {
    if (startsWith(".")) return 1 to removePrefix(".")
    if (startsWith("/")) return -1 to removePrefix("/")
    return 1 to this
  }

  /**
   * Converts the string to a unit code and a power if it is valid. Otherwise, returns null.
   *
   * For example:
   * - "m" -> "m" to 1
   * - "m2" -> "m" to 2
   * - "m-1" -> "m" to -1
   * - "invalid" -> null
   */
  private fun String.toUnitAndPower(): Pair<String, Int>? {
    (baseUnitMap.keys + unitMap.keys)
      .filter { startsWith(it) }
      .forEach { code ->
        val power = removePrefix(code)
        if (power.isBlank()) {
          return code to 1
        }
        power.toIntOrNull()?.let {
          return code to it
        }
      }
    return null
  }
}
