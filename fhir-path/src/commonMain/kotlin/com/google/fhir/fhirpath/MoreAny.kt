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

import com.google.fhir.fhirpath.functions.DEFAULT_UNIT
import com.google.fhir.fhirpath.types.FhirPathDate
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathQuantity
import com.google.fhir.fhirpath.types.FhirPathSystemType
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.fhirpath.types.FhirPathTypeResolver
import com.google.fhir.fhirpath.types.FhirR4BComplexType
import com.google.fhir.fhirpath.types.FhirR4BPrimitiveType
import com.google.fhir.fhirpath.types.FhirR4ComplexType
import com.google.fhir.fhirpath.types.FhirR4PrimitiveType
import com.google.fhir.fhirpath.types.FhirR5ComplexType
import com.google.fhir.fhirpath.types.FhirR5PrimitiveType
import com.google.fhir.fhirpath.types.FhirType
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

/**
 * Maps a FHIR R4 type to a FHIRPath system type it can be implicitly converted to and a function
 * that does the conversion.
 *
 * See [specification](https://build.fhir.org/fhirpath.html#types).
 */
internal val fhirR4TypeToFhirPathType =
  mapOf<FhirType, Pair<FhirPathSystemType, (element: Any) -> Any>>(
    // FHIR R4 primitive types
    FhirR4PrimitiveType.Boolean to
      (FhirPathSystemType.BOOLEAN to { it -> (it as com.google.fhir.model.r4.Boolean).value!! }),
    FhirR4PrimitiveType.String to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4.String).value!! }),
    FhirR4PrimitiveType.Uri to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4.Uri).value!! }),
    FhirR4PrimitiveType.Code to
      (FhirPathSystemType.STRING to
        { it ->
          // A code in the Kotlin FHIR library is represented either as an Enumeration if it is
          // bound to a value set, or a Code if it is not.
          when (it) {
            is com.google.fhir.model.r4.Enumeration<*> -> it.value.toString()
            is com.google.fhir.model.r4.Code -> it.value!!
            else -> error("Unknown code type")
          }
        }),
    FhirR4PrimitiveType.Oid to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4.Oid).value!! }),
    FhirR4PrimitiveType.Id to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4.Id).value!! }),
    FhirR4PrimitiveType.Uuid to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4.Uuid).value!! }),
    FhirR4PrimitiveType.Markdown to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4.Markdown).value!! }),
    FhirR4PrimitiveType.Base64Binary to
      (FhirPathSystemType.STRING to
        { it ->
          (it as com.google.fhir.model.r4.Base64Binary).value!!
        }),
    FhirR4PrimitiveType.Integer to
      (FhirPathSystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.Integer).value!! }),
    FhirR4PrimitiveType.UnsignedInt to
      (FhirPathSystemType.INTEGER to
        { it ->
          (it as com.google.fhir.model.r4.UnsignedInt).value!!
        }),
    FhirR4PrimitiveType.PositiveInt to
      (FhirPathSystemType.INTEGER to
        { it ->
          (it as com.google.fhir.model.r4.PositiveInt).value!!
        }),
    FhirR4PrimitiveType.Decimal to
      (FhirPathSystemType.DECIMAL to { it -> (it as com.google.fhir.model.r4.Decimal).value!! }),
    FhirR4PrimitiveType.Date to
      (FhirPathSystemType.DATE to
        { it ->
          FhirPathDate.fromFhirR4Date((it as com.google.fhir.model.r4.Date).value!!)
        }),
    FhirR4PrimitiveType.DateTime to
      (FhirPathSystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirR4DateTime((it as com.google.fhir.model.r4.DateTime).value!!)
        }),
    FhirR4PrimitiveType.Time to
      (FhirPathSystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as com.google.fhir.model.r4.Time).value!!)
        }),

    // FHIR R4 complex types
    FhirR4ComplexType.Quantity to
      (FhirPathSystemType.QUANTITY to
        {
          (it as com.google.fhir.model.r4.Quantity).let {
            val pair = (it.value!!.value!! to it.code!!.value!!)
            FhirPathQuantity(value = pair.first, unit = pair.second)
          }
        }),
  )

/**
 * Maps a FHIR R4B type to a FHIRPath system type it can be implicitly converted to and a function
 * that does the conversion.
 *
 * See [specification](https://build.fhir.org/fhirpath.html#types).
 */
