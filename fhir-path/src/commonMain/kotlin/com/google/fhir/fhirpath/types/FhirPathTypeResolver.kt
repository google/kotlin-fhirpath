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

internal sealed class FhirPathTypeResolver {
  fun resolveFromString(string: String): FhirPathType {
    val parts = string.split('.')
    val name = parts.last()

    if (string.contains('.')) {
      val (namespace, name) = string.split('.')
      return when (namespace) {
        "FHIR" -> {
          resolveFhirTypeFromString(name)
        }
        "System" -> {
          requireNotNull(FhirPathSystemType.fromString(name)) { "Unknown System type $name" }
        }
        else -> error("Unknown type namespace: $namespace")
      }
    }

    // Unqualified type names are resolved as FHIR types first and then FHIRPath system types, as
    // specified in https://hl7.org/fhirpath/#models.
    try {
      return resolveFhirTypeFromString(name)
    } catch (_: Exception) {}
    FhirPathSystemType.fromString(name)?.let {
      return it
    }
    error("Unknown type $string")
  }

  fun resolveFromObject(value: Any): FhirPathType? {
    FhirPathSystemType.fromObject(value)?.let {
      return it
    }
    resolveFhirTypeFromObject(value)?.let {
      return it
    }
    return null
  }

  protected abstract fun resolveFhirTypeFromString(name: String): FhirType

  protected abstract fun resolveFhirTypeFromObject(value: Any): FhirType?

  /** Converts the value to a string if possible, otherwise returns null. */
  abstract fun convertToString(value: Any): String?
}
