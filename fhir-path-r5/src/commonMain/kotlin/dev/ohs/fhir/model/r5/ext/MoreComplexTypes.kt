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

package dev.ohs.fhir.model.r5.ext

import dev.ohs.fhir.model.r5.Address
import dev.ohs.fhir.model.r5.Age
import dev.ohs.fhir.model.r5.Annotation
import dev.ohs.fhir.model.r5.Attachment
import dev.ohs.fhir.model.r5.Availability
import dev.ohs.fhir.model.r5.BackboneElement
import dev.ohs.fhir.model.r5.Base64Binary
import dev.ohs.fhir.model.r5.Boolean as R5Boolean
import dev.ohs.fhir.model.r5.Canonical
import dev.ohs.fhir.model.r5.Code
import dev.ohs.fhir.model.r5.CodeableConcept
import dev.ohs.fhir.model.r5.CodeableReference
import dev.ohs.fhir.model.r5.Coding
import dev.ohs.fhir.model.r5.ContactDetail
import dev.ohs.fhir.model.r5.ContactPoint
import dev.ohs.fhir.model.r5.Contributor
import dev.ohs.fhir.model.r5.Count
import dev.ohs.fhir.model.r5.DataRequirement
import dev.ohs.fhir.model.r5.Date
import dev.ohs.fhir.model.r5.DateTime
import dev.ohs.fhir.model.r5.Decimal
import dev.ohs.fhir.model.r5.Distance
import dev.ohs.fhir.model.r5.Dosage
import dev.ohs.fhir.model.r5.Duration
import dev.ohs.fhir.model.r5.Element
import dev.ohs.fhir.model.r5.ElementDefinition
import dev.ohs.fhir.model.r5.Enumeration
import dev.ohs.fhir.model.r5.Expression
import dev.ohs.fhir.model.r5.ExtendedContactDetail
import dev.ohs.fhir.model.r5.Extension
import dev.ohs.fhir.model.r5.HumanName
import dev.ohs.fhir.model.r5.Id
import dev.ohs.fhir.model.r5.Identifier
import dev.ohs.fhir.model.r5.Instant
import dev.ohs.fhir.model.r5.Integer
import dev.ohs.fhir.model.r5.Integer64
import dev.ohs.fhir.model.r5.Markdown
import dev.ohs.fhir.model.r5.MarketingStatus
import dev.ohs.fhir.model.r5.Meta
import dev.ohs.fhir.model.r5.MonetaryComponent
import dev.ohs.fhir.model.r5.Money
import dev.ohs.fhir.model.r5.Narrative
import dev.ohs.fhir.model.r5.Oid
import dev.ohs.fhir.model.r5.ParameterDefinition
import dev.ohs.fhir.model.r5.Period
import dev.ohs.fhir.model.r5.PositiveInt
import dev.ohs.fhir.model.r5.ProductShelfLife
import dev.ohs.fhir.model.r5.Quantity
import dev.ohs.fhir.model.r5.Range
import dev.ohs.fhir.model.r5.Ratio
import dev.ohs.fhir.model.r5.RatioRange
import dev.ohs.fhir.model.r5.Reference
import dev.ohs.fhir.model.r5.RelatedArtifact
import dev.ohs.fhir.model.r5.SampledData
import dev.ohs.fhir.model.r5.Signature
import dev.ohs.fhir.model.r5.String as R5String
import dev.ohs.fhir.model.r5.Time
import dev.ohs.fhir.model.r5.Timing
import dev.ohs.fhir.model.r5.TriggerDefinition
import dev.ohs.fhir.model.r5.UnsignedInt
import dev.ohs.fhir.model.r5.Uri
import dev.ohs.fhir.model.r5.Url
import dev.ohs.fhir.model.r5.UsageContext
import dev.ohs.fhir.model.r5.Uuid
import dev.ohs.fhir.model.r5.VirtualServiceDetail
import dev.ohs.fhir.model.r5.Xhtml
import kotlin.Any
import kotlin.Boolean as KotlinBoolean
import kotlin.String as KotlinString
import kotlin.collections.List

internal fun Enumeration<*>.getProperty(name: KotlinString): Any? =
  when (name) {
    "id" -> this.id
    "extension" -> this.extension
    "value" -> this.value
    else -> error("$name is not a valid property name")
  }

internal fun Enumeration<*>.hasProperty(name: KotlinString): KotlinBoolean =
  when (name) {
    "id" -> true
    "extension" -> true
    "value" -> true
    else -> false
  }

