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

package com.google.fhir.fhirpath.model

/**
 * Set to true for strict mode (throws on invalid property access). Set to false for lenient mode
 * (returns empty for invalid properties).
 */
internal const val STRICT_MODE = false

internal sealed class FhirModelNavigator {
  fun accessProperty(obj: Any, fieldName: String): Any? {
    if (!STRICT_MODE) {
      // Allow graceful handling of invalid property access (returns null instead of throwing)
      if (!hasProperty(obj, fieldName)) return null
    }

    val element = getProperty(obj, fieldName)

    // Always unwrap choice type values. For example, the expression `Patient.multipleBirth` will
    // be of type `Boolean` or `Integer` rather than `Patient.MultipleBirth.Boolean` or
    // `Patient.MultipleBirth.Integer`.
    // FHIR types are not converted to FHIRPath types at this point since information such as id and
    // extension as well as type information need to be preserved. The implicit conversion only
    // happens at the last minute when necessary (e.g. when FHIR type is compared to FHIRPath type).
    return element?.let { unwrapProperty(element) }
  }

  abstract fun getAllChildren(any: Any): Collection<Any>

  abstract fun canHaveChildren(obj: Any): Boolean

  protected abstract fun hasProperty(obj: Any, fieldName: String): Boolean

  protected abstract fun getProperty(obj: Any, fieldName: String): Any?

  protected abstract fun unwrapProperty(any: Any): Any?
}
