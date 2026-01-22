/*
 * Copyright 2026 Google LLC
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

package com.google.fhir.fhirpath.types

import com.google.fhir.model.r5.Enumeration
import com.google.fhir.model.r5.Resource
import com.google.fhir.model.r5.ext.getFhirType
import com.google.fhir.model.r5.terminologies.ResourceType

internal object FhirR5TypeResolver : FhirPathTypeResolver() {
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
      is com.google.fhir.model.r5.String -> value.value
      is Enumeration<*> -> value.toString()
      else -> null
    }
}
