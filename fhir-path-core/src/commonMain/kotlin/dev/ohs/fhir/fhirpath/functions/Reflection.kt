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

package dev.ohs.fhir.fhirpath.functions

import dev.ohs.fhir.fhirpath.types.ClassInfo
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import dev.ohs.fhir.fhirpath.types.SimpleTypeInfo
import dev.ohs.fhir.fhirpath.types.TypeInfo

/** See [specification](https://hl7.org/fhirpath/STU3/en/#type--collection). */
internal fun Collection<Any>.type(
  fhirPathTypeResolver: FhirPathTypeResolver
): Collection<TypeInfo> = mapNotNull { item ->
  val fhirPathType = fhirPathTypeResolver.resolveFromObject(item) ?: return@mapNotNull null
  val isSystemType = fhirPathType.namespace == "System"
  if (isSystemType) {
    SimpleTypeInfo(namespace = fhirPathType.namespace, name = fhirPathType.typeName)
  } else {
    ClassInfo(namespace = fhirPathType.namespace, name = fhirPathType.typeName)
  }
}
