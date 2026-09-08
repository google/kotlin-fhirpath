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

abstract class FhirPathTypeResolver {
  fun resolveFromString(string: String): FhirPathType {
    // Type names may be escaped with backticks, e.g. `FHIR.`Patient``.
    val parts = string.split('.').map { it.removeSurrounding("`") }
    val name = parts.last()

    if (parts.size > 1) {
      val (namespace, name) = parts
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
    // specified in https://hl7.org/fhirpath/STU3/en/#models.
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

  /** Converts the value to its equivalent FHIRPath system type if one exists. */
  abstract fun toFhirPathType(value: Any): Any
}
