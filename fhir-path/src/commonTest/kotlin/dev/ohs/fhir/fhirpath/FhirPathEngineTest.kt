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

import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import dev.ohs.fhir.model.r4.Resource
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.Enabled
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML

private const val TEST_RESOURCE_DIR = "third_party/fhir-test-cases/r4"
private const val TEST_INPUT_DIR = "${TEST_RESOURCE_DIR}/resources"

private val jsonR4 = Json { ignoreUnknownKeys = true }

private val fhirPathEngine = FhirPathEngine.forR4()
private val fhirPathEngineStrict = FhirPathEngine.forR4(strictMode = true)

/**
 * A map from the test group name to the reason why the test group is skipped.
 *
 * N.B. This should be kept in sync with the conformance table in the `README.md` file.
 */
val skippedTestGroupToReasonMap = emptyMap<String, String>()

/**
 * A map from the test case name to the reason why the test case is skipped.
 *
 * N.B. This should be kept in sync with the conformance table in the `README.md` file.
 */
val skippedTestCaseToReasonMap =
  mapOf(
    "testPolymorphismAsB" to
      "No error should be thrown according to https://hl7.org/fhirpath/#as-type-specifier",
    "testDollarOrderNotAllowed" to
      "Ordered function validation not implemented. Test expects error when using skip() on unordered collection (children()), but engine does not track collection ordering.",
    "testDateTimeGreaterThanDate1" to
      "Comparison of two date time values, one with a timezone offset one without",
    "testConformsTo3" to
      "Intentional deviation: an unresolvable structure returns empty following R5 (https://hl7.org/fhir/R5/fhirpath.html#functions); R4 requires an error",
    "testSubSetOf3" to
      "The test resource is invalid and missing (https://github.com/FHIR/fhir-test-cases/issues/247); the scope of \"\$this\" is unclear (https://jira.hl7.org/browse/FHIR-44601)",
    "testQuantity4" to "https://github.com/FHIR/fhir-test-cases/pull/243",
    "testIif11" to
      "https://jira.hl7.org/browse/FHIR-44774; https://jira.hl7.org/browse/FHIR-44601; https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/scope.20of.20this/with/531507415; https://chat.fhir.org/#narrow/stream/179266-fhirpath/topic/context.20of.20the.20.60iif.20.60; https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/receiver.20of.20iif/with/558282370",
    "testNow1" to "As `testDateTimeGreaterThanDate1`",
    "testSort8" to "Test uses `-\$this` for descending string sort, but spec uses asc/desc.",
    "testSort10" to "Test uses `-` prefix for descending sort, but spec uses asc/desc.",
    "testPlusDate19" to
      "Test ignores decimal portion of second duration (0.1 's'), contradicting FHIRPath spec section on DateTime arithmetic",
    "testType22" to
      "`is` with an unknown `System` type should evaluate to false, but the type resolver throws `Unknown System type Patient`.",
    "LowBoundaryDateTimeMillisecond1" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "HighBoundaryDateTimeMillisecond1" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "HighBoundaryDateTimeMillisecond3" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "testPolymorphicsB" to "Allow invalid test where it's not strict mode but expects output",
    "testPeriodInvariantNew" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "testContainedId" to "TBD",
  )

@OptIn(ExperimentalKotest::class)
class FhirPathEngineTest :
  FunSpec({
    val inputMap: Map<String, Resource> =
      listJsonFiles(TEST_INPUT_DIR)
        .flatMap { entry ->
          val resource = jsonR4.decodeFromString<Resource>(entry.value)
          listOf(entry.key to resource, entry.key.replace(".json$".toRegex(), ".xml") to resource)
        }
        .toMap()
    val xmlContent = loadFile("${TEST_RESOURCE_DIR}/tests-fhir-r4.xml")
    val testSuite = XML.decodeFromString<Tests>(xmlContent)

    testSuite.groups.forEach { group ->
      context(group.name).config(
        enabledOrReasonIf = {
          skippedTestGroupToReasonMap[group.name]?.let { Enabled.disabled(it) } ?: Enabled.enabled
        }
      ) {
        group.tests.forEach { testCase ->
          test(testCase.name).config(
            enabledOrReasonIf = {
              skippedTestCaseToReasonMap[testCase.name]?.let { Enabled.disabled(it) }
                ?: Enabled.enabled
            }
          ) {
            val engine =
              if (testCase.mode == "strict" || testCase.expression.mode == "strict")
                fhirPathEngineStrict
              else fhirPathEngine

            if (testCase.expression.invalid != null) {
              assertFailsWith<Exception> {
                engine.evaluateExpression(
                  testCase.expression.value,
                  testCase.inputfile?.let { inputMap[it] },
                )
              }
            } else {
              val results =
                engine.evaluateExpression(
                  testCase.expression.value,
                  testCase.inputfile?.let { inputMap[it] },
                )
              dev.ohs.fhir.fhirpath.assertEquals(testCase.outputs, results)
            }
          }
        }
      }
    }
  })

private fun assertEquals(expected: List<Output>, actual: Collection<Any>) {
  assertEquals(expected.size, actual.size)
  expected.zip(actual).forEach { dev.ohs.fhir.fhirpath.assertEquals(it.first, it.second) }
}

private fun assertEquals(expected: Output, actual: Any) {
  val type = expected.type ?: inferType(actual)
  when (type) {
    "date" -> assertEquals(FhirPathDate.fromString(expected.value.trimStart('@')), actual)
    "dateTime" -> assertEquals(FhirPathDateTime.fromString(expected.value.trimStart('@')), actual)
    "time" -> assertEquals(FhirPathTime.fromString(expected.value.trimStart('@', 'T')), actual)
    "code",
    "string" -> assertEquals(expected.value, actual.toStringValue())
    "boolean" -> assertEquals(expected.value, if (actual is Boolean) actual.toString() else "true")
    "integer" -> assertEquals(expected.value, (actual as Int).toString())
    "decimal" -> assertEquals(FhirPathDecimal.fromString(expected.value), actual as FhirPathDecimal)
    "Quantity" -> assertEquals(expected.value, actual.toQuantityString())
    else -> throw AssertionError("Unknown type: $type")
  }
}

private fun inferType(actual: Any): String =
  when (actual) {
    is Boolean -> "boolean"
    is Int -> "integer"
    is FhirPathDecimal -> "decimal"
    is FhirPathDate -> "date"
    is FhirPathDateTime -> "dateTime"
    is FhirPathTime -> "time"
    is dev.ohs.fhir.model.r4.Quantity,
    is FhirPathQuantity -> "Quantity"
    is String,
    is dev.ohs.fhir.model.r4.String -> "string"
    else -> throw AssertionError("Unknown actual type: ${actual::class}")
  }

private fun Any.toStringValue(): String =
  when (this) {
    is String -> this
    is dev.ohs.fhir.model.r4.String -> value!!
    else -> throw AssertionError("Cannot extract string value from: $this")
  }

private fun Any.toQuantityString(): String =
  when (this) {
    is dev.ohs.fhir.model.r4.Quantity -> "${value!!.value} ${code!!.value}"
    is FhirPathQuantity -> {
      val cleanUnit = unit?.trim('\'')
      "$value '$cleanUnit'"
    }
    else -> throw AssertionError("Cannot extract Quantity value from: $this")
  }

expect fun loadFile(file: String): String

expect fun listJsonFiles(dir: String): Map<String, String>
