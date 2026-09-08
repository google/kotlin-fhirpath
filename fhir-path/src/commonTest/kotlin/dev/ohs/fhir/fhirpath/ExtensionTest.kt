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
import dev.ohs.fhir.model.r4.Enumeration
import dev.ohs.fhir.model.r4.Extension
import dev.ohs.fhir.model.r4.Observation
import dev.ohs.fhir.model.r4.String as FhirString
import kotlin.test.Test
import kotlin.test.assertEquals

private val fhirPathEngine = FhirPathEngine.forR4()

class ExtensionTest {

  @Test
  fun `extension on enumeration with value is accessible`() {
    val observation =
      Observation(
        status =
          Enumeration(
            value = Observation.ObservationStatus.Final,
            extension =
              listOf(
                Extension(
                  url = "https://example.org/x",
                  value = Extension.Value.String(FhirString(value = "e")),
                )
              ),
          ),
        code = CodeableConcept(text = FhirString(value = "w")),
      )
    assertEquals(
      listOf("e"),
      fhirPathEngine
        .evaluateExpression("status.extension('https://example.org/x').value", observation)
        .toList(),
    )
  }

  @Test
  fun `extension on enumeration without value is accessible`() {
    val observation =
      Observation(
        status =
          Enumeration(
            value = null,
            extension =
              listOf(
                Extension(
                  url = "https://example.org/x",
                  value = Extension.Value.String(FhirString(value = "e")),
                )
              ),
          ),
        code = CodeableConcept(text = FhirString(value = "w")),
      )
    assertEquals(
      listOf("e"),
      fhirPathEngine
        .evaluateExpression("status.extension('https://example.org/x').value", observation)
        .toList(),
    )
  }
}