internal fun Enumeration<*>.getAllChildren(): List<Any> = buildList {
  this@getAllChildren.id?.let { add(it) }
  addAll(this@getAllChildren.extension)
  this@getAllChildren.value?.let { add(it) }
}

internal fun Element.getProperty(name: KotlinString): Any? =
  when (this) {
    is Address -> getProperty(name)
    is Age -> getProperty(name)
    is Annotation -> getProperty(name)
    is Attachment -> getProperty(name)
    is Availability -> getProperty(name)
    is BackboneElement -> getProperty(name)
    is CodeableConcept -> getProperty(name)
    is CodeableReference -> getProperty(name)
    is Coding -> getProperty(name)
    is ContactDetail -> getProperty(name)
    is ContactPoint -> getProperty(name)
    is Contributor -> getProperty(name)
    is Count -> getProperty(name)
    is DataRequirement -> getProperty(name)
    is Distance -> getProperty(name)
    is Dosage -> getProperty(name)
    is Duration -> getProperty(name)
    is ElementDefinition -> getProperty(name)
    is Expression -> getProperty(name)
    is ExtendedContactDetail -> getProperty(name)
    is Extension -> getProperty(name)
    is HumanName -> getProperty(name)
    is Identifier -> getProperty(name)
    is MarketingStatus -> getProperty(name)
    is Meta -> getProperty(name)
    is MonetaryComponent -> getProperty(name)
    is Money -> getProperty(name)
    is Narrative -> getProperty(name)
    is ParameterDefinition -> getProperty(name)
    is Period -> getProperty(name)
    is ProductShelfLife -> getProperty(name)
    is Quantity -> getProperty(name)
    is Range -> getProperty(name)
    is Ratio -> getProperty(name)
    is RatioRange -> getProperty(name)
    is Reference -> getProperty(name)
    is RelatedArtifact -> getProperty(name)
    is SampledData -> getProperty(name)
    is Signature -> getProperty(name)
    is Timing -> getProperty(name)
    is TriggerDefinition -> getProperty(name)
    is UsageContext -> getProperty(name)
    is VirtualServiceDetail -> getProperty(name)
    is Base64Binary -> getProperty(name)
    is R5Boolean -> getProperty(name)
    is Canonical -> getProperty(name)
    is Code -> getProperty(name)
    is Date -> getProperty(name)
    is DateTime -> getProperty(name)
    is Decimal -> getProperty(name)
    is Id -> getProperty(name)
    is Instant -> getProperty(name)
    is Integer -> getProperty(name)
    is Integer64 -> getProperty(name)
    is Markdown -> getProperty(name)
    is Oid -> getProperty(name)
    is PositiveInt -> getProperty(name)
    is R5String -> getProperty(name)
    is Time -> getProperty(name)
    is UnsignedInt -> getProperty(name)
    is Uri -> getProperty(name)
    is Url -> getProperty(name)
    is Uuid -> getProperty(name)
    is Xhtml -> getProperty(name)
    is Enumeration<*> -> getProperty(name)
    else -> null
  }

