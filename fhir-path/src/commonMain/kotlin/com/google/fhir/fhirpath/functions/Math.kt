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

import com.google.fhir.fhirpath.operators.DECIMAL_MODE
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/** See [specification](https://hl7.org/fhirpath/N1/#abs-integer-decimal-quantity). */
internal fun Collection<Any>.abs(): Collection<Any> {
  check(size <= 1) { "abs() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int -> listOf(abs(value))
    is Long -> listOf(abs(value))
    is BigDecimal -> listOf(value.abs())
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
    is BigDecimal ->
      listOf(value.ceil().intValue()) // TODO: handle the case where the value needs to be a Long
    else -> error("ceiling() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#exp-decimal) */
internal fun Collection<Any>.exp(): Collection<Any> {
  check(size <= 1) { "exp() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int -> listOf(exp(value.toDouble()).toBigDecimal())
    is Long -> listOf(exp(value.toDouble()).toBigDecimal())
    is BigDecimal -> listOf(exp(value.doubleValue()).toBigDecimal())
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
    is BigDecimal ->
      listOf(value.floor().intValue()) // TODO: handle the case where the value needs to be a Long
    else -> error("floor() can only be applied to numbers")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#ln-decimal) */
internal fun Collection<Any>.ln(): Collection<Any> {
  check(size <= 1) { "ln() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int -> listOf(ln(value.toDouble()).toBigDecimal())
    is Long -> listOf(ln(value.toDouble()).toBigDecimal())
    is BigDecimal -> listOf(ln(value.doubleValue()).toBigDecimal())
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
      is BigDecimal -> value.doubleValue()
      else -> error("log() can only be applied to numbers")
    }
  val baseDouble =
    when (val param = params.singleOrNull() ?: return emptyList()) {
      is Int -> param.toDouble()
      is Long -> param.toDouble()
      is BigDecimal -> param.doubleValue()
      else -> error("log() can only be applied to numbers")
    }
  return listOf((ln(valueDouble) / ln(baseDouble)).toBigDecimal())
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
      is BigDecimal -> value.doubleValue()
      else -> error("power() can only be applied to numbers")
    }
  val exponentDouble =
    when (exponent) {
      is Int -> exponent.toDouble()
      is Long -> exponent.toDouble()
      is BigDecimal -> exponent.doubleValue()
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
    // N.B. the specification does not specify what to do if the result is out of range for Integer.
    return listOf(result.toLong())
  }
  return listOf(result.toBigDecimal())
}

/** See [specification](https://hl7.org/fhirpath/N1/#roundprecision-integer-decimal) */
internal fun Collection<Any>.round(params: List<Any>): Collection<Any> {
  check(size <= 1) { "round() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  val precision = params.singleOrNull()?.let { it as Int } ?: 0
  check(precision >= 0) { "round() precision must be non-negative" }
  return when (value) {
    is Int -> listOf(value.toBigDecimal())
    is Long -> listOf(value.toBigDecimal())
    is BigDecimal ->
      listOf(
        value.roundToDigitPositionAfterDecimalPoint(precision.toLong(), DECIMAL_MODE.roundingMode)
      )
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
      is BigDecimal -> value.doubleValue()
      else -> error("power() can only be applied to numbers")
    }
  val sqrt = sqrt(valueDouble)
  if (sqrt.isNaN()) return emptyList()
  return listOf(sqrt.toBigDecimal())
}

/** See [specification](https://hl7.org/fhirpath/N1/#truncate-integer) */
internal fun Collection<Any>.truncate(): Collection<Any> {
  check(size <= 1) { "truncate() cannot be called on a collection with more than 1 item" }
  val value = this.singleOrNull() ?: return emptyList()
  return when (value) {
    is Int,
    Long -> listOf(value)
    is BigDecimal -> listOf(value.toBigInteger().intValue())
    else -> error("truncate() can only be applied to numbers")
  }
}
