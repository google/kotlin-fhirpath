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

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathDate
import dev.ohs.fhir.fhirpath.types.FhirPathDecimal
import dev.ohs.fhir.fhirpath.types.FhirPathTime
import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateTimeComponentTest :
  FunSpec({
    val engine = FhirPathEngine.forR4()

    // yearOf()
    test("yearOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.yearOf()", null))
    }

    test("yearOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.yearOf()", null))
    }

    test("yearOf() extracts year from Date") {
      assertEquals(listOf(2023), engine.evaluateExpression("@2023-05-15.yearOf()", null))
    }

    test("yearOf() extracts year from DateTime") {
      assertEquals(listOf(2024), engine.evaluateExpression("@2024-01-01T12:30:45Z.yearOf()", null))
    }

    test("yearOf() extracts year from partial Date with only year") {
      assertEquals(listOf(2023), engine.evaluateExpression("@2023.yearOf()", null))
    }

    test("yearOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@2023-01-01 | @2024-01-01).yearOf()", null)
      }
    }

    // monthOf()
    test("monthOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.monthOf()", null))
    }

    test("monthOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.monthOf()", null))
    }

    test("monthOf() returns empty list when Date is missing month") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@2023.monthOf()", null))
    }

    test("monthOf() extracts month from Date") {
      assertEquals(listOf(5), engine.evaluateExpression("@2023-05-15.monthOf()", null))
    }

    test("monthOf() extracts month from DateTime") {
      assertEquals(listOf(11), engine.evaluateExpression("@2023-11-01T12:00:00Z.monthOf()", null))
    }

    test("monthOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@2023-05-15 | @2023-11-01).monthOf()", null)
      }
    }

    // dayOf()
    test("dayOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.dayOf()", null))
    }

    test("dayOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.dayOf()", null))
    }

    test("dayOf() returns empty list when Date is missing day") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@2023-05.dayOf()", null))
    }

    test("dayOf() extracts day from Date") {
      assertEquals(listOf(15), engine.evaluateExpression("@2023-05-15.dayOf()", null))
    }

    test("dayOf() extracts day from DateTime") {
      assertEquals(listOf(30), engine.evaluateExpression("@2023-04-30T10:00:00Z.dayOf()", null))
    }

    test("dayOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@2023-05-15 | @2023-05-16).dayOf()", null)
      }
    }

    // hourOf()
    test("hourOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.hourOf()", null))
    }

    test("hourOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.hourOf()", null))
    }

    test("hourOf() returns empty list when Date is missing hour") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@2023-05-15.hourOf()", null))
    }

    test("hourOf() extracts hour from Time") {
      assertEquals(listOf(14), engine.evaluateExpression("@T14:30:00.hourOf()", null))
    }

    test("hourOf() extracts hour from DateTime") {
      assertEquals(listOf(8), engine.evaluateExpression("@2023-05-15T08:15:00Z.hourOf()", null))
    }

    test("hourOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@T14:30:00 | @T15:30:00).hourOf()", null)
      }
    }

    // minuteOf()
    test("minuteOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.minuteOf()", null))
    }

    test("minuteOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.minuteOf()", null))
    }

    test("minuteOf() returns empty list when Time is missing minute") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@T14.minuteOf()", null))
    }

    test("minuteOf() extracts minute from Time") {
      assertEquals(listOf(30), engine.evaluateExpression("@T14:30:00.minuteOf()", null))
    }

    test("minuteOf() extracts minute from DateTime") {
      assertEquals(listOf(15), engine.evaluateExpression("@2023-05-15T08:15:00Z.minuteOf()", null))
    }

    test("minuteOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@T14:30:00 | @T14:45:00).minuteOf()", null)
      }
    }

    // secondOf()
    test("secondOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.secondOf()", null))
    }

    test("secondOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.secondOf()", null))
    }

    test("secondOf() returns empty list when Time is missing seconds") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@T14:30.secondOf()", null))
    }

    test("secondOf() returns empty list when DateTime is missing seconds") {
      assertEquals(
        emptyList<Any>(),
        engine.evaluateExpression("@2023-05-15T08:15Z.secondOf()", null),
      )
    }

    test("secondOf() extracts integer second from Time ignoring fractional seconds") {
      assertEquals(listOf(45), engine.evaluateExpression("@T14:30:45.123.secondOf()", null))
    }

    test("secondOf() extracts integer second from DateTime ignoring fractional seconds") {
      assertEquals(
        listOf(9),
        engine.evaluateExpression("@2023-05-15T08:15:09.999Z.secondOf()", null),
      )
    }

    test("secondOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@T14:30:45 | @T14:30:50).secondOf()", null)
      }
    }

    // millisecondOf()
    test("millisecondOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.millisecondOf()", null))
    }

    test("millisecondOf() returns empty list when called on invalid type") {
      assertEquals(
        emptyList<Any>(),
        engine.evaluateExpression("'not_a_date'.millisecondOf()", null),
      )
    }

    test("millisecondOf() returns empty list when Time is missing milliseconds") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@T14:30:45.millisecondOf()", null))
    }

    test("millisecondOf() returns empty list when DateTime is missing milliseconds") {
      assertEquals(
        emptyList<Any>(),
        engine.evaluateExpression("@2012-01-01T12:30:40.millisecondOf()", null),
      )
    }

    test("millisecondOf() extracts millisecond from Time") {
      assertEquals(listOf(123), engine.evaluateExpression("@T14:30:45.123.millisecondOf()", null))
    }

    test("millisecondOf() extracts millisecond from DateTime") {
      assertEquals(
        listOf(2),
        engine.evaluateExpression("@2012-01-01T12:30:00.002-07:00.millisecondOf()", null),
      )
    }

    test("millisecondOf() pads 1-digit fractional second to milliseconds") {
      assertEquals(listOf(500), engine.evaluateExpression("@T14:30:45.5.millisecondOf()", null))
    }

    test("millisecondOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@T14:30:45.123 | @T14:30:45.456).millisecondOf()", null)
      }
    }

    // timezoneOffsetOf()
    test("timezoneOffsetOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.timezoneOffsetOf()", null))
    }

    test("timezoneOffsetOf() returns empty list when called on invalid type") {
      assertEquals(
        emptyList<Any>(),
        engine.evaluateExpression("'not_a_date'.timezoneOffsetOf()", null),
      )
    }

    test("timezoneOffsetOf() returns empty list when DateTime is missing timezone offset") {
      assertEquals(
        emptyList<Any>(),
        engine.evaluateExpression("@2023-05-15T12:00:00.timezoneOffsetOf()", null),
      )
    }

    test("timezoneOffsetOf() extracts decimal hours for positive offset") {
      assertEquals(
        listOf(FhirPathDecimal.fromString("5.5")),
        engine.evaluateExpression("@2023-05-15T12:00:00+05:30.timezoneOffsetOf()", null),
      )
    }

    test("timezoneOffsetOf() extracts decimal hours for negative offset") {
      assertEquals(
        listOf(FhirPathDecimal.fromInt(-4)),
        engine.evaluateExpression("@2023-05-15T12:00:00-04:00.timezoneOffsetOf()", null),
      )
    }

    test("timezoneOffsetOf() returns zero for UTC Z offset") {
      assertEquals(
        listOf(FhirPathDecimal.fromInt(0)),
        engine.evaluateExpression("@2023-05-15T12:00:00Z.timezoneOffsetOf()", null),
      )
    }

    test("timezoneOffsetOf() extracts 45-minute decimal offset") {
      assertEquals(
        listOf(FhirPathDecimal.fromString("8.75")),
        engine.evaluateExpression("@2012-01-01T12:30:00.000+08:45.timezoneOffsetOf()", null),
      )
    }

    test("timezoneOffsetOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression(
          "(@2023-05-15T12:00:00Z | @2023-05-15T12:00:00+01:00).timezoneOffsetOf()",
          null,
        )
      }
    }

    // dateOf()
    test("dateOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.dateOf()", null))
    }

    test("dateOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.dateOf()", null))
    }

    test("dateOf() returns identity when called on Date") {
      assertEquals(
        listOf(FhirPathDate(2023, 5, 15)),
        engine.evaluateExpression("@2023-05-15.dateOf()", null),
      )
    }

    test("dateOf() extracts Date from DateTime") {
      assertEquals(
        listOf(FhirPathDate(2023, 5, 15)),
        engine.evaluateExpression("@2023-05-15T12:30:00Z.dateOf()", null),
      )
    }

    test("dateOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@2023-05-15 | @2023-05-16).dateOf()", null)
      }
    }

    // timeOf()
    test("timeOf() returns empty list when called on empty collection") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("{}.timeOf()", null))
    }

    test("timeOf() returns empty list when called on invalid type") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("'not_a_date'.timeOf()", null))
    }

    test("timeOf() returns empty list when Date is missing time") {
      assertEquals(emptyList<Any>(), engine.evaluateExpression("@2023-05-15.timeOf()", null))
    }

    test("timeOf() returns identity when called on Time") {
      assertEquals(
        listOf(FhirPathTime(14, 30, 45.toBigDecimal())),
        engine.evaluateExpression("@T14:30:45.timeOf()", null),
      )
    }

    test("timeOf() extracts Time from DateTime") {
      assertEquals(
        listOf(FhirPathTime(12, 30, 45.toBigDecimal())),
        engine.evaluateExpression("@2023-05-15T12:30:45Z.timeOf()", null),
      )
    }

    test("timeOf() throws exception when called on collection with multiple items") {
      assertFailsWith<IllegalStateException> {
        engine.evaluateExpression("(@T14:30:45 | @T15:30:45).timeOf()", null)
      }
    }
  })
