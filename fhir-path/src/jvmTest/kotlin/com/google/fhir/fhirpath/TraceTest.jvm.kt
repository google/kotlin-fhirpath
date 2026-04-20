/*
 * Copyright 2025 Google LLC
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
    val result = evaluateFhirPathWithTraces("name.trace('names').given", patient)

    assertTrue(result.traces.containsKey("names"))
    val traceInvocations = result.traces["names"]!!
    assertEquals(1, traceInvocations.size)
    assertEquals(3, traceInvocations[0].size)
  }

  @Test
  fun `trace captures values with projection`() {
    val patient = loadPatient()
    val result = evaluateFhirPathWithTraces("name.trace('families', family).given", patient)

    val traceInvocations = result.traces["families"]!!
    assertEquals(1, traceInvocations.size)
    val values = traceInvocations[0].map { it.value }
    assertTrue(values.contains("Chalmers"))
    assertTrue(values.contains("Windsor"))
  }

  @Test
  fun `trace paths are derived from expression`() {
    val patient = loadPatient()
    val result = evaluateFhirPathWithTraces("name.trace('names')", patient)

    val entries = result.traces["names"]!![0]
    assertEquals("Patient.name[0]", entries[0].path)
    assertEquals("Patient.name[1]", entries[1].path)
    assertEquals("Patient.name[2]", entries[2].path)
  }

  @Test
  fun `trace with deeper path includes full expression`() {
    val patient = loadPatient()
    val result = evaluateFhirPathWithTraces("name.given.trace('givens')", patient)

    val entries = result.traces["givens"]!![0]
    assertTrue(entries.isNotEmpty())
    assertTrue(entries[0].path.startsWith("Patient.name.given["))
  }

  @Test
  fun `multiple traces with different labels`() {
    val patient = loadPatient()
    val result =
      evaluateFhirPathWithTraces(
        "name.trace('all').where(use = 'official').trace('official').given",
        patient,
      )

    assertTrue(result.traces.containsKey("all"))
    assertTrue(result.traces.containsKey("official"))
    assertEquals(3, result.traces["all"]!![0].size)
    assertEquals(1, result.traces["official"]!![0].size)
  }

  @Test
  fun `no trace in expression produces empty traces map`() {
    val patient = loadPatient()
    val result = evaluateFhirPathWithTraces("name.given", patient)

    assertTrue(result.traces.isEmpty())
  }

  @Test
  fun `trace does not alter evaluation result`() {
    val patient = loadPatient()
    val withTrace = evaluateFhirPathWithTraces("name.trace('t').given.count()", patient)
    val withoutTrace = evaluateFhirPath("name.given.count()", patient)

    assertEquals(withoutTrace.toList(), withTrace.result.toList())
  }

  @Test
  fun `expression without trace produces empty traces map`() {
    val patient = loadPatient()
    val result = evaluateFhirPathWithTraces("name.given.count()", patient)

    assertEquals(listOf(5), result.result.toList())
    assertTrue(result.traces.isEmpty())
  }
}
