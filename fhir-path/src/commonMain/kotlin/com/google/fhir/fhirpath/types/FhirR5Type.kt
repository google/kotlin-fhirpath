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

package com.google.fhir.fhirpath.types

import com.google.fhir.model.r5.terminologies.ResourceType

internal sealed interface FhirR5Type : FhirType {
  abstract override val typeName: String
  override val fhirVersion: FhirVersion
    get() = FhirVersion.R5
}

internal data class FhirR5ResourceType(val resourceType: ResourceType) : FhirR5Type {
  override val typeName: String = resourceType.getCode()
}
