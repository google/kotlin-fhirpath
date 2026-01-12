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

import com.google.fhir.fhirpath.types.FhirPathDate
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.model.r4.FhirR4Json
import com.google.fhir.model.r4.Resource
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.Enabled
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import nl.adaptivity.xmlutil.serialization.XML

private const val TEST_RESOURCE_DIR = "third_party/fhir-test-cases/r4"
private const val TEST_INPUT_DIR = "${TEST_RESOURCE_DIR}/resources"

private val jsonR4 = FhirR4Json()

/**
 * A map from the test group name to the reason why the test group is skipped.
 *
 * N.B. This should be kept in sync with the conformance table in the `README.md` file.
 */
val skippedTestGroupToReasonMap =
  mapOf(
    "testRepeat" to "Unimplemented",
    "testEncodeDecode" to "Unimplemented",
    "testEscapeUnescape" to "Unimplemented",
    "testTrace" to "Unimplemented",
    "testSort" to "Function `sort` is not defined in the specification",
    "testVariables" to "Unimplemented",
    "testExtension" to "Unimplemented",
    "testType" to "Unimplemented",
    "testConformsTo" to "Unimplemented",
    "LowBoundary" to "Unimplemented",
    "HighBoundary" to "Unimplemented",
    "Comparable" to "Unimplemented",
    "Precision" to "Unimplemented",
    "testInheritance" to "Unimplemented",
  )

/**
 * A map from the test case name to the reason why the test case is skipped.
 *
 * N.B. This should be kept in sync with the conformance table in the `README.md` file.
 */
val skippedTestCaseToReasonMap =
  mapOf(
    "testPolymorphismB" to "Strict mode is not implemented yet",
    "testPolymorphismAsB" to
      "No error should be thrown according to https://hl7.org/fhirpath/#as-type-specifier",
    "testDateTimeGreaterThanDate1" to
      "Comparison of two date time values, one with a timezone offset one without",
    "testStringIntegerLiteralToQuantity" to
      "Unclear if integers should be converted to decimals as part of quantity. See https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Quantity.20and.20Decimal/near/543270110",
    "testQuantityLiteralWkToString" to
      "Unclear if integers should be converted to decimals as part of quantity. See https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Quantity.20and.20Decimal/near/543270110",
    "testQuantityLiteralWeekToString" to
      "Unclear if integers should be converted to decimals as part of quantity. See https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Quantity.20and.20Decimal/near/543270110",
    "testQuantity4" to "https://github.com/FHIR/fhir-test-cases/pull/243",
    "testSubSetOf3" to
      "The test resource is invalid and missing (https://github.com/FHIR/fhir-test-cases/issues/247); the scope of \"\$this\" is unclear (https://jira.hl7.org/browse/FHIR-44601)",
    "testIif11" to
      "https://jira.hl7.org/browse/FHIR-44774; https://jira.hl7.org/browse/FHIR-44601; https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/scope.20of.20this/with/531507415; https://chat.fhir.org/#narrow/stream/179266-fhirpath/topic/context.20of.20the.20.60iif.20.60; https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/receiver.20of.20iif/with/558282370",
    "testNow1" to "As `testDateTimeGreaterThanDate1`",
    "testPlusDate13" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Definite.20durations.20above.20seconds.20in.20date.20time.20arithmetic/with/564095766",
    "testPlusDate15" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Definite.20durations.20above.20seconds.20in.20date.20time.20arithmetic/with/564095766",
    "testPlusDate18" to
      "To be fixed together with `testPlusDate13`, `testPlusDate15`, `testPlusDate21`, `testPlusDate22` for a consistent implementation.",
    "testPlusDate19" to
      "To be fixed together with `testPlusDate13`, `testPlusDate15`, `testPlusDate21`, `testPlusDate22` for a consistent implementation.",
    "testPlusDate20" to
      "To be fixed together with `testPlusDate13`, `testPlusDate15`, `testPlusDate21`, `testPlusDate22` for a consistent implementation.",
    "testPlusDate21" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Definite.20durations.20above.20seconds.20in.20date.20time.20arithmetic/with/564095766",
    "testPlusDate22" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Definite.20durations.20above.20seconds.20in.20date.20time.20arithmetic/with/564095766",
    "testMinus5" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Definite.20durations.20above.20seconds.20in.20date.20time.20arithmetic/with/564095766",
    "testPrecedence3" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/FHIRPath.20test.20suite.20for.20precedence.20correct.3F/with/564497251",
    "testPrecedence4" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/FHIRPath.20test.20suite.20for.20precedence.20correct.3F/with/564497251",
    "testDollarOrderNotAllowed" to
      "Ordered function validation not implemented. Test expects error when using skip() on unordered collection (children()), but engine does not track collection ordering.",
    "testSimpleFail" to "Strict mode is not implemented yet",
    "testSimpleWithWrongContext" to "Strict mode is not implemented yet",
    "testPolymorphicsB" to "Allow invalid test where it's not strict mode but expects output",
    "testIndex" to "TBD",
    "testPeriodInvariantOld" to "hasValue() is not implemented.",
    "testPeriodInvariantNew" to "lowBoundary() and lowBoundary() are not implemented.",
    "testCombine2" to "FHIR String and Kotlin String comparison issue in exclude()",
    "testCombine3" to "As above",
    "testContainedId" to "TBD",
  )

