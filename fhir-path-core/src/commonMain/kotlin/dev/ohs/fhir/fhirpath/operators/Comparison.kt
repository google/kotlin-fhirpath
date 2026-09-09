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

package dev.ohs.fhir.fhirpath.operators

import dev.ohs.fhir.fhirpath.asComparableOperands
import dev.ohs.fhir.fhirpath.toEqualCanonicalized
import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver

internal fun compare(left: Any, right: Any, fhirPathTypeResolver: FhirPathTypeResolver): Int? {
  val (leftFhirPath, rightFhirPath) = (left to right).asComparableOperands(fhirPathTypeResolver)

  return when {
    leftFhirPath is String && rightFhirPath is String -> {
      leftFhirPath.compareTo(rightFhirPath)
    }
    leftFhirPath is Int && rightFhirPath is Int -> {
      leftFhirPath.compareTo(rightFhirPath)
    }
    leftFhirPath is Long && rightFhirPath is Long -> {
      leftFhirPath.compareTo(rightFhirPath)
    }
    leftFhirPath is FhirPathDecimal && rightFhirPath is FhirPathDecimal -> {
      leftFhirPath.compareTo(rightFhirPath)
    }
    leftFhirPath is FhirPathQuantity && rightFhirPath is FhirPathQuantity -> {
      with(leftFhirPath.toEqualCanonicalized() to rightFhirPath.toEqualCanonicalized()) {
        if (first.unit!! != second.unit!!) return null
        return first.value?.compareTo(second.value!!)
      }
    }
    leftFhirPath is FhirPathDate && rightFhirPath is FhirPathDate ->
      leftFhirPath.compareTo(rightFhirPath)
    leftFhirPath is FhirPathDateTime && rightFhirPath is FhirPathDateTime ->
      leftFhirPath.compareTo(rightFhirPath)
    leftFhirPath is FhirPathTime && rightFhirPath is FhirPathTime ->
      leftFhirPath.compareTo(rightFhirPath)
    else -> error("Cannot compare $leftFhirPath and $rightFhirPath")
  }
}

/**
 * Returns whether the two singleton quantities have comparable units, i.e. whether their units
 * canonicalize to the same UCUM base unit (e.g. `cm` and `[in_i]` are both lengths, so they are
 * comparable; `cm` and `s` are not). A quantity with an unknown unit is only comparable to a
 * quantity with the same unit.
 *
 * This function checks whether equality, not the equivalence, can be determined. For example, `(1
 * year) ~ (1 'a')` is true, but `(1 year).comparable(1 'a')` is false, since `(1 year) = (1 'a')`
 * is empty.
 *
 * See [specification](https://hl7.org/fhirpath/STU3/en/#comparableother--quantity--boolean).
 */
internal fun Collection<Any>.comparable(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Boolean> {
  check(size <= 1) { "comparable() cannot be called on a collection with more than 1 item" }
  val left =
    singleOrNull()?.toFhirPathType(fhirPathTypeResolver) as? FhirPathQuantity ?: return emptyList()
  val right =
    params.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) as? FhirPathQuantity
      ?: return emptyList()

  val leftUnit = left.toEqualCanonicalized().unit ?: return listOf(false)
  val rightUnit = right.toEqualCanonicalized().unit ?: return listOf(false)
  return listOf(leftUnit == rightUnit)
}
