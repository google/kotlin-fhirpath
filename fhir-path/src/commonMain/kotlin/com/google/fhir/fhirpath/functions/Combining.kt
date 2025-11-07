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

/**
 * See [specification](https://hl7.org/fhirpath/N1/#unionother-collection).
 *
 * NB: The URL is inconsistent with other functions due to a function signature error in the
 * documentation.
 *
 * TODO: Correct URL once https://jira.hl7.org/browse/FHIR-52050 is addressed.
 */
internal fun Collection<Any>.union(other: Collection<Any>): Collection<Any> {
  return (this + other).distinct()
}

/** See [specification](https://hl7.org/fhirpath/N1/#combineother-collection-collection). */
internal fun Collection<Any>.combine(other: Collection<Any>): Collection<Any> {
  return this + other
}
