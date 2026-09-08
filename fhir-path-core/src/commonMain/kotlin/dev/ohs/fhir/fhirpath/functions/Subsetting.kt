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

package dev.ohs.fhir.fhirpath.functions

import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver

/** See [specification](https://hl7.org/fhirpath/STU3/en/#single--any). */
internal fun Collection<Any>.singleFun(): Collection<Any> =
  if (isEmpty()) {
    emptyList()
  } else {
    listOf(this.single())
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#first--any). */
internal fun Collection<Any>.firstFun(): Collection<Any> =
  if (isEmpty()) {
    emptyList()
  } else {
    listOf(this.first())
  }

/** See [specification](https://hl7.org/fhirpath/STU3/en/#last--any). */
internal fun Collection<Any>.lastFun(): Collection<Any> =
  if (isEmpty()) {
    emptyList()
  } else {
    listOf(this.last())
  }

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#intersectother-collection--collection).
 *
 * The specification requires duplicates to be eliminated from the result (unlike [exclude], which
 * keeps them). Original items are preserved; conversion to FHIRPath types is only used for
 * comparison, and the first occurrence of duplicate values is kept.
 */
internal fun Collection<Any>.intersectFun(
  other: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val otherConverted = other.mapTo(mutableSetOf()) { it.toFhirPathType(fhirPathTypeResolver) }
  return this.filter { otherConverted.contains(it.toFhirPathType(fhirPathTypeResolver)) }
    .distinctBy { it.toFhirPathType(fhirPathTypeResolver) }
}

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#excludeother-collection--collection).
 *
 * Unlike [intersectFun], the specification does not eliminate duplicates here. Original items are
 * preserved; conversion to FHIRPath types is only used for comparison.
 */
internal fun Collection<Any>.exclude(
  other: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  val otherConverted = other.mapTo(mutableSetOf()) { it.toFhirPathType(fhirPathTypeResolver) }
  return this.filterNot { otherConverted.contains(it.toFhirPathType(fhirPathTypeResolver)) }
}
