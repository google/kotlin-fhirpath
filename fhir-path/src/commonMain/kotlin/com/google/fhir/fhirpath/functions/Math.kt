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

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.math.truncate

/** See [specification](https://hl7.org/fhirpath/N1/#abs-integer-decimal-quantity). */
internal fun Collection<Any>.abs(): Collection<Any> {
  check(size <= 1) { "abs() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int -> listOf(abs(value))
    is Long -> listOf(abs(value))
    is Double -> listOf(abs(value))
    // TODO: implement for quantity
    else -> error("abs() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#ceiling-integer) */
internal fun Collection<Any>.ceiling(): Collection<Any> {
  check(size <= 1) { "ceiling() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int,
    Long -> listOf(value)
    is Double -> listOf(ceil(value))
    else -> error("ceiling() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#exp-decimal) */
internal fun Collection<Any>.exp(): Collection<Any> {
  check(size <= 1) { "exp() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int -> listOf(exp(value.toDouble()))
    is Long -> listOf(exp(value.toDouble()))
    is Double -> listOf(exp(value))
    else -> error("exp() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#floor-integer) */
internal fun Collection<Any>.floor(): Collection<Any> {
  check(size <= 1) { "floor() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int,
    Long -> listOf(value)
    is Double -> listOf(floor(value))
    else -> error("floor() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#ln-decimal) */
internal fun Collection<Any>.ln(): Collection<Any> {
  check(size <= 1) { "ln() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int -> listOf(ln(value.toDouble()))
    is Long -> listOf(ln(value.toDouble()))
    is Double -> listOf(ln(value))
    else -> error("ln() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#logbase-decimal-decimal) */
internal fun Collection<Any>.log(params: List<Any>): Collection<Any> {
  check(size <= 1) { "log() cannot be called on a collection with more than 1 item" }
  val valueDouble =
    when (val value = this.singleOrNull() ?: return emptyList()) {
      is Int -> value.toDouble()
      is Long -> value.toDouble()
      is Double -> value
      else -> error("log() can only be applied to numbers")
    }
  val base =
    when (val param = params.singleOrNull() ?: return emptyList()) {
      is Int -> param.toDouble()
      is Long -> param.toDouble()
      is Double -> param
      else -> error("log() can only be applied to numbers")
    }
  return listOf(ln(valueDouble) / ln(base))
}

/**
 * See [specification](https://hl7.org/fhirpath/N1/#powerexponent-integer-decimal-integer-decimal)
 */
internal fun Collection<Any>.power(params: List<Any>): Collection<Any> {
  check(size <= 1) { "power() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  val exponent = params.singleOrNull() ?: return emptyList()
  val valueDouble =
    when (value) {
      is Int -> value.toDouble()
      is Long -> value.toDouble()
      is Double -> value
      else -> error("power() can only be applied to numbers")
    }
  val exponentDouble =
    when (exponent) {
      is Int -> exponent.toDouble()
      is Long -> exponent.toDouble()
      is Double -> exponent
      else -> error("power() can only be applied to numbers")
    }
  val result = valueDouble.pow(exponentDouble)

  if (result.isNaN()) return emptyList()
  if (value is Int && exponent is Int) return listOf(result.toInt())
  if (
    (value is Long && exponent is Long) ||
      (value is Int && exponent is Long) ||
      (value is Long && exponent is Int)
  ) {
    return listOf(result.toLong())
  }
  return listOf(result)
}

/** See [specification](https://hl7.org/fhirpath/N1/#roundprecision-integer-decimal) */
internal fun Collection<Any>.round(params: List<Any>): Collection<Any> {
  check(size <= 1) { "round() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  val precision = params.singleOrNull()?.let { it as Int } ?: 0
  check(precision >= 0) { "round() precision must be non-negative" }
  return when (value) {
    is Int -> listOf(value.toDouble())
    is Long -> listOf(value.toDouble())
    is Double -> listOf(round(value * 10.0.pow(precision)) / 10.0.pow(precision))
    else -> error("round() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#sqrt-decimal) */
internal fun Collection<Any>.sqrt(): Collection<Any> {
  check(size <= 1) { "sqrt() cannot be called on a collection with more than 1 item" }
  val valueDouble =
    when (val value = this.singleOrNull() ?: return emptyList()) {
      is Int -> value.toDouble()
      is Long -> value.toDouble()
      is Double -> value
      else -> error("power() can only be applied to numbers")
    }
  val sqrt = sqrt(valueDouble)
  if (sqrt.isNaN()) return emptyList()
  return listOf(sqrt)
}

/** See [specification](https://hl7.org/fhirpath/N1/#truncate-integer) */
internal fun Collection<Any>.truncate(): Collection<Any> {
  check(size <= 1) { "truncate() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int,
    Long -> listOf(value)
    is Double -> listOf(truncate(value))
    else -> error("truncate() can only be applied to numbers")
  }
}
