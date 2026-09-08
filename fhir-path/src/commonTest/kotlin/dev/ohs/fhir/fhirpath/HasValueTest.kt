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

import dev.ohs.fhir.model.r4.CodeableConcept
import dev.ohs.fhir.model.r4.DateTime
import dev.ohs.fhir.model.r4.Enumeration
import dev.ohs.fhir.model.r4.Extension
import dev.ohs.fhir.model.r4.Quantity
import dev.ohs.fhir.model.r4.String as FhirString
import dev.ohs.fhir.model.r4.Task
import kotlin.test.Test
import kotlin.test.assertEquals

private val fhirPathEngine = FhirPathEngine.forR4()

private val task =
  Task(
    status = Enumeration(value = Task.TaskStatus.Completed),
    intent =
      Enumeration(
        id = "no-value",
        extension =
          listOf(
            Extension(
              url = "https://example.org/x",
              value = Extension.Value.String(FhirString(value = "e")),
            )
          ),
        value = null,
      ),
    code = CodeableConcept(text = FhirString(value = "w")),
    authoredOn =
      DateTime(
        id = "no-value",
        extension =
          listOf(
            Extension(
              url = "https://example.org/x",
              value = Extension.Value.String(FhirString(value = "e")),
            )
          ),
        value = null,
      ),
    input =
      listOf(
        Task.Input(
          type = CodeableConcept(text = FhirString(value = "w")),
          value = Task.Input.Value.Quantity(Quantity()),
        )
      ),
  )

class HasValueTest {

  @Test
  fun `empty collection returns empty`() {
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("{}.hasValue()", task).toList())
  }

  @Test
  fun `system literal returns empty`() {
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("'hello'.hasValue()", task).toList(),
    )
  }

  @Test
  fun `complex type without value property returns empty`() {
    assertEquals(emptyList(), fhirPathEngine.evaluateExpression("code.hasValue()", task).toList())
  }

  @Test
  fun `complex type with value property returns empty`() {
    // hasValue() only applies to FHIR primitives; complex types like Quantity return empty even if
    // they have a value property.
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("input.value.hasValue()", task).toList(),
    )
  }

  @Test
  fun `primitive without value returns false`() {
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("authoredOn.hasValue()", task).toList(),
    )
  }

  @Test
  fun `enumeration without value returns false`() {
    assertEquals(
      listOf(false),
      fhirPathEngine.evaluateExpression("intent.hasValue()", task).toList(),
    )
  }

  @Test
  fun `primitive with value returns true`() {
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("code.text.hasValue()", task).toList(),
    )
  }

  @Test
  fun `enumeration with value returns true`() {
    assertEquals(
      listOf(true),
      fhirPathEngine.evaluateExpression("status.hasValue()", task).toList(),
    )
  }

  @Test
  fun `collection with multiple items returns empty`() {
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("status.combine(status).hasValue()", task).toList(),
    )
  }
}
