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

package com.google.fhir.fhirpath.codegen.model.schema

import com.squareup.kotlinpoet.ClassName

/**
 * Determines if an [Element] is a BackboneElement.
 *
 * In the FHIR resource ElementDefinition, there are a number of fields (e.g. `slicing`, `base`,
 * `type`) of type Element. However, for the purpose of code generation, they should be treated as
 * BackboneElements and nested classes should be generated for them.
 *
 * They are the only known instances of elements of type Element in StructureDefinitions. Therefore,
 * for simplicity, this function treats all elements of type Element as BackboneElements.
 */
fun Element.isBackboneElement(): Boolean {
  val typeCode = type?.singleOrNull()?.code
  return typeCode == "BackboneElement" || typeCode == "Element"
}

fun Element.getElementName() = path.substringAfterLast('.').removeSuffix("[x]")

fun Element.getNestedClassName(className: ClassName) =
  path.split(".").drop(1).fold(className) { nestedName, name ->
    nestedName.nestedClass(name.capitalized())
  }
