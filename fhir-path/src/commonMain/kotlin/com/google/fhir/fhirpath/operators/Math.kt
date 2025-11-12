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

import com.google.fhir.model.r4.Decimal
import com.google.fhir.model.r4.Quantity
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

val DECIMAL_MODE =
  DecimalMode(
    decimalPrecision = 15, // TODO: clarify this with the specification
    roundingMode =
      RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, // See https://jira.hl7.org/browse/FHIR-53159
  )

/** See [specification](https://hl7.org/fhirpath/N1/#multiplication). */
internal fun multiplication(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()

  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(rightItem * leftItem)
    leftItem is Int && rightItem is Quantity -> listOf(rightItem * leftItem.toBigDecimal())
    leftItem is Long && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(rightItem * leftItem)
    leftItem is Long && rightItem is Quantity -> listOf(rightItem * leftItem.toBigDecimal())
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem * rightItem)
    leftItem is BigDecimal && rightItem is Quantity -> listOf(rightItem * leftItem)
    leftItem is Quantity && rightItem is Int -> {
      listOf(leftItem * rightItem.toBigDecimal())
    }
    leftItem is Quantity && rightItem is Long -> {
      listOf(leftItem * rightItem.toBigDecimal())
    }
    leftItem is Quantity && rightItem is BigDecimal -> {
      listOf(leftItem * rightItem)
    }
    leftItem is Quantity && rightItem is Quantity -> TODO("Implement multiplying two quantities")
    else -> error("Cannot multiply $leftItem and $rightItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#division). */
internal fun division(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()
  val leftBigDecimal =
    when (leftItem) {
      is Int -> leftItem.toBigDecimal()
      is Long -> leftItem.toBigDecimal()
      is BigDecimal -> leftItem
      else -> error("Operand of division must be a number")
    }
  val rightBigDecimal =
    when (rightItem) {
      is Int -> rightItem.toBigDecimal()
      is Long -> rightItem.toBigDecimal()
      is BigDecimal -> rightItem
      else -> error("Operand of division must be a number")
    }

  if (rightBigDecimal == BigDecimal.ZERO) return emptyList()
  return listOf(leftBigDecimal.divide(rightBigDecimal, DECIMAL_MODE))
}

/** See [specification](https://hl7.org/fhirpath/N1/#addition). */
internal fun addition(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()

  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem + rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem + rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(rightItem + leftItem)
    leftItem is Long && rightItem is Int -> listOf(leftItem + rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem + rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(rightItem + leftItem)
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem + rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem + rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem + rightItem)
    leftItem is String && rightItem is String -> listOf(leftItem + rightItem)
    leftItem is Quantity && rightItem is Quantity -> TODO("Implement adding two quantities")
    else -> error("Cannot add $leftItem and $rightItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#subtraction). */
internal fun subtraction(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val rightItem = right.singleOrNull() ?: return emptyList()
  return when {
    leftItem is Int && rightItem is Int -> listOf(leftItem - rightItem)
    leftItem is Int && rightItem is Long -> listOf(leftItem - rightItem)
    leftItem is Int && rightItem is BigDecimal -> listOf(-rightItem + leftItem)
    leftItem is Long && rightItem is Int -> listOf(leftItem - rightItem)
    leftItem is Long && rightItem is Long -> listOf(leftItem - rightItem)
    leftItem is Long && rightItem is BigDecimal -> listOf(-rightItem + leftItem)
    leftItem is BigDecimal && rightItem is Int -> listOf(leftItem - rightItem)
    leftItem is BigDecimal && rightItem is Long -> listOf(leftItem - rightItem)
    leftItem is BigDecimal && rightItem is BigDecimal -> listOf(leftItem - rightItem)
    leftItem is Quantity && rightItem is Quantity -> TODO("Implement subtracting two quantities")
    else -> error("Cannot subtract $rightItem from $leftItem")
  }
}

/** See [specification](https://hl7.org/fhirpath/N1/#div). */
internal fun div(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftBigDecimal =
    when (val leftItem = left.singleOrNull() ?: return emptyList()) {
      is Int -> leftItem.toBigDecimal()
      is Long -> leftItem.toBigDecimal()
      is BigDecimal -> leftItem
      else -> error("Operand of div must be a number")
    }
  val rightBigDecimal =
    when (val rightItem = right.singleOrNull() ?: return emptyList()) {
      is Int -> rightItem.toBigDecimal()
      is Long -> rightItem.toBigDecimal()
      is BigDecimal -> rightItem
      else -> error("Operand of div must be a number")
    }
  if (rightBigDecimal == BigDecimal.ZERO) return emptyList()

  val (quotient, _) = leftBigDecimal divrem rightBigDecimal
  return listOf(quotient.intValue())
}

/** See [specification](https://hl7.org/fhirpath/N1/#mod). */
internal fun mod(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  val leftItem = left.singleOrNull() ?: return emptyList()
  val leftBigDecimal =
    when (leftItem) {
      is Int -> leftItem.toBigDecimal()
      is Long -> leftItem.toBigDecimal()
      is BigDecimal -> leftItem
      else -> error("Operand of mod must be a number")
    }
  val rightItem = right.singleOrNull() ?: return emptyList()
  val rightBigDecimal =
    when (rightItem) {
      is Int -> rightItem.toBigDecimal()
      is Long -> rightItem.toBigDecimal()
      is BigDecimal -> rightItem
      else -> error("Operand of mod must be a number")
    }
  if (rightBigDecimal.isZero()) return emptyList()

  val (_, remainder) = leftBigDecimal divrem rightBigDecimal
  if (leftItem is Int && rightItem is Int) return listOf(remainder.intValue())
  if (
    (leftItem is Long && rightItem is Long) ||
      (leftItem is Int && rightItem is Long) ||
      (leftItem is Long && rightItem is Int)
  ) {
    // N.B. the specification does not specify what to do if the result is out of range for Integer.
    return listOf(remainder.longValue())
  }
  return listOf(remainder)
}

/** See [specification](https://hl7.org/fhirpath/N1/#string-concatenation) */
internal fun concat(left: Collection<Any>, right: Collection<Any>): Collection<Any> {
  check(left.size <= 1) { "& cannot be called on a collection with more than 1 item" }
  check(right.size <= 1) { "& cannot be called on a collection with more than 1 item" }

  val leftString = (left.singleOrNull() as String?) ?: ""
  val rightString: String = (right.singleOrNull() as String?) ?: ""
  return listOf(leftString + rightString)
}

private operator fun Quantity.times(multiplier: BigDecimal): Quantity {
  return Quantity(
    id = this.id,
    extension = this.extension,
    value =
      with(this.value!!) {
        Decimal(id = this.id, extension = this.extension, value = this.value!! * multiplier)
      },
    comparator = this.comparator,
    unit = this.unit,
    system = this.system,
    code = this.code,
  )
}
