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
import kotlin.test.Test
import kotlin.test.assertEquals

private val fhirPathEngine = FhirPathEngine.forR4()

class ToQuantityTest {

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
}
