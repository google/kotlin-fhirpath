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

package com.google.fhir.fhirpath.operators

/** See [specification](https://hl7.org/fhirpath/N1/#and). */
internal fun and(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  check(left.size <= 1 && right.size <= 1) {
    "and() cannot be called on a collection with more than 1 item"
  }
  val leftBoolean = left.evaluateToBoolean()
  val rightBoolean = right.evaluateToBoolean()
  return when {
    leftBoolean == true && rightBoolean == true -> listOf(true)
    leftBoolean == false || rightBoolean == false -> listOf(false)
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#or). */
internal fun or(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  check(left.size <= 1 && right.size <= 1) {
    "and() cannot be called on a collection with more than 1 item"
  }
  val leftBoolean = left.evaluateToBoolean()
  val rightBoolean = right.evaluateToBoolean()
  return when {
    leftBoolean == false && rightBoolean == false -> listOf(false)
    leftBoolean == true || rightBoolean == true -> listOf(true)
    else -> emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#not-boolean). */
internal fun Collection<Any>.not(): Collection<Any> {
  check(size <= 1) { "not() cannot be called on a collection with more than 1 item" }
  val singletonBoolean = evaluateToBoolean()
  if (singletonBoolean == null) return emptyList()
  return listOf(!singletonBoolean)
}

/** See [specification](https://hl7.org/fhirpath/N1/#xor). */
internal fun xor(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  check(left.size <= 1 && right.size <= 1) {
    "and() cannot be called on a collection with more than 1 item"
  }
  val leftBoolean = left.evaluateToBoolean()
  val rightBoolean = right.evaluateToBoolean()
  return when {
    leftBoolean == null || rightBoolean == null -> emptyList()
    leftBoolean != rightBoolean -> listOf(true)
    else -> listOf(false)
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#implies). */
internal fun implies(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  check(left.size <= 1 && right.size <= 1) {
    "and() cannot be called on a collection with more than 1 item"
  }
  val leftBoolean = left.evaluateToBoolean()
  val rightBoolean = right.evaluateToBoolean()
  return when (leftBoolean) {
    true -> rightBoolean?.let { listOf(it) } ?: emptyList()
    false -> listOf(true)
    else -> if (rightBoolean == true) listOf(true) else emptyList()
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#singleton-evaluation-of-collections). */
private fun Collection<Any>.evaluateToBoolean(): Boolean? {
  check(size <= 1) {
    "Singleton boolean evaluation cannot be applied to collections with more than 1 item"
  }
  val singleton = singleOrNull() ?: return null
  val singletonBoolean = singleton as? Boolean ?: return true
  return singletonBoolean
}
