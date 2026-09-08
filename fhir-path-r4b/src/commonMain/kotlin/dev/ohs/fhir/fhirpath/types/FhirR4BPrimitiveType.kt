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

import dev.ohs.fhir.model.r4b.Enumeration
import kotlin.Any

public enum class FhirR4BPrimitiveType(override val typeName: kotlin.String) :
  FhirR4BType, FhirPrimitiveType {
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
    public fun fromString(`value`: kotlin.String): FhirR4BPrimitiveType? =
      entries.find { it.typeName == value }

    public fun fromObject(`value`: Any): FhirR4BPrimitiveType? =
      when (value) {
        is dev.ohs.fhir.model.r4b.Base64Binary -> Base64Binary
        is dev.ohs.fhir.model.r4b.Boolean -> Boolean
        is dev.ohs.fhir.model.r4b.Canonical -> Canonical
        is dev.ohs.fhir.model.r4b.Code -> Code
        is dev.ohs.fhir.model.r4b.Date -> Date
        is dev.ohs.fhir.model.r4b.DateTime -> DateTime
        is dev.ohs.fhir.model.r4b.Decimal -> Decimal
        is dev.ohs.fhir.model.r4b.Id -> Id
        is dev.ohs.fhir.model.r4b.Instant -> Instant
        is dev.ohs.fhir.model.r4b.PositiveInt -> PositiveInt
        is dev.ohs.fhir.model.r4b.UnsignedInt -> UnsignedInt
        is dev.ohs.fhir.model.r4b.Integer -> Integer
        is dev.ohs.fhir.model.r4b.Markdown -> Markdown
        is dev.ohs.fhir.model.r4b.Oid -> Oid
        is dev.ohs.fhir.model.r4b.String -> String
        is dev.ohs.fhir.model.r4b.Time -> Time
        is dev.ohs.fhir.model.r4b.Url -> Url
        is dev.ohs.fhir.model.r4b.Uuid -> Uuid
        is dev.ohs.fhir.model.r4b.Uri -> Uri
        is dev.ohs.fhir.model.r4b.Xhtml -> Xhtml
        is Enumeration<*> -> Code
        else -> null
      }
  }
}
