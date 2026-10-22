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

import dev.ohs.fhir.fhirpath.types.FhirPathQuantity
import dev.ohs.fhir.fhirpath.types.toFhirPathDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class UcumCanonicalFormTest {
  private fun quantity(value: Int, unit: String) =
    FhirPathQuantity(value = value.toFhirPathDecimal(), unit = unit)

  @Test fun baseUnit() = assertEquals(quantity(1, "'g'"), quantity(1, "'g'").toEqualCanonicalized())

  @Test
  fun prefixedBaseUnit() =
    assertEquals(quantity(1000, "'g'"), quantity(1, "'kg'").toEqualCanonicalized())

  @Test
  fun derivedUnit() =
    assertEquals(quantity(1000, "'g.m.s-2'"), quantity(1, "'N'").toEqualCanonicalized())

  @Test
  fun prefixedDerivedUnit() =
    assertEquals(quantity(1_000_000, "'g.m.s-2'"), quantity(1, "'kN'").toEqualCanonicalized())

  @Test
  fun definiteCalendarDuration() =
    assertEquals(quantity(604_800, "'s'"), quantity(1, "week").toEqualCanonicalized())

  @Test
  fun variableCalendarDurationNotEqualCanonicalized() =
    assertEquals(quantity(1, "year"), quantity(1, "year").toEqualCanonicalized())

  @Test
  fun variableCalendarDurationEquivalentCanonicalized() =
    assertEquals(quantity(31_557_600, "'s'"), quantity(1, "year").toEquivalentCanonicalized())
}
