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
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.BackboneElement
import com.google.fhir.model.r4.Element
import com.google.fhir.model.r4.Resource
import com.google.fhir.model.r4.ext.getProperty
import com.google.fhir.model.r4.ext.getPropertyInChoiceValue
import com.google.fhir.model.r4.ext.hasProperty
import com.google.fhir.model.r4.ext.hasPropertyInChoiceValue
import com.google.fhir.model.r4.ext.unwrapChoiceValue
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

/**
 * Set to true for strict mode (throws on invalid property access). Set to false for lenient mode
 * (returns empty for invalid properties).
 */
private const val STRICT_MODE = false

/**
 * Maps a FHIR type to a FHIRPath system type it can be implicitly converted to and a function that
 * does the conversion.
 *
 * See [specification](https://build.fhir.org/fhirpath.html#types).
 */
val fhirTypeToFhirPathType =
  mapOf<FhirType, Pair<SystemType, (element: Element) -> Any>>(
    // FHIR primitive types
    FhirR4PrimitiveType.Boolean to
      (SystemType.BOOLEAN to { it -> (it as com.google.fhir.model.r4.Boolean).value!! }),
    FhirR4PrimitiveType.String to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.String).value!! }),
    FhirR4PrimitiveType.Uri to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Uri).value!! }),
    FhirR4PrimitiveType.Code to
      (SystemType.STRING to
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
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Oid).value!! }),
    FhirR4PrimitiveType.Id to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Id).value!! }),
    FhirR4PrimitiveType.Uuid to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Uuid).value!! }),
    FhirR4PrimitiveType.Markdown to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Markdown).value!! }),
    FhirR4PrimitiveType.Base64Binary to
      (SystemType.STRING to { it -> (it as com.google.fhir.model.r4.Base64Binary).value!! }),
    FhirR4PrimitiveType.Integer to
      (SystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.Integer).value!! }),
    FhirR4PrimitiveType.UnsignedInt to
      (SystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.UnsignedInt).value!! }),
    FhirR4PrimitiveType.PositiveInt to
      (SystemType.INTEGER to { it -> (it as com.google.fhir.model.r4.PositiveInt).value!! }),
    FhirR4PrimitiveType.Decimal to
      (SystemType.DECIMAL to { it -> (it as com.google.fhir.model.r4.Decimal).value!! }),
    FhirR4PrimitiveType.Date to
      (SystemType.DATE to
        { it ->
          FhirPathDate.fromFhirDate((it as com.google.fhir.model.r4.Date).value!!)
        }),
    FhirR4PrimitiveType.DateTime to
      (SystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirDateTime((it as com.google.fhir.model.r4.DateTime).value!!)
        }),
    FhirR4PrimitiveType.Time to
      (SystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as com.google.fhir.model.r4.Time).value!!)
        }),

    // FHIR complex types
    FhirR4ComplexType.Quantity to
      (SystemType.QUANTITY to
        {
          (it as com.google.fhir.model.r4.Quantity).let {
            val pair = (it.value!!.value!! to it.code!!.value!!)
            FhirPathQuantity(value = pair.first, unit = pair.second)
          }
        }),
  )

/**
 * Maps a pair of FHIRPath types where the former can be implicitly converted to the latter to a
 * function that does the conversion.
 *
 * See [specification](https://hl7.org/fhirpath/#conversion).
 */
val fhirPathTypeToFhirPathType =
  mapOf<Pair<SystemType, SystemType>, (any: Any) -> Any>(
    SystemType.INTEGER to SystemType.LONG to { it -> (it as Int).toLong() },
    SystemType.INTEGER to SystemType.DECIMAL to { it -> (it as Int).toBigDecimal() },
    SystemType.INTEGER to
      SystemType.QUANTITY to
      { it ->
        FhirPathQuantity(value = it.toString().toBigDecimal(), unit = DEFAULT_UNIT)
      },
    SystemType.LONG to SystemType.DECIMAL to { it -> (it as Long).toBigDecimal() },
    SystemType.DECIMAL to
      SystemType.QUANTITY to
      { it ->
        FhirPathQuantity(value = it as BigDecimal, unit = DEFAULT_UNIT)
      },
    SystemType.DATE to
      SystemType.DATETIME to
      { it ->
        val date = it as FhirPathDate
        FhirPathDateTime(year = date.year, month = date.month, day = date.day)
      },
  )

internal fun Any.accessMember(fieldName: String): Any? {
  // Allows graceful handling of invalid property access (returns null instead of throwing).
  if (!STRICT_MODE) {
    val hasProperty =
      when (this) {
        is Resource -> this.hasProperty(fieldName)
        is BackboneElement -> this.hasProperty(fieldName)
        is Element -> this.hasProperty(fieldName)
        else -> this.hasPropertyInChoiceValue(fieldName)
      }
    if (!hasProperty) return null
  }

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
      // TODO: get value from FHIR primitive types (e.g. extension value)

      // Sealed interface
      else -> this.getPropertyInChoiceValue(fieldName)
    }

  // Always unwrap choice type values. For example, the expression `Patient.multipleBirth` will
  // be of type `Boolean` or `Integer` rather than `Patient.MultipleBirth.Boolean` or
  // `Patient.MultipleBirth.Integer`.
  // FHIR types are not converted to FHIRPath types at this point since information such as id and
  // extension as well as type information need to be preserved. The implicit conversion only
  // happens at the last minute when necessary (e.g. when FHIR type is compared to FHIRPath type).
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
  // TODO: convert types such as FhirDate to FhirPathDate
  FhirType.fromObject(this)?.let { fhirType ->
    fhirTypeToFhirPathType[fhirType]?.let { (_, transform) ->
      return@toFhirPathType transform(this as Element)
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
