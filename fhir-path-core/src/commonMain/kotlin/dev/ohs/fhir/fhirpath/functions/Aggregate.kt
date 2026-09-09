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

import dev.ohs.fhir.fhirpath.operators.compare
import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import dev.ohs.fhir.fhirpath.types.plus
import dev.ohs.fhir.fhirpath.types.toFhirPathDecimal
import kotlin.reflect.KClass

/** See [specification](https://hl7.org/fhirpath/STU3/en/#sum--integer--long--decimal--quantity). */
@Suppress("UNCHECKED_CAST")
internal fun Collection<Any>.sumFun(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  if (isEmpty()) return emptyList()
  val converted = map { it.toFhirPathType(fhirPathTypeResolver) }
  val sum =
    when (val type = converted.singleType("sum")) {
      Int::class -> (converted as Collection<Int>).sum()
      Long::class -> (converted as Collection<Long>).sum()
      FhirPathDecimal::class ->
        (converted as Collection<FhirPathDecimal>).reduce(FhirPathDecimal::plus)
      FhirPathQuantity::class ->
        (converted as Collection<FhirPathQuantity>).reduce { a, b ->
          (a + b) ?: error("Cannot sum quantities with incompatible units: $a and $b")
        }
      else -> error("sum() cannot be applied to type ${type.simpleName}: ${converted.first()}")
    }
  return listOf(sum)
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#min--integer--long--decimal--quantity--date--datetime--time--string).
 */
internal fun Collection<Any>.minFun(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> =
  findMinOrMax(fhirPathTypeResolver, "min")

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#max--integer--long--decimal--quantity--date--datetime--time--string).
 */
internal fun Collection<Any>.maxFun(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> =
  findMinOrMax(fhirPathTypeResolver, "max")

/** See [specification](https://hl7.org/fhirpath/STU3/en/#avg--decimal--quantity). */
internal fun Collection<Any>.avgFun(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  if (isEmpty()) return emptyList()
  val converted = map { item ->
    when (val type = item.toFhirPathType(fhirPathTypeResolver)) {
      is Int,
      is Long -> (type as Number).toLong().toFhirPathDecimal()
      else -> type
    }
  }
  val count = size.toFhirPathDecimal()
  return listOf(
    when (val sum = converted.sumFun(fhirPathTypeResolver).single()) {
      is FhirPathDecimal -> sum / count
      is FhirPathQuantity -> sum.copy(value = sum.value?.div(count))
      else -> error("Unexpected sum type in avg(): $sum")
    }
  )
}

private fun Collection<Any>.singleType(functionName: String): KClass<out Any> =
  map { it::class }.distinct().singleOrNull()
    ?: error("All items in $functionName() input collection must be the same type")

private val comparableTypes =
  setOf(
    Int::class,
    Long::class,
    FhirPathDecimal::class,
    FhirPathQuantity::class,
    FhirPathDate::class,
    FhirPathDateTime::class,
    FhirPathTime::class,
    String::class,
  )

private fun Collection<Any>.findMinOrMax(
  fhirPathTypeResolver: FhirPathTypeResolver,
  functionName: String,
): Collection<Any> {
  if (isEmpty()) return emptyList()
  val converted = map { it.toFhirPathType(fhirPathTypeResolver) }
  // Explicitly validate type because single-item collections bypass comparator invocation in
  // min/max.
  check(converted.singleType(functionName) in comparableTypes) {
    "$functionName() cannot be applied to type ${converted.first()::class.simpleName}: ${converted.first()}"
  }
  val comparator =
    Comparator<Any> { a, b ->
      compare(a, b, fhirPathTypeResolver)
        ?: error("Items in collection are not comparable in $functionName(): $a and $b")
    }
  val result =
    when (functionName) {
      "min" -> converted.minWithOrNull(comparator)
      "max" -> converted.maxWithOrNull(comparator)
      else -> error("Unexpected function: $functionName")
    }
  return listOfNotNull(result)
}
