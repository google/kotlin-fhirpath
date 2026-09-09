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

package dev.ohs.fhir.fhirpath

import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AggregateTest :
  FunSpec({
    val engine = FhirPathEngine.forR4()

    test("aggregate keeps inner total independent from outer total") {
      val result =
        engine.evaluateExpression(
          "(1 | 2).aggregate((10 | 20 | 30).aggregate(\$total + \$this, 0) + \$total + \$this, 0)",
          null,
        )
      assertEquals(listOf(123), result.toList())
    }

    test("sum returns empty on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.sum()", null))
    }

    test("sum adds integers") {
      assertEquals(listOf(6), engine.evaluateExpression("(1 | 2 | 3).sum()", null))
    }

    test("sum adds decimals") {
      assertEquals(
        listOf(FhirPathDecimal.fromString("3.5")),
        engine.evaluateExpression("(1.0 | 2.5).sum()", null),
      )
    }

    test("sum adds quantities") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromInt(15), "'m'")),
        engine.evaluateExpression("(10 'm' | 5 'm').sum()", null),
      )
    }

    test("sum adds quantities with compatible units") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromString("1.5"), "'kg'")),
        engine.evaluateExpression("(1 'kg' | 500 'g').sum()", null),
      )
    }

    test("sum fails on mixed types") {
      assertFailsWith<Exception> { engine.evaluateExpression("(1 | 2.5).sum()", null) }
    }

    test("sum fails on non-numeric types") {
      assertFailsWith<Exception> { engine.evaluateExpression("('a' | 'b').sum()", null) }
    }

    test("sum fails on quantities in incompatible units") {
      assertFailsWith<Exception> { engine.evaluateExpression("(10 'm' | 5 's').sum()", null) }
    }

    test("min returns empty on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.min()", null))
    }

    test("min finds smallest number") {
      assertEquals(listOf(1), engine.evaluateExpression("(3 | 1 | 2).min()", null))
    }

    test("min finds smallest string") {
      assertEquals(listOf("a"), engine.evaluateExpression("('b' | 'a' | 'c').min()", null))
    }

    test("min finds earliest date") {
      assertEquals(
        listOf(FhirPathDate(2022, 1, 1)),
        engine.evaluateExpression("(@2023-05-15 | @2022-01-01).min()", null),
      )
    }

    test("min finds smallest quantity") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromInt(5), "'m'")),
        engine.evaluateExpression("(10 'm' | 5 'm').min()", null),
      )
    }

    test("min finds smallest quantity with compatible units") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromInt(500), "'g'")),
        engine.evaluateExpression("(1 'kg' | 500 'g').min()", null),
      )
    }

    test("min fails on mixed types") {
      assertFailsWith<Exception> { engine.evaluateExpression("(1 | 2.5).min()", null) }
    }

    test("min fails on non-comparable types") {
      assertFailsWith<Exception> { engine.evaluateExpression("(true).min()", null) }
    }

    test("min fails on quantities in incompatible units") {
      assertFailsWith<Exception> { engine.evaluateExpression("(10 'm' | 5 's').min()", null) }
    }

    test("max returns empty on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.max()", null))
    }

    test("max finds largest number") {
      assertEquals(listOf(3), engine.evaluateExpression("(3 | 1 | 2).max()", null))
    }

    test("max finds largest string") {
      assertEquals(listOf("c"), engine.evaluateExpression("('b' | 'a' | 'c').max()", null))
    }

    test("max finds latest date") {
      assertEquals(
        listOf(FhirPathDate(2023, 5, 15)),
        engine.evaluateExpression("(@2023-05-15 | @2022-01-01).max()", null),
      )
    }

    test("max finds largest quantity") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromInt(10), "'m'")),
        engine.evaluateExpression("(10 'm' | 5 'm').max()", null),
      )
    }

    test("max finds largest quantity with compatible units") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromInt(1), "'kg'")),
        engine.evaluateExpression("(1 'kg' | 500 'g').max()", null),
      )
    }

    test("max fails on mixed types") {
      assertFailsWith<Exception> { engine.evaluateExpression("(1 | 2.5).max()", null) }
    }

    test("max fails on non-comparable types") {
      assertFailsWith<Exception> { engine.evaluateExpression("(true).max()", null) }
    }

    test("max fails on quantities in incompatible units") {
      assertFailsWith<Exception> { engine.evaluateExpression("(10 'm' | 5 's').max()", null) }
    }

    test("avg returns empty on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.avg()", null))
    }

    test("avg averages integers to decimal") {
      assertEquals(
        listOf(FhirPathDecimal.fromInt(2)),
        engine.evaluateExpression("(1 | 2 | 3).avg()", null),
      )
    }

    test("avg averages decimals") {
      assertEquals(
        listOf(FhirPathDecimal.fromString("1.75")),
        engine.evaluateExpression("(1.0 | 2.5).avg()", null),
      )
    }

    test("avg averages mixed integer and decimal") {
      assertEquals(
        listOf(FhirPathDecimal.fromString("1.75")),
        engine.evaluateExpression("(1 | 2.5).avg()", null),
      )
    }

    test("avg averages quantities") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromInt(15), "'m'")),
        engine.evaluateExpression("(10 'm' | 20 'm').avg()", null),
      )
    }

    test("avg averages quantities with compatible units") {
      assertEquals(
        listOf(FhirPathQuantity(FhirPathDecimal.fromString("0.75"), "'kg'")),
        engine.evaluateExpression("(1 'kg' | 500 'g').avg()", null),
      )
    }

    test("avg fails on mixed numeric and quantity") {
      assertFailsWith<Exception> { engine.evaluateExpression("(1 | 2 'm').avg()", null) }
    }

    test("avg fails on non-numeric types") {
      assertFailsWith<Exception> { engine.evaluateExpression("('a' | 'b').avg()", null) }
    }

    test("avg fails on quantities in incompatible units") {
      assertFailsWith<Exception> { engine.evaluateExpression("(10 'm' | 5 's').avg()", null) }
    }
  })
