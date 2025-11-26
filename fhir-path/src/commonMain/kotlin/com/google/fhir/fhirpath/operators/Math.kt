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

package com.google.fhir.fhirpath.operators

import com.google.fhir.fhirpath.toEqualCanonicalized
import com.google.fhir.model.r4.Decimal
import com.google.fhir.model.r4.Quantity
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

val DECIMAL_MODE =
  DecimalMode(
    decimalPrecision = 15, // TODO: clarify this with the specification
    roundingMode =
      RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, // See https://jira.hl7.org/browse/FHIR-53159
  )

/** See [specification](https://hl7.org/fhirpath/N1/#multiplication). */
internal fun multiplication(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()

  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(rightItem * leftItem)
    leftItem is Int && rightItem is Quantity -> listOf(rightItem * leftItem.toBigDecimal())
    leftItem is Long && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(rightItem * leftItem)
    leftItem is Long && rightItem is Quantity -> listOf(rightItem * leftItem.toBigDecimal())
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is Quantity -> listOf(rightItem * leftItem)
    leftItem is Quantity && rightItem is Int -> {
      listOf(leftItem * rightItem.toBigDecimal())
    }
    leftItem is Quantity && rightItem is Long -> {
      listOf(leftItem * rightItem.toBigDecimal())
    }
    leftItem is Quantity && rightItem is BigDecimal -> {
      listOf(leftItem * rightItem)
    }
    leftItem is Quantity && rightItem is Quantity -> {
      val leftCanonical = leftItem.toEqualCanonicalized()
      val rightCanonical = rightItem.toEqualCanonicalized()

      val resultValue = leftCanonical.value!!.value!! * rightCanonical.value!!.value!!

      val leftUnits = parseUcumUnit(leftCanonical.unit?.value ?: "")
      val rightUnits = parseUcumUnit(rightCanonical.unit?.value ?: "")
      val combinedUnits = combineUnitsMultiply(leftUnits, rightUnits)
      val resultUnitString = formatUcumUnit(combinedUnits)

      listOf(
        Quantity(
          value = Decimal(value = resultValue),
          unit = com.google.fhir.model.r4.String(value = resultUnitString),
        )
      )
    }
    else -> error("Cannot multiply $leftItem and $rightItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#division). */
internal fun division(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()

  if (leftItem is Quantity && rightItem is Quantity) {
    val leftCanonical = leftItem.toEqualCanonicalized()
    val rightCanonical = rightItem.toEqualCanonicalized()

    if (rightCanonical.value!!.value!! == BigDecimal.ZERO) return emptyList()

    val resultValue = leftCanonical.value!!.value!!.divide(rightCanonical.value!!.value!!, DECIMAL_MODE)

    val leftUnits = parseUcumUnit(leftCanonical.unit?.value ?: "")
    val rightUnits = parseUcumUnit(rightCanonical.unit?.value ?: "")
    val combinedUnits = combineUnitsDivide(leftUnits, rightUnits)
    val resultUnitString = formatUcumUnit(combinedUnits)

    return listOf(
      Quantity(
        value = Decimal(value = resultValue),
        unit = com.google.fhir.model.r4.String(value = resultUnitString),
      )
    )
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
internal fun addition(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()

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
    leftItem is Quantity && rightItem is Quantity -> TODO("Implement adding two quantities")
    else -> error("Cannot add $leftItem and $rightItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#subtraction). */
internal fun subtraction(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()
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
    leftItem is Quantity && rightItem is Quantity -> TODO("Implement subtracting two quantities")
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

private operator fun Quantity.times(multiplier: BigDecimal): Quantity {
  return Quantity(
    id = this.id,
    extension = this.extension,
    value =
      with(this.value!!) {
        Decimal(id = this.id, extension = this.extension, value = this.value!! * multiplier)
      },
    comparator = this.comparator,
    unit = this.unit,
    system = this.system,
    code = this.code,
  )
}

/**
 * Parses a UCUM unit string into a map of base units to their exponents.
 *
 * Examples:
 * - "'m'" → {m=1}
 * - "m2" → {m=2}
 * - "g/m" → {g=1, m=-1}
 * - "m2.s-2" → {m=2, s=-2}
 */
private fun parseUcumUnit(unitString: String): Map<String, Int> {
  // Strip single quotes if present
  val cleanString = unitString.trim('\'')
  if (cleanString.isEmpty() || cleanString == "1") return emptyMap()

  val result = mutableMapOf<String, Int>()
  val components = cleanString.split(Regex("(?=[./])"))

  for (component in components) {
    val isDivision = component.startsWith("/")
    val cleanComponent = component.removePrefix("/").removePrefix(".")

    // Match pattern: unit name followed by optional exponent
    val match = Regex("([a-zA-Z]+)(-?\\d*)").matchEntire(cleanComponent)
    if (match != null) {
      val unit = match.groupValues[1]
      val exponentStr = match.groupValues[2]
      val exponent = if (exponentStr.isEmpty()) 1 else exponentStr.toInt()
      val finalExponent = if (isDivision) -exponent else exponent

      result[unit] = (result[unit] ?: 0) + finalExponent
    }
  }

  return result
}

/**
 * Combines two unit maps by adding their exponents (for multiplication).
 *
 * Example: {m=1} × {m=1} → {m=2}
 */
private fun combineUnitsMultiply(
  left: Map<String, Int>,
  right: Map<String, Int>,
): Map<String, Int> {
  val result = left.toMutableMap()
  for ((unit, exponent) in right) {
    result[unit] = (result[unit] ?: 0) + exponent
  }
  return result.filterValues { it != 0 }
}

/**
 * Combines two unit maps by subtracting their exponents (for division).
 *
 * Example: {m=1} ÷ {m=1} → {} (dimensionless)
 */
private fun combineUnitsDivide(
  left: Map<String, Int>,
  right: Map<String, Int>,
): Map<String, Int> {
  val result = left.toMutableMap()
  for ((unit, exponent) in right) {
    result[unit] = (result[unit] ?: 0) - exponent
  }
  return result.filterValues { it != 0 }
}

/**
 * Formats a unit map into a UCUM unit string with single quotes.
 *
 * Examples:
 * - {m=2} → "'m2'"
 * - {g=1, m=-1} → "'g/m'"
 * - {} → "'1'" (dimensionless)
 */
private fun formatUcumUnit(units: Map<String, Int>): String {
  if (units.isEmpty()) return "'1'"

  val positive = units.filter { it.value > 0 }.entries.sortedBy { it.key }
  val negative = units.filter { it.value < 0 }.entries.sortedBy { it.key }

  val positivePart =
    positive.joinToString(".") { (unit, exp) -> if (exp == 1) unit else "$unit$exp" }

  val negativePart =
    negative.joinToString(".") { (unit, exp) -> if (exp == -1) unit else "$unit${-exp}" }

  val unitString = when {
    positive.isEmpty() -> "1/$negativePart"
    negative.isEmpty() -> positivePart
    else -> "$positivePart/$negativePart"
  }

  return "'$unitString'"
}
