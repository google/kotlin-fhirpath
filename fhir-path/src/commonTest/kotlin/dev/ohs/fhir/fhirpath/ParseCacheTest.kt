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

import dev.ohs.fhir.model.r4.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

private val jsonR4 = Json { ignoreUnknownKeys = true }

private const val PATIENT_JSON =
  """{"resourceType":"Patient","id":"example","name":[{"family":"Chalmers","given":["Peter"]}]}"""

/** Comfortably above the engine's cache bound so the overflow path is exercised. */
private const val DISTINCT_EXPRESSIONS = 2_000

class ParseCacheTest {

  @Test
  fun `evaluation stays correct past the cache bound`() {
    val engine = FhirPathEngine.forR4()
    val patient: Resource = jsonR4.decodeFromString(PATIENT_JSON)

    repeat(DISTINCT_EXPRESSIONS) {
      assertEquals(listOf("v$it"), engine.evaluateExpression("'v$it'", null).toList())
    }

    assertEquals(listOf("Peter"), engine.evaluateExpression("Patient.name.given", patient).toList())
    assertEquals(listOf("v0"), engine.evaluateExpression("'v0'", null).toList())
  }
}
