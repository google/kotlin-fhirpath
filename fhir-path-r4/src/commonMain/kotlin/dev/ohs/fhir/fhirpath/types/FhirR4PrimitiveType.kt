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

import dev.ohs.fhir.model.r4.Enumeration
import kotlin.Any

public enum class FhirR4PrimitiveType(override val typeName: kotlin.String) :
  FhirR4Type, FhirPrimitiveType {
  Base64Binary("base64Binary"),
  Boolean("boolean"),
  Canonical("canonical"),
  Code("code"),
  Date("date"),
  DateTime("dateTime"),
  Decimal("decimal"),
  Id("id"),
  Instant("instant"),
  Integer("integer"),
  Markdown("markdown"),
  Oid("oid"),
  PositiveInt("positiveInt"),
  String("string"),
  Time("time"),
  UnsignedInt("unsignedInt"),
  Uri("uri"),
  Url("url"),
  Uuid("uuid"),
  Xhtml("xhtml");

  public companion object {
    public fun fromString(`value`: kotlin.String): FhirR4PrimitiveType? =
      entries.find { it.typeName == value }

    public fun fromObject(`value`: Any): FhirR4PrimitiveType? =
      when (value) {
        is dev.ohs.fhir.model.r4.Base64Binary -> Base64Binary
        is dev.ohs.fhir.model.r4.Boolean -> Boolean
        is dev.ohs.fhir.model.r4.Canonical -> Canonical
        is dev.ohs.fhir.model.r4.Code -> Code
        is dev.ohs.fhir.model.r4.Date -> Date
        is dev.ohs.fhir.model.r4.DateTime -> DateTime
        is dev.ohs.fhir.model.r4.Decimal -> Decimal
        is dev.ohs.fhir.model.r4.Id -> Id
        is dev.ohs.fhir.model.r4.Instant -> Instant
        is dev.ohs.fhir.model.r4.PositiveInt -> PositiveInt
        is dev.ohs.fhir.model.r4.UnsignedInt -> UnsignedInt
        is dev.ohs.fhir.model.r4.Integer -> Integer
        is dev.ohs.fhir.model.r4.Markdown -> Markdown
        is dev.ohs.fhir.model.r4.Oid -> Oid
        is dev.ohs.fhir.model.r4.String -> String
        is dev.ohs.fhir.model.r4.Time -> Time
        is dev.ohs.fhir.model.r4.Url -> Url
        is dev.ohs.fhir.model.r4.Uuid -> Uuid
        is dev.ohs.fhir.model.r4.Uri -> Uri
        is dev.ohs.fhir.model.r4.Xhtml -> Xhtml
        is Enumeration<*> -> Code
        else -> null
      }
  }
}
