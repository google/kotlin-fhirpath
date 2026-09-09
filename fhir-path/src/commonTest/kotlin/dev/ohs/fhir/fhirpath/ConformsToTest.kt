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

import dev.ohs.fhir.model.r4.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

private val fhirPathEngine = FhirPathEngine.forR4()

private val patient: Resource =
  Json { ignoreUnknownKeys = true }
    .decodeFromString("""{"resourceType": "Patient", "name": [{"family": "Chalmers"}]}""")

class ConformsToTest {

  @Test
  fun `element conforms to its base data type profile`() {
    assertTrue(
      fhirPathEngine
        .evaluateExpression(
          "name.first().conformsTo('http://hl7.org/fhir/StructureDefinition/HumanName')",
          patient,
        )
        .single() as Boolean
    )
  }

  @Test
  fun `element does not conform to a different data type profile`() {
    assertFalse(
      fhirPathEngine
        .evaluateExpression(
          "name.first().conformsTo('http://hl7.org/fhir/StructureDefinition/Address')",
          patient,
        )
        .single() as Boolean
    )
  }

  @Test
  fun `unresolvable structure returns empty`() {
    // R4 requires an error here, but the current spec revised this to empty; see the kdoc.
    assertEquals(
      emptyList(),
      fhirPathEngine
        .evaluateExpression(
          "conformsTo('http://hl7.org/fhir/StructureDefinition/NotARealType')",
          patient,
        )
        .toList(),
    )
  }

  @Test
  fun `structure resolving only to a System type returns empty`() {
    // `String` is not a FHIR structure definition (FHIR's is lowercase `string`), so it is
    // unresolvable rather than falling back to the System type and returning false. The input
    // element is a FHIR string, so with the lowercase URL this would return true; the empty
    // result is about the structure, not the input.
    assertEquals(
      emptyList(),
      fhirPathEngine
        .evaluateExpression(
          "name.first().family.conformsTo('http://hl7.org/fhir/StructureDefinition/String')",
          patient,
        )
        .toList(),
    )
  }

  @Test
  fun `multiple input items returns empty`() {
    assertEquals(
      emptyList(),
      fhirPathEngine
        .evaluateExpression(
          "('a' | 'b').conformsTo('http://hl7.org/fhir/StructureDefinition/string')",
          patient,
        )
        .toList(),
    )
  }

  @Test
  fun `empty structure returns empty`() {
    assertEquals(
      emptyList(),
      fhirPathEngine.evaluateExpression("name.first().conformsTo({})", patient).toList(),
    )
  }

  @Test
  fun `empty input returns empty`() {
    assertEquals(
      emptyList(),
      fhirPathEngine
        .evaluateExpression(
          "{}.conformsTo('http://hl7.org/fhir/StructureDefinition/Patient')",
          patient,
        )
        .toList(),
    )
  }
}
