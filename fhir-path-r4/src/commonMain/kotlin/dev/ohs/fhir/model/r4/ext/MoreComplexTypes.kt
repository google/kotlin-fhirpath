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

package dev.ohs.fhir.model.r4.ext

import dev.ohs.fhir.model.r4.Address
import dev.ohs.fhir.model.r4.Age
import dev.ohs.fhir.model.r4.Annotation
import dev.ohs.fhir.model.r4.Attachment
import dev.ohs.fhir.model.r4.BackboneElement
import dev.ohs.fhir.model.r4.Base64Binary
import dev.ohs.fhir.model.r4.Boolean as R4Boolean
import dev.ohs.fhir.model.r4.Canonical
import dev.ohs.fhir.model.r4.Code
import dev.ohs.fhir.model.r4.CodeableConcept
import dev.ohs.fhir.model.r4.Coding
import dev.ohs.fhir.model.r4.ContactDetail
import dev.ohs.fhir.model.r4.ContactPoint
import dev.ohs.fhir.model.r4.Contributor
import dev.ohs.fhir.model.r4.Count
import dev.ohs.fhir.model.r4.DataRequirement
import dev.ohs.fhir.model.r4.Date
import dev.ohs.fhir.model.r4.DateTime
import dev.ohs.fhir.model.r4.Decimal
import dev.ohs.fhir.model.r4.Distance
import dev.ohs.fhir.model.r4.Dosage
import dev.ohs.fhir.model.r4.Duration
import dev.ohs.fhir.model.r4.Element
import dev.ohs.fhir.model.r4.ElementDefinition
import dev.ohs.fhir.model.r4.Enumeration
import dev.ohs.fhir.model.r4.Expression
import dev.ohs.fhir.model.r4.Extension
import dev.ohs.fhir.model.r4.HumanName
import dev.ohs.fhir.model.r4.Id
import dev.ohs.fhir.model.r4.Identifier
import dev.ohs.fhir.model.r4.Instant
import dev.ohs.fhir.model.r4.Integer
import dev.ohs.fhir.model.r4.Markdown
import dev.ohs.fhir.model.r4.MarketingStatus
import dev.ohs.fhir.model.r4.Meta
import dev.ohs.fhir.model.r4.Money
import dev.ohs.fhir.model.r4.Narrative
import dev.ohs.fhir.model.r4.Oid
import dev.ohs.fhir.model.r4.ParameterDefinition
import dev.ohs.fhir.model.r4.Period
import dev.ohs.fhir.model.r4.Population
import dev.ohs.fhir.model.r4.PositiveInt
import dev.ohs.fhir.model.r4.ProdCharacteristic
import dev.ohs.fhir.model.r4.ProductShelfLife
import dev.ohs.fhir.model.r4.Quantity
import dev.ohs.fhir.model.r4.Range
import dev.ohs.fhir.model.r4.Ratio
import dev.ohs.fhir.model.r4.Reference
import dev.ohs.fhir.model.r4.RelatedArtifact
import dev.ohs.fhir.model.r4.SampledData
import dev.ohs.fhir.model.r4.Signature
import dev.ohs.fhir.model.r4.String as R4String
import dev.ohs.fhir.model.r4.SubstanceAmount
import dev.ohs.fhir.model.r4.Time
import dev.ohs.fhir.model.r4.Timing
import dev.ohs.fhir.model.r4.TriggerDefinition
import dev.ohs.fhir.model.r4.UnsignedInt
import dev.ohs.fhir.model.r4.Uri
import dev.ohs.fhir.model.r4.Url
import dev.ohs.fhir.model.r4.UsageContext
import dev.ohs.fhir.model.r4.Uuid
import dev.ohs.fhir.model.r4.Xhtml
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
    is BackboneElement -> getProperty(name)
    is CodeableConcept -> getProperty(name)
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
    is Extension -> getProperty(name)
    is HumanName -> getProperty(name)
    is Identifier -> getProperty(name)
    is MarketingStatus -> getProperty(name)
    is Meta -> getProperty(name)
    is Money -> getProperty(name)
    is Narrative -> getProperty(name)
    is ParameterDefinition -> getProperty(name)
    is Period -> getProperty(name)
    is Population -> getProperty(name)
    is ProdCharacteristic -> getProperty(name)
    is ProductShelfLife -> getProperty(name)
    is Quantity -> getProperty(name)
    is Range -> getProperty(name)
    is Ratio -> getProperty(name)
    is Reference -> getProperty(name)
    is RelatedArtifact -> getProperty(name)
    is SampledData -> getProperty(name)
    is Signature -> getProperty(name)
    is SubstanceAmount -> getProperty(name)
    is Timing -> getProperty(name)
    is TriggerDefinition -> getProperty(name)
    is UsageContext -> getProperty(name)
    is Base64Binary -> getProperty(name)
    is R4Boolean -> getProperty(name)
    is Canonical -> getProperty(name)
    is Code -> getProperty(name)
    is Date -> getProperty(name)
    is DateTime -> getProperty(name)
    is Decimal -> getProperty(name)
    is Id -> getProperty(name)
    is Instant -> getProperty(name)
    is Integer -> getProperty(name)
    is Markdown -> getProperty(name)
    is Oid -> getProperty(name)
    is PositiveInt -> getProperty(name)
    is R4String -> getProperty(name)
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
    is BackboneElement -> hasProperty(name)
    is CodeableConcept -> hasProperty(name)
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
    is Extension -> hasProperty(name)
    is HumanName -> hasProperty(name)
    is Identifier -> hasProperty(name)
    is MarketingStatus -> hasProperty(name)
    is Meta -> hasProperty(name)
    is Money -> hasProperty(name)
    is Narrative -> hasProperty(name)
    is ParameterDefinition -> hasProperty(name)
    is Period -> hasProperty(name)
    is Population -> hasProperty(name)
    is ProdCharacteristic -> hasProperty(name)
    is ProductShelfLife -> hasProperty(name)
    is Quantity -> hasProperty(name)
    is Range -> hasProperty(name)
    is Ratio -> hasProperty(name)
    is Reference -> hasProperty(name)
    is RelatedArtifact -> hasProperty(name)
    is SampledData -> hasProperty(name)
    is Signature -> hasProperty(name)
    is SubstanceAmount -> hasProperty(name)
    is Timing -> hasProperty(name)
    is TriggerDefinition -> hasProperty(name)
    is UsageContext -> hasProperty(name)
    is Base64Binary -> hasProperty(name)
    is R4Boolean -> hasProperty(name)
    is Canonical -> hasProperty(name)
    is Code -> hasProperty(name)
    is Date -> hasProperty(name)
    is DateTime -> hasProperty(name)
    is Decimal -> hasProperty(name)
    is Id -> hasProperty(name)
    is Instant -> hasProperty(name)
    is Integer -> hasProperty(name)
    is Markdown -> hasProperty(name)
    is Oid -> hasProperty(name)
    is PositiveInt -> hasProperty(name)
    is R4String -> hasProperty(name)
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
    is BackboneElement -> getAllChildren()
    is CodeableConcept -> getAllChildren()
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
    is Extension -> getAllChildren()
    is HumanName -> getAllChildren()
    is Identifier -> getAllChildren()
    is MarketingStatus -> getAllChildren()
    is Meta -> getAllChildren()
    is Money -> getAllChildren()
    is Narrative -> getAllChildren()
    is ParameterDefinition -> getAllChildren()
    is Period -> getAllChildren()
    is Population -> getAllChildren()
    is ProdCharacteristic -> getAllChildren()
    is ProductShelfLife -> getAllChildren()
    is Quantity -> getAllChildren()
    is Range -> getAllChildren()
    is Ratio -> getAllChildren()
    is Reference -> getAllChildren()
    is RelatedArtifact -> getAllChildren()
    is SampledData -> getAllChildren()
    is Signature -> getAllChildren()
    is SubstanceAmount -> getAllChildren()
    is Timing -> getAllChildren()
    is TriggerDefinition -> getAllChildren()
    is UsageContext -> getAllChildren()
    is Base64Binary -> getAllChildren()
    is R4Boolean -> getAllChildren()
    is Canonical -> getAllChildren()
    is Code -> getAllChildren()
    is Date -> getAllChildren()
    is DateTime -> getAllChildren()
    is Decimal -> getAllChildren()
    is Id -> getAllChildren()
    is Instant -> getAllChildren()
    is Integer -> getAllChildren()
    is Markdown -> getAllChildren()
    is Oid -> getAllChildren()
    is PositiveInt -> getAllChildren()
    is R4String -> getAllChildren()
    is Time -> getAllChildren()
    is UnsignedInt -> getAllChildren()
    is Uri -> getAllChildren()
    is Url -> getAllChildren()
    is Uuid -> getAllChildren()
    is Xhtml -> getAllChildren()
    is Enumeration<*> -> getAllChildren()
    else -> emptyList()
  }