internal val fhirR4BTypeToFhirPathType =
  mapOf<FhirType, Pair<FhirPathSystemType, (element: Any) -> Any>>(
    // FHIR R4B primitive types
    FhirR4BPrimitiveType.Boolean to
      (FhirPathSystemType.BOOLEAN to { it -> (it as com.google.fhir.model.r4b.Boolean).value!! }),
    FhirR4BPrimitiveType.String to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4b.String).value!! }),
    FhirR4BPrimitiveType.Uri to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4b.Uri).value!! }),
    FhirR4BPrimitiveType.Code to
      (FhirPathSystemType.STRING to
        { it ->
          // A code in the Kotlin FHIR library is represented either as an Enumeration if it is
          // bound to a value set, or a Code if it is not.
          when (it) {
            is com.google.fhir.model.r4b.Enumeration<*> -> it.value.toString()
            is com.google.fhir.model.r4b.Code -> it.value!!
            else -> error("Unknown code type")
          }
        }),
    FhirR4BPrimitiveType.Oid to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4b.Oid).value!! }),
    FhirR4BPrimitiveType.Id to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4b.Id).value!! }),
    FhirR4BPrimitiveType.Uuid to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4b.Uuid).value!! }),
    FhirR4BPrimitiveType.Markdown to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r4b.Markdown).value!! }),
    FhirR4BPrimitiveType.Base64Binary to
      (FhirPathSystemType.STRING to
        { it ->
          (it as com.google.fhir.model.r4b.Base64Binary).value!!
        }),
    FhirR4BPrimitiveType.Integer to
      (FhirPathSystemType.INTEGER to { it -> (it as com.google.fhir.model.r4b.Integer).value!! }),
    FhirR4BPrimitiveType.UnsignedInt to
      (FhirPathSystemType.INTEGER to
        { it ->
          (it as com.google.fhir.model.r4b.UnsignedInt).value!!
        }),
    FhirR4BPrimitiveType.PositiveInt to
      (FhirPathSystemType.INTEGER to
        { it ->
          (it as com.google.fhir.model.r4b.PositiveInt).value!!
        }),
    FhirR4BPrimitiveType.Decimal to
      (FhirPathSystemType.DECIMAL to { it -> (it as com.google.fhir.model.r4b.Decimal).value!! }),
    FhirR4BPrimitiveType.Date to
      (FhirPathSystemType.DATE to
        { it ->
          FhirPathDate.fromFhirR4BDate((it as com.google.fhir.model.r4b.Date).value!!)
        }),
    FhirR4BPrimitiveType.DateTime to
      (FhirPathSystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirR4BDateTime((it as com.google.fhir.model.r4b.DateTime).value!!)
        }),
    FhirR4BPrimitiveType.Time to
      (FhirPathSystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as com.google.fhir.model.r4b.Time).value!!)
        }),

    // FHIR R4B complex types
    FhirR4BComplexType.Quantity to
      (FhirPathSystemType.QUANTITY to
        {
          (it as com.google.fhir.model.r4b.Quantity).let {
            val pair = (it.value!!.value!! to it.code!!.value!!)
            FhirPathQuantity(value = pair.first, unit = pair.second)
          }
        }),
  )

/**
 * Maps a FHIR R5 type to a FHIRPath system type it can be implicitly converted to and a function
 * that does the conversion.
 *
 * See [specification](https://build.fhir.org/fhirpath.html#types).
 */
