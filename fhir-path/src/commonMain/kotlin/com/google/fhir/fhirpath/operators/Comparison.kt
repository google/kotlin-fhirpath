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
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.FhirDate
import com.google.fhir.model.r4.Quantity

internal fun compare(left: Any, right: Any): Int? {
  val (leftFhirPath, rightFhirPath) = (left to right).asComparableOperands()

  return when {
    leftFhirPath is String && rightFhirPath is String -> {
      leftFhirPath.compareTo(rightFhirPath)
    }
    leftFhirPath is Number && rightFhirPath is Number -> {
      leftFhirPath.toDouble().compareTo(rightFhirPath.toDouble())
    }
    leftFhirPath is Quantity && rightFhirPath is Quantity -> {
      with(leftFhirPath.toEqualCanonicalized() to rightFhirPath.toEqualCanonicalized()) {
        if (first.unit?.value!! != second.unit?.value!!) return null
        return first.value?.value?.compareTo(second.value!!.value!!)
      }
    }
    leftFhirPath is FhirDate && rightFhirPath is FhirDate -> leftFhirPath.compareTo(rightFhirPath)
    leftFhirPath is FhirPathDateTime && rightFhirPath is FhirPathDateTime ->
      leftFhirPath.compareTo(rightFhirPath)
    leftFhirPath is FhirPathTime && rightFhirPath is FhirPathTime ->
      leftFhirPath.compareTo(rightFhirPath)
    else -> error("Cannot compare $leftFhirPath and $rightFhirPath")
  }
}

private fun FhirDate.compareTo(other: FhirDate): Int? {
  return when {
    this is FhirDate.Year && other is FhirDate.Year -> this.value compareTo other.value
    this is FhirDate.YearMonth && other is FhirDate.YearMonth ->
      compareValuesBy(this, other, { it.value.year }, { it.value.month })
    this is FhirDate.Date && other is FhirDate.Date -> this.date compareTo other.date
    else -> null
  }
}
