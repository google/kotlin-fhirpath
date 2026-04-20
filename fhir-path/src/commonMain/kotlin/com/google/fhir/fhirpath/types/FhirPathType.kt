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

package com.google.fhir.fhirpath.types

import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * A FHIRPath type. The taxonomy of FHIRPath types is as follows:
 * - `FhirPathType`: all valid types in FHIRPath
 *     - `FhirType`: sealed interface below representing FHIR types used in FHIRPath
 *         - `FhirR4Type`
 *             - `FhirR4PrimitiveType`: generated enum class
 *             - `FhirR4ComplexType`: generated enum class
 *             - `FhirR4ResourceType`: wrapper around `com.google.fhir.model.r4.terminologies.ResourceType`
 *         - `FhirR4BType`
 *             - `FhirR4BPrimitiveType`: generated enum class
 *             - `FhirR4BComplexType`: generated enum class
 *             - `FhirR4BResourceType`: wrapper around
 *               `com.google.fhir.model.r4b.terminologies.ResourceType`
 *         - `FhirR5Type`
 *             - `FhirR5PrimitiveType`: generated enum class
 *             - `FhirR5ComplexType`: generated enum class
 *             - `FhirR5ResourceType`: wrapper around `com.google.fhir.model.r5.terminologies.ResourceType`
 *     - `FhirPathSystemType`: enum class below representing system types used in FHIRPath
 *
 * In FHIR, primitive types are defined using FHIRPath system types as underlying types. For
 * example, FHIR String might have data elements such as `id` and `extension`, but uses a FHIRPath
 * system string type for its actual value. In our implementation, FHIR types are preserved as long
 * as possible to preserve type information and data elements, but are unwrapped as FHIRPath system
 * types when necessary (e.g. for mathematical calculations).
 *
 * See [specification](https://hl7.org/fhirpath/N1/#types-and-reflection).
 *
 * For using FHIR types in FHIRPath, see
 * [specification](https://fhir.hl7.org/fhir/fhirpath.html#types).
 */
internal sealed interface FhirPathType {
  val namespace: String
  val typeName: String
}

internal sealed interface FhirType : FhirPathType {
  override val namespace: String
    get() = "FHIR"

  val fhirVersion: FhirVersion
  abstract override val typeName: String
}

internal enum class FhirPathSystemType(override val typeName: String) : FhirPathType {
  BOOLEAN("Boolean"),
  STRING("String"),
  INTEGER("Integer"),
  LONG("Long"),
  DECIMAL("Decimal"),
  DATE("Date"),
  DATETIME("DateTime"),
  TIME("Time"),
  QUANTITY("Quantity");

  override val namespace = "System"

  companion object {
    fun fromString(value: String): FhirPathSystemType? {
      return entries.find { it.typeName == value }
    }

    fun fromObject(value: Any): FhirPathSystemType? {
      return when (value) {
        is Boolean -> BOOLEAN
        is String -> STRING
        is Int -> INTEGER
        is Long -> LONG
        is BigDecimal -> DECIMAL
        is FhirPathDate -> DATE
        is FhirPathDateTime -> DATETIME
        is FhirPathTime -> TIME
        is FhirPathQuantity -> QUANTITY
        else -> null
      }
    }
  }
}
