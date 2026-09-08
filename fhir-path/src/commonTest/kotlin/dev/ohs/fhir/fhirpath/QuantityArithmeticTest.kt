/*
 * Copyright 2026 Open Health Stack Foundation
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

import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.model.r4.Patient
import kotlin.test.Test
import kotlin.test.assertEquals

class QuantityArithmeticTest {
  private val fhirPathEngine = FhirPathEngine.forR4()
  private val emptyContext = Patient()

  @Test
  fun testQuantityAdditionSameUnit() {
    val result = fhirPathEngine.evaluateExpression("5 'mg' + 3 'mg'", emptyContext)
    assertEquals(1, result.size)
    val quantity = result.single() as FhirPathQuantity
    assertEquals(FhirPathDecimal.fromInt(8), quantity.value)
    assertEquals("'mg'", quantity.unit)
  }

  @Test
  fun testQuantityAdditionCompatibleUnits() {
    val result = fhirPathEngine.evaluateExpression("1 'kg' + 500 'g'", emptyContext)
    assertEquals(1, result.size)
    val quantity = result.single() as FhirPathQuantity
    assertEquals(FhirPathDecimal.fromString("1.5"), quantity.value)
    assertEquals("'kg'", quantity.unit)
  }

  @Test
  fun testQuantitySubtractionCompatibleUnits() {
    val result = fhirPathEngine.evaluateExpression("1 'kg' - 500 'g'", emptyContext)
    assertEquals(1, result.size)
    val quantity = result.single() as FhirPathQuantity
    assertEquals(FhirPathDecimal.fromString("0.5"), quantity.value)
    assertEquals("'kg'", quantity.unit)
  }

  @Test
  fun testQuantityAdditionIncompatibleUnits() {
    val result = fhirPathEngine.evaluateExpression("1 'kg' + 1 'm'", emptyContext)
    assertEquals(0, result.size)
  }
}
