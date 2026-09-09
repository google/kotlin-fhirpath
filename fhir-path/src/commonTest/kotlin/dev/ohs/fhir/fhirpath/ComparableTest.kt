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

import dev.ohs.fhir.model.r4.Decimal
import dev.ohs.fhir.model.r4.FhirDecimal
import dev.ohs.fhir.model.r4.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals

private val fhirPathEngine = FhirPathEngine.forR4()

class ComparableTest {

  @Test
  fun `empty input returns empty`() {
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("{}.comparable(1 'kg')", null).toList(),
    )
  }

  @Test
  fun `quantity without a code returns empty`() {
    val quantity = Quantity(value = Decimal(value = FhirDecimal.fromString("5")))
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("comparable(1 'kg')", quantity).toList(),
    )
  }

  @Test
  fun `calendar duration comparable to UCUM duration returns false`() {
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("(1 year).comparable(1 'a')", null).toList(),
    )
  }

  @Test
  fun `quantities with different dimensions return false`() {
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("(1 'kg').comparable(1 's')", null).toList(),
    )
  }

  @Test
  fun `quantities with same dimension return true`() {
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("(1 'kg').comparable(1 'g')", null).toList(),
    )
  }

  @Test
  fun `identical unknown units comparable returns true`() {
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("(1 '[s]').comparable(1 '[s]')", null).toList(),
    )
  }
}
