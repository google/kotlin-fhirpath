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

/** See [specification](https://hl7.org/fhirpath/STU3/en/#empty--boolean). */
internal fun Collection<Any>.empty(): Collection<Boolean> = listOf(isEmpty())

// exists() and all() are implemented as higher-order functions in FhirPathEvaluator.kt

/** See [specification](https://hl7.org/fhirpath/STU3/en/#alltrue--boolean). */
internal fun Collection<Boolean>.allTrue(): Collection<Boolean> = listOf(all { it })

/** See [specification](https://hl7.org/fhirpath/STU3/en/#anytrue--boolean). */
internal fun Collection<Boolean>.anyTrue(): Collection<Boolean> = listOf(any { it })

/** See [specification](https://hl7.org/fhirpath/STU3/en/#allfalse--boolean). */
internal fun Collection<Boolean>.allFalse(): Collection<Boolean> = listOf(all { !it })

/** See [specification](https://hl7.org/fhirpath/STU3/en/#anyfalse--boolean). */
internal fun Collection<Boolean>.anyFalse(): Collection<Boolean> = listOf(any { !it })

/** See [specification](https://hl7.org/fhirpath/STU3/en/#subsetofother--collection--boolean). */
internal fun Collection<Any>.subsetOf(
  params: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Boolean> {
  val paramsConverted = params.mapTo(mutableSetOf()) { it.toFhirPathType(fhirPathTypeResolver) }
  return listOf(paramsConverted.containsAll(map { it.toFhirPathType(fhirPathTypeResolver) }))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#supersetofother--collection--boolean). */
internal fun Collection<Any>.supersetOf(
  params: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Boolean> {
  val thisConverted = mapTo(mutableSetOf()) { it.toFhirPathType(fhirPathTypeResolver) }
  return listOf(thisConverted.containsAll(params.map { it.toFhirPathType(fhirPathTypeResolver) }))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#count--integer). */
internal fun Collection<Any>.count(): Collection<Int> = listOf(size)

/** See [specification](https://hl7.org/fhirpath/STU3/en/#distinct--collection). */
// Use Kotlin's distinct()
// internal fun Collection<Any>.distinct(): Collection<Any>

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#distinct--collection).
 *
 * Duplicates are detected on the converted FHIRPath values, but the original items are kept and the
 * first occurrence of each value wins. The specification does not prescribe which duplicate to
 * keep.
 */
internal fun Collection<Any>.distinctFun(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Any> {
  return distinctBy { it.toFhirPathType(fhirPathTypeResolver) }
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#isdistinct--boolean). */
internal fun Collection<Any>.isDistinct(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<Boolean> = listOf(count() == distinctFun(fhirPathTypeResolver).count())
