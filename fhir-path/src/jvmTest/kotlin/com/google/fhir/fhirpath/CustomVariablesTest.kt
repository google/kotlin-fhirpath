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

  @Test
  fun `should resolve custom variable`() {
    val result =
      evaluateFhirPath(
        expression = "%myString",
        resource = null,
        variables = mapOf("myString" to "hello"),
      )
    assertEquals(listOf("hello"), result.toList())
  }

  @Test
  fun `should resolve null variable as empty`() {
    val result =
      evaluateFhirPath(
        expression = "%nullVar",
        resource = null,
        variables = mapOf("nullVar" to null),
      )
    assertEquals(emptyList<Any>(), result.toList())
  }

  @Test
  fun `should throw for unknown variable`() {
    assertFailsWith<Exception> { evaluateFhirPath(expression = "%unknownVar", resource = null) }
  }
}
