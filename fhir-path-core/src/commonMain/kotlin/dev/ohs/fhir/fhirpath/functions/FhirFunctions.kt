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

package dev.ohs.fhir.fhirpath.functions

import dev.ohs.fhir.fhirpath.model.FhirModelNavigator
import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import dev.ohs.fhir.fhirpath.types.FhirPrimitiveType
import dev.ohs.fhir.fhirpath.types.FhirType

/**
 * Returns the extensions with the given url on each item in the input collection.
 *
 * `extension(url)` is a shorthand for `extension.where(url = url)`. It works on any element
 * carrying extensions, including primitives (e.g. `Patient.birthDate.extension(...)`).
 *
 * See [specification](https://hl7.org/fhir/R5/fhirpath.html#functions).
 */
internal fun Collection<Any>.extension(
  params: List<Any>,
  fhirModelNavigator: FhirModelNavigator,
): Collection<Any> {
  val url = params.singleOrNull() as? String ?: return emptyList()
  return flatMap { item ->
      when (val extensions = fhirModelNavigator.accessProperty(item, "extension")) {
        null -> emptyList()
        is List<*> -> extensions.filterNotNull()
        else -> error("Expected `extension` to be a list but was: $extensions")
      }
    }
    .filter { fhirModelNavigator.accessProperty(it, "url") == url }
}

/**
 * Returns whether the input is a single FHIR primitive that has an actual value, as opposed to only
 * carrying an `id` or `extension` (e.g. a `_given` element with an extension but no value). Returns
 * an empty list if the input collection is not a single FHIR primitive.
 *
 * See [specification](https://hl7.org/fhir/R5/fhirpath.html#functions).
 */
internal fun Collection<Any>.hasValue(
  fhirPathTypeResolver: FhirPathTypeResolver,
  fhirModelNavigator: FhirModelNavigator,
): Collection<Boolean> {
  // Return an empty collection if the input collection is not a single FHIR primitive
  val item = singleOrNull() ?: return emptyList()
  if (fhirPathTypeResolver.resolveFromObject(item) !is FhirPrimitiveType) {
    return emptyList()
  }
  return listOf(fhirModelNavigator.accessProperty(item, "value") != null)
}

private const val BASE_STRUCTURE_DEFINITION_PREFIX = "http://hl7.org/fhir/StructureDefinition/"

/**
 * Returns whether the single input element conforms to the profile specified by the structure
 * argument.
 *
 * Only the base FHIR profiles (`http://hl7.org/fhir/StructureDefinition/<Type>`) are supported: the
 * input's type is compared to `<Type>`. If the input is not a single item, the structure is empty,
 * or the structure cannot be resolved to a valid profile, the result is empty. Other profiles, such
 * as those defined in implementation guides, would need profile validation, which is not
 * implemented, so they are unresolvable and yield empty.
 *
 * This follows R5 (https://hl7.org/fhir/R5/fhirpath.html#functions). R4 and R4B require an error
 * for an unresolvable structure instead; `testConformsTo3` in the R4 conformance suite is skipped
 * for this reason.
 */
internal fun Collection<Any>.conformsTo(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Boolean> {
  val item = singleOrNull() ?: return emptyList()
  // The structure argument can be a FHIR string (e.g. from a resource element), so it is
  // converted before use like any other string parameter.
  val structure =
    params.singleOrNull()?.toFhirPathType(fhirPathTypeResolver) as? String ?: return emptyList()

  if (!structure.startsWith(BASE_STRUCTURE_DEFINITION_PREFIX)) {
    return emptyList()
  }
  // resolveFromString throws for an unknown type name; an unresolvable structure yields empty.
  val targetType =
    try {
      fhirPathTypeResolver.resolveFromString(
        structure.removePrefix(BASE_STRUCTURE_DEFINITION_PREFIX)
      )
    } catch (_: IllegalStateException) {
      return emptyList()
    }
  // The resolver falls back to System types for names that are not FHIR types (e.g. `String`
  // rather than `string`); those are not valid structure definitions, so they yield empty too.
  if (targetType !is FhirType) {
    return emptyList()
  }
  return listOf(fhirPathTypeResolver.resolveFromObject(item) == targetType)
}
