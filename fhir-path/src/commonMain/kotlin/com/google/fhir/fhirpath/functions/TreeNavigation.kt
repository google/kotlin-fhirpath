/*
 * Copyright 2025 Google LLC
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

package com.google.fhir.fhirpath.functions

import com.google.fhir.fhirpath.ext.getAllChildren
import com.google.fhir.model.r4.BackboneElement
import com.google.fhir.model.r4.Element
import com.google.fhir.model.r4.Resource

/**
 * Returns all immediate child nodes from each item in the input collection.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#tree-navigation).
 */
internal fun Collection<Any>.children(): Collection<Any> {
  return flatMap { item ->
    when (item) {
      is Resource -> item.getAllChildren()
      is BackboneElement -> item.getAllChildren()
      is Element -> item.getAllChildren()
      else -> emptyList()
    }
  }
}

/**
 * Returns all descendant nodes of every item in the input collection.
 *
 * Excludes the nodes in the input collection itself (only descendants, not the starting node). This
 * is a shorthand for `repeat(children())`.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#tree-navigation).
 */
internal fun Collection<Any>.descendants(): Collection<Any> =
  children().let { children ->
    if (children.isEmpty()) emptyList() else children + children.descendants()
  }
