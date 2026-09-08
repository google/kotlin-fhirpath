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

import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDateTime
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import dev.ohs.fhir.model.r4.Patient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class DateTimeArithmeticTest {
  private val fhirPathEngine = FhirPathEngine.forR4()
  private val context = Patient()

  // Date arithmetic

  // Date + years
  @Test fun `add years to date`() = assertEquals("2016-06-15", evalDate("@2014-06-15 + 2 years"))

  @Test
  fun `subtract years from date`() = assertEquals("2012-06-15", evalDate("@2014-06-15 - 2 years"))

  @Test
  fun `add years in leap year`() = assertEquals("2025-02-28", evalDate("@2024-02-29 + 1 year"))

  @Test fun `add years to year date`() = assertEquals("2016", evalDate("@2014 + 2 years"))

  @Test
  fun `add years to year-month date`() = assertEquals("2016-06", evalDate("@2014-06 + 2 years"))

  // Date + months
  @Test fun `add months to date`() = assertEquals("2014-03-15", evalDate("@2014-01-15 + 2 months"))

  @Test
  fun `subtract months from date`() = assertEquals("2013-11-15", evalDate("@2014-01-15 - 2 months"))

  @Test
  fun `add months clamps to month end`() =
    assertEquals("2014-02-28", evalDate("@2014-01-31 + 1 month"))

  @Test
  fun `subtract months clamps in leap year`() =
    assertEquals("2024-02-29", evalDate("@2024-03-31 - 1 month"))

  @Test fun `add months to year date`() = assertEquals("2015", evalDate("@2014 + 23 months"))

  @Test
  fun `add months to year-month date`() = assertEquals("2014-08", evalDate("@2014-06 + 2 months"))

  @Test
  fun `add fractional years truncates`() =
    assertEquals("1974-12-25", evalDate("@1973-12-25 + 1.5 years"))

  @Test
  fun `add fractional months truncates`() =
    assertEquals("1974-01-25", evalDate("@1973-12-25 + 1.5 months"))

  // Date + weeks
  @Test fun `add weeks to date`() = assertEquals("1974-01-01", evalDate("@1973-12-25 + 1 week"))

  @Test
  fun `subtract weeks wraps year`() = assertEquals("2025-12-25", evalDate("@2026-01-01 - 1 week"))

  @Test fun `add weeks to year date`() = assertEquals("2014", evalDate("@2014 + 1 week"))

  @Test
  fun `add weeks to year-month date`() = assertEquals("2026-03", evalDate("@2026-02 + 5 weeks"))

  // Date + days
  @Test fun `add days to date`() = assertEquals("2014-01-11", evalDate("@2014-01-01 + 10 days"))

  @Test
  fun `subtract days from date`() = assertEquals("2013-12-22", evalDate("@2014-01-01 - 10 days"))

  @Test
  fun `subtract days across century leap`() =
    assertEquals("2000-02-29", evalDate("@2000-03-01 - 1 day"))

  @Test fun `add days to year date`() = assertEquals("2017", evalDate("@2016 + 365 days"))

  @Test
  fun `add days to year-month date`() = assertEquals("2014-06", evalDate("@2014-06 + 10 days"))

  @Test
  fun `add fractional days truncates`() =
    assertEquals("1974-01-01", evalDate("@1973-12-25 + 7.9 days"))

  @Test
  fun `subtract fractional days truncates`() =
    assertEquals("2026-02-28", evalDate("@2026-03-01 - 1.8 days"))

  // Date + UCUM abbreviations
  @Test
  fun `add UCUM wk abbreviation to date`() =
    assertEquals("1974-01-01", evalDate("@1973-12-25 + 1 'wk'"))

  @Test
  fun `subtract UCUM d abbreviation from date`() =
    assertEquals("2025-12-31", evalDate("@2026-01-01 - 1 'd'"))

  @Test
  fun `adding physical UCUM code to date fails`() {
    assertFails { fhirPathEngine.evaluateExpression("@1973-12-25 + 1 'mo'", context) }
  }

  @Test
  fun `adding time units to date fails`() {
    assertFails { fhirPathEngine.evaluateExpression("@1973-12-25 + 1 hour", context) }
  }

  // DateTime arithmetic

  @Test
  fun `add years to datetime preserves time`() =
    assertEquals("2016-06-15T10:30:00", evalDateTime("@2014-06-15T10:30:00 + 2 years"))

  @Test
  fun `subtract months on datetime preserves offset`() =
    assertEquals("2024-02-29T08:00:00+10:00", evalDateTime("@2024-03-31T08:00:00+10:00 - 1 month"))

  @Test
  fun `add weeks to datetime preserves time`() =
    assertEquals("1974-01-01T12:00:00", evalDateTime("@1973-12-25T12:00:00 + 1 week"))

  @Test
  fun `add days to datetime preserves time`() =
    assertEquals("2014-01-11T15:30:00", evalDateTime("@2014-01-01T15:30:00 + 10 days"))

  // DateTime + hours
  @Test
  fun `add hours to datetime rolls over date`() =
    assertEquals("2026-02-01T01:00:00", evalDateTime("@2026-01-31T23:00:00 + 2 hours"))

  @Test
  fun `subtract hours from datetime`() =
    assertEquals("2014-01-01T00:00:00", evalDateTime("@2014-01-01T01:00:00 - 1 hour"))

  @Test
  fun `add fractional hours to datetime truncates`() =
    assertEquals("2026-01-01T13:00:00", evalDateTime("@2026-01-01T12:00:00 + 1.5 hours"))

  @Test
  fun `add fractional minutes to datetime truncates`() =
    assertEquals("2026-01-01T12:01:00", evalDateTime("@2026-01-01T12:00:00 + 1.5 minutes"))

  // DateTime + minutes
  @Test
  fun `add minutes to datetime`() =
    assertEquals("2026-01-01T13:30:00", evalDateTime("@2026-01-01T13:00:00 + 30 minutes"))

  @Test
  fun `subtract minutes from datetime`() =
    assertEquals("2026-02-28T23:45:00", evalDateTime("@2026-03-01T00:15:00 - 30 minutes"))

  @Test
  fun `add minutes to hour precision datetime`() =
    assertEquals("2020-01-01T11", evalDateTime("@2020-01-01T10 + 90 minutes"))

  @Test
  fun `add minutes to minute precision datetime`() =
    assertEquals("2020-01-01T10:45", evalDateTime("@2020-01-01T10:30 + 15 minutes"))

  // DateTime + seconds & milliseconds
  @Test
  fun `add seconds to datetime with offset`() =
    assertEquals(
      "1973-12-25T00:00:42.53+10:00",
      evalDateTime("@1973-12-25T00:00:00.000+10:00 + 42.53 seconds"),
    )

  @Test
  fun `subtract seconds from datetime`() =
    assertEquals("2026-01-01T13:00:15", evalDateTime("@2026-01-01T13:00:30 - 15 's'"))

  @Test
  fun `add milliseconds to datetime`() =
    assertEquals(
      "2026-01-01T13:00:00.75",
      evalDateTime("@2026-01-01T13:00:00.250 + 500 milliseconds"),
    )

  @Test
  fun `subtract milliseconds from datetime`() =
    assertEquals("2026-01-01T13:00:00.25", evalDateTime("@2026-01-01T13:00:00.500 - 250 'ms'"))

  @Test
  fun `add fractional ms to datetime applies fraction`() =
    assertEquals("2026-01-01T12:00:00.0005", evalDateTime("@2026-01-01T12:00:00.000 + 0.5 'ms'"))

  // Time arithmetic

  // Time + hours
  @Test
  fun `add hours to time wraps 24h`() = assertEquals("00:30:00", evalTime("@T23:30:00 + 1 hour"))

  @Test
  fun `subtract hours from time wraps 24h`() =
    assertEquals("23:30:00", evalTime("@T00:30:00 - 1 hour"))

  @Test
  fun `add large hours without overflow`() =
    assertEquals("17:00:00", evalTime("@T01:00:00 + 1000000000 hours"))

  @Test
  fun `add fractional hours to time truncates`() =
    assertEquals("13:00:00", evalTime("@T12:00:00 + 1.5 hours"))

  @Test
  fun `add fractional minutes to time truncates`() =
    assertEquals("12:01:00", evalTime("@T12:00:00 + 1.5 minutes"))

  // Time + minutes
  @Test
  fun `add minutes to time wraps across hour`() =
    assertEquals("00:15:00", evalTime("@T23:45:00 + 30 minutes"))

  @Test
  fun `subtract minutes from time wraps across hour`() =
    assertEquals("23:45:00", evalTime("@T00:15:00 - 30 minutes"))

  // Time + seconds
  @Test fun `add seconds to time`() = assertEquals("12:00:45", evalTime("@T12:00:30 + 15 seconds"))

  @Test
  fun `subtract seconds from time`() = assertEquals("12:00:15", evalTime("@T12:00:30 - 15 's'"))

  @Test
  fun `add fractional seconds to time`() =
    assertEquals("12:00:00.5", evalTime("@T12:00:00.000 + 0.5 's'"))

  @Test
  fun `subtract fractional seconds from time`() =
    assertEquals("12:00:00.5", evalTime("@T12:00:01.000 - 0.5 's'"))

  // Time + milliseconds
  @Test
  fun `add milliseconds to time`() =
    assertEquals("12:00:00.75", evalTime("@T12:00:00.250 + 500 milliseconds"))

  @Test
  fun `subtract milliseconds from time`() =
    assertEquals("12:00:00.25", evalTime("@T12:00:00.500 - 250 'ms'"))

  @Test
  fun `add fractional ms to time applies fraction`() =
    assertEquals("12:00:00.0005", evalTime("@T12:00:00.000 + 0.5 'ms'"))

  @Test
  fun `adding date units to time fails`() {
    assertFails { fhirPathEngine.evaluateExpression("@T12:00:00 + 1 day", context) }
  }

  private fun evalDate(expression: String): String =
    (fhirPathEngine.evaluateExpression(expression, context).single() as FhirPathDate).toString()

  private fun evalDateTime(expression: String): String =
    (fhirPathEngine.evaluateExpression(expression, context).single() as FhirPathDateTime)
      .toIsoString()

  private fun evalTime(expression: String): String =
    (fhirPathEngine.evaluateExpression(expression, context).single() as FhirPathTime).toIsoString()

  private fun FhirPathDateTime.toIsoString(): String = buildString {
    append(year.toString().padStart(4, '0'))
    if (month != null) append("-").append(month.toString().padStart(2, '0'))
    if (day != null) append("-").append(day.toString().padStart(2, '0'))
    if (hour != null) append("T").append(hour.toString().padStart(2, '0'))
    if (minute != null) append(":").append(minute.toString().padStart(2, '0'))
    val sec = second
    if (sec != null) {
      val secondLiteral = sec.toPlainStringPreservingDecimalPlaces()
      val integerPartLength =
        if (secondLiteral.contains('.')) secondLiteral.substringBefore('.').length
        else secondLiteral.length
      append(":").append(if (integerPartLength == 1) "0$secondLiteral" else secondLiteral)
    }
    if (utcOffset != null) {
      append(utcOffset.toString())
    }
  }

  private fun FhirPathTime.toIsoString(): String = buildString {
    append(hour.toString().padStart(2, '0'))
    if (minute != null) append(":").append(minute.toString().padStart(2, '0'))
    val sec = second
    if (sec != null) {
      val secondLiteral = sec.toPlainStringPreservingDecimalPlaces()
      val integerPartLength =
        if (secondLiteral.contains('.')) secondLiteral.substringBefore('.').length
        else secondLiteral.length
      append(":").append(if (integerPartLength == 1) "0$secondLiteral" else secondLiteral)
    }
  }
}
