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

public enum class FhirR4BComplexType(override val typeName: String) : FhirR4BType {
  Address("Address"),
  Age("Age"),
  Annotation("Annotation"),
  Attachment("Attachment"),
  BackboneElement("BackboneElement"),
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
  Extension("Extension"),
  HumanName("HumanName"),
  Identifier("Identifier"),
  MarketingStatus("MarketingStatus"),
  Meta("Meta"),
  Money("Money"),
  Narrative("Narrative"),
  ParameterDefinition("ParameterDefinition"),
  Period("Period"),
  Population("Population"),
  ProdCharacteristic("ProdCharacteristic"),
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
  UsageContext("UsageContext");

  public companion object {
    public fun fromString(`value`: String): FhirR4BComplexType? =
      entries.find { it.typeName == value }

    public fun fromObject(`value`: Any): FhirR4BComplexType? =
      when (value) {
        is dev.ohs.fhir.model.r4b.Address -> Address
        is dev.ohs.fhir.model.r4b.Age -> Age
        is dev.ohs.fhir.model.r4b.Annotation -> Annotation
        is dev.ohs.fhir.model.r4b.Attachment -> Attachment
        is dev.ohs.fhir.model.r4b.BackboneElement -> BackboneElement
        is dev.ohs.fhir.model.r4b.CodeableConcept -> CodeableConcept
        is dev.ohs.fhir.model.r4b.CodeableReference -> CodeableReference
        is dev.ohs.fhir.model.r4b.Coding -> Coding
        is dev.ohs.fhir.model.r4b.ContactDetail -> ContactDetail
        is dev.ohs.fhir.model.r4b.ContactPoint -> ContactPoint
        is dev.ohs.fhir.model.r4b.Contributor -> Contributor
        is dev.ohs.fhir.model.r4b.Count -> Count
        is dev.ohs.fhir.model.r4b.DataRequirement -> DataRequirement
        is dev.ohs.fhir.model.r4b.DataType -> DataType
        is dev.ohs.fhir.model.r4b.Distance -> Distance
        is dev.ohs.fhir.model.r4b.Dosage -> Dosage
        is dev.ohs.fhir.model.r4b.Duration -> Duration
        is dev.ohs.fhir.model.r4b.ElementDefinition -> ElementDefinition
        is dev.ohs.fhir.model.r4b.Expression -> Expression
        is dev.ohs.fhir.model.r4b.Extension -> Extension
        is dev.ohs.fhir.model.r4b.HumanName -> HumanName
        is dev.ohs.fhir.model.r4b.Identifier -> Identifier
        is dev.ohs.fhir.model.r4b.MarketingStatus -> MarketingStatus
        is dev.ohs.fhir.model.r4b.Meta -> Meta
        is dev.ohs.fhir.model.r4b.Money -> Money
        is dev.ohs.fhir.model.r4b.Narrative -> Narrative
        is dev.ohs.fhir.model.r4b.ParameterDefinition -> ParameterDefinition
        is dev.ohs.fhir.model.r4b.Period -> Period
        is dev.ohs.fhir.model.r4b.Population -> Population
        is dev.ohs.fhir.model.r4b.ProdCharacteristic -> ProdCharacteristic
        is dev.ohs.fhir.model.r4b.ProductShelfLife -> ProductShelfLife
        is dev.ohs.fhir.model.r4b.Quantity -> Quantity
        is dev.ohs.fhir.model.r4b.Range -> Range
        is dev.ohs.fhir.model.r4b.Ratio -> Ratio
        is dev.ohs.fhir.model.r4b.RatioRange -> RatioRange
        is dev.ohs.fhir.model.r4b.Reference -> Reference
        is dev.ohs.fhir.model.r4b.RelatedArtifact -> RelatedArtifact
        is dev.ohs.fhir.model.r4b.SampledData -> SampledData
        is dev.ohs.fhir.model.r4b.Signature -> Signature
        is dev.ohs.fhir.model.r4b.Timing -> Timing
        is dev.ohs.fhir.model.r4b.TriggerDefinition -> TriggerDefinition
        is dev.ohs.fhir.model.r4b.UsageContext -> UsageContext
        else -> null
      }
  }
}