internal val fhirR5TypeToFhirPathType =
  mapOf<FhirType, Pair<FhirPathSystemType, (element: Any) -> Any>>(
    // FHIR R5 primitive types
    FhirR5PrimitiveType.Boolean to
      (FhirPathSystemType.BOOLEAN to { it -> (it as com.google.fhir.model.r5.Boolean).value!! }),
    FhirR5PrimitiveType.String to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r5.String).value!! }),
    FhirR5PrimitiveType.Uri to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r5.Uri).value!! }),
    FhirR5PrimitiveType.Code to
      (FhirPathSystemType.STRING to
        { it ->
          // A code in the Kotlin FHIR library is represented either as an Enumeration if it is
          // bound to a value set, or a Code if it is not.
          when (it) {
            is com.google.fhir.model.r5.Enumeration<*> -> it.value.toString()
            is com.google.fhir.model.r5.Code -> it.value!!
            else -> error("Unknown code type")
          }
        }),
    FhirR5PrimitiveType.Oid to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r5.Oid).value!! }),
    FhirR5PrimitiveType.Id to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r5.Id).value!! }),
    FhirR5PrimitiveType.Uuid to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r5.Uuid).value!! }),
    FhirR5PrimitiveType.Markdown to
      (FhirPathSystemType.STRING to { it -> (it as com.google.fhir.model.r5.Markdown).value!! }),
    FhirR5PrimitiveType.Base64Binary to
      (FhirPathSystemType.STRING to
        { it ->
          (it as com.google.fhir.model.r5.Base64Binary).value!!
        }),
    FhirR5PrimitiveType.Integer to
      (FhirPathSystemType.INTEGER to { it -> (it as com.google.fhir.model.r5.Integer).value!! }),
    FhirR5PrimitiveType.UnsignedInt to
      (FhirPathSystemType.INTEGER to
        { it ->
          (it as com.google.fhir.model.r5.UnsignedInt).value!!
        }),
    FhirR5PrimitiveType.PositiveInt to
      (FhirPathSystemType.INTEGER to
        { it ->
          (it as com.google.fhir.model.r5.PositiveInt).value!!
        }),
    FhirR5PrimitiveType.Decimal to
      (FhirPathSystemType.DECIMAL to { it -> (it as com.google.fhir.model.r5.Decimal).value!! }),
    FhirR5PrimitiveType.Date to
      (FhirPathSystemType.DATE to
        { it ->
          FhirPathDate.fromFhirR5Date((it as com.google.fhir.model.r5.Date).value!!)
        }),
    FhirR5PrimitiveType.DateTime to
      (FhirPathSystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirR5DateTime((it as com.google.fhir.model.r5.DateTime).value!!)
        }),
    FhirR5PrimitiveType.Time to
      (FhirPathSystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as com.google.fhir.model.r5.Time).value!!)
        }),

    // FHIR R5 complex types
    FhirR5ComplexType.Quantity to
      (FhirPathSystemType.QUANTITY to
        {
          (it as com.google.fhir.model.r5.Quantity).let {
            val pair = (it.value!!.value!! to it.code!!.value!!)
            FhirPathQuantity(value = pair.first, unit = pair.second)
          }
        }),
  )

internal val fhirTypeToFhirPathType =
  fhirR4TypeToFhirPathType + fhirR4BTypeToFhirPathType + fhirR5TypeToFhirPathType

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
        (it as Int).toBigDecimal()
      },
    FhirPathSystemType.INTEGER to
      FhirPathSystemType.QUANTITY to
      { it ->
        FhirPathQuantity(value = it.toString().toBigDecimal(), unit = DEFAULT_UNIT)
      },
    FhirPathSystemType.LONG to FhirPathSystemType.DECIMAL to { it -> (it as Long).toBigDecimal() },
    FhirPathSystemType.DECIMAL to
      FhirPathSystemType.QUANTITY to
      { it ->
        FhirPathQuantity(value = it as BigDecimal, unit = DEFAULT_UNIT)
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
 * For example, an object of type `com.google.fhir.model.r4.String` will be converted to a
 * Kotlin.String (the internal representation of FHIRPath system type System.String).
 */
internal fun Any.toFhirPathType(fhirPathTypeResolver: FhirPathTypeResolver): Any {
  // TODO: convert types such as FhirDate to FhirPathDate
  fhirPathTypeResolver.resolveFromObject(this)?.let { fhirType ->
    fhirTypeToFhirPathType[fhirType]?.let { (_, transform) ->
      return@toFhirPathType transform(this)
    }
  }
  return this
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
 * For example, a pair of objects of type Fhir.integer and System.Decimal will be converted to two
 * objects of type System.Decimal; a pair of objects of type Fhir.date and System.Decimal will be
 * converted to two objects of type System.Date and System.Decimal.
 */
internal fun Pair<Any, Any>.asComparableOperands(
  fhirPathTypeResolver: FhirPathTypeResolver
): Pair<Any, Any> {
  return (first.toFhirPathType(fhirPathTypeResolver) to second.toFhirPathType(fhirPathTypeResolver))
    .toCommonFhirPathType()
}
