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
import com.google.fhir.fhirpath.types.FhirPathDate
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathQuantity
import com.google.fhir.fhirpath.types.FhirPathTime
import com.ionspin.kotlin.bignum.decimal.BigDecimal

internal fun compare(left: Any, right: Any): Int? {
  val (leftFhirPath, rightFhirPath) = (left to right).asComparableOperands()

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
    leftFhirPath is BigDecimal && rightFhirPath is BigDecimal -> {
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
