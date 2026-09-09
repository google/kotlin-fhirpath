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

import dev.ohs.fhir.fhirpath.functions.DEFAULT_UNIT
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathSystemType
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import dev.ohs.fhir.fhirpath.types.toFhirPathDecimal

/**
 * Maps a pair of FHIRPath types where the former can be implicitly converted to the latter to a
 * function that does the conversion.
 *
 * See [specification](https://hl7.org/fhirpath/#conversion).
 */
internal val fhirPathTypeToFhirPathType =
  mapOf<Pair<FhirPathSystemType, FhirPathSystemType>, (any: Any) -> Any>(
    FhirPathSystemType.INTEGER to FhirPathSystemType.LONG to { it -> (it as Int).toLong() },
    FhirPathSystemType.INTEGER to
      FhirPathSystemType.DECIMAL to
      { it ->
        (it as Int).toFhirPathDecimal()
      },
    FhirPathSystemType.INTEGER to
      FhirPathSystemType.QUANTITY to
      { it ->
        FhirPathQuantity(value = (it as Int).toFhirPathDecimal(), unit = DEFAULT_UNIT)
      },
    FhirPathSystemType.LONG to
      FhirPathSystemType.DECIMAL to
      { it ->
        (it as Long).toFhirPathDecimal()
      },
    FhirPathSystemType.DECIMAL to
      FhirPathSystemType.QUANTITY to
      { it ->
        FhirPathQuantity(value = it as FhirPathDecimal, unit = DEFAULT_UNIT)
      },
    FhirPathSystemType.DATE to
      FhirPathSystemType.DATETIME to
      { it ->
        val date = it as FhirPathDate
        FhirPathDateTime(year = date.year, month = date.month, day = date.day)
      },
  )

/**
 * Converts the object to its equivalent FHIRPath system type if one exists, or returns the object
 * itself, otherwise.
 *
 * For example, an object of type `dev.ohs.fhir.model.r4.String` will be converted to a
 * Kotlin.String (the internal representation of FHIRPath system type System.String).
 */
internal fun Any.toFhirPathType(fhirPathTypeResolver: FhirPathTypeResolver): Any {
  return fhirPathTypeResolver.toFhirPathType(this)
}

/**
 * Converts one of the pair of objects to a FHIRPath system type that matches the other, if such
 * implicit conversion is possible, or returns the original pair, otherwise.
 *
 * Possible implicit conversions are defined [here](https://hl7.org/fhirpath/#conversion).
 *
 * For example, a pair of objects of type System.Integer and System.Decimal will be converted to two
 * objects of type System.Decimal.
 */
private fun Pair<Any, Any>.toCommonFhirPathType(): Pair<Any, Any> {
  val firstType = FhirPathSystemType.fromObject(first) ?: return this
  val secondType = FhirPathSystemType.fromObject(second) ?: return this

  fhirPathTypeToFhirPathType[firstType to secondType]?.let {
    return it(first) to second
  }
  fhirPathTypeToFhirPathType[secondType to firstType]?.let {
    return first to it(second)
  }
  return this
}

/**
 * Converts the pair of objects as comparable operands by converting them to FHIRPath system types,
 * and then the common type if possible.
 *
 * Note if the two objects cannot be converted to the same FHIRPath system type, they will still be
 * converted to different FHIRPath system types.
 *
 * For example, a pair of objects of type `Fhir.integer` and System.Decimal will be converted to two
 * objects of type System.Decimal; a pair of objects of type `Fhir.date` and System.Decimal will be
 * converted to two objects of type System.Date and System.Decimal.
 */
internal fun Pair<Any, Any>.asComparableOperands(
  fhirPathTypeResolver: FhirPathTypeResolver
): Pair<Any, Any> {
  return (first.toFhirPathType(fhirPathTypeResolver) to second.toFhirPathType(fhirPathTypeResolver))
    .toCommonFhirPathType()
}

/**
 * Coerces the object to the target FHIRPath system type if an implicit conversion exists.
 *
 * Possible implicit conversions are defined [here](https://hl7.org/fhirpath/#conversion).
 */
internal fun Any.coerceToType(targetType: FhirPathSystemType): Any {
  val currentType = FhirPathSystemType.fromObject(this) ?: return this
  if (currentType == targetType) return this
  return fhirPathTypeToFhirPathType[currentType to targetType]?.invoke(this) ?: this
}
