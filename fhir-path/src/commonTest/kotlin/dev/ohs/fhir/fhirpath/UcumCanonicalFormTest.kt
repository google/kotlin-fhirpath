/*
 * Copyright 2026 Open Health Stack Foundation
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

import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import kotlin.test.Test
import kotlin.test.assertEquals

/** Canonical UCUM strings omit an exponent of 1, matching UCUM's tables and fhir-ucum. */
class UcumCanonicalFormTest {
  private fun canonicalUnit(unit: String) =
    FhirPathQuantity(value = FhirPathDecimal.ONE, unit = "'$unit'").toEqualCanonicalized().unit

  @Test
  fun baseUnitHasNoExponent() = assertEquals("'g'", canonicalUnit("g"))

  @Test
  fun prefixedBaseUnitHasNoExponent() = assertEquals("'g'", canonicalUnit("kg"))

  @Test
  fun derivedUnitOmitsOnlyExponentOne() = assertEquals("'g.m.s-2'", canonicalUnit("N"))

  @Test
  fun otherExponentsAreKept() {
    assertEquals("'m2'", canonicalUnit("ar"))
    assertEquals("'s-1'", canonicalUnit("Hz"))
    assertEquals("'m-2.rad2.s'", canonicalUnit("lx"))
  }
}
