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

import dev.ohs.fhir.model.r5.Enumeration
import dev.ohs.fhir.model.r5.Resource
import dev.ohs.fhir.model.r5.ext.getFhirType
import dev.ohs.fhir.model.r5.terminologies.ResourceType

fun FhirPathDate.Companion.fromFhirR5Date(fhirDate: dev.ohs.fhir.model.r5.FhirDate): FhirPathDate {
  return fromString(fhirDate.toString())
}

fun FhirPathDateTime.Companion.fromFhirR5DateTime(
  fhirDateTime: dev.ohs.fhir.model.r5.FhirDateTime
): FhirPathDateTime {
  return fromString(fhirDateTime.toString())
}

fun FhirPathDecimal.Companion.fromFhirR5Decimal(
  fhirDecimal: dev.ohs.fhir.model.r5.FhirDecimal
): FhirPathDecimal {
  return fromString(fhirDecimal.toString())
}

private val fhirR5TypeToFhirPathType =
  mapOf<FhirType, Pair<FhirPathSystemType, (element: Any) -> Any>>(
    // FHIR R5 primitive types
    FhirR5PrimitiveType.Boolean to
      (FhirPathSystemType.BOOLEAN to { it -> (it as dev.ohs.fhir.model.r5.Boolean).value!! }),
    FhirR5PrimitiveType.String to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.String).value!! }),
    FhirR5PrimitiveType.Uri to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.Uri).value!! }),
    FhirR5PrimitiveType.Code to
      (FhirPathSystemType.STRING to
        { it ->
          when (it) {
            is Enumeration<*> -> it.value.toString()
            is dev.ohs.fhir.model.r5.Code -> it.value!!
            else -> error("Unknown code type")
          }
        }),
    FhirR5PrimitiveType.Oid to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.Oid).value!! }),
    FhirR5PrimitiveType.Id to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.Id).value!! }),
    FhirR5PrimitiveType.Uuid to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.Uuid).value!! }),
    FhirR5PrimitiveType.Markdown to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.Markdown).value!! }),
    FhirR5PrimitiveType.Base64Binary to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r5.Base64Binary).value!! }),
    FhirR5PrimitiveType.Integer to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r5.Integer).value!! }),
    FhirR5PrimitiveType.UnsignedInt to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r5.UnsignedInt).value!! }),
    FhirR5PrimitiveType.PositiveInt to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r5.PositiveInt).value!! }),
    FhirR5PrimitiveType.Decimal to
      (FhirPathSystemType.DECIMAL to
        { it ->
          FhirPathDecimal.fromFhirR5Decimal((it as dev.ohs.fhir.model.r5.Decimal).value!!)
        }),
    FhirR5PrimitiveType.Date to
      (FhirPathSystemType.DATE to
        { it ->
          FhirPathDate.fromFhirR5Date((it as dev.ohs.fhir.model.r5.Date).value!!)
        }),
    FhirR5PrimitiveType.DateTime to
      (FhirPathSystemType.DATETIME to
        { it ->
          FhirPathDateTime.fromFhirR5DateTime((it as dev.ohs.fhir.model.r5.DateTime).value!!)
        }),
    FhirR5PrimitiveType.Time to
      (FhirPathSystemType.TIME to
        { it ->
          FhirPathTime.fromLocalTime((it as dev.ohs.fhir.model.r5.Time).value!!)
        }),

    // FHIR R5 complex types
    FhirR5ComplexType.Quantity to
      (FhirPathSystemType.QUANTITY to
        {
          (it as dev.ohs.fhir.model.r5.Quantity).let {
            val pair = (FhirPathDecimal.fromFhirR5Decimal(it.value!!.value!!) to it.code!!.value!!)
            FhirPathQuantity(value = pair.first, unit = pair.second)
          }
        }),
  )

object FhirR5TypeResolver : FhirPathTypeResolver() {
  override fun resolveFhirTypeFromString(name: String): FhirR5Type {
    FhirR5PrimitiveType.Companion.fromString(name)?.let {
      return it
    }
    FhirR5ComplexType.Companion.fromString(name)?.let {
      return it
    }
    return FhirR5ResourceType(ResourceType.fromCode(name))
  }

  override fun resolveFhirTypeFromObject(value: Any): FhirR5Type? {
    FhirR5PrimitiveType.fromObject(value)?.let {
      return it
    }
    FhirR5ComplexType.fromObject(value)?.let {
      return it
    }
    (value as? Resource)?.getFhirType()?.let {
      return it
    }
    return null
  }

  override fun convertToString(value: Any): String? =
    when (value) {
      is dev.ohs.fhir.model.r5.String -> value.value
      is Enumeration<*> -> value.toString()
      else -> null
    }

  override fun toFhirPathType(value: Any): Any {
    resolveFhirTypeFromObject(value)?.let { fhirType ->
      fhirR5TypeToFhirPathType[fhirType]?.let { (_, transform) ->
        return transform(value)
      }
    }
    return value
  }
}
