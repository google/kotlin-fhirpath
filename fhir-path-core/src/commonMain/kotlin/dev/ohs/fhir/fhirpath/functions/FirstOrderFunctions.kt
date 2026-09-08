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

import dev.ohs.fhir.fhirpath.model.FhirModelNavigator
import dev.ohs.fhir.fhirpath.operators.comparable
import dev.ohs.fhir.fhirpath.operators.not
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
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
  fhirPathTypeResolver: FhirPathTypeResolver,
  fhirModelNavigator: FhirModelNavigator,
): Collection<Any> =
  when (functionName) {
    // Existence
    // https://hl7.org/fhirpath/STU3/en/#existence
    // exists() and all() are implemented as higher-order functions in FhirPathEvaluator.kt
    "empty" -> this.empty()
    "allTrue" -> (this as Collection<Boolean>).allTrue()
    "anyTrue" -> (this as Collection<Boolean>).anyTrue()
    "allFalse" -> (this as Collection<Boolean>).allFalse()
    "anyFalse" -> (this as Collection<Boolean>).anyFalse()
    "subsetOf" -> this.subsetOf(params, fhirPathTypeResolver)
    "supersetOf" -> this.supersetOf(params, fhirPathTypeResolver)
    "count" -> this.count()
    "distinct" -> this.distinctFun(fhirPathTypeResolver)
    "isDistinct" -> this.isDistinct(fhirPathTypeResolver)

    // Filtering and projection
    // https://hl7.org/fhirpath/STU3/en/#filtering-and-projection
    // These functions are higher-order functions and are handled directly in the evaluator.

    // Subsetting
    // https://hl7.org/fhirpath/STU3/en/#subsetting
    "single" -> this.singleFun()
    "first" -> this.firstFun()
    "last" -> this.lastFun()
    "tail" -> this.drop(1) // Use Kotlin's drop()
    "skip" -> this.drop(params[0] as Int) // Use Kotlin's drop()
    "take" -> this.take(params[0] as Int) // Use Kotlin's take()
    "intersect" -> this.intersectFun(params, fhirPathTypeResolver)
    "exclude" -> this.exclude(params, fhirPathTypeResolver)

    // Combining
    // https://hl7.org/fhirpath/STU3/en/#combining
    "union" -> this.union(params, fhirPathTypeResolver)
    "combine" -> this.combine(params)

    // Conversion
    // https://hl7.org/fhirpath/STU3/en/#conversion
    "toBoolean" -> this.toBoolean(fhirPathTypeResolver)
    "convertsToBoolean" -> this.convertsToBoolean(fhirPathTypeResolver)
    "toInteger" -> this.toInteger(fhirPathTypeResolver)
    "convertsToInteger" -> this.convertsToInteger(fhirPathTypeResolver)
    "toLong" -> this.toLong(fhirPathTypeResolver)
    "convertsToLong" -> this.convertsToLong(fhirPathTypeResolver)
    "toDate" -> this.toDate(fhirPathTypeResolver)
    "convertsToDate" -> this.convertsToDate(fhirPathTypeResolver)
    "toDateTime" -> this.toDateTime(fhirPathTypeResolver)
    "convertsToDateTime" -> this.convertsToDateTime(fhirPathTypeResolver)
    "toDecimal" -> this.toDecimal(fhirPathTypeResolver)
    "convertsToDecimal" -> this.convertsToDecimal(fhirPathTypeResolver)
    "toQuantity" -> this.toQuantity(params.firstOrNull()?.toString(), fhirPathTypeResolver)
    "convertsToQuantity" -> this.convertsToQuantity(fhirPathTypeResolver)
    "toString" -> this.toStringFun(fhirPathTypeResolver)
    "convertsToString" -> this.convertsToString(fhirPathTypeResolver)
    "toTime" -> this.toTime(fhirPathTypeResolver)
    "convertsToTime" -> this.convertsToTime(fhirPathTypeResolver)

    // String manipulation
    // https://hl7.org/fhirpath/STU3/en/#string-manipulation
    "indexOf" -> this.indexOf(params, fhirPathTypeResolver)
    "substring" -> this.substring(params, fhirPathTypeResolver)
    "startsWith" -> this.startsWith(params, fhirPathTypeResolver)
    "endsWith" -> this.endsWith(params, fhirPathTypeResolver)
    "contains" -> this.strContains(params, fhirPathTypeResolver)
    "upper" -> this.upper(fhirPathTypeResolver)
    "lower" -> this.lower(fhirPathTypeResolver)
    "replace" -> this.replace(params, fhirPathTypeResolver)
    "matches" -> this.matches(params, fhirPathTypeResolver)
    "matchesFull" -> this.matchesFull(params, fhirPathTypeResolver) // STU
    "replaceMatches" -> this.replaceMatches(params, fhirPathTypeResolver)
    "length" -> this.length(fhirPathTypeResolver)
    "toChars" -> this.toChars(fhirPathTypeResolver)

    // Additional string functions (STU)
    // https://build.fhir.org/ig/HL7/FHIRPath/#additional-string-functions
    "encode" -> this.encode(params, fhirPathTypeResolver)
    "decode" -> this.decode(params, fhirPathTypeResolver)
    "escape" -> this.escape(params, fhirPathTypeResolver)
    "unescape" -> this.unescape(params, fhirPathTypeResolver)
    "trim" -> this.trim(fhirPathTypeResolver)
    "split" -> this.split(params, fhirPathTypeResolver)
    "join" -> this.join(params, fhirPathTypeResolver)

    // Math
    // https://hl7.org/fhirpath/STU3/en/#math
    "abs" -> this.abs(fhirPathTypeResolver)
    "ceiling" -> this.ceiling(fhirPathTypeResolver)
    "exp" -> this.exp(fhirPathTypeResolver)
    "floor" -> this.floor(fhirPathTypeResolver)
    "ln" -> this.ln(fhirPathTypeResolver)
    "log" -> this.log(params, fhirPathTypeResolver)
    "power" -> this.power(params, fhirPathTypeResolver)
    "round" -> this.round(params, fhirPathTypeResolver)
    "sqrt" -> this.sqrt(fhirPathTypeResolver)
    "truncate" -> this.truncate(fhirPathTypeResolver)

    // Tree navigation
    // https://hl7.org/fhirpath/STU3/en/#tree-navigation
    "children" -> this.children(fhirModelNavigator)
    "descendants" -> this.descendants(fhirModelNavigator)

    // Utility functions
    // https://hl7.org/fhirpath/STU3/en/#utility-functions
    "now" -> now(now)
    "timeOfDay" -> timeOfDay(now)
    "today" -> today(now)
    "lowBoundary" -> this.lowBoundary(params, fhirPathTypeResolver)
    "highBoundary" -> this.highBoundary(params, fhirPathTypeResolver)
    "precision" -> this.precision(fhirPathTypeResolver)
    "yearOf" -> this.yearOf(fhirPathTypeResolver)
    "monthOf" -> this.monthOf(fhirPathTypeResolver)
    "dayOf" -> this.dayOf(fhirPathTypeResolver)
    "hourOf" -> this.hourOf(fhirPathTypeResolver)
    "minuteOf" -> this.minuteOf(fhirPathTypeResolver)
    "secondOf" -> this.secondOf(fhirPathTypeResolver)
    "millisecondOf" -> this.millisecondOf(fhirPathTypeResolver)
    "timezoneOffsetOf" -> this.timezoneOffsetOf(fhirPathTypeResolver)
    "dateOf" -> this.dateOf(fhirPathTypeResolver)
    "timeOf" -> this.timeOf(fhirPathTypeResolver)

    // Comparison
    // https://hl7.org/fhirpath/STU3/en/#comparison
    "comparable" -> this.comparable(params, fhirPathTypeResolver)

    // Defined as a boolean logic operator in the specification, but the grammar handles this as a
    // function invocation.
    "not" -> this.not()

    // Aggregate
    // https://hl7.org/fhirpath/STU3/en/#aggregates
    // aggregate is a higher-order function handled directly in the evaluator
    "sum" -> this.sumFun(fhirPathTypeResolver)
    "min" -> this.minFun(fhirPathTypeResolver)
    "max" -> this.maxFun(fhirPathTypeResolver)
    "avg" -> this.avgFun(fhirPathTypeResolver)

    // Reflection
    // https://hl7.org/fhirpath/STU3/en/#types-and-reflection
    // https://build.fhir.org/ig/HL7/FHIRPath/en/#reflection
    "type" -> this.type(fhirPathTypeResolver)

    // FHIR-specific functions
    // https://hl7.org/fhir/fhirpath.html#functions
    "extension" -> this.extension(params, fhirModelNavigator)
    "hasValue" -> this.hasValue(fhirPathTypeResolver, fhirModelNavigator)

    else -> error("Function '$functionName' is not implemented.")
  }
