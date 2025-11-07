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

import com.google.fhir.fhirpath.operators.not
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Invokes first-order functions that do not require in-context expression valuation (e.g. where) or
 * short-circuiting (e.g. iif).
 */
@OptIn(ExperimentalTime::class)
internal fun Collection<Any>.invoke(
  functionName: String,
  params: List<Any>,
  now: Instant,
): Collection<Any> =
  when (functionName) {
    // Existence
    // https://hl7.org/fhirpath/N1/#existence
    // exists() and all() are implemented as higher-order functions in FhirPathEvaluator.kt
    "empty" -> this.empty()
    "allTrue" -> (this as Collection<Boolean>).allTrue()
    "anyTrue" -> (this as Collection<Boolean>).anyTrue()
    "allFalse" -> (this as Collection<Boolean>).allFalse()
    "anyFalse" -> (this as Collection<Boolean>).anyFalse()
    "subsetOf" -> this.subsetOf(params)
    "supersetOf" -> this.supersetOf(params)
    "count" -> this.count()
    "distinct" -> this.distinct() // Use Kotlin's distinct()
    "isDistinct" -> this.isDistinct()

    // Filtering and projection
    // https://hl7.org/fhirpath/N1/#filtering-and-projection
    // These functions are higher-order functions and are handled directly in the evaluator.

    // Subsetting
    // https://hl7.org/fhirpath/N1/#subsetting
    "single" -> this.singleFun()
    "first" -> this.firstFun()
    "last" -> this.lastFun()
    "tail" -> this.drop(1) // Use Kotlin's drop()
    "skip" -> this.drop(params[0] as Int) // Use Kotlin's drop()
    "take" -> this.take(params[0] as Int) // Use Kotlin's take()
    "intersect" -> this.intersectFun(params)
    "exclude" -> this.exclude(params)

    // Combining
    // https://hl7.org/fhirpath/N1/#combining
    "union" -> this.union(params)
    "combine" -> this.combine(params)

    // Conversion
    // https://hl7.org/fhirpath/N1/#conversion
    "toBoolean" -> this.toBoolean()
    "convertsToBoolean" -> this.convertsToBoolean()
    "toInteger" -> this.toInteger()
    "convertsToInteger" -> this.convertsToInteger()
    "toDate" -> this.toDate()
    "convertsToDate" -> this.convertsToDate()
    "toDateTime" -> this.toDateTime()
    "convertsToDateTime" -> this.convertsToDateTime()
    "toDecimal" -> this.toDecimal()
    "convertsToDecimal" -> this.convertsToDecimal()
    "toQuantity" -> this.toQuantity(params.firstOrNull()?.toString())
    "convertsToQuantity" -> this.convertsToQuantity()
    "toString" -> this.toStringFun()
    "convertsToString" -> this.convertsToString()
    "toTime" -> this.toTime()
    "convertsToTime" -> this.convertsToTime()

    // String manipulation
    // https://hl7.org/fhirpath/N1/#string-manipulation
    "indexOf" -> this.indexOf(params)
    "substring" -> this.substring(params)
    "startsWith" -> this.startsWith(params)
    "endsWith" -> this.endsWith(params)
    "contains" -> this.strContains(params)
    "upper" -> this.upper()
    "lower" -> this.lower()
    "replace" -> this.replace(params)
    "matches" -> this.matches(params)
    "matchesFull" -> this.matchesFull(params) // STU
    "replaceMatches" -> this.replaceMatches(params)
    "length" -> this.length()
    "toChars" -> this.toChars()

    // Additional string functions (STU)
    // https://build.fhir.org/ig/HL7/FHIRPath/#additional-string-functions
    "trim" -> this.trim()
    "split" -> this.split(params)
    "join" -> this.join(params)

    // Math
    // https://hl7.org/fhirpath/N1/#math
    "abs" -> this.abs()
    "ceiling" -> this.ceiling()
    "exp" -> this.exp()
    "floor" -> this.floor()
    "ln" -> this.ln()
    "log" -> this.log(params)
    "power" -> this.power(params)
    "round" -> this.round(params)
    "sqrt" -> this.sqrt()
    "truncate" -> this.truncate()

    // Tree navigation
    // https://hl7.org/fhirpath/N1/#tree-navigation

    // Utility functions
    // https://hl7.org/fhirpath/N1/#utility-functions
    "now" -> now(now)
    "timeOfDay" -> timeOfDay(now)
    "today" -> today(now)
    "lowBoundary" -> this.lowBoundary(params)
    "highBoundary" -> this.highBoundary(params)
    "precision" -> this.precision()

    // Defined as a boolean logic operator in the specification, but the grammar handles this as a
    // function invocation.
    "not" -> this.not()

    else -> error("Function '$functionName' is not implemented.")
  }
