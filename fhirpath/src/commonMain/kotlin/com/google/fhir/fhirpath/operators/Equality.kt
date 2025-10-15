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

import com.google.fhir.fhirpath.asComparableOperands
import com.google.fhir.fhirpath.toEqualCanonicalized
import com.google.fhir.fhirpath.toEquivalentCanonicalized
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.FhirDate
import com.google.fhir.model.r4.Quantity

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
  val (leftFhirPath, rightFhirPath) = (left to right).asComparableOperands()

  return when {
    leftFhirPath is String && rightFhirPath is String -> {
      leftFhirPath == rightFhirPath
    }
    leftFhirPath is Number && rightFhirPath is Number -> {
      leftFhirPath.toDouble() == rightFhirPath.toDouble()
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
    leftFhirPath is Quantity && rightFhirPath is Quantity -> {
      with(leftFhirPath.toEqualCanonicalized() to rightFhirPath.toEqualCanonicalized()) {
        if (first.unit?.value!! != second.unit?.value!!) return null
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
  val (leftFhirPath, rightFhirPath) = (left to right).asComparableOperands()

  return when {
    leftFhirPath is String && rightFhirPath is String -> {
      leftFhirPath.normalize() == rightFhirPath.normalize()
    }
    leftFhirPath is Number && rightFhirPath is Number -> {
      leftFhirPath.toDouble() == rightFhirPath.toDouble()
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
    leftFhirPath is Quantity && rightFhirPath is Quantity -> {
      with(leftFhirPath.toEquivalentCanonicalized() to rightFhirPath.toEquivalentCanonicalized()) {
        return first.value == second.value && first.unit?.value!! == second.unit?.value!!
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
