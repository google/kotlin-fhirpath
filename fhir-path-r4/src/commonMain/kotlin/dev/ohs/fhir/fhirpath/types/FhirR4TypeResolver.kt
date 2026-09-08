/*
 * Copyright 2026 Open Health Stack Foundation
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

package dev.ohs.fhir.fhirpath.types

import dev.ohs.fhir.model.r4.Enumeration
import dev.ohs.fhir.model.r4.Resource
import dev.ohs.fhir.model.r4.ext.getFhirType
import dev.ohs.fhir.model.r4.terminologies.ResourceType

fun FhirPathDate.Companion.fromFhirR4Date(fhirDate: dev.ohs.fhir.model.r4.FhirDate): FhirPathDate {
  return fromString(fhirDate.toString())
}

fun FhirPathDateTime.Companion.fromFhirR4DateTime(
  fhirDateTime: dev.ohs.fhir.model.r4.FhirDateTime
): FhirPathDateTime {
  return fromString(fhirDateTime.toString())
}

fun FhirPathDecimal.Companion.fromFhirR4Decimal(
  fhirDecimal: dev.ohs.fhir.model.r4.FhirDecimal
): FhirPathDecimal {
  return fromString(fhirDecimal.toString())
}

private val fhirR4TypeToFhirPathType =
  mapOf<FhirType, Pair<FhirPathSystemType, (element: Any) -> Any>>(
    // FHIR R4 primitive types
    FhirR4PrimitiveType.Boolean to
      (FhirPathSystemType.BOOLEAN to { it -> (it as dev.ohs.fhir.model.r4.Boolean).value!! }),
    FhirR4PrimitiveType.String to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.String).value!! }),
    FhirR4PrimitiveType.Uri to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.Uri).value!! }),
    FhirR4PrimitiveType.Code to
      (FhirPathSystemType.STRING to
        { it ->
          when (it) {
            is Enumeration<*> -> it.value.toString()
            is dev.ohs.fhir.model.r4.Code -> it.value!!
            else -> error("Unknown code type")
          }
        }),
    FhirR4PrimitiveType.Oid to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.Oid).value!! }),
    FhirR4PrimitiveType.Id to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.Id).value!! }),
    FhirR4PrimitiveType.Uuid to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.Uuid).value!! }),
    FhirR4PrimitiveType.Markdown to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.Markdown).value!! }),
    FhirR4PrimitiveType.Base64Binary to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4.Base64Binary).value!! }),
    FhirR4PrimitiveType.Integer to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r4.Integer).value!! }),
    FhirR4PrimitiveType.UnsignedInt to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r4.UnsignedInt).value!! }),
    FhirR4PrimitiveType.PositiveInt to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r4.PositiveInt).value!! }),
    FhirR4PrimitiveType.Decimal to
      (FhirPathSystemType.DECIMAL to
        { it ->
          FhirPathDecimal.fromFhirR4Decimal((it as dev.ohs.fhir.model.r4.Decimal).value!!)
        }),
    FhirR4PrimitiveType.Date to
      (FhirPathSystemType.DATE to
        { it ->
          FhirPathDate.fromFhirR4Date((it as dev.ohs.fhir.model.r4.Date).value!!)
        }),
    FhirR4PrimitiveType.DateTime to
      (FhirPathSystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirR4DateTime((it as dev.ohs.fhir.model.r4.DateTime).value!!)
        }),
    FhirR4PrimitiveType.Time to
      (FhirPathSystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as dev.ohs.fhir.model.r4.Time).value!!)
        }),

    // FHIR R4 complex types
    FhirR4ComplexType.Quantity to
      (FhirPathSystemType.QUANTITY to
        {
          (it as dev.ohs.fhir.model.r4.Quantity).let {
            val pair = (FhirPathDecimal.fromFhirR4Decimal(it.value!!.value!!) to it.code!!.value!!)
            FhirPathQuantity(value = pair.first, unit = pair.second)
          }
        }),
  )

object FhirR4TypeResolver : FhirPathTypeResolver() {
  override fun resolveFhirTypeFromString(name: String): FhirR4Type {
    FhirR4PrimitiveType.Companion.fromString(name)?.let {
      return it
    }
    FhirR4ComplexType.Companion.fromString(name)?.let {
      return it
    }
    return FhirR4ResourceType(ResourceType.fromCode(name))
  }

  override fun resolveFhirTypeFromObject(value: Any): FhirR4Type? {
    FhirR4PrimitiveType.fromObject(value)?.let {
      return it
    }
    FhirR4ComplexType.fromObject(value)?.let {
      return it
    }
    (value as? Resource)?.getFhirType()?.let {
      return it
    }
    return null
  }

  override fun convertToString(value: Any): String? =
    when (value) {
      is dev.ohs.fhir.model.r4.String -> value.value
      is Enumeration<*> -> value.toString()
      else -> null
    }

  override fun toFhirPathType(value: Any): Any {
    resolveFhirTypeFromObject(value)?.let { fhirType ->
      fhirR4TypeToFhirPathType[fhirType]?.let { (_, transform) ->
        return transform(value)
      }
    }
    return value
  }
}
