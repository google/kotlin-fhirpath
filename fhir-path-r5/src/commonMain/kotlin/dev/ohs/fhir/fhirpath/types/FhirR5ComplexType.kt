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

import kotlin.Any
import kotlin.String

public enum class FhirR5ComplexType(override val typeName: String) : FhirR5Type {
  Address("Address"),
  Age("Age"),
  Annotation("Annotation"),
  Attachment("Attachment"),
  Availability("Availability"),
  BackboneElement("BackboneElement"),
  BackboneType("BackboneType"),
  Base("Base"),
  CodeableConcept("CodeableConcept"),
  CodeableReference("CodeableReference"),
  Coding("Coding"),
  ContactDetail("ContactDetail"),
  ContactPoint("ContactPoint"),
  Contributor("Contributor"),
  Count("Count"),
  DataRequirement("DataRequirement"),
  DataType("DataType"),
  Distance("Distance"),
  Dosage("Dosage"),
  Duration("Duration"),
  ElementDefinition("ElementDefinition"),
  Expression("Expression"),
  ExtendedContactDetail("ExtendedContactDetail"),
  Extension("Extension"),
  HumanName("HumanName"),
  Identifier("Identifier"),
  MarketingStatus("MarketingStatus"),
  Meta("Meta"),
  MonetaryComponent("MonetaryComponent"),
  Money("Money"),
  Narrative("Narrative"),
  ParameterDefinition("ParameterDefinition"),
  Period("Period"),
  PrimitiveType("PrimitiveType"),
  ProductShelfLife("ProductShelfLife"),
  Quantity("Quantity"),
  Range("Range"),
  Ratio("Ratio"),
  RatioRange("RatioRange"),
  Reference("Reference"),
  RelatedArtifact("RelatedArtifact"),
  SampledData("SampledData"),
  Signature("Signature"),
  Timing("Timing"),
  TriggerDefinition("TriggerDefinition"),
  UsageContext("UsageContext"),
  VirtualServiceDetail("VirtualServiceDetail");

  public companion object {
    public fun fromString(`value`: String): FhirR5ComplexType? =
      entries.find { it.typeName == value }

    public fun fromObject(`value`: Any): FhirR5ComplexType? =
      when (value) {
        is dev.ohs.fhir.model.r5.Address -> Address
        is dev.ohs.fhir.model.r5.Age -> Age
        is dev.ohs.fhir.model.r5.Annotation -> Annotation
        is dev.ohs.fhir.model.r5.Attachment -> Attachment
        is dev.ohs.fhir.model.r5.Availability -> Availability
        is dev.ohs.fhir.model.r5.BackboneElement -> BackboneElement
        is dev.ohs.fhir.model.r5.BackboneType -> BackboneType
        is dev.ohs.fhir.model.r5.Base -> Base
        is dev.ohs.fhir.model.r5.CodeableConcept -> CodeableConcept
        is dev.ohs.fhir.model.r5.CodeableReference -> CodeableReference
        is dev.ohs.fhir.model.r5.Coding -> Coding
        is dev.ohs.fhir.model.r5.ContactDetail -> ContactDetail
        is dev.ohs.fhir.model.r5.ContactPoint -> ContactPoint
        is dev.ohs.fhir.model.r5.Contributor -> Contributor
        is dev.ohs.fhir.model.r5.Count -> Count
        is dev.ohs.fhir.model.r5.DataRequirement -> DataRequirement
        is dev.ohs.fhir.model.r5.DataType -> DataType
        is dev.ohs.fhir.model.r5.Distance -> Distance
        is dev.ohs.fhir.model.r5.Dosage -> Dosage
        is dev.ohs.fhir.model.r5.Duration -> Duration
        is dev.ohs.fhir.model.r5.ElementDefinition -> ElementDefinition
        is dev.ohs.fhir.model.r5.Expression -> Expression
        is dev.ohs.fhir.model.r5.ExtendedContactDetail -> ExtendedContactDetail
        is dev.ohs.fhir.model.r5.Extension -> Extension
        is dev.ohs.fhir.model.r5.HumanName -> HumanName
        is dev.ohs.fhir.model.r5.Identifier -> Identifier
        is dev.ohs.fhir.model.r5.MarketingStatus -> MarketingStatus
        is dev.ohs.fhir.model.r5.Meta -> Meta
        is dev.ohs.fhir.model.r5.MonetaryComponent -> MonetaryComponent
        is dev.ohs.fhir.model.r5.Money -> Money
        is dev.ohs.fhir.model.r5.Narrative -> Narrative
        is dev.ohs.fhir.model.r5.ParameterDefinition -> ParameterDefinition
        is dev.ohs.fhir.model.r5.Period -> Period
        is dev.ohs.fhir.model.r5.PrimitiveType -> PrimitiveType
        is dev.ohs.fhir.model.r5.ProductShelfLife -> ProductShelfLife
        is dev.ohs.fhir.model.r5.Quantity -> Quantity
        is dev.ohs.fhir.model.r5.Range -> Range
        is dev.ohs.fhir.model.r5.Ratio -> Ratio
        is dev.ohs.fhir.model.r5.RatioRange -> RatioRange
        is dev.ohs.fhir.model.r5.Reference -> Reference
        is dev.ohs.fhir.model.r5.RelatedArtifact -> RelatedArtifact
        is dev.ohs.fhir.model.r5.SampledData -> SampledData
        is dev.ohs.fhir.model.r5.Signature -> Signature
        is dev.ohs.fhir.model.r5.Timing -> Timing
        is dev.ohs.fhir.model.r5.TriggerDefinition -> TriggerDefinition
        is dev.ohs.fhir.model.r5.UsageContext -> UsageContext
        is dev.ohs.fhir.model.r5.VirtualServiceDetail -> VirtualServiceDetail
        else -> null
      }
  }
}
