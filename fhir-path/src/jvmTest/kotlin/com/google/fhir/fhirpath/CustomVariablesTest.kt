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

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class CustomVariablesTest {

  // identifier path tests

  @Test
  fun `1 - identifier - camelCase`() {
    val result =
      evaluateFhirPath(
        expression = "%myVar",
        resource = null,
        variables = mapOf("myVar" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `2 - identifier - snake_case`() {
    val result =
      evaluateFhirPath(
        expression = "%my_var",
        resource = null,
        variables = mapOf("my_var" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `3 - identifier - mixed case with numbers`() {
    val result =
      evaluateFhirPath(
        expression = "%MyVar123",
        resource = null,
        variables = mapOf("MyVar123" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `4 - identifier - underscore prefix`() {
    val result =
      evaluateFhirPath(
        expression = "%_private",
        resource = null,
        variables = mapOf("_private" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `5 - delimited identifier - kebab-case in backticks`() {
    val result =
      evaluateFhirPath(
        expression = "%`my-var`",
        resource = null,
        variables = mapOf("my-var" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `6 - delimited identifier - spaces in backticks`() {
    val result =
      evaluateFhirPath(
        expression = "%`has spaces`",
        resource = null,
        variables = mapOf("has spaces" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `7 - delimited identifier - number prefix in backticks`() {
    val result =
      evaluateFhirPath(
        expression = "%`123start`",
        resource = null,
        variables = mapOf("123start" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  // STRING path tests

  @Test
  fun `8 - string - kebab-case in single quotes`() {
    val result =
      evaluateFhirPath(
        expression = "%'my-var'",
        resource = null,
        variables = mapOf("my-var" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `9 - string - spaces in single quotes`() {
    val result =
      evaluateFhirPath(
        expression = "%'has spaces'",
        resource = null,
        variables = mapOf("has spaces" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `10 - string - number prefix in single quotes`() {
    val result =
      evaluateFhirPath(
        expression = "%'123start'",
        resource = null,
        variables = mapOf("123start" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `11 - string - special characters in single quotes`() {
    val result =
      evaluateFhirPath(
        expression = "%'any.thing!@#'",
        resource = null,
        variables = mapOf("any.thing!@#" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  // Invalid (parse errors)

  @Test
  fun `12 - invalid - kebab-case without quotes`() {
    assertFailsWith<Exception> {
      evaluateFhirPath(
        expression = "%my-var",
        resource = null,
        variables = mapOf("my-var" to "hello"),
      )
    }
  }

  @Test
  fun `13 - invalid - number prefix without quotes`() {
    assertFailsWith<Exception> {
      evaluateFhirPath(
        expression = "%123var",
        resource = null,
        variables = mapOf("123var" to "hello"),
      )
    }
  }

  // Other tests

  @Test
  fun `14 - null variable returns empty`() {
    val result =
      evaluateFhirPath(
        expression = "%nullVar",
        resource = null,
        variables = mapOf("nullVar" to null),
      )
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `15 - unknown variable throws error`() {
    assertFailsWith<Exception> { evaluateFhirPath(expression = "%unknownVar", resource = null) }
  }
}