internal fun Element.hasProperty(name: KotlinString): KotlinBoolean =
  when (this) {
    is Address -> hasProperty(name)
    is Age -> hasProperty(name)
    is Annotation -> hasProperty(name)
    is Attachment -> hasProperty(name)
    is Availability -> hasProperty(name)
    is BackboneElement -> hasProperty(name)
    is CodeableConcept -> hasProperty(name)
    is CodeableReference -> hasProperty(name)
    is Coding -> hasProperty(name)
    is ContactDetail -> hasProperty(name)
    is ContactPoint -> hasProperty(name)
    is Contributor -> hasProperty(name)
    is Count -> hasProperty(name)
    is DataRequirement -> hasProperty(name)
    is Distance -> hasProperty(name)
    is Dosage -> hasProperty(name)
    is Duration -> hasProperty(name)
    is ElementDefinition -> hasProperty(name)
    is Expression -> hasProperty(name)
    is ExtendedContactDetail -> hasProperty(name)
    is Extension -> hasProperty(name)
    is HumanName -> hasProperty(name)
    is Identifier -> hasProperty(name)
    is MarketingStatus -> hasProperty(name)
    is Meta -> hasProperty(name)
    is MonetaryComponent -> hasProperty(name)
    is Money -> hasProperty(name)
    is Narrative -> hasProperty(name)
    is ParameterDefinition -> hasProperty(name)
    is Period -> hasProperty(name)
    is ProductShelfLife -> hasProperty(name)
    is Quantity -> hasProperty(name)
    is Range -> hasProperty(name)
    is Ratio -> hasProperty(name)
    is RatioRange -> hasProperty(name)
    is Reference -> hasProperty(name)
    is RelatedArtifact -> hasProperty(name)
    is SampledData -> hasProperty(name)
    is Signature -> hasProperty(name)
    is Timing -> hasProperty(name)
    is TriggerDefinition -> hasProperty(name)
    is UsageContext -> hasProperty(name)
    is VirtualServiceDetail -> hasProperty(name)
    is Base64Binary -> hasProperty(name)
    is R5Boolean -> hasProperty(name)
    is Canonical -> hasProperty(name)
    is Code -> hasProperty(name)
    is Date -> hasProperty(name)
    is DateTime -> hasProperty(name)
    is Decimal -> hasProperty(name)
    is Id -> hasProperty(name)
    is Instant -> hasProperty(name)
    is Integer -> hasProperty(name)
    is Integer64 -> hasProperty(name)
    is Markdown -> hasProperty(name)
    is Oid -> hasProperty(name)
    is PositiveInt -> hasProperty(name)
    is R5String -> hasProperty(name)
    is Time -> hasProperty(name)
    is UnsignedInt -> hasProperty(name)
    is Uri -> hasProperty(name)
    is Url -> hasProperty(name)
    is Uuid -> hasProperty(name)
    is Xhtml -> hasProperty(name)
    is Enumeration<*> -> hasProperty(name)
    else -> false
  }

internal fun Element.getAllChildren(): List<Any> =
  when (this) {
    is Address -> getAllChildren()
    is Age -> getAllChildren()
    is Annotation -> getAllChildren()
    is Attachment -> getAllChildren()
    is Availability -> getAllChildren()
    is BackboneElement -> getAllChildren()
    is CodeableConcept -> getAllChildren()
    is CodeableReference -> getAllChildren()
    is Coding -> getAllChildren()
    is ContactDetail -> getAllChildren()
    is ContactPoint -> getAllChildren()
    is Contributor -> getAllChildren()
    is Count -> getAllChildren()
    is DataRequirement -> getAllChildren()
    is Distance -> getAllChildren()
    is Dosage -> getAllChildren()
    is Duration -> getAllChildren()
    is ElementDefinition -> getAllChildren()
    is Expression -> getAllChildren()
    is ExtendedContactDetail -> getAllChildren()
    is Extension -> getAllChildren()
    is HumanName -> getAllChildren()
    is Identifier -> getAllChildren()
    is MarketingStatus -> getAllChildren()
    is Meta -> getAllChildren()
    is MonetaryComponent -> getAllChildren()
    is Money -> getAllChildren()
    is Narrative -> getAllChildren()
    is ParameterDefinition -> getAllChildren()
    is Period -> getAllChildren()
    is ProductShelfLife -> getAllChildren()
    is Quantity -> getAllChildren()
    is Range -> getAllChildren()
    is Ratio -> getAllChildren()
    is RatioRange -> getAllChildren()
    is Reference -> getAllChildren()
    is RelatedArtifact -> getAllChildren()
    is SampledData -> getAllChildren()
    is Signature -> getAllChildren()
    is Timing -> getAllChildren()
    is TriggerDefinition -> getAllChildren()
    is UsageContext -> getAllChildren()
    is VirtualServiceDetail -> getAllChildren()
    is Base64Binary -> getAllChildren()
    is R5Boolean -> getAllChildren()
    is Canonical -> getAllChildren()
    is Code -> getAllChildren()
    is Date -> getAllChildren()
    is DateTime -> getAllChildren()
    is Decimal -> getAllChildren()
    is Id -> getAllChildren()
    is Instant -> getAllChildren()
    is Integer -> getAllChildren()
    is Integer64 -> getAllChildren()
    is Markdown -> getAllChildren()
    is Oid -> getAllChildren()
    is PositiveInt -> getAllChildren()
    is R5String -> getAllChildren()
    is Time -> getAllChildren()
    is UnsignedInt -> getAllChildren()
    is Uri -> getAllChildren()
    is Url -> getAllChildren()
    is Uuid -> getAllChildren()
    is Xhtml -> getAllChildren()
    is Enumeration<*> -> getAllChildren()
    else -> emptyList()
  }