@OptIn(ExperimentalKotest::class)
class FhirPathEngineTest :
  FunSpec({
    val inputMap: Map<String, Resource> =
      listJsonFiles(TEST_INPUT_DIR)
        .mapKeys { it.key.replace(".json$".toRegex(), ".xml") }
        .mapValues { jsonR4.decodeFromString(it.value) }
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
            if (testCase.expression.invalid != null) {
              assertFailsWith<Exception> {
                evaluateFhirPath(
                  testCase.expression.value,
                  testCase.inputfile?.let { inputMap[it] },
                )
              }
            } else {
              val results =
                evaluateFhirPath(
                  testCase.expression.value,
                  testCase.inputfile?.let { inputMap[it] },
                )
              com.google.fhir.fhirpath.assertEquals(testCase.outputs, results)
            }
          }
        }
      }
    }
  })

private fun assertEquals(expected: List<Output>, actual: Collection<Any>) {
  assertEquals(expected.size, actual.size)
  expected.zip(actual).forEach { com.google.fhir.fhirpath.assertEquals(it.first, it.second) }
}

private fun assertEquals(expected: Output, actual: Any) {
  when (expected.type) {
    "date" -> assertEquals(FhirPathDate.fromString(expected.value.trimStart('@')), actual)
    "dateTime" -> assertEquals(FhirPathDateTime.fromString(expected.value.trimStart('@')), actual)
    "code" -> assertEquals(expected.value, actual)
    "string" -> {
      when (actual) {
        is String -> {
          assertEquals(expected.value, actual)
        }
        is com.google.fhir.model.r4.String -> {
          assertEquals(expected.value, actual.value)
        }
      }
    }
    "boolean" -> {
      when (actual) {
        is Boolean -> {
          assertEquals(expected.value, actual.toString())
        }
        else -> {
          assertEquals(expected.value, "true") // Single items are considered true
        }
      }
    }
    "integer" -> assertEquals(expected.value, (actual as Int).toString())
    "decimal" -> assertEquals(expected.value.toBigDecimal(), actual as BigDecimal)
    "Quantity" ->
      assertEquals(
        expected.value,
        (actual as com.google.fhir.model.r4.Quantity).let {
          "${it.value!!.value} ${it.code!!.value}"
        },
      )
    else -> throw AssertionError("Unknown output type: ${expected.type}")
  }
}

expect fun loadFile(file: String): String

expect fun listJsonFiles(dir: String): Map<String, String>
