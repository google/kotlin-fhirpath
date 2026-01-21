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

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

/**
 * Tests for FHIRPath environment variables.
 *
 * See https://hl7.org/fhirpath/#environment-variables and
 * https://fhir.hl7.org/fhir/fhirpath.html#vars
 */
class EnvironmentVariablesTest {

  @Test
  fun `sct returns SNOMED CT URL`() {
    val result = evaluateFhirPath(expression = "%sct", resource = null)
    assertEquals(listOf("http://snomed.info/sct"), result.toList())
  }

  @Test
  fun `loinc returns LOINC URL`() {
    val result = evaluateFhirPath(expression = "%loinc", resource = null)
    assertEquals(listOf("http://loinc.org"), result.toList())
  }

  @Test
  fun `ucum returns UCUM URL`() {
    val result = evaluateFhirPath(expression = "%ucum", resource = null)
    assertEquals(listOf("http://unitsofmeasure.org"), result.toList())
  }

  @Test
  fun `vs-name returns ValueSet URL`() {
    val result = evaluateFhirPath(expression = "%'vs-administrative-gender'", resource = null)
    assertEquals(listOf("http://hl7.org/fhir/ValueSet/administrative-gender"), result.toList())
  }

  @Test
  fun `ext-name returns StructureDefinition URL`() {
    val result = evaluateFhirPath(expression = "%'ext-patient-birthPlace'", resource = null)
    assertEquals(
      listOf("http://hl7.org/fhir/StructureDefinition/patient-birthPlace"),
      result.toList(),
    )
  }

  @Test
  fun `environment variable returns value`() {
    val result =
      evaluateFhirPath(
        expression = "%myVar",
        resource = null,
        variables = mapOf("myVar" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `null environment variable returns empty`() {
    val result =
      evaluateFhirPath(
        expression = "%nullVar",
        resource = null,
        variables = mapOf("nullVar" to null),
      )
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `unknown environment variable throws error`() {
    assertFailsWith<Exception> { evaluateFhirPath(expression = "%unknownVar", resource = null) }
  }
}
