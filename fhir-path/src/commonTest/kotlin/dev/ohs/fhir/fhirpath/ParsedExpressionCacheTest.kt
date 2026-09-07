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

import dev.ohs.fhir.model.r4.HumanName
import dev.ohs.fhir.model.r4.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

private val jsonR4 = Json { ignoreUnknownKeys = true }

// Exceeds the engine's internal cache bound so that the overflow reset is exercised.
private const val OVERFLOW_EXPRESSION_COUNT = 1_200

private const val PATIENT_JSON =
  """{
  "resourceType": "Patient",
  "id": "example",
  "gender": "male",
  "birthDate": "1974-12-25",
  "name": [
    {
      "use": "official",
      "family": "Chalmers",
      "given": [
        "Peter",
        "James"
      ]
    }
  ]
}"""

private const val HUMAN_NAME_JSON =
  """{
  "use": "maiden",
  "family": "Windsor",
  "given": [
    "Jim"
  ]
}"""

private val expressions =
  listOf("name.given", "name.family", "name.use", "gender", "birthDate", "name.given.first()")

class ParsedExpressionCacheTest {
  private val patient: Resource = jsonR4.decodeFromString(PATIENT_JSON)
  private val humanName: HumanName = jsonR4.decodeFromString(HUMAN_NAME_JSON)

  @Test
  fun `caching engine returns the same results as a non-caching engine`() {
    val cachingEngine = FhirPathEngine.forR4(cacheParsedExpressions = true)
    val nonCachingEngine = FhirPathEngine.forR4()

    repeat(3) {
      for (expression in expressions) {
        assertEquals(
          nonCachingEngine.evaluateExpression(expression, patient).toList(),
          cachingEngine.evaluateExpression(expression, patient).toList(),
        )
      }
    }
  }

  @Test
  fun `cached expression stays correct across bases of different types`() {
    val engine = FhirPathEngine.forR4(cacheParsedExpressions = true)

    assertEquals(listOf("Jim"), engine.evaluateExpression("given", humanName).toList())
    assertEquals(emptyList(), engine.evaluateExpression("given", patient).toList())
    assertEquals(listOf("Jim"), engine.evaluateExpression("given", humanName).toList())

    assertEquals(
      listOf("Peter", "James"),
      engine.evaluateExpression("name.given", patient).toList(),
    )
    assertEquals(emptyList(), engine.evaluateExpression("name.given", humanName).toList())
    assertEquals(
      listOf("Peter", "James"),
      engine.evaluateExpression("name.given", patient).toList(),
    )
  }

  @Test
  fun `evaluation stays correct once the cache exceeds its bound`() {
    val engine = FhirPathEngine.forR4(cacheParsedExpressions = true)

    repeat(OVERFLOW_EXPRESSION_COUNT) {
      assertEquals(listOf("value$it"), engine.evaluateExpression("'value$it'", patient).toList())
    }

    assertEquals(
      listOf("Peter", "James"),
      engine.evaluateExpression("name.given", patient).toList(),
    )
  }

  @Test
  fun `disabling the cache does not change results`() {
    val engine = FhirPathEngine.forR4(cacheParsedExpressions = false)

    repeat(3) {
      assertEquals(
        listOf("Peter", "James"),
        engine.evaluateExpression("name.given", patient).toList(),
      )
    }
    assertTrue(engine.evaluateExpression("name.suffix", patient).isEmpty())
  }
}
