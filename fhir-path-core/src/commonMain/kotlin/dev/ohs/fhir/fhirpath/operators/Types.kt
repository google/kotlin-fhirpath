/*
 * Copyright 2025-2026 Open Health Stack Foundation
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

package dev.ohs.fhir.fhirpath.operators

import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver

/** See [specification](https://hl7.org/fhirpath/STU3/en/#istype--type-specifier). */
internal fun Collection<Any>.`is`(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Boolean> {
  check(size <= 1) { "is cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()

  val type = fhirPathTypeResolver.resolveFromObject(item)
  val targetType = params.single() as? FhirPathType ?: return listOf(false)
  return listOf(type != null && type.isSubtypeOf(targetType))
}

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#oftypetype--type-specifier--collection).
 */
internal fun Collection<Any>.ofType(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val targetType = params.single() as? FhirPathType ?: return emptyList()
  return filter { item ->
    val type = fhirPathTypeResolver.resolveFromObject(item)
    type != null && (type == targetType || type.typeName == targetType.typeName)
  }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#astype--type-specifier). */
internal fun Collection<Any>.`as`(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "as cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()

  val type = fhirPathTypeResolver.resolveFromObject(item)
  val targetType = params.single() as? FhirPathType ?: return emptyList()

  if (type != null && (type == targetType || type.typeName == targetType.typeName)) {
    return this
  }

  val converted = item.toFhirPathType(fhirPathTypeResolver)
  val convertedType = fhirPathTypeResolver.resolveFromObject(converted)
  if (
    convertedType != null &&
      (convertedType == targetType || convertedType.typeName == targetType.typeName)
  ) {
    return listOf(converted)
  }

  return emptyList()
}
