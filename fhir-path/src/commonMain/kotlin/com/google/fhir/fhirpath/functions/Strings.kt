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

import com.google.fhir.model.r4.Enumeration
import kotlin.math.min

/**
 * Makes the dot match all characters, including line breaks.
 *
 * See https://www.regular-expressions.info/modifiers.html.
 */
const val SINGLE_LINE_MODE_REGEX_PREFIX = "(?s)"

/** See [specification](https://hl7.org/fhirpath/N1/#indexofsubstring-string-integer). */
internal fun Collection<Any>.indexOf(params: List<Any>): Collection<Any> {
  check(size <= 1) { "indexOf() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val substring = params.singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(input.indexOf(substring))
}

/**
 * See [specification](https://hl7.org/fhirpath/N1/#substringstart-integer-length-integer-string).
 */
internal fun Collection<Any>.substring(params: List<Any>): Collection<Any> {
  check(size <= 1) { "substring() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val start = params.firstOrNull() as? Int ?: return emptyList()
  val length = params.getOrNull(1)?.let { it as Int }

  if (start >= input.length || start < 0) {
    return emptyList()
  }

  if (length != null) {
    val endIndex = min(start + length, input.length)
    return listOf(input.substring(start, endIndex))
  }

  return listOf(input.substring(start))
}

/** See [specification](https://hl7.org/fhirpath/N1/#startsprefix-string-boolean). */
internal fun Collection<Any>.startsWith(params: List<Any>): Collection<Any> {
  check(size <= 1) { "startsWith() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val prefix = params.single().unwrapString()!!
  return listOf(input.startsWith(prefix))
}

/** See [specification](https://hl7.org/fhirpath/N1/#endssuffix-string-boolean). */
internal fun Collection<Any>.endsWith(params: List<Any>): Collection<Any> {
  check(size <= 1) { "endsWith() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val suffix = params.single().unwrapString()!!
  return listOf(input.endsWith(suffix))
}

/** See [specification](https://hl7.org/fhirpath/N1/#containssubstring-string-boolean). */
internal fun Collection<Any>.strContains(params: List<Any>): Collection<Any> {
  check(size <= 1) { "contains() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val substring = params.single().unwrapString()!!
  return listOf(input.contains(substring))
}

/** See [specification](https://hl7.org/fhirpath/N1/#upper-string). */
internal fun Collection<Any>.upper(): Collection<Any> {
  check(size <= 1) { "upper() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(input.uppercase())
}

/** See [specification](https://hl7.org/fhirpath/N1/#lower-string). */
internal fun Collection<Any>.lower(): Collection<Any> {
  check(size <= 1) { "lower() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(input.lowercase())
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/N1/#replacepattern-string-substitution-string-string).
 */
internal fun Collection<Any>.replace(params: List<Any>): Collection<Any> {
  check(size <= 1) { "replace() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val pattern = params[0].unwrapString()!!
  val substitution = params[1].unwrapString()!!
  return listOf(input.replace(pattern, substitution))
}

/** See [specification](https://hl7.org/fhirpath/N1/#matchesregex-string-boolean). */
internal fun Collection<Any>.matches(params: List<Any>): Collection<Any> {
  check(size <= 1) { "matches() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val regexText = params.singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(regexText.toSingleLineModeRegex().containsMatchIn(input))
}

/**
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#matchesfullregex--string--boolean).
 */
internal fun Collection<Any>.matchesFull(params: List<Any>): Collection<Any> {
  check(size <= 1) { "matches() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  val regex = params.singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(input.matches(regex.toSingleLineModeRegex()))
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/N1/#replacematchesregex-string-substitution-string-string).
 */
internal fun Collection<Any>.replaceMatches(params: List<Any>): Collection<Any> {
  check(size <= 1) { "replaceMatches() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()

  if (params.size < 2) {
    return emptyList()
  }
  val regex = params[0].unwrapString()!!
  val substitution = params[1].unwrapString()!!

  if (regex.isEmpty()) {
    return this
  }
  return listOf(input.replace(regex.toSingleLineModeRegex(), substitution))
}

/** See [specification](https://hl7.org/fhirpath/N1/#length-integer). */
internal fun Collection<Any>.length(): Collection<Any> {
  check(size <= 1) { "length() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(input.length)
}

/** See [specification](https://hl7.org/fhirpath/N1/#tochars-collection). */
internal fun Collection<Any>.toChars(): Collection<Any> {
  check(size <= 1) { "toChars() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  return input.toCharArray().map { it.toString() }
}

// Standard for Trial Use (STU)

/** See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#trim--string). */
internal fun Collection<Any>.trim(): Collection<Any> {
  check(size <= 1) { "trim() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  return listOf(input.trim())
}

/**
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#splitseparator-string--collection).
 */
internal fun Collection<Any>.split(params: List<Any>): Collection<Any> {
  check(size <= 1) { "split() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString() ?: return emptyList()
  return input.split(params.singleOrNull()?.unwrapString() ?: return emptyList())
}

/** See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#joinseparator-string--string). */
internal fun Collection<Any>.join(params: List<Any>): Collection<Any> {
  if (isEmpty()) return emptyList()
  return listOf(joinToString(params.singleOrNull()?.unwrapString() ?: "") { it.unwrapString()!! })
}

private fun Any.unwrapString(): String? {
  return when (this) {
    is com.google.fhir.model.r4.String -> value
    is String -> this
    is Enumeration<*> -> value.toString()
    else -> null
  }
}

private fun String.toSingleLineModeRegex(): Regex = "$SINGLE_LINE_MODE_REGEX_PREFIX$this".toRegex()
