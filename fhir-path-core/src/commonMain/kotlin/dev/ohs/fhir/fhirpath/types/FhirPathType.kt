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

package dev.ohs.fhir.fhirpath.types

/**
 * Engine-internal representation of a type in the FHIRPath type system.
 *
 * Used internally for type resolution, type checking, and dispatch (e.g. evaluating operators and
 * functions like `is`, `as`, and `ofType()`). It represents a resolved `typeSpecifier` grammar
 * symbol like `System.Integer` or `FHIR.Patient`.
 *
 * Distinct from [TypeInfo]:
 * - [FhirPathType] is an engine-internal descriptor and is never part of a FHIRPath collection.
 * - [TypeInfo] is a runtime reflection object returned by `type()` as part of a FHIRPath collection
 *   and exposes properties (`.namespace`, `.name`, `.baseType`).
 *
 * Taxonomy:
 * - [FhirPathType]: all valid types in FHIRPath
 *     - [FhirType]: sealed interface representing FHIR model types
 *         - `FhirR4Type` (`FhirR4PrimitiveType`, `FhirR4ComplexType`, `FhirR4ResourceType`)
 *         - `FhirR4BType` (`FhirR4BPrimitiveType`, `FhirR4BComplexType`, `FhirR4BResourceType`)
 *         - `FhirR5Type` (`FhirR5PrimitiveType`, `FhirR5ComplexType`, `FhirR5ResourceType`)
 *     - [FhirPathSystemType]: enum class representing FHIRPath system types
 *
 * In FHIR, primitive types (e.g. FHIR `string`) are defined with underlying FHIRPath system types
 * (e.g. `System.String`). In our implementation, FHIR types are preserved as long as possible to
 * retain metadata (`id`, `extension`), but unwrapped to system types when needed for calculations.
 *
 * See [FHIRPath Specification](https://hl7.org/fhirpath/N1/#types-and-reflection) and
 * [FHIR Types in FHIRPath](https://fhir.hl7.org/fhir/fhirpath.html#types).
 */
interface FhirPathType {
  val namespace: String
  val typeName: String

  fun isSubtypeOf(superType: FhirPathType): Boolean {
    if (this == superType || typeName == superType.typeName) return true
    return isPrimitiveSubtypeOf(superType) ||
      isQuantitySubtypeOf(superType) ||
      isResourceSubtypeOf(superType)
  }

  private fun isPrimitiveSubtypeOf(superType: FhirPathType): Boolean {
    return when (superType.typeName.lowercase()) {
      "uri" ->
        when (typeName) {
          "uuid",
          "oid",
          "url",
          "canonical",
          "uri" -> true
          else -> false
        }
      "string" ->
        when (typeName) {
          "code",
          "id",
          "markdown",
          "string" -> true
          else -> false
        }
      "integer" ->
        when (typeName) {
          "positiveInt",
          "unsignedInt",
          "integer" -> true
          else -> false
        }
      else -> false
    }
  }

  private fun isQuantitySubtypeOf(superType: FhirPathType): Boolean {
    if (!superType.typeName.equals("Quantity", ignoreCase = true)) return false
    return when (typeName) {
      "Age",
      "Count",
      "Distance",
      "Duration",
      "Quantity" -> true
      else -> false
    }
  }

  private fun isResourceSubtypeOf(superType: FhirPathType): Boolean {
    if (superType.typeName != "Resource" && superType.typeName != "DomainResource") return false
    return this is FhirType && this.typeName != "Element"
  }
}

interface FhirType : FhirPathType {
  override val namespace: String
    get() = "FHIR"

  val fhirVersion: FhirVersion
  abstract override val typeName: String
}

/**
 * Marker for FHIR primitive types (e.g. `string`, `code`, `boolean`). Primitives are Elements whose
 * actual value lives in a `value` property, which may be absent when the element only carries an
 * `id` or `extension`; `hasValue()` relies on this distinction.
 */
interface FhirPrimitiveType : FhirType

enum class FhirPathSystemType(override val typeName: String) : FhirPathType {
  BOOLEAN("Boolean"),
  STRING("String"),
  INTEGER("Integer"),
  LONG("Long"),
  DECIMAL("Decimal"),
  DATE("Date"),
  DATETIME("DateTime"),
  TIME("Time"),
  QUANTITY("Quantity"),

  /**
   * While not defined as a system type in the FHIRPath specification, this addition gives
   * [TypeInfo] values a resolvable type, allowing expressions such as `(1.type()).type()` to work.
   */
  TYPE_INFO("TypeInfo");

  override val namespace = "System"

  companion object {
    fun fromString(value: String): FhirPathSystemType? {
      return entries.find { it.typeName == value }
    }

    fun fromObject(value: Any): FhirPathSystemType? {
      return when (value) {
        is TypeInfo -> TYPE_INFO
        is Boolean -> BOOLEAN
        is String -> STRING
        is Int -> INTEGER
        is Long -> LONG
        is FhirPathDecimal -> DECIMAL
        is FhirPathDate -> DATE
        is FhirPathDateTime -> DATETIME
        is FhirPathTime -> TIME
        is FhirPathQuantity -> QUANTITY
        else -> null
      }
    }
  }
}
