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

  @Test
  fun `toDate on dateTime returns date portion`() {
    val result = fhirPathEngine.evaluateExpression("@2025-05-10T14:30:00.000.toDate()", null)
    assertEquals(listOf(FhirPathDate(2025, 5, 10)), result.toList())
  }

  @Test
  fun `toQuantity with unit conversion`() {
    val result = fhirPathEngine.evaluateExpression("'5 \\'kg\\''.toQuantity('g')", null)
    assertEquals(listOf(FhirPathQuantity(FhirPathDecimal.fromInt(5000), "'g'")), result.toList())
  }

  @Test
  fun `toQuantity with calendar duration unit conversion`() {
    val result = fhirPathEngine.evaluateExpression("'1 hour'.toQuantity('minute')", null)
    assertEquals(listOf(FhirPathQuantity(FhirPathDecimal.fromInt(60), "minute")), result.toList())
  }

  @Test
  fun `toLong on Long value`() {
    val result =
      fhirPathEngine.evaluateExpression("%val.toLong()", null, mapOf("val" to 1234567890123L))
    assertEquals(listOf(1234567890123L), result.toList())
  }

  @Test
  fun `toLong on Integer value`() {
    val result = fhirPathEngine.evaluateExpression("42.toLong()", null)
    assertEquals(listOf(42L), result.toList())
  }

  @Test
  fun `toLong on String value`() {
    val result = fhirPathEngine.evaluateExpression("'1234567890123'.toLong()", null)
    assertEquals(listOf(1234567890123L), result.toList())
  }

  @Test
  fun `toLong on invalid String value`() {
    val result = fhirPathEngine.evaluateExpression("'not_a_number'.toLong()", null)
    assertEquals(emptyList(), result.toList())
  }

  @Test
  fun `toLong on Boolean value`() {
    val trueResult = fhirPathEngine.evaluateExpression("true.toLong()", null)
    assertEquals(listOf(1L), trueResult.toList())

    val falseResult = fhirPathEngine.evaluateExpression("false.toLong()", null)
    assertEquals(listOf(0L), falseResult.toList())
  }

  @Test
  fun `convertsToLong on convertible values`() {
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("42.convertsToLong()", null).toList(),
    )
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("'1234567890123'.convertsToLong()", null).toList(),
    )
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("true.convertsToLong()", null).toList(),
    )
    assertEquals(
      listOf(true),
      fhirPathEngine
        .evaluateExpression("%val.convertsToLong()", null, mapOf("val" to 100L))
        .toList(),
    )
  }

  @Test
  fun `convertsToLong on non-convertible values`() {
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("'abc'.convertsToLong()", null).toList(),
    )
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("@2020-01-01.convertsToLong()", null).toList(),
    )
  }

  @Test
  fun `convertsToLong on empty collection`() {
    val result = fhirPathEngine.evaluateExpression("{}.convertsToLong()", null)
    assertEquals(emptyList(), result.toList())
  }
}
