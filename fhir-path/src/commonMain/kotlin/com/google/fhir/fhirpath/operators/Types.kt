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

package com.google.fhir.fhirpath.operators

import com.google.fhir.fhirpath.fhirTypeToFhirPathType
import com.google.fhir.fhirpath.types.FhirPathTypeResolver

/** See [specification](https://hl7.org/fhirpath/N1/#istype-type-specifier). */
internal fun Collection<Any>.`is`(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Boolean> {
  check(size <= 1) { "is cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()

  val type = fhirPathTypeResolver.resolveFromObject(item)
  val targetType = params.single()
  return listOf(type == targetType)
}

/** See [specification](https://hl7.org/fhirpath/N1/#astype-type-specifier). */
internal fun Collection<Any>.`as`(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "as cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()

  val type = fhirPathTypeResolver.resolveFromObject(item)
  val targetType = params.single()

  if (type == targetType) {
    return this
  }

  fhirTypeToFhirPathType[type]?.let {
    if (it.first == targetType) {
      return listOf(it.second(item))
    }
  }

  return emptyList()
}
