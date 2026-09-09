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

import dev.ohs.fhir.model.r4b.Enumeration
import dev.ohs.fhir.model.r4b.Resource
import dev.ohs.fhir.model.r4b.ext.getFhirType
import dev.ohs.fhir.model.r4b.terminologies.ResourceType

fun FhirPathDate.Companion.fromFhirR4BDate(
  fhirDate: dev.ohs.fhir.model.r4b.FhirDate
): FhirPathDate {
  return fromString(fhirDate.toString())
}

fun FhirPathDateTime.Companion.fromFhirR4BDateTime(
  fhirDateTime: dev.ohs.fhir.model.r4b.FhirDateTime
): FhirPathDateTime {
  return fromString(fhirDateTime.toString())
}

fun FhirPathDecimal.Companion.fromFhirR4BDecimal(
  fhirDecimal: dev.ohs.fhir.model.r4b.FhirDecimal
): FhirPathDecimal {
  return fromString(fhirDecimal.toString())
}

private val fhirR4BTypeToFhirPathType =
  mapOf<FhirType, Pair<FhirPathSystemType, (element: Any) -> Any?>>(
    // FHIR R4B primitive types
    FhirR4BPrimitiveType.Boolean to
      (FhirPathSystemType.BOOLEAN to { it -> (it as dev.ohs.fhir.model.r4b.Boolean).value }),
    FhirR4BPrimitiveType.String to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.String).value }),
    FhirR4BPrimitiveType.Uri to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.Uri).value }),
    FhirR4BPrimitiveType.Code to
      (FhirPathSystemType.STRING to
        { it ->
          when (it) {
            is Enumeration<*> -> it.value?.toString()
            is dev.ohs.fhir.model.r4b.Code -> it.value
            else -> error("Unknown code type")
          }
        }),
    FhirR4BPrimitiveType.Oid to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.Oid).value }),
    FhirR4BPrimitiveType.Id to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.Id).value }),
    FhirR4BPrimitiveType.Uuid to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.Uuid).value }),
    FhirR4BPrimitiveType.Markdown to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.Markdown).value }),
    FhirR4BPrimitiveType.Base64Binary to
      (FhirPathSystemType.STRING to { it -> (it as dev.ohs.fhir.model.r4b.Base64Binary).value }),
    FhirR4BPrimitiveType.Integer to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r4b.Integer).value }),
    FhirR4BPrimitiveType.UnsignedInt to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r4b.UnsignedInt).value }),
    FhirR4BPrimitiveType.PositiveInt to
      (FhirPathSystemType.INTEGER to { it -> (it as dev.ohs.fhir.model.r4b.PositiveInt).value }),
    FhirR4BPrimitiveType.Decimal to
      (FhirPathSystemType.DECIMAL to
        { it ->
          (it as dev.ohs.fhir.model.r4b.Decimal).value?.let {
            FhirPathDecimal.fromFhirR4BDecimal(it)
          }
        }),
    FhirR4BPrimitiveType.Date to
      (FhirPathSystemType.DATE to
        { it ->
          (it as dev.ohs.fhir.model.r4b.Date).value?.let { FhirPathDate.fromFhirR4BDate(it) }
        }),
    FhirR4BPrimitiveType.DateTime to
      (FhirPathSystemType.DATETIME to
        { it ->
          (it as dev.ohs.fhir.model.r4b.DateTime).value?.let {
            FhirPathDateTime.fromFhirR4BDateTime(it)
          }
        }),
    FhirR4BPrimitiveType.Time to
      (FhirPathSystemType.TIME to
        { it ->
          (it as dev.ohs.fhir.model.r4b.Time).value?.let { FhirPathTime.fromLocalTime(it) }
        }),

    // FHIR R4B complex types
    FhirR4BComplexType.Quantity to
      (FhirPathSystemType.QUANTITY to
        {
          (it as dev.ohs.fhir.model.r4b.Quantity).let {
            val value =
              it.value?.value?.let { FhirPathDecimal.fromFhirR4BDecimal(it) } ?: return@let null
            val unit = it.code?.value ?: it.unit?.value ?: return@let null
            FhirPathQuantity(value = value, unit = unit)
          }
        }),
  )

object FhirR4BTypeResolver : FhirPathTypeResolver() {
  override fun resolveFhirTypeFromString(name: String): FhirR4BType {
    FhirR4BPrimitiveType.Companion.fromString(name)?.let {
      return it
    }
    FhirR4BComplexType.Companion.fromString(name)?.let {
      return it
    }
    return FhirR4BResourceType(ResourceType.fromCode(name))
  }

  override fun resolveFhirTypeFromObject(value: Any): FhirR4BType? {
    FhirR4BPrimitiveType.fromObject(value)?.let {
      return it
    }
    FhirR4BComplexType.fromObject(value)?.let {
      return it
    }
    (value as? Resource)?.getFhirType()?.let {
      return it
    }
    return null
  }

  override fun toFhirPathType(value: Any): Any {
    resolveFhirTypeFromObject(value)?.let { fhirType ->
      fhirR4BTypeToFhirPathType[fhirType]?.let { (_, transform) ->
        return transform(value) ?: value
      }
    }
    return value
  }
}
