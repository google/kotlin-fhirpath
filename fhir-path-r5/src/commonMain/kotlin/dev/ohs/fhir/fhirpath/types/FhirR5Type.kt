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

import dev.ohs.fhir.model.r5.Resource
import dev.ohs.fhir.model.r5.ext.getFhirType
import dev.ohs.fhir.model.r5.terminologies.ResourceType

interface FhirR5Type : FhirType {
  abstract override val typeName: String
  override val fhirVersion: FhirVersion
    get() = FhirVersion.R5

  companion object {
    fun fromObject(value: Any): FhirType? {
      FhirR5PrimitiveType.fromObject(value)?.let {
        return it
      }
      FhirR5ComplexType.fromObject(value)?.let {
        return it
      }
      (value as? Resource)?.getFhirType()?.let {
        return it
      }
      return null
    }
  }
}

data class FhirR5ResourceType(val resourceType: ResourceType) : FhirR5Type {
  override val typeName: String = resourceType.code
}
