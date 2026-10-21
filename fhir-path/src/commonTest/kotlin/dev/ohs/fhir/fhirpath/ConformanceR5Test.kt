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

private const val TEST_RESOURCE_DIR = "third_party/fhir-test-cases/r5"

private val jsonR5 = Json { ignoreUnknownKeys = true }

/**
 * A map from the test group name to the reason why the test group is skipped.
 *
 * N.B. This should be kept in sync with the test conformance table in `docs/conformance.md`.
 */
val skippedR5TestGroupToReasonMap =
  mapOf(
    "defineVariable" to
      "Function `defineVariable` is not implemented. Most tests fail with the not implemented error, dvConceptMapExample fails on its expected output instead, and the group's expected error tests would accidentally pass since the not implemented error satisfies their assertion.",
    "cdaTests" to
      "Function `hasTemplateIdOf` is not implemented, and the input `ccda.xml` is a CDA document, not a FHIR JSON resource.",
    "TerminologyTests" to "The `%terminologies` API is not implemented.",
    "HTMLChecks" to
      "Function `htmlChecks` is not implemented, and the input `parameters-example-html.xml` only exists as XML upstream. htmlTest01 fails earlier because `text.div` does not parse, since `div` is the division operator keyword in the grammar.",
  )

/**
 * A map from the test case name to the reason why the test case is skipped.
 *
 * N.B. This should be kept in sync with the test conformance table in `docs/conformance.md`.
 */
