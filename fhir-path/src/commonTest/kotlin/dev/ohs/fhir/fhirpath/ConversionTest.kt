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
import kotlin.test.Test
import kotlin.test.assertEquals

private val fhirPathEngine = FhirPathEngine.forR4()

class ConversionTest {

  // toInteger / convertsToInteger

  @Test
  fun `toInteger on empty collection`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("{}.toInteger()", null).toList())

  @Test
  fun `toInteger on Integer value`() =
    assertEquals(listOf(42), fhirPathEngine.evaluateExpression("42.toInteger()", null).toList())

  @Test
  fun `toInteger on Long value within Int range`() =
    assertEquals(listOf(12345), fhirPathEngine.evaluateExpression("toInteger()", 12345L).toList())

  @Test
  fun `toInteger on Long value exceeding Int range`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("toInteger()", 3000000000L).toList(),
    )

  @Test
  fun `toInteger on String value`() =
    assertEquals(listOf(42), fhirPathEngine.evaluateExpression("'42'.toInteger()", null).toList())

  @Test
  fun `toInteger on invalid String value`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'not_a_number'.toInteger()", null).toList(),
    )

  @Test
  fun `toInteger on Boolean true`() =
    assertEquals(listOf(1), fhirPathEngine.evaluateExpression("true.toInteger()", null).toList())

  @Test
  fun `toInteger on Boolean false`() =
    assertEquals(listOf(0), fhirPathEngine.evaluateExpression("false.toInteger()", null).toList())

  @Test
  fun `toInteger on incompatible type`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("@2020-01-01.toInteger()", null).toList(),
    )

  @Test
  fun `convertsToInteger on empty collection`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("{}.convertsToInteger()", null).toList(),
    )

  @Test
  fun `convertsToInteger on Integer value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("42.convertsToInteger()", null).toList(),
    )

  @Test
  fun `convertsToInteger on Long value within Int range`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("convertsToInteger()", 12345L).toList(),
    )

  @Test
  fun `convertsToInteger on Long value exceeding Int range`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("convertsToInteger()", 3000000000L).toList(),
    )

  @Test
  fun `convertsToInteger on String value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'42'.convertsToInteger()", null).toList(),
    )

  @Test
  fun `convertsToInteger on invalid String value`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'abc'.convertsToInteger()", null).toList(),
    )

  @Test
  fun `convertsToInteger on Boolean value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("true.convertsToInteger()", null).toList(),
    )

  @Test
  fun `convertsToInteger on incompatible type`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("@2020-01-01.convertsToInteger()", null).toList(),
    )

  // toLong / convertsToLong

  @Test
  fun `toLong on empty collection`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("{}.toLong()", null).toList())

  @Test
  fun `toLong on Long value`() =
    assertEquals(
      listOf(1234567890123L),
      fhirPathEngine.evaluateExpression("toLong()", 1234567890123L).toList(),
    )

  @Test
  fun `toLong on Integer value`() =
    assertEquals(listOf(42L), fhirPathEngine.evaluateExpression("42.toLong()", null).toList())

  @Test
  fun `toLong on String value`() =
    assertEquals(
      listOf(1234567890123L),
      fhirPathEngine.evaluateExpression("'1234567890123'.toLong()", null).toList(),
    )

  @Test
  fun `toLong on invalid String value`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'not_a_number'.toLong()", null).toList(),
    )

  @Test
  fun `toLong on Boolean true`() =
    assertEquals(listOf(1L), fhirPathEngine.evaluateExpression("true.toLong()", null).toList())

  @Test
  fun `toLong on Boolean false`() =
    assertEquals(listOf(0L), fhirPathEngine.evaluateExpression("false.toLong()", null).toList())

  @Test
  fun `toLong on incompatible type`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("@2020-01-01.toLong()", null).toList(),
    )

  @Test
  fun `convertsToLong on empty collection`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("{}.convertsToLong()", null).toList(),
    )

  @Test
  fun `convertsToLong on Long value`() =
    assertEquals(listOf(true), fhirPathEngine.evaluateExpression("convertsToLong()", 100L).toList())

  @Test
  fun `convertsToLong on Integer value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("42.convertsToLong()", null).toList(),
    )

  @Test
  fun `convertsToLong on String value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'1234567890123'.convertsToLong()", null).toList(),
    )

  @Test
  fun `convertsToLong on invalid String value`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'abc'.convertsToLong()", null).toList(),
    )

  @Test
  fun `convertsToLong on Boolean value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("true.convertsToLong()", null).toList(),
    )

  @Test
  fun `convertsToLong on incompatible type`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("@2020-01-01.convertsToLong()", null).toList(),
    )

  // toDate / convertsToDate

  @Test
  fun `toDate on empty collection`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("{}.toDate()", null).toList())

  @Test
  fun `toDate on Date value`() =
    assertEquals(
      listOf(FhirPathDate(2025, 5, 10)),
      fhirPathEngine.evaluateExpression("@2025-05-10.toDate()", null).toList(),
    )

  @Test
  fun `toDate on DateTime value returns date portion`() =
    assertEquals(
      listOf(FhirPathDate(2025, 5, 10)),
      fhirPathEngine.evaluateExpression("@2025-05-10T14:30:00.000.toDate()", null).toList(),
    )

  @Test
  fun `toDate on String value`() =
    assertEquals(
      listOf(FhirPathDate(2025, 5, 10)),
      fhirPathEngine.evaluateExpression("'2025-05-10'.toDate()", null).toList(),
    )

  @Test
  fun `toDate on invalid String value`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'not_a_date'.toDate()", null).toList(),
    )

  @Test
  fun `toDate on incompatible type`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("42.toDate()", null).toList())

  @Test
  fun `convertsToDate on empty collection`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("{}.convertsToDate()", null).toList(),
    )

  @Test
  fun `convertsToDate on Date value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("@2025-05-10.convertsToDate()", null).toList(),
    )

  @Test
  fun `convertsToDate on DateTime value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("@2025-05-10T14:30:00.000.convertsToDate()", null).toList(),
    )

  @Test
  fun `convertsToDate on String value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'2025-05-10'.convertsToDate()", null).toList(),
    )

  @Test
  fun `convertsToDate on invalid String value`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'not_a_date'.convertsToDate()", null).toList(),
    )

  @Test
  fun `convertsToDate on incompatible type`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("42.convertsToDate()", null).toList(),
    )

  // toDecimal / convertsToDecimal

  @Test
  fun `toDecimal on empty collection`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("{}.toDecimal()", null).toList())

  @Test
  fun `toDecimal on Decimal value`() =
    assertEquals(
      listOf(FhirPathDecimal.fromString("123.45")),
      fhirPathEngine.evaluateExpression("123.45.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on Integer value`() =
    assertEquals(
      listOf(FhirPathDecimal.fromInt(42)),
      fhirPathEngine.evaluateExpression("42.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on Long value`() =
    assertEquals(
      listOf(FhirPathDecimal.fromLong(1234567890123L)),
      fhirPathEngine.evaluateExpression("toDecimal()", 1234567890123L).toList(),
    )

  @Test
  fun `toDecimal on Boolean true`() =
    assertEquals(
      listOf(FhirPathDecimal.fromInt(1)),
      fhirPathEngine.evaluateExpression("true.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on Boolean false`() =
    assertEquals(
      listOf(FhirPathDecimal.fromInt(0)),
      fhirPathEngine.evaluateExpression("false.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on String value preserving precision`() =
    assertEquals(
      listOf(FhirPathDecimal.fromString("12345678901234567890.12345678901234567890")),
      fhirPathEngine
        .evaluateExpression("'12345678901234567890.12345678901234567890'.toDecimal()", null)
        .toList(),
    )

  @Test
  fun `toDecimal on String value preserving scale with trailing zeros`() {
    val result = fhirPathEngine.evaluateExpression("'1.500'.toDecimal()", null).toList()
    assertEquals(listOf(FhirPathDecimal.fromString("1.500")), result)
    assertEquals(3L, (result.single() as FhirPathDecimal).decimalPlaces)
  }

  @Test
  fun `toDecimal on signed String value`() =
    assertEquals(
      listOf(FhirPathDecimal.fromString("-123.45")),
      fhirPathEngine.evaluateExpression("'-123.45'.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on invalid String value with non-numeric characters`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'not_a_number'.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on invalid String value with scientific notation`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("'1e5'.toDecimal()", null).toList())

  @Test
  fun `toDecimal on invalid String value with multiple decimal points`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'1.2.3'.toDecimal()", null).toList(),
    )

  @Test
  fun `toDecimal on incompatible type`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("@2020-01-01.toDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on empty collection`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("{}.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on Decimal value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("123.45.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on Integer value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("42.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on Long value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("convertsToDecimal()", 1234567890123L).toList(),
    )

  @Test
  fun `convertsToDecimal on Boolean value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("true.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on String value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'123.45'.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on invalid String value with non-numeric characters`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'abc'.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on invalid String value with scientific notation`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'1e5'.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on invalid String value with multiple decimal points`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'1.2.3'.convertsToDecimal()", null).toList(),
    )

  @Test
  fun `convertsToDecimal on incompatible type`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("@2020-01-01.convertsToDecimal()", null).toList(),
    )

  // toQuantity / convertsToQuantity

  @Test
  fun `toQuantity on empty collection`() =
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("{}.toQuantity()", null).toList())

  @Test
  fun `toQuantity on Quantity with unit conversion`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromInt(5000), "'g'")),
      fhirPathEngine.evaluateExpression("'5 \\'kg\\''.toQuantity('g')", null).toList(),
    )

  @Test
  fun `toQuantity on Quantity with incompatible unit conversion`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'5 \\'kg\\''.toQuantity('cm')", null).toList(),
    )

  @Test
  fun `toQuantity on Quantity with calendar duration unit conversion`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromInt(60), "minute")),
      fhirPathEngine.evaluateExpression("'1 hour'.toQuantity('minute')", null).toList(),
    )

  @Test
  fun `toQuantity on Integer value`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromInt(5), "'1'")),
      fhirPathEngine.evaluateExpression("5.toQuantity()", null).toList(),
    )

  @Test
  fun `toQuantity on Long value`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromLong(5L), "'1'")),
      fhirPathEngine.evaluateExpression("toQuantity()", 5L).toList(),
    )

  @Test
  fun `toQuantity on Decimal value`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromString("5.5"), "'1'")),
      fhirPathEngine.evaluateExpression("5.5.toQuantity()", null).toList(),
    )

  @Test
  fun `toQuantity on Boolean true`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromInt(1), "'1'")),
      fhirPathEngine.evaluateExpression("true.toQuantity()", null).toList(),
    )

  @Test
  fun `toQuantity on String value`() =
    assertEquals(
      listOf(FhirPathQuantity(FhirPathDecimal.fromString("5.5"), "'mg'")),
      fhirPathEngine.evaluateExpression("'5.5 \\'mg\\''.toQuantity()", null).toList(),
    )

  @Test
  fun `toQuantity on invalid String value`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'invalid_qty'.toQuantity()", null).toList(),
    )

  @Test
  fun `toQuantity on incompatible type`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("@2020-01-01.toQuantity()", null).toList(),
    )

  @Test
  fun `convertsToQuantity on empty collection`() =
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("{}.convertsToQuantity()", null).toList(),
    )

  @Test
  fun `convertsToQuantity on Quantity value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'5 \\'kg\\''.convertsToQuantity()", null).toList(),
    )

  @Test
  fun `convertsToQuantity on Integer value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("5.convertsToQuantity()", null).toList(),
    )

  @Test
  fun `convertsToQuantity on String value`() =
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'5.5 \\'mg\\''.convertsToQuantity()", null).toList(),
    )

  @Test
  fun `convertsToQuantity on invalid String value`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'invalid_qty'.convertsToQuantity()", null).toList(),
    )

  @Test
  fun `convertsToQuantity on incompatible type`() =
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("@2020-01-01.convertsToQuantity()", null).toList(),
    )
}
