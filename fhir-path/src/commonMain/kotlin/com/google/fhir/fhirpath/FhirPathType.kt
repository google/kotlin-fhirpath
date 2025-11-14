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

package com.google.fhir.fhirpath

import com.google.fhir.fhirpath.ext.getFhirType
import com.google.fhir.fhirpath.types.FhirPathDateTime
import com.google.fhir.fhirpath.types.FhirPathTime
import com.google.fhir.model.r4.FhirDate
import com.google.fhir.model.r4.Quantity
import com.google.fhir.model.r4.Resource
import com.google.fhir.model.r4.terminologies.ResourceType
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * A FHIRPath type. It can be either a FHIR type or a system type.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#types-and-reflection).
 */
sealed interface FhirPathType {
  val namespace: String
  val typeName: String

  companion object {
    fun fromString(string: String): FhirPathType {
      val parts = string.split('.')
      val name = parts.last()

      if (string.contains('.')) {
        val (namespace, name) = string.split('.')
        when (namespace) {
          "FHIR" -> {
            FhirPrimitiveType.fromString(name)?.let {
              return it
            }
            FhirComplexType.fromString(name)?.let {
              return it
            }
            return FhirResourceType(ResourceType.fromCode(name))
          }
          "System" -> {
            return requireNotNull(SystemType.fromString(name)) { "Unknown System type $name" }
          }
          else -> error("Unknown namespace $namespace for type")
        }
      }

      // Unqualified type names are resolved as FHIRPath system types first and then FHIR types.
      SystemType.fromString(name)?.let {
        return it
      }
      FhirPrimitiveType.fromString(name)?.let {
        return it
      }
      FhirComplexType.fromString(name)?.let {
        return it
      }
      return try {
        FhirResourceType(ResourceType.fromCode(name))
      } catch (_: Exception) {
        error("Unknown type $string")
      }
    }

    fun fromObject(value: Any): FhirPathType? {
      SystemType.fromObject(value)?.let {
        return it
      }
      FhirType.fromObject(value)?.let {
        return it
      }
      return null
    }
  }
}

sealed interface FhirType : FhirPathType {
  override val namespace: String
    get() = "FHIR"

  abstract override val typeName: String

  companion object {
    fun fromObject(value: Any): FhirType? {
      FhirPrimitiveType.fromObject(value)?.let {
        return it
      }
      FhirComplexType.fromObject(value)?.let {
        return it
      }
      (value as? Resource)?.getFhirType()?.let {
        return it
      }
      return null
    }
  }
}

data class FhirResourceType(val resourceType: ResourceType) : FhirType {
  override val typeName: String
    get() = resourceType.getCode()
}

enum class SystemType(override val typeName: String) : FhirPathType {
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
    fun fromString(value: String): SystemType? {
      return entries.find { it.typeName == value }
    }

    fun fromObject(value: Any): SystemType? {
      return when (value) {
        is Boolean -> BOOLEAN
        is String -> STRING
        is Int -> INTEGER
        is Long -> LONG
        is BigDecimal -> DECIMAL
        is FhirDate -> DATE
        is FhirPathDateTime -> DATETIME
        is FhirPathTime -> TIME
        is Quantity -> QUANTITY
        else -> null
      }
    }
  }
}
