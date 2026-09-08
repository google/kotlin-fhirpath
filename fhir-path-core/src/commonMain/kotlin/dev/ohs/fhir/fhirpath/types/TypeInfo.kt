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

package dev.ohs.fhir.fhirpath.types

/**
 * A runtime evaluation value returned by the `type()` function.
 *
 * Unlike [FhirPathType], which is an engine-internal type descriptor used for type resolution and
 * checking (e.g. by `is`, `as`, `ofType()`), a [TypeInfo] is a first-class value in a FHIRPath
 * collection. It also has accessible properties (`.namespace`, `.name`, `.baseType`).
 *
 * The FHIRPath specification defines the [TypeInfo] structures and the `type()` function that
 * returns them, but does not define [TypeInfo] as a system type (like `Boolean` or `String`), nor
 * does it provide a literal syntax for it, or specify what `(1.type()).type()` should return. In
 * this implementation, we assign [TypeInfo] values the type [FhirPathSystemType.TYPE_INFO]
 * (`System.TypeInfo`) so that self-reflection works.
 *
 * The FHIRPath specification defines two concrete subtypes:
 * - [SimpleTypeInfo]: for primitive types (e.g. `System.String`, `FHIR.boolean`)
 * - [ClassInfo]: for complex types and resources (e.g. `FHIR.Patient`, `FHIR.HumanName`)
 *
 * See [specification](https://hl7.org/fhirpath/STU3/en/#structures).
 */
sealed interface TypeInfo {
  val namespace: String
  val name: String
  val baseType: String
}

/** [TypeInfo] for primitive types. */
data class SimpleTypeInfo(
  override val namespace: String,
  override val name: String,
  override val baseType: String = "System.Any",
) : TypeInfo

/** [TypeInfo] for complex types and resources. */
data class ClassInfo(
  override val namespace: String,
  override val name: String,
  override val baseType: String = "FHIR.Element",
) : TypeInfo
