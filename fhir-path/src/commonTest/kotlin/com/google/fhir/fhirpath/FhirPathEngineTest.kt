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

import com.google.fhir.model.r4.Date
import com.google.fhir.model.r4.Enumeration
import com.google.fhir.model.r4.FhirR4Json
import com.google.fhir.model.r4.Resource
import evaluateFhirPath
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.Enabled
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import nl.adaptivity.xmlutil.serialization.XML

private const val TEST_RESOURCE_DIR = "third_party/fhir-test-cases/r4"
private const val TEST_INPUT_DIR = "${TEST_RESOURCE_DIR}/resources"

private val jsonR4 = FhirR4Json()

/** A map from the test group name to the reason why the test group is skipped. */
val skippedTestGroupToReasonMap =
  mapOf(
    "testRepeat" to "Unimplemented",
    "testAggregate" to "Unimplemented",
    "testEncodeDecode" to "Unimplemented",
    "testEscapeUnescape" to "Unimplemented",
    "testSort" to "Function `sort` is not defined in the specification",
    "testCombine()" to "Unimplemented",
    "testVariables" to "Unimplemented",
    "testExtension" to "Unimplemented",
    "testType" to "Unimplemented",
    "testConformsTo" to "Unimplemented",
    "LowBoundary" to "Unimplemented",
    "HighBoundary" to "Unimplemented",
    "Comparable" to "Unimplemented",
    "Precision" to "Unimplemented",
    "period" to "Unimplemented",
    "testInheritance" to "Unimplemented",
  )

/** A map from the test case name to the reason why the test case is skipped. */
val skippedTestCaseToReasonMap =
  mapOf(
    "testPolymorphismAsB" to
      "No error should be thrown according to https://hl7.org/fhirpath/#as-type-specifier",
    "testDateTimeGreaterThanDate1" to
      "Unclear in the specification whether the result should still be empty if two values have different precisions but the comparison can still be certain (e.g. 2025 is greater than 2024-01)",
    "testDecimalLiteralToInteger" to "The result should be true",
    "testStringIntegerLiteralToQuantity" to
      "Unclear if integers should be converted to decimals as part of quantity. See https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Quantity.20and.20Decimal/near/543270110",
    "testQuantityLiteralWkToString" to
      "Unclear if integers should be converted to decimals as part of quantity. See https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Quantity.20and.20Decimal/near/543270110",
    "testQuantityLiteralWeekToString" to
      "Unclear if integers should be converted to decimals as part of quantity. See https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/Quantity.20and.20Decimal/near/543270110",
    "testQuantity4" to "https://github.com/FHIR/fhir-test-cases/pull/243",
    "testExists3" to "TBD",
    "testSubSetOf3" to "TBD",
    "testQuantity9" to "TBD",
    "testQuantity10" to "TBD",
    "testQuantity11" to "TBD",
    "testDistinct2" to "TBD",
    "testDistinct3" to "TBD",
    "testDistinct5" to "TBD",
    "testDistinct6" to "TBD",
    "testSelect3" to "TBD",
    "testIif10" to "TBD",
    "testIif11" to "TBD",
    "testMatchesSingleLineMode1" to "TBD",
    "testReplace5" to "TBD",
    "testReplace6" to "TBD",
    "testTrace2" to "TBD",
    "testNow1" to "As `testDateTimeGreaterThanDate1`",
    "testEquality28" to "TBD",
    "testNEquality24" to "TBD",
    "testEquivalent11" to "TBD",
    "testEquivalent22" to "TBD",
    "testLessThan22" to "TBD",
    "testLessThan24" to "TBD",
    "testLessOrEqual22" to "TBD",
    "testLessOrEqual24" to "TBD",
    "testGreatorOrEqual22" to "TBD",
    "testGreatorOrEqual24" to "TBD",
    "testGreaterThan22" to "TBD",
    "testGreaterThan24" to "TBD",
    "testPlusDate1" to "TBD",
    "testPlusDate2" to "TBD",
    "testPlusDate3" to "TBD",
    "testPlusDate4" to "TBD",
    "testPlusDate5" to "TBD",
    "testPlusDate6" to "TBD",
    "testPlusDate7" to "TBD",
    "testPlusDate8" to "TBD",
    "testPlusDate9" to "TBD",
    "testPlusDate10" to "TBD",
    "testPlusDate11" to "TBD",
    "testPlusDate12" to "TBD",
    "testPlusDate13" to "TBD",
    "testPlusDate14" to "TBD",
    "testPlusDate15" to "TBD",
    "testPlusDate16" to "TBD",
    "testPlusDate17" to "TBD",
    "testPlusDate18" to "TBD",
    "testPlusDate19" to "TBD",
    "testPlusDate20" to "TBD",
    "testPlusDate21" to "TBD",
    "testPlusDate22" to "TBD",
    "testMinus3" to "TBD",
    "testMinus5" to "TBD",
    "testMod4" to "TBD",
    "testAbs3" to "TBD",
    "testPrecedence3" to "TBD",
    "testPrecedence4" to "TBD",
    "testPrecedence6" to "TBD",
    "testIndex" to "TBD",
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
    "date" -> assertEquals(expected.value, "@${(actual as Date).value.toString()}")
    "code" -> assertEquals(expected.value, (actual as Enumeration<*>).value.toString())
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
    "decimal" -> assertEquals(expected.value.toDouble(), actual as Double)
    "Quantity" ->
      assertEquals(
        expected.value,
        (actual as com.google.fhir.model.r4.Quantity).let {
          "${it.value!!.value} ${it.unit!!.value}"
        },
      )
    else -> throw AssertionError("Unknown output type: ${expected.type}")
  }
}

expect fun loadFile(file: String): String

expect fun listJsonFiles(dir: String): Map<String, String>
