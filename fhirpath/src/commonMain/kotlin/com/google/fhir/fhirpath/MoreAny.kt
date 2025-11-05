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

package com.google.fhir.fhirpath

import com.google.fhir.fhirpath.ext.getProperty
import com.google.fhir.fhirpath.ext.getPropertyInChoiceValue
import com.google.fhir.fhirpath.ext.unwrapChoiceValue
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.BackboneElement
import com.google.fhir.model.r4.Decimal
import com.google.fhir.model.r4.Element
import com.google.fhir.model.r4.FhirDate
import com.google.fhir.model.r4.Quantity
import com.google.fhir.model.r4.Resource
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

/** See [specification](https://build.fhir.org/fhirpath.html#types). */
val fhirTypeToFhirPathType =
  mapOf<FhirType, Pair<SystemType, (element: Element) -> Any>>(
    // FHIR primitive types
    FhirPrimitiveType.Boolean to
      (SystemType.BOOLEAN to { it -> (it as com.google.fhir.model.r4.Boolean).value!! }),
    FhirPrimitiveType.String to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.String).value!! }),
    FhirPrimitiveType.Uri to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Uri).value!! }),
    FhirPrimitiveType.Code to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Code).value!! }),
    FhirPrimitiveType.Oid to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Oid).value!! }),
    FhirPrimitiveType.Id to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Id).value!! }),
    FhirPrimitiveType.Uuid to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Uuid).value!! }),
    FhirPrimitiveType.Markdown to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Markdown).value!! }),
    FhirPrimitiveType.Base64Binary to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Base64Binary).value!! }),
    FhirPrimitiveType.Integer to
      (SystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.Integer).value!! }),
    FhirPrimitiveType.UnsignedInt to
      (SystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.UnsignedInt).value!! }),
    FhirPrimitiveType.PositiveInt to
      (SystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.PositiveInt).value!! }),
    FhirPrimitiveType.Decimal to
      (SystemType.DECIMAL to { it -> (it as com.google.fhir.model.r4.Decimal).value!! }),
    FhirPrimitiveType.Date to
      (SystemType.DATE to { it -> (it as com.google.fhir.model.r4.Date).value!! }),
    FhirPrimitiveType.DateTime to
      (SystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirDateTime((it as com.google.fhir.model.r4.DateTime).value!!)
        }),
    FhirPrimitiveType.Time to
      (SystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as com.google.fhir.model.r4.Time).value!!)
        }),

    // FHIR complex types
    FhirComplexType.Quantity to (SystemType.QUANTITY to { it }),
  )

/** See [specification](https://hl7.org/fhirpath/#conversion). */
val fhirPathTypeToFhirPathType =
  mapOf<Pair<SystemType, SystemType>, (any: Any) -> Any>(
    SystemType.INTEGER to SystemType.LONG to { it -> (it as Int).toLong() },
    SystemType.INTEGER to SystemType.DECIMAL to { it -> (it as Int).toDouble() },
    SystemType.INTEGER to
      SystemType.QUANTITY to
      { it ->
        Quantity(
          value = Decimal(value = it.toString().toBigDecimal()),
          unit = com.google.fhir.model.r4.String(value = "1"),
        )
      },
    SystemType.LONG to SystemType.DECIMAL to { it -> (it as Long).toDouble() },
    SystemType.DECIMAL to
      SystemType.QUANTITY to
      { it ->
        Quantity(
          value = Decimal(value = it.toString().toBigDecimal()),
          unit = com.google.fhir.model.r4.String(value = "1"),
        )
      },
    SystemType.DATE to
      SystemType.DATETIME to
      { it ->
        val date = it as FhirDate
        when (date) {
          is FhirDate.Year -> FhirPathDateTime(year = date.value)
          is FhirDate.YearMonth ->
            FhirPathDateTime(year = date.value.year, month = date.value.month.number)
          is FhirDate.Date ->
            FhirPathDateTime(
              year = date.date.year,
              month = date.date.monthNumber,
              day = date.date.dayOfMonth,
            )
        }
      },
  )

internal fun Any.accessMember(fieldName: String): Any? {
  val element =
    when (this) {
      is Resource -> {
        this.getProperty(fieldName)
      }
      is BackboneElement -> {
        this.getProperty(fieldName)
      }
      is Element -> {
        this.getProperty(fieldName)
      }
      // TODO: get value from FHIR primitive types

      // Sealed interface
      else -> this.getPropertyInChoiceValue(fieldName)
    }

  // Always unwrap choice type values. For example, the expression `Patient.multipleBirth` will
  // be of type `Boolean` or `Integer` rather than `Patient.MultipleBirth.Boolean` or
  // `Patient.MultipleBirth.Integer`.
  return element?.unwrapChoiceValue() ?: element
}

/**
 * Converts the object to its equivalent FHIRPath system type if one exists, or returns the object
 * itself, otherwise.
 *
 * For example, an object of type `com.google.fhir.model.r4.String` will be converted to a
 * Kotlin.String (the internal representation of FHIRPath system type System.String).
 */
internal fun Any.toFhirPathType(): Any {
  FhirType.fromObject(this)?.let { fhirType ->
    fhirTypeToFhirPathType[fhirType]?.let { (_, transform) ->
      val result = transform(this as Element)
      return result
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
  val firstType = SystemType.fromObject(first) ?: return this
  val secondType = SystemType.fromObject(second) ?: return this

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
internal fun Pair<Any, Any>.asComparableOperands(): Pair<Any, Any> {
  return (first.toFhirPathType() to second.toFhirPathType()).toCommonFhirPathType()
}
