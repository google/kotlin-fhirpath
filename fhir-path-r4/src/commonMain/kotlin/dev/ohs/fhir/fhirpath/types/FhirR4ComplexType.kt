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

public enum class FhirR4ComplexType(override val typeName: String) : FhirR4Type {
  Address("Address"),
  Age("Age"),
  Annotation("Annotation"),
  Attachment("Attachment"),
  BackboneElement("BackboneElement"),
  CodeableConcept("CodeableConcept"),
  Coding("Coding"),
  ContactDetail("ContactDetail"),
  ContactPoint("ContactPoint"),
  Contributor("Contributor"),
  Count("Count"),
  DataRequirement("DataRequirement"),
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
  Reference("Reference"),
  RelatedArtifact("RelatedArtifact"),
  SampledData("SampledData"),
  Signature("Signature"),
  SubstanceAmount("SubstanceAmount"),
  Timing("Timing"),
  TriggerDefinition("TriggerDefinition"),
  UsageContext("UsageContext");

  public companion object {
    public fun fromString(`value`: String): FhirR4ComplexType? =
      entries.find { it.typeName == value }

    public fun fromObject(`value`: Any): FhirR4ComplexType? =
      when (value) {
        is dev.ohs.fhir.model.r4.Address -> Address
        is dev.ohs.fhir.model.r4.Age -> Age
        is dev.ohs.fhir.model.r4.Annotation -> Annotation
        is dev.ohs.fhir.model.r4.Attachment -> Attachment
        is dev.ohs.fhir.model.r4.BackboneElement -> BackboneElement
        is dev.ohs.fhir.model.r4.CodeableConcept -> CodeableConcept
        is dev.ohs.fhir.model.r4.Coding -> Coding
        is dev.ohs.fhir.model.r4.ContactDetail -> ContactDetail
        is dev.ohs.fhir.model.r4.ContactPoint -> ContactPoint
        is dev.ohs.fhir.model.r4.Contributor -> Contributor
        is dev.ohs.fhir.model.r4.Count -> Count
        is dev.ohs.fhir.model.r4.DataRequirement -> DataRequirement
        is dev.ohs.fhir.model.r4.Distance -> Distance
        is dev.ohs.fhir.model.r4.Dosage -> Dosage
        is dev.ohs.fhir.model.r4.Duration -> Duration
        is dev.ohs.fhir.model.r4.ElementDefinition -> ElementDefinition
        is dev.ohs.fhir.model.r4.Expression -> Expression
        is dev.ohs.fhir.model.r4.Extension -> Extension
        is dev.ohs.fhir.model.r4.HumanName -> HumanName
        is dev.ohs.fhir.model.r4.Identifier -> Identifier
        is dev.ohs.fhir.model.r4.MarketingStatus -> MarketingStatus
        is dev.ohs.fhir.model.r4.Meta -> Meta
        is dev.ohs.fhir.model.r4.Money -> Money
        is dev.ohs.fhir.model.r4.Narrative -> Narrative
        is dev.ohs.fhir.model.r4.ParameterDefinition -> ParameterDefinition
        is dev.ohs.fhir.model.r4.Period -> Period
        is dev.ohs.fhir.model.r4.Population -> Population
        is dev.ohs.fhir.model.r4.ProdCharacteristic -> ProdCharacteristic
        is dev.ohs.fhir.model.r4.ProductShelfLife -> ProductShelfLife
        is dev.ohs.fhir.model.r4.Quantity -> Quantity
        is dev.ohs.fhir.model.r4.Range -> Range
        is dev.ohs.fhir.model.r4.Ratio -> Ratio
        is dev.ohs.fhir.model.r4.Reference -> Reference
        is dev.ohs.fhir.model.r4.RelatedArtifact -> RelatedArtifact
        is dev.ohs.fhir.model.r4.SampledData -> SampledData
        is dev.ohs.fhir.model.r4.Signature -> Signature
        is dev.ohs.fhir.model.r4.SubstanceAmount -> SubstanceAmount
        is dev.ohs.fhir.model.r4.Timing -> Timing
        is dev.ohs.fhir.model.r4.TriggerDefinition -> TriggerDefinition
        is dev.ohs.fhir.model.r4.UsageContext -> UsageContext
        else -> null
      }
  }
}
