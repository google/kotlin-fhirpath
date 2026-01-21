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

package com.google.fhir.fhirpath.model

import com.google.fhir.model.r4.BackboneElement
import com.google.fhir.model.r4.Element
import com.google.fhir.model.r4.Resource
import com.google.fhir.model.r4.ext.getAllChildren
import com.google.fhir.model.r4.ext.getProperty
import com.google.fhir.model.r4.ext.getPropertyInChoiceValue
import com.google.fhir.model.r4.ext.hasProperty
import com.google.fhir.model.r4.ext.hasPropertyInChoiceValue
import com.google.fhir.model.r4.ext.unwrapChoiceValue

internal object FhirR4ModelNavigator : FhirModelNavigator() {
  override fun hasProperty(obj: Any, propertyName: String): Boolean {
    return when (obj) {
      is Resource -> obj.hasProperty(propertyName)
      is BackboneElement -> obj.hasProperty(propertyName)
      is Element -> obj.hasProperty(propertyName)
      else -> obj.hasPropertyInChoiceValue(propertyName)
    }
  }

  override fun getProperty(obj: Any, propertyName: String): Any? {
    return when (obj) {
      is Resource -> {
        obj.getProperty(propertyName)
      }
      is BackboneElement -> {
        obj.getProperty(propertyName)
      }
      is Element -> {
        obj.getProperty(propertyName)
      }
      // TODO: get value from FHIR primitive types (e.g. extension value)

      // Sealed interface
      else -> obj.getPropertyInChoiceValue(propertyName)
    }
  }

  override fun unwrapProperty(any: Any): Any? {
    return any.unwrapChoiceValue() ?: any
  }

  override fun getAllChildren(item: Any): Collection<Any> =
    when (item) {
      is Resource -> item.getAllChildren()
      is BackboneElement -> item.getAllChildren()
      is Element -> item.getAllChildren()
      else -> emptyList()
    }
}
