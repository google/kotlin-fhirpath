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

import com.google.fhir.fhirpath.asComparableOperands
import com.google.fhir.fhirpath.toEqualCanonicalized
import com.google.fhir.fhirpath.toEquivalentCanonicalized
import com.google.fhir.fhirpath.toFhirPathType
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathQuantity
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.FhirDate
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * See [specification](https://hl7.org/fhirpath/N1/#equals).
 *
 * The FHIRPath specification states every pair of items must be equal for two collections to be
 * equal. However, it does not explicitly states how to account for item comparison results that are
 * empty sets. This can be interpreted as an inconsistency since this definition does not reduce
 * nicely to the special case of collections with a single item. Consider two collections with a
 * single item in each that are incomparable (returns empty set if compared), if we use the
 * definition of equality for collections, we would conclude the two collections are not equal. But
 * the special case states that an empty set should be returned. This nuance results in inconsistent
 * behaviors in different implementations.
 *
 * In our implementation we choose to treat empty sets as inconclusive, and return an empty set if
 * the equality of two collections cannot be decided.
 *
 * See
 * [discussion](https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Collection.20equality/with/540473873).
 * Also see: https://jira.hl7.org/browse/FHIR-53076
 */
internal fun equal(left: Collection<Any>, right: Collection<Any>): Boolean? {
  if (left.isEmpty() || right.isEmpty()) {
    return null
  }
  if (left.size != right.size) {
    return false
  }

  val pairwiseComparisons = left.zip(right).map { (l, r) -> itemsEqual(l, r) }
  return when {
    pairwiseComparisons.any { it == false } -> false
    pairwiseComparisons.all { it == true } -> true
    else -> null
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#equivalent). */
internal fun equivalent(left: Collection<Any>, right: Collection<Any>): Boolean {
  if (left.isEmpty() && right.isEmpty()) {
    return true
  }
  if (left.isEmpty() || right.isEmpty()) {
    return false
  }
  if (left.size != right.size) {
    return false
  }

  var toBeMatched = right.toMutableList()
  for (item in left) {
    val match = toBeMatched.firstOrNull { itemsEquivalent(item, it) } ?: return false
    toBeMatched.remove(match)
  }
  return true
}

/** See [specification](https://hl7.org/fhirpath/N1/#equals). */
private fun itemsEqual(left: Any, right: Any): Boolean? {
  val (leftFhirPath, rightFhirPath) =
    (left.toFhirPathType() to right.toFhirPathType()).asComparableOperands()

  return when {
    leftFhirPath is String && rightFhirPath is String -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is Int && rightFhirPath is Int -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is Long && rightFhirPath is Long -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is BigDecimal && rightFhirPath is BigDecimal -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is Boolean && rightFhirPath is Boolean -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirDate && rightFhirPath is FhirDate -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirPathDateTime && rightFhirPath is FhirPathDateTime -> {
      // TODO: Handle equivalent timezones (+00:00 = -00:00 = Z)
      leftFhirPath.compareTo(rightFhirPath)?.let { it == 0 }
    }
    leftFhirPath is FhirPathTime && rightFhirPath is FhirPathTime -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirPathQuantity && rightFhirPath is FhirPathQuantity -> {
      with(leftFhirPath.toEqualCanonicalized() to rightFhirPath.toEqualCanonicalized()) {
        val leftUnits = parseUcumUnit(first.unit!!)
        val rightUnits = parseUcumUnit(second.unit!!)
        if (leftUnits != rightUnits) return null
        return first.value == second.value
      }
    }
    else -> {
      leftFhirPath == rightFhirPath
    }
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#equivalent). */
private fun itemsEquivalent(left: Any, right: Any): Boolean {
  val (leftFhirPath, rightFhirPath) =
    (left.toFhirPathType() to right.toFhirPathType()).asComparableOperands()

  return when {
    leftFhirPath is String && rightFhirPath is String -> {
      leftFhirPath.normalize() == rightFhirPath.normalize()
    }
    leftFhirPath is Int && rightFhirPath is Int -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is Long && rightFhirPath is Long -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is BigDecimal && rightFhirPath is BigDecimal -> {
      // Determine decimal equivalence to the precision of the less precise operand
      val minLastDigitPosition =
        minOf(leftFhirPath.lastDigitPosition, rightFhirPath.lastDigitPosition)
      leftFhirPath.roundToDigitPosition(minLastDigitPosition, DECIMAL_MODE.roundingMode) ==
        rightFhirPath.roundToDigitPosition(minLastDigitPosition, DECIMAL_MODE.roundingMode)
    }
    leftFhirPath is Boolean && rightFhirPath is Boolean -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirDate && rightFhirPath is FhirDate -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirPathDateTime && rightFhirPath is FhirPathDateTime -> {
      // TODO: Handle timezone conversion
      // TODO: Handle equivalent timezones (+00:00 = -00:00 = Z)
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirPathTime && rightFhirPath is FhirPathTime -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is FhirPathQuantity && rightFhirPath is FhirPathQuantity -> {
      with(leftFhirPath.toEquivalentCanonicalized() to rightFhirPath.toEquivalentCanonicalized()) {
        return first.value == second.value && first.unit!! == second.unit!!
      }
    }
    // TODO: Handle implicit conversion from Date to DateTime
    // TODO: Handle implicit conversion from Integer and Decimal to Quantity
    else -> {
      leftFhirPath == rightFhirPath
    }
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#string-equivalence). */
private fun String.normalize() = lowercase().replace(Regex("\\s+"), " ").trim()

/**
 * The last digit position with precision. This is used to round the decimal value to the less
 * precise operand whilst determining equivalence.
 *
 * This value is calculated using `precision` and `exponent` of the [BigDecimal]. For values with
 * non-negative exponent, the last digit position is simply the precision. For values with negative
 * exponent, the last digit position is the precision minus the exponent since there are additional
 * zeros that are not accounted for in the precision, but should be accounted for when calculating
 * the last digit position.
 *
 * For example:
 * - 670 has precision 3, and the last digit position is 3
 * - 6.7 has precision 2, and the last digit position is 2
 * - 0.67 has precision 2, and the last digit position is 3
 * - 0.0067 has precision 2, and the last digit position is 5
 */
private val BigDecimal.lastDigitPosition: Long
  get() {
    return if (exponent >= 0) precision else precision - exponent
  }
