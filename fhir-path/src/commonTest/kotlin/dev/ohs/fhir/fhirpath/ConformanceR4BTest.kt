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

import io.kotest.core.spec.style.FunSpec
import kotlinx.serialization.json.Json

private const val TEST_RESOURCE_DIR = "third_party/fhir-test-cases/r4b"

private val jsonR4B = Json { ignoreUnknownKeys = true }

/**
 * A map from the test case name to the reason why the test case is skipped.
 *
 * N.B. This should be kept in sync with the test conformance table in `docs/conformance.md`.
 */
val skippedR4BTestCaseToReasonMap =
  mapOf(
    "testPolymorphismAsB" to
      "No error should be thrown according to https://hl7.org/fhirpath/#as-type-specifier",
    "testDollarOrderNotAllowed" to
      "Ordered function validation not implemented. Test expects error when using skip() on unordered collection (children()), but engine does not track collection ordering.",
    "testDateTimeGreaterThanDate1" to
      "Comparison of two date time values, one with a timezone offset one without",
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
    "testConformsTo3" to
      "Intentional deviation: an unresolvable structure returns empty following R5 (https://hl7.org/fhir/R5/fhirpath.html#functions); R4B requires an error",
    "LowBoundaryDateTimeMillisecond1" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "HighBoundaryDateTimeMillisecond1" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "HighBoundaryDateTimeMillisecond3" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "testPolymorphicsB" to "Allow invalid test where it's not strict mode but expects output",
    "testPeriodInvariantNew" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
  )

class ConformanceR4BTest :
  FunSpec({
    conformanceSuite(
      ConformanceSuiteConfig(
        testCasesXmlFilePath = "${TEST_RESOURCE_DIR}/tests-fhir-r4b.xml",
        resourcesDir = "${TEST_RESOURCE_DIR}/resources",
        lenientEngine = FhirPathEngine.forR4B(),
        strictEngine = FhirPathEngine.forR4B(strictMode = true),
        resourceDecoder = { jsonR4B.decodeFromString<dev.ohs.fhir.model.r4b.Resource>(it) },
        fhirStringValueExtractor = { (it as? dev.ohs.fhir.model.r4b.String)?.value },
        fhirQuantityStringExtractor = {
          (it as? dev.ohs.fhir.model.r4b.Quantity)?.let { q ->
            "${q.value!!.value} ${q.code!!.value}"
          }
        },
        skippedCases = skippedR4BTestCaseToReasonMap,
      )
    )
  })
