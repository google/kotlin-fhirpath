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
import dev.ohs.fhir.model.r4.Patient
import dev.ohs.fhir.model.r4.String as FhirString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val fhirPathEngine = FhirPathEngine.forR4()

/**
 * Tests for FHIRPath environment variables, including:
 * 1. [FHIRPath STU3 sec. 9](https://hl7.org/fhirpath/STU3/en/#environment-variables): `%context`,
 *    `%ucum`, and custom `%name` variables.
 * 2. [FHIR R5 sec. 2.1.9.1.4](https://hl7.org/fhir/R5/fhirpath.html#variables): `%resource` and
 *    `%rootResource`.
 * 3. [FHIR R5 sec. 2.1.9.1.7](https://hl7.org/fhir/R5/fhirpath.html#vars): `%sct`, `%loinc`,
 *    `%'vs-[name]'`, and `%'ext-[name]'`.
 */
class EnvironmentVariablesTest {

  // %context

  @Test
  fun `context returns null base as empty collection`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%context", base = null)
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `context returns primitive evaluation context`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%context", base = "hello")
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `context returns resource evaluation context`() {
    val patient = Patient(id = "p1", name = listOf(HumanName(family = FhirString(value = "Smith"))))
    val result = fhirPathEngine.evaluateExpression(expression = "%context.id", base = patient)
    assertEquals(listOf("p1"), result.toList())
  }

  @Test
  fun `context returns initial context within nested lambda scopes`() {
    val patient =
      Patient(
        id = "p1",
        name =
          listOf(
            HumanName(
              family = FhirString(value = "Smith"),
              given = listOf(FhirString(value = "John")),
            ),
            HumanName(
              family = FhirString(value = "Doe"),
              given = listOf(FhirString(value = "Jane")),
            ),
          ),
      )
    val result =
      fhirPathEngine.evaluateExpression(
        expression = "Patient.name.where(family = 'Smith').select(%context.id)",
        base = patient,
      )
    assertEquals(listOf("p1"), result.toList())
  }

  // %ucum

  @Test
  fun `ucum returns UCUM URL`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%ucum", base = null)
    assertEquals(listOf("http://unitsofmeasure.org"), result.toList())
  }

  // Custom variables

  @Test
  fun `null environment variable returns empty`() {
    val result =
      fhirPathEngine.evaluateExpression(
        expression = "%nullVar",
        base = null,
        variables = mapOf("nullVar" to null),
      )
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `environment variable returns value`() {
    val result =
      fhirPathEngine.evaluateExpression(
        expression = "%myVar",
        base = null,
        variables = mapOf("myVar" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `unknown environment variable throws error`() {
    assertFailsWith<Exception> {
      fhirPathEngine.evaluateExpression(expression = "%unknownVar", base = null)
    }
  }

  // %resource

  @Test
  fun `resource returns empty collection when base is null`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%resource", base = null)
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `resource returns base resource`() {
    val patient = Patient(id = "p1")
    val result = fhirPathEngine.evaluateExpression(expression = "%resource.id", base = patient)
    assertEquals(listOf("p1"), result.toList())
  }

  @Test
  fun `resource returns base resource within nested lambda scopes`() {
    val patient = Patient(id = "p1", name = listOf(HumanName(family = FhirString(value = "Smith"))))
    val result =
      fhirPathEngine.evaluateExpression(
        expression = "Patient.name.where(family = 'Smith').select(%resource.id)",
        base = patient,
      )
    assertEquals(listOf("p1"), result.toList())
  }

  // %rootResource

  @Test
  fun `rootResource returns empty collection when base is null`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%rootResource", base = null)
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `rootResource returns base resource`() {
    val patient = Patient(id = "p1")
    val result = fhirPathEngine.evaluateExpression(expression = "%rootResource.id", base = patient)
    assertEquals(listOf("p1"), result.toList())
  }

  @Test
  fun `rootResource returns base resource within nested lambda scopes`() {
    val patient = Patient(id = "p1", name = listOf(HumanName(family = FhirString(value = "Smith"))))
    val result =
      fhirPathEngine.evaluateExpression(
        expression = "Patient.name.where(family = 'Smith').select(%rootResource.id)",
        base = patient,
      )
    assertEquals(listOf("p1"), result.toList())
  }

  // %sct

  @Test
  fun `sct returns SNOMED CT URL`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%sct", base = null)
    assertEquals(listOf("http://snomed.info/sct"), result.toList())
  }

  // %loinc

  @Test
  fun `loinc returns LOINC URL`() {
    val result = fhirPathEngine.evaluateExpression(expression = "%loinc", base = null)
    assertEquals(listOf("http://loinc.org"), result.toList())
  }

  // %vs-[name]

  @Test
  fun `vs-name returns ValueSet URL`() {
    val result =
      fhirPathEngine.evaluateExpression(expression = "%'vs-administrative-gender'", base = null)
    assertEquals(listOf("http://hl7.org/fhir/ValueSet/administrative-gender"), result.toList())
  }

  // %ext-[name]

  @Test
  fun `ext-name returns StructureDefinition URL`() {
    val result =
      fhirPathEngine.evaluateExpression(expression = "%'ext-patient-birthPlace'", base = null)
    assertEquals(
      listOf("http://hl7.org/fhir/StructureDefinition/patient-birthPlace"),
      result.toList(),
    )
  }
}
