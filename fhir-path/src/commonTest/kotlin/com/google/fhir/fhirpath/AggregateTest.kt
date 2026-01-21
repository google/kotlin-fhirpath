/*
 * Copyright 2026 Google LLC
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

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

private val fhirPathEngine = FhirPathEngine.forR4()

class AggregateTest :
  FunSpec({
    test("nested aggregate inner total is independent from outer total") {
      val result =
        fhirPathEngine.evaluateFhirPath(
          "(1 | 2).aggregate((10 | 20 | 30).aggregate(\$total + \$this, 0) + \$total + \$this, 0)",
          null,
        )
      assertEquals(listOf(123), result.toList())
    }
  })
