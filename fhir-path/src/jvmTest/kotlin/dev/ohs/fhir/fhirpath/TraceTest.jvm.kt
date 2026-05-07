/*
 * Copyright 2025-2026 Google LLC
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

package com.google.fhir.fhirpath

import com.google.fhir.model.r4.FhirR4Json
import com.google.fhir.model.r4.Resource
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

private val jsonR4 = FhirR4Json { ignoreUnknownKeys = true }

private val engine = FhirPathEngine.forR4()

private fun loadPatient(): Resource {
  val json =
    File(
        "${System.getProperty("projectRootDir")}/third_party/fhir-test-cases/r4/resources/patient-example.json"
      )
      .readText()
  return jsonR4.decodeFromString(json)
}

class TraceTest {

  @Test
  fun `trace captures values without projection`() {
    val patient = loadPatient()
    engine.evaluateExpression("name.trace('names').given", patient)

    assertTrue(engine.traces.containsKey("names"))
    val entries = engine.traces["names"]!!
    assertEquals(3, entries.size)
  }

  @Test
  fun `trace captures values with projection`() {
    val patient = loadPatient()
    engine.evaluateExpression("name.trace('families', family).given", patient)

    val entries = engine.traces["families"]!!
    val values = entries.map { it.value }
    assertTrue(values.contains("Chalmers"))
    assertTrue(values.contains("Windsor"))
  }

  @Test
  fun `trace paths are derived from expression`() {
    val patient = loadPatient()
    engine.evaluateExpression("name.trace('names')", patient)

    val entries = engine.traces["names"]!!
    assertEquals("Patient.name[0]", entries[0].path)
    assertEquals("Patient.name[1]", entries[1].path)
    assertEquals("Patient.name[2]", entries[2].path)
  }

  @Test
  fun `trace with deeper path includes full expression`() {
    val patient = loadPatient()
    engine.evaluateExpression("name.given.trace('givens')", patient)

    val entries = engine.traces["givens"]!!
    assertTrue(entries.isNotEmpty())
    assertTrue(entries[0].path.startsWith("Patient.name.given["))
  }

  @Test
  fun `multiple traces with different labels`() {
    val patient = loadPatient()
    engine.evaluateExpression(
      "name.trace('all').where(use = 'official').trace('official').given",
      patient,
    )

    assertTrue(engine.traces.containsKey("all"))
    assertTrue(engine.traces.containsKey("official"))
    assertEquals(3, engine.traces["all"]!!.size)
    assertEquals(1, engine.traces["official"]!!.size)
  }

  @Test
  fun `no trace in expression produces empty traces map`() {
    val patient = loadPatient()
    engine.evaluateExpression("name.given", patient)

    assertTrue(engine.traces.isEmpty())
  }

  @Test
  fun `trace does not alter evaluation result`() {
    val patient = loadPatient()
    val withTrace = engine.evaluateExpression("name.trace('t').given.count()", patient)
    val withoutTrace = engine.evaluateExpression("name.given.count()", patient)

    assertEquals(withoutTrace.toList(), withTrace.toList())
  }

  @Test
  fun `traces are cleared between evaluations`() {
    val patient = loadPatient()
    engine.evaluateExpression("name.trace('first')", patient)
    assertTrue(engine.traces.containsKey("first"))

    engine.evaluateExpression("name.given.count()", patient)
    assertTrue(engine.traces.isEmpty())
  }
}
