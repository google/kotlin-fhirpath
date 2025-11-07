/*
 * Copyright 2025 Google LLC
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

package com.google.fhir.fhirpath.functions

/** See [specification](https://hl7.org/fhirpath/N1/#empty-boolean). */
internal fun Collection<Any>.empty(): Collection<Boolean> = listOf(isEmpty())

// exists() and all() are implemented as higher-order functions in FhirPathEvaluator.kt

/** See [specification](https://hl7.org/fhirpath/N1/#alltrue-boolean). */
internal fun Collection<Boolean>.allTrue(): Collection<Boolean> = listOf(all { it })

/** See [specification](https://hl7.org/fhirpath/N1/#anytrue-boolean). */
internal fun Collection<Boolean>.anyTrue(): Collection<Boolean> = listOf(any { it })

/** See [specification](https://hl7.org/fhirpath/N1/#allfalse-boolean). */
internal fun Collection<Boolean>.allFalse(): Collection<Boolean> = listOf(all { !it })

/** See [specification](https://hl7.org/fhirpath/N1/#anyfalse-boolean). */
internal fun Collection<Boolean>.anyFalse(): Collection<Boolean> = listOf(any { !it })

/** See [specification](https://hl7.org/fhirpath/N1/#subsetofother-collection-boolean). */
internal fun Collection<Any>.subsetOf(params: Collection<Any>): Collection<Boolean> =
  listOf(all { params.contains(it) })

/** See [specification](https://hl7.org/fhirpath/N1/#supersetofother-collection-boolean). */
internal fun Collection<Any>.supersetOf(params: Collection<Any>): Collection<Boolean> =
  listOf(params.all { this.contains(it) })

/** See [specification](https://hl7.org/fhirpath/N1/#count-integer). */
internal fun Collection<Any>.count(): Collection<Int> = listOf(size)

/** See [specification](https://hl7.org/fhirpath/N1/#distinct-collection). */
// Use Kotlin's distinct()
// internal fun Collection<Any>.distinct(): Collection<Any>

/** See [specification](https://hl7.org/fhirpath/N1/#isdistinct-boolean). */
internal fun Collection<Any>.isDistinct(): Collection<Boolean> =
  listOf(count() == distinct().count())
