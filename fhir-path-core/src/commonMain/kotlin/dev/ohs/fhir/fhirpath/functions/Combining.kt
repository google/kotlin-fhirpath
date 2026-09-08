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

/**
 * See [specification](https://hl7.org/fhirpath/STU3/en/#unionother--collection--collection).
 *
 * Duplicates are detected on the converted FHIRPath values, but the original items are kept and the
 * first occurrence of each value wins. The surviving representation therefore depends on operand
 * order: if `a` holds a FHIR string and `b` a FHIRPath string of the same value, `a.union(b)` keeps
 * the FHIR string and `b.union(a)` keeps the FHIRPath string. Both results are equal per the `=`
 * operator, and the specification does not prescribe which duplicate to keep.
 */
internal fun Collection<Any>.union(
  other: Collection<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  return (this + other).distinctBy { it.toFhirPathType(fhirPathTypeResolver) }
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#combineother--collection-preserveorder--boolean--collection).
 */
internal fun Collection<Any>.combine(other: Collection<Any>): Collection<Any> {
  return this + other
}