val skippedR5TestCaseToReasonMap =
  mapOf(
    "testPolymorphismIsA1" to
      "The generated `FhirR5ComplexType.fromObject` dispatch has arms for R5's abstract types (`Base`, `DataType`, `BackboneType`, `PrimitiveType`), which every model class implements, so they shadow later subtype arms and the resource fallback and type resolution returns the abstract type.",
    "testPolymorphismIsA2" to "As `testPolymorphismIsA1`.",
    "testPolymorphismAsA" to "As `testPolymorphismIsA1`.",
    "testPolymorphismAsAFunction" to "As `testPolymorphismIsA1`.",
    "testPolymorphismAsB" to
      "No error should be thrown according to https://hl7.org/fhirpath/#as-type-specifier",
    "testDollarOrderNotAllowed" to
      "Ordered function validation not implemented. Test expects error when using skip() on unordered collection (children()), but engine does not track collection ordering.",
    "testDateTimeGreaterThanDate1" to
      "Comparison of two date time values, one with a timezone offset one without",
    "testLiteralYearToDateTime" to
      "`'2015'.toDateTime()` correctly yields a year precision DateTime, but comparing it to the Date literal `@2015` returns empty instead of true because the Date operand is not implicitly converted to DateTime for equality.",
    "testLiteralMonthToDateTime" to "As `testLiteralYearToDateTime`.",
    "testLiteralDayToDateTime" to "As `testLiteralYearToDateTime`.",
    "testSubSetOf3" to
      "The input `explanationofbenefit-example.json` cannot be decoded by the R5 model (NullPointerException in kotlin-fhir), so the test runs without its input and crashes.",
    "testQuantity4" to "https://github.com/FHIR/fhir-test-cases/pull/243",
    "testIif11" to
      "https://jira.hl7.org/browse/FHIR-44774; https://jira.hl7.org/browse/FHIR-44601; https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/scope.20of.20this/with/531507415; https://chat.fhir.org/#narrow/stream/179266-fhirpath/topic/context.20of.20the.20.60iif.20.60; https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/receiver.20of.20iif/with/558282370",
    "testIif12" to
      "`iif` evaluates its arguments against the outer resource instead of the input collection, so `text` resolves to `Patient.text` (a Narrative) rather than `HumanName.text`. Same context scoping problem as `testIif11`.",
    "testSubstring10a" to
      "The expression is marked invalid semantic, expecting an error from static analysis. The engine performs no static semantic checking, evaluates the expression, and returns a result instead of erroring.",
    "testNow1" to "As `testDateTimeGreaterThanDate1`",
    "testSort8" to "Test uses `-\$this` for descending string sort, but spec uses asc/desc.",
    "testSort10" to "Test uses `-` prefix for descending sort, but spec uses asc/desc.",
    "testEquality28" to
      "The FHIR Quantity input fails to convert to a FHIRPath Quantity because of the abstract type shadowing described in `testPolymorphismIsA1`. Equality between the unconverted model Quantity and the quantity literal returns false, and the ordering operators throw.",
    "testEquivalent22" to "As `testEquality28`.",
    "testLessThan22" to "As `testEquality28`.",
    "testLessOrEqual22" to "As `testEquality28`.",
    "testGreatorOrEqual22" to "As `testEquality28`.",
    "testGreaterThan22" to "As `testEquality28`.",
    "testInEmptyValue" to "`in` with an empty operand crashes with NoSuchElementException.",
    "testInEmptyBoth" to "`in` with an empty operand crashes with NoSuchElementException.",
    "testContainsCollectionEmpty4" to
      "`contains` with an empty operand crashes with NoSuchElementException.",
    "testDiv6" to
      "`-5.5 div 2` evaluates to -3 because the engine floors negative decimal division, but the specification truncates toward zero, expecting -2.",
    "testMod6" to
      "`-5.5 mod 2` evaluates to 0.5, but the specification defines mod with the dividend's sign, expecting -1.5. Same floored arithmetic as `testDiv6`.",
    "testType16" to "As `testPolymorphismIsA1`.",
    "testType17" to "As `testPolymorphismIsA1`.",
    "testType18" to "As `testPolymorphismIsA1`.",
    "testType19" to "As `testPolymorphismIsA1`.",
    "testType20" to "As `testPolymorphismIsA1`.",
    "testType21" to "As `testPolymorphismIsA1`.",
    "testType22" to
      "`is` with an unknown `System` type should evaluate to false, but the type resolver throws `Unknown System type Patient`.",
    "testType23" to "As `testPolymorphismIsA1`.",
    "testConformsTo1" to "As `testPolymorphismIsA1`.",
    "testConformsTo3" to
      "Intentional deviation: an unresolvable structure returns empty following R5 (https://hl7.org/fhir/R5/fhirpath.html#functions); R4 requires an error",
    "LowBoundaryDateMonth" to
      "The r5 suite declares the expected output type as dateTime while the engine returns a Date. The r4 suite has the same test with no declared type, and it passes there because the comparison infers the type from the actual value.",
    "LowBoundaryDateTimeMillisecond1" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "LowBoundaryDateTimeMillisecond3" to "As `LowBoundaryDateMonth`.",
    "HighBoundaryDateMonth" to "As `LowBoundaryDateMonth`.",
    "HighBoundaryDateTimeMillisecond1" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "HighBoundaryDateTimeMillisecond3" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "testPolymorphicsB" to "Allow invalid test where it's not strict mode but expects output",
    "testPolymorphicsC" to
      "Navigating a choice element by its suffixed name (`Observation.valueQuantity`) is not implemented. The r4 suite keeps this test commented out.",
    "testPeriodInvariantNew" to
      "https://chat.fhir.org/#narrow/channel/179266-fhirpath/topic/lowBoundary.20and.20highBoundary.20with.20incomplete.20date.20time/with/611113639",
    "testFHIRPathAsFunction22" to "As `testPolymorphismIsA1`.",
    "testMultipleResolve" to "Function `resolve` is not implemented.",
  )

class ConformanceR5Test :
  FunSpec({
    conformanceSuite(
      ConformanceSuiteConfig(
        testCasesXmlFilePath = "${TEST_RESOURCE_DIR}/tests-fhir-r5.xml",
        resourcesDir = "${TEST_RESOURCE_DIR}/resources",
        lenientEngine = FhirPathEngine.forR5(),
        strictEngine = FhirPathEngine.forR5(strictMode = true),
        resourceDecoder = { jsonR5.decodeFromString<dev.ohs.fhir.model.r5.Resource>(it) },
        fhirStringValueExtractor = { (it as? dev.ohs.fhir.model.r5.String)?.value },
        fhirQuantityStringExtractor = {
          (it as? dev.ohs.fhir.model.r5.Quantity)?.let { q ->
            "${q.value!!.value} ${q.code!!.value}"
          }
        },
        skippedGroups = skippedR5TestGroupToReasonMap,
        skippedCases = skippedR5TestCaseToReasonMap,
      )
    )
  })
