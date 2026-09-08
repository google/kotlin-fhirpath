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

import dev.ohs.fhir.fhirpath.types.FhirR4ResourceType
import dev.ohs.fhir.fhirpath.types.FhirR4Type
import dev.ohs.fhir.model.r4.Account
import dev.ohs.fhir.model.r4.ActivityDefinition
import dev.ohs.fhir.model.r4.AdverseEvent
import dev.ohs.fhir.model.r4.AllergyIntolerance
import dev.ohs.fhir.model.r4.Appointment
import dev.ohs.fhir.model.r4.AppointmentResponse
import dev.ohs.fhir.model.r4.AuditEvent
import dev.ohs.fhir.model.r4.Basic
import dev.ohs.fhir.model.r4.Binary
import dev.ohs.fhir.model.r4.BiologicallyDerivedProduct
import dev.ohs.fhir.model.r4.BodyStructure
import dev.ohs.fhir.model.r4.Bundle
import dev.ohs.fhir.model.r4.CapabilityStatement
import dev.ohs.fhir.model.r4.CarePlan
import dev.ohs.fhir.model.r4.CareTeam
import dev.ohs.fhir.model.r4.CatalogEntry
import dev.ohs.fhir.model.r4.ChargeItem
import dev.ohs.fhir.model.r4.ChargeItemDefinition
import dev.ohs.fhir.model.r4.Claim
import dev.ohs.fhir.model.r4.ClaimResponse
import dev.ohs.fhir.model.r4.ClinicalImpression
import dev.ohs.fhir.model.r4.CodeSystem
import dev.ohs.fhir.model.r4.Communication
import dev.ohs.fhir.model.r4.CommunicationRequest
import dev.ohs.fhir.model.r4.CompartmentDefinition
import dev.ohs.fhir.model.r4.Composition
import dev.ohs.fhir.model.r4.ConceptMap
import dev.ohs.fhir.model.r4.Condition
import dev.ohs.fhir.model.r4.Consent
import dev.ohs.fhir.model.r4.Contract
import dev.ohs.fhir.model.r4.Coverage
import dev.ohs.fhir.model.r4.CoverageEligibilityRequest
import dev.ohs.fhir.model.r4.CoverageEligibilityResponse
import dev.ohs.fhir.model.r4.DetectedIssue
import dev.ohs.fhir.model.r4.Device
import dev.ohs.fhir.model.r4.DeviceDefinition
import dev.ohs.fhir.model.r4.DeviceMetric
import dev.ohs.fhir.model.r4.DeviceRequest
import dev.ohs.fhir.model.r4.DeviceUseStatement
import dev.ohs.fhir.model.r4.DiagnosticReport
import dev.ohs.fhir.model.r4.DocumentManifest
import dev.ohs.fhir.model.r4.DocumentReference
import dev.ohs.fhir.model.r4.EffectEvidenceSynthesis
import dev.ohs.fhir.model.r4.Encounter
import dev.ohs.fhir.model.r4.Endpoint
import dev.ohs.fhir.model.r4.EnrollmentRequest
import dev.ohs.fhir.model.r4.EnrollmentResponse
import dev.ohs.fhir.model.r4.EpisodeOfCare
import dev.ohs.fhir.model.r4.EventDefinition
import dev.ohs.fhir.model.r4.Evidence
import dev.ohs.fhir.model.r4.EvidenceVariable
import dev.ohs.fhir.model.r4.ExampleScenario
import dev.ohs.fhir.model.r4.ExplanationOfBenefit
import dev.ohs.fhir.model.r4.FamilyMemberHistory
import dev.ohs.fhir.model.r4.Flag
import dev.ohs.fhir.model.r4.Goal
import dev.ohs.fhir.model.r4.GraphDefinition
import dev.ohs.fhir.model.r4.Group
import dev.ohs.fhir.model.r4.GuidanceResponse
import dev.ohs.fhir.model.r4.HealthcareService
import dev.ohs.fhir.model.r4.ImagingStudy
import dev.ohs.fhir.model.r4.Immunization
import dev.ohs.fhir.model.r4.ImmunizationEvaluation
import dev.ohs.fhir.model.r4.ImmunizationRecommendation
import dev.ohs.fhir.model.r4.ImplementationGuide
import dev.ohs.fhir.model.r4.InsurancePlan
import dev.ohs.fhir.model.r4.Invoice
import dev.ohs.fhir.model.r4.Library
import dev.ohs.fhir.model.r4.Linkage
import dev.ohs.fhir.model.r4.List as R4List
import dev.ohs.fhir.model.r4.Location
import dev.ohs.fhir.model.r4.Measure
import dev.ohs.fhir.model.r4.MeasureReport
import dev.ohs.fhir.model.r4.Media
import dev.ohs.fhir.model.r4.Medication
import dev.ohs.fhir.model.r4.MedicationAdministration
import dev.ohs.fhir.model.r4.MedicationDispense
import dev.ohs.fhir.model.r4.MedicationKnowledge
import dev.ohs.fhir.model.r4.MedicationRequest
import dev.ohs.fhir.model.r4.MedicationStatement
import dev.ohs.fhir.model.r4.MedicinalProduct
import dev.ohs.fhir.model.r4.MedicinalProductAuthorization
import dev.ohs.fhir.model.r4.MedicinalProductContraindication
import dev.ohs.fhir.model.r4.MedicinalProductIndication
import dev.ohs.fhir.model.r4.MedicinalProductIngredient
import dev.ohs.fhir.model.r4.MedicinalProductInteraction
import dev.ohs.fhir.model.r4.MedicinalProductManufactured
import dev.ohs.fhir.model.r4.MedicinalProductPackaged
import dev.ohs.fhir.model.r4.MedicinalProductPharmaceutical
import dev.ohs.fhir.model.r4.MedicinalProductUndesirableEffect
import dev.ohs.fhir.model.r4.MessageDefinition
import dev.ohs.fhir.model.r4.MessageHeader
import dev.ohs.fhir.model.r4.MolecularSequence
import dev.ohs.fhir.model.r4.NamingSystem
import dev.ohs.fhir.model.r4.NutritionOrder
import dev.ohs.fhir.model.r4.Observation
import dev.ohs.fhir.model.r4.ObservationDefinition
import dev.ohs.fhir.model.r4.OperationDefinition
import dev.ohs.fhir.model.r4.OperationOutcome
import dev.ohs.fhir.model.r4.Organization
import dev.ohs.fhir.model.r4.OrganizationAffiliation
import dev.ohs.fhir.model.r4.Parameters
import dev.ohs.fhir.model.r4.Patient
import dev.ohs.fhir.model.r4.PaymentNotice
import dev.ohs.fhir.model.r4.PaymentReconciliation
import dev.ohs.fhir.model.r4.Person
import dev.ohs.fhir.model.r4.PlanDefinition
import dev.ohs.fhir.model.r4.Practitioner
import dev.ohs.fhir.model.r4.PractitionerRole
import dev.ohs.fhir.model.r4.Procedure
import dev.ohs.fhir.model.r4.Provenance
import dev.ohs.fhir.model.r4.Questionnaire
import dev.ohs.fhir.model.r4.QuestionnaireResponse
import dev.ohs.fhir.model.r4.RelatedPerson
import dev.ohs.fhir.model.r4.RequestGroup
import dev.ohs.fhir.model.r4.ResearchDefinition
import dev.ohs.fhir.model.r4.ResearchElementDefinition
import dev.ohs.fhir.model.r4.ResearchStudy
import dev.ohs.fhir.model.r4.ResearchSubject
import dev.ohs.fhir.model.r4.Resource
import dev.ohs.fhir.model.r4.RiskAssessment
import dev.ohs.fhir.model.r4.RiskEvidenceSynthesis
import dev.ohs.fhir.model.r4.Schedule
import dev.ohs.fhir.model.r4.SearchParameter
import dev.ohs.fhir.model.r4.ServiceRequest
import dev.ohs.fhir.model.r4.Slot
import dev.ohs.fhir.model.r4.Specimen
import dev.ohs.fhir.model.r4.SpecimenDefinition
import dev.ohs.fhir.model.r4.StructureDefinition
import dev.ohs.fhir.model.r4.StructureMap
import dev.ohs.fhir.model.r4.Subscription
import dev.ohs.fhir.model.r4.Substance
import dev.ohs.fhir.model.r4.SubstanceNucleicAcid
import dev.ohs.fhir.model.r4.SubstancePolymer
import dev.ohs.fhir.model.r4.SubstanceProtein
import dev.ohs.fhir.model.r4.SubstanceReferenceInformation
import dev.ohs.fhir.model.r4.SubstanceSourceMaterial
import dev.ohs.fhir.model.r4.SubstanceSpecification
import dev.ohs.fhir.model.r4.SupplyDelivery
import dev.ohs.fhir.model.r4.SupplyRequest
import dev.ohs.fhir.model.r4.Task
import dev.ohs.fhir.model.r4.TerminologyCapabilities
import dev.ohs.fhir.model.r4.TestReport
import dev.ohs.fhir.model.r4.TestScript
import dev.ohs.fhir.model.r4.ValueSet
import dev.ohs.fhir.model.r4.VerificationResult
import dev.ohs.fhir.model.r4.VisionPrescription
import dev.ohs.fhir.model.r4.terminologies.ResourceType
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List as CollectionsList

internal fun Resource.getFhirType(): FhirR4Type? =
  when (this) {
    is Account -> FhirR4ResourceType(ResourceType.Account)
    is ActivityDefinition -> FhirR4ResourceType(ResourceType.ActivityDefinition)
    is AdverseEvent -> FhirR4ResourceType(ResourceType.AdverseEvent)
    is AllergyIntolerance -> FhirR4ResourceType(ResourceType.AllergyIntolerance)
    is Appointment -> FhirR4ResourceType(ResourceType.Appointment)
    is AppointmentResponse -> FhirR4ResourceType(ResourceType.AppointmentResponse)
    is AuditEvent -> FhirR4ResourceType(ResourceType.AuditEvent)
    is Basic -> FhirR4ResourceType(ResourceType.Basic)
    is Binary -> FhirR4ResourceType(ResourceType.Binary)
    is BiologicallyDerivedProduct -> FhirR4ResourceType(ResourceType.BiologicallyDerivedProduct)
    is BodyStructure -> FhirR4ResourceType(ResourceType.BodyStructure)
    is Bundle -> FhirR4ResourceType(ResourceType.Bundle)
    is CapabilityStatement -> FhirR4ResourceType(ResourceType.CapabilityStatement)
    is CarePlan -> FhirR4ResourceType(ResourceType.CarePlan)
    is CareTeam -> FhirR4ResourceType(ResourceType.CareTeam)
    is CatalogEntry -> FhirR4ResourceType(ResourceType.CatalogEntry)
    is ChargeItem -> FhirR4ResourceType(ResourceType.ChargeItem)
    is ChargeItemDefinition -> FhirR4ResourceType(ResourceType.ChargeItemDefinition)
    is Claim -> FhirR4ResourceType(ResourceType.Claim)
    is ClaimResponse -> FhirR4ResourceType(ResourceType.ClaimResponse)
    is ClinicalImpression -> FhirR4ResourceType(ResourceType.ClinicalImpression)
    is CodeSystem -> FhirR4ResourceType(ResourceType.CodeSystem)
    is Communication -> FhirR4ResourceType(ResourceType.Communication)
    is CommunicationRequest -> FhirR4ResourceType(ResourceType.CommunicationRequest)
    is CompartmentDefinition -> FhirR4ResourceType(ResourceType.CompartmentDefinition)
    is Composition -> FhirR4ResourceType(ResourceType.Composition)
    is ConceptMap -> FhirR4ResourceType(ResourceType.ConceptMap)
    is Condition -> FhirR4ResourceType(ResourceType.Condition)
    is Consent -> FhirR4ResourceType(ResourceType.Consent)
    is Contract -> FhirR4ResourceType(ResourceType.Contract)
    is Coverage -> FhirR4ResourceType(ResourceType.Coverage)
    is CoverageEligibilityRequest -> FhirR4ResourceType(ResourceType.CoverageEligibilityRequest)
    is CoverageEligibilityResponse -> FhirR4ResourceType(ResourceType.CoverageEligibilityResponse)
    is DetectedIssue -> FhirR4ResourceType(ResourceType.DetectedIssue)
    is Device -> FhirR4ResourceType(ResourceType.Device)
    is DeviceDefinition -> FhirR4ResourceType(ResourceType.DeviceDefinition)
    is DeviceMetric -> FhirR4ResourceType(ResourceType.DeviceMetric)
    is DeviceRequest -> FhirR4ResourceType(ResourceType.DeviceRequest)
    is DeviceUseStatement -> FhirR4ResourceType(ResourceType.DeviceUseStatement)
    is DiagnosticReport -> FhirR4ResourceType(ResourceType.DiagnosticReport)
    is DocumentManifest -> FhirR4ResourceType(ResourceType.DocumentManifest)
    is DocumentReference -> FhirR4ResourceType(ResourceType.DocumentReference)
    is EffectEvidenceSynthesis -> FhirR4ResourceType(ResourceType.EffectEvidenceSynthesis)
    is Encounter -> FhirR4ResourceType(ResourceType.Encounter)
    is Endpoint -> FhirR4ResourceType(ResourceType.Endpoint)
    is EnrollmentRequest -> FhirR4ResourceType(ResourceType.EnrollmentRequest)
    is EnrollmentResponse -> FhirR4ResourceType(ResourceType.EnrollmentResponse)
    is EpisodeOfCare -> FhirR4ResourceType(ResourceType.EpisodeOfCare)
    is EventDefinition -> FhirR4ResourceType(ResourceType.EventDefinition)
    is Evidence -> FhirR4ResourceType(ResourceType.Evidence)
    is EvidenceVariable -> FhirR4ResourceType(ResourceType.EvidenceVariable)
    is ExampleScenario -> FhirR4ResourceType(ResourceType.ExampleScenario)
    is ExplanationOfBenefit -> FhirR4ResourceType(ResourceType.ExplanationOfBenefit)
    is FamilyMemberHistory -> FhirR4ResourceType(ResourceType.FamilyMemberHistory)
    is Flag -> FhirR4ResourceType(ResourceType.Flag)
    is Goal -> FhirR4ResourceType(ResourceType.Goal)
    is GraphDefinition -> FhirR4ResourceType(ResourceType.GraphDefinition)
    is Group -> FhirR4ResourceType(ResourceType.Group)
    is GuidanceResponse -> FhirR4ResourceType(ResourceType.GuidanceResponse)
    is HealthcareService -> FhirR4ResourceType(ResourceType.HealthcareService)
    is ImagingStudy -> FhirR4ResourceType(ResourceType.ImagingStudy)
    is Immunization -> FhirR4ResourceType(ResourceType.Immunization)
    is ImmunizationEvaluation -> FhirR4ResourceType(ResourceType.ImmunizationEvaluation)
    is ImmunizationRecommendation -> FhirR4ResourceType(ResourceType.ImmunizationRecommendation)
    is ImplementationGuide -> FhirR4ResourceType(ResourceType.ImplementationGuide)
    is InsurancePlan -> FhirR4ResourceType(ResourceType.InsurancePlan)
    is Invoice -> FhirR4ResourceType(ResourceType.Invoice)
    is Library -> FhirR4ResourceType(ResourceType.Library)
    is Linkage -> FhirR4ResourceType(ResourceType.Linkage)
    is R4List -> FhirR4ResourceType(ResourceType.List)
    is Location -> FhirR4ResourceType(ResourceType.Location)
    is Measure -> FhirR4ResourceType(ResourceType.Measure)
    is MeasureReport -> FhirR4ResourceType(ResourceType.MeasureReport)
    is Media -> FhirR4ResourceType(ResourceType.Media)
    is Medication -> FhirR4ResourceType(ResourceType.Medication)
    is MedicationAdministration -> FhirR4ResourceType(ResourceType.MedicationAdministration)
    is MedicationDispense -> FhirR4ResourceType(ResourceType.MedicationDispense)
    is MedicationKnowledge -> FhirR4ResourceType(ResourceType.MedicationKnowledge)
    is MedicationRequest -> FhirR4ResourceType(ResourceType.MedicationRequest)
    is MedicationStatement -> FhirR4ResourceType(ResourceType.MedicationStatement)
    is MedicinalProduct -> FhirR4ResourceType(ResourceType.MedicinalProduct)
    is MedicinalProductAuthorization ->
      FhirR4ResourceType(ResourceType.MedicinalProductAuthorization)
    is MedicinalProductContraindication ->
      FhirR4ResourceType(ResourceType.MedicinalProductContraindication)
    is MedicinalProductIndication -> FhirR4ResourceType(ResourceType.MedicinalProductIndication)
    is MedicinalProductIngredient -> FhirR4ResourceType(ResourceType.MedicinalProductIngredient)
    is MedicinalProductInteraction -> FhirR4ResourceType(ResourceType.MedicinalProductInteraction)
    is MedicinalProductManufactured -> FhirR4ResourceType(ResourceType.MedicinalProductManufactured)
    is MedicinalProductPackaged -> FhirR4ResourceType(ResourceType.MedicinalProductPackaged)
    is MedicinalProductPharmaceutical ->
      FhirR4ResourceType(ResourceType.MedicinalProductPharmaceutical)
    is MedicinalProductUndesirableEffect ->
      FhirR4ResourceType(ResourceType.MedicinalProductUndesirableEffect)
    is MessageDefinition -> FhirR4ResourceType(ResourceType.MessageDefinition)
    is MessageHeader -> FhirR4ResourceType(ResourceType.MessageHeader)
    is MolecularSequence -> FhirR4ResourceType(ResourceType.MolecularSequence)
    is NamingSystem -> FhirR4ResourceType(ResourceType.NamingSystem)
    is NutritionOrder -> FhirR4ResourceType(ResourceType.NutritionOrder)
    is Observation -> FhirR4ResourceType(ResourceType.Observation)
    is ObservationDefinition -> FhirR4ResourceType(ResourceType.ObservationDefinition)
    is OperationDefinition -> FhirR4ResourceType(ResourceType.OperationDefinition)
    is OperationOutcome -> FhirR4ResourceType(ResourceType.OperationOutcome)
    is Organization -> FhirR4ResourceType(ResourceType.Organization)
    is OrganizationAffiliation -> FhirR4ResourceType(ResourceType.OrganizationAffiliation)
    is Parameters -> FhirR4ResourceType(ResourceType.Parameters)
    is Patient -> FhirR4ResourceType(ResourceType.Patient)
    is PaymentNotice -> FhirR4ResourceType(ResourceType.PaymentNotice)
    is PaymentReconciliation -> FhirR4ResourceType(ResourceType.PaymentReconciliation)
    is Person -> FhirR4ResourceType(ResourceType.Person)
    is PlanDefinition -> FhirR4ResourceType(ResourceType.PlanDefinition)
    is Practitioner -> FhirR4ResourceType(ResourceType.Practitioner)
    is PractitionerRole -> FhirR4ResourceType(ResourceType.PractitionerRole)
    is Procedure -> FhirR4ResourceType(ResourceType.Procedure)
    is Provenance -> FhirR4ResourceType(ResourceType.Provenance)
    is Questionnaire -> FhirR4ResourceType(ResourceType.Questionnaire)
    is QuestionnaireResponse -> FhirR4ResourceType(ResourceType.QuestionnaireResponse)
    is RelatedPerson -> FhirR4ResourceType(ResourceType.RelatedPerson)
    is RequestGroup -> FhirR4ResourceType(ResourceType.RequestGroup)
    is ResearchDefinition -> FhirR4ResourceType(ResourceType.ResearchDefinition)
    is ResearchElementDefinition -> FhirR4ResourceType(ResourceType.ResearchElementDefinition)
    is ResearchStudy -> FhirR4ResourceType(ResourceType.ResearchStudy)
    is ResearchSubject -> FhirR4ResourceType(ResourceType.ResearchSubject)
    is RiskAssessment -> FhirR4ResourceType(ResourceType.RiskAssessment)
    is RiskEvidenceSynthesis -> FhirR4ResourceType(ResourceType.RiskEvidenceSynthesis)
    is Schedule -> FhirR4ResourceType(ResourceType.Schedule)
    is SearchParameter -> FhirR4ResourceType(ResourceType.SearchParameter)
    is ServiceRequest -> FhirR4ResourceType(ResourceType.ServiceRequest)
    is Slot -> FhirR4ResourceType(ResourceType.Slot)
    is Specimen -> FhirR4ResourceType(ResourceType.Specimen)
    is SpecimenDefinition -> FhirR4ResourceType(ResourceType.SpecimenDefinition)
    is StructureDefinition -> FhirR4ResourceType(ResourceType.StructureDefinition)
    is StructureMap -> FhirR4ResourceType(ResourceType.StructureMap)
    is Subscription -> FhirR4ResourceType(ResourceType.Subscription)
    is Substance -> FhirR4ResourceType(ResourceType.Substance)
    is SubstanceNucleicAcid -> FhirR4ResourceType(ResourceType.SubstanceNucleicAcid)
    is SubstancePolymer -> FhirR4ResourceType(ResourceType.SubstancePolymer)
    is SubstanceProtein -> FhirR4ResourceType(ResourceType.SubstanceProtein)
    is SubstanceReferenceInformation ->
      FhirR4ResourceType(ResourceType.SubstanceReferenceInformation)
    is SubstanceSourceMaterial -> FhirR4ResourceType(ResourceType.SubstanceSourceMaterial)
    is SubstanceSpecification -> FhirR4ResourceType(ResourceType.SubstanceSpecification)
    is SupplyDelivery -> FhirR4ResourceType(ResourceType.SupplyDelivery)
    is SupplyRequest -> FhirR4ResourceType(ResourceType.SupplyRequest)
    is Task -> FhirR4ResourceType(ResourceType.Task)
    is TerminologyCapabilities -> FhirR4ResourceType(ResourceType.TerminologyCapabilities)
    is TestReport -> FhirR4ResourceType(ResourceType.TestReport)
    is TestScript -> FhirR4ResourceType(ResourceType.TestScript)
    is ValueSet -> FhirR4ResourceType(ResourceType.ValueSet)
    is VerificationResult -> FhirR4ResourceType(ResourceType.VerificationResult)
    is VisionPrescription -> FhirR4ResourceType(ResourceType.VisionPrescription)
    else -> null
  }

internal fun Resource.getProperty(name: String): Any? =
  when (this) {
    is Account -> getProperty(name)
    is ActivityDefinition -> getProperty(name)
    is AdverseEvent -> getProperty(name)
    is AllergyIntolerance -> getProperty(name)
    is Appointment -> getProperty(name)
    is AppointmentResponse -> getProperty(name)
    is AuditEvent -> getProperty(name)
    is Basic -> getProperty(name)
    is Binary -> getProperty(name)
    is BiologicallyDerivedProduct -> getProperty(name)
    is BodyStructure -> getProperty(name)
    is Bundle -> getProperty(name)
    is CapabilityStatement -> getProperty(name)
    is CarePlan -> getProperty(name)
    is CareTeam -> getProperty(name)
    is CatalogEntry -> getProperty(name)
    is ChargeItem -> getProperty(name)
    is ChargeItemDefinition -> getProperty(name)
    is Claim -> getProperty(name)
    is ClaimResponse -> getProperty(name)
    is ClinicalImpression -> getProperty(name)
    is CodeSystem -> getProperty(name)
    is Communication -> getProperty(name)
    is CommunicationRequest -> getProperty(name)
    is CompartmentDefinition -> getProperty(name)
    is Composition -> getProperty(name)
    is ConceptMap -> getProperty(name)
    is Condition -> getProperty(name)
    is Consent -> getProperty(name)
    is Contract -> getProperty(name)
    is Coverage -> getProperty(name)
    is CoverageEligibilityRequest -> getProperty(name)
    is CoverageEligibilityResponse -> getProperty(name)
    is DetectedIssue -> getProperty(name)
    is Device -> getProperty(name)
    is DeviceDefinition -> getProperty(name)
    is DeviceMetric -> getProperty(name)
    is DeviceRequest -> getProperty(name)
    is DeviceUseStatement -> getProperty(name)
    is DiagnosticReport -> getProperty(name)
    is DocumentManifest -> getProperty(name)
    is DocumentReference -> getProperty(name)
    is EffectEvidenceSynthesis -> getProperty(name)
    is Encounter -> getProperty(name)
    is Endpoint -> getProperty(name)
    is EnrollmentRequest -> getProperty(name)
    is EnrollmentResponse -> getProperty(name)
    is EpisodeOfCare -> getProperty(name)
    is EventDefinition -> getProperty(name)
    is Evidence -> getProperty(name)
    is EvidenceVariable -> getProperty(name)
    is ExampleScenario -> getProperty(name)
    is ExplanationOfBenefit -> getProperty(name)
    is FamilyMemberHistory -> getProperty(name)
    is Flag -> getProperty(name)
    is Goal -> getProperty(name)
    is GraphDefinition -> getProperty(name)
    is Group -> getProperty(name)
    is GuidanceResponse -> getProperty(name)
    is HealthcareService -> getProperty(name)
    is ImagingStudy -> getProperty(name)
    is Immunization -> getProperty(name)
    is ImmunizationEvaluation -> getProperty(name)
    is ImmunizationRecommendation -> getProperty(name)
    is ImplementationGuide -> getProperty(name)
    is InsurancePlan -> getProperty(name)
    is Invoice -> getProperty(name)
    is Library -> getProperty(name)
    is Linkage -> getProperty(name)
    is R4List -> getProperty(name)
    is Location -> getProperty(name)
    is Measure -> getProperty(name)
    is MeasureReport -> getProperty(name)
    is Media -> getProperty(name)
    is Medication -> getProperty(name)
    is MedicationAdministration -> getProperty(name)
    is MedicationDispense -> getProperty(name)
    is MedicationKnowledge -> getProperty(name)
    is MedicationRequest -> getProperty(name)
    is MedicationStatement -> getProperty(name)
    is MedicinalProduct -> getProperty(name)
    is MedicinalProductAuthorization -> getProperty(name)
    is MedicinalProductContraindication -> getProperty(name)
    is MedicinalProductIndication -> getProperty(name)
    is MedicinalProductIngredient -> getProperty(name)
    is MedicinalProductInteraction -> getProperty(name)
    is MedicinalProductManufactured -> getProperty(name)
    is MedicinalProductPackaged -> getProperty(name)
    is MedicinalProductPharmaceutical -> getProperty(name)
    is MedicinalProductUndesirableEffect -> getProperty(name)
    is MessageDefinition -> getProperty(name)
    is MessageHeader -> getProperty(name)
    is MolecularSequence -> getProperty(name)
    is NamingSystem -> getProperty(name)
    is NutritionOrder -> getProperty(name)
    is Observation -> getProperty(name)
    is ObservationDefinition -> getProperty(name)
    is OperationDefinition -> getProperty(name)
    is OperationOutcome -> getProperty(name)
    is Organization -> getProperty(name)
    is OrganizationAffiliation -> getProperty(name)
    is Parameters -> getProperty(name)
    is Patient -> getProperty(name)
    is PaymentNotice -> getProperty(name)
    is PaymentReconciliation -> getProperty(name)
    is Person -> getProperty(name)
    is PlanDefinition -> getProperty(name)
    is Practitioner -> getProperty(name)
    is PractitionerRole -> getProperty(name)
    is Procedure -> getProperty(name)
    is Provenance -> getProperty(name)
    is Questionnaire -> getProperty(name)
    is QuestionnaireResponse -> getProperty(name)
    is RelatedPerson -> getProperty(name)
    is RequestGroup -> getProperty(name)
    is ResearchDefinition -> getProperty(name)
    is ResearchElementDefinition -> getProperty(name)
    is ResearchStudy -> getProperty(name)
    is ResearchSubject -> getProperty(name)
    is RiskAssessment -> getProperty(name)
    is RiskEvidenceSynthesis -> getProperty(name)
    is Schedule -> getProperty(name)
    is SearchParameter -> getProperty(name)
    is ServiceRequest -> getProperty(name)
    is Slot -> getProperty(name)
    is Specimen -> getProperty(name)
    is SpecimenDefinition -> getProperty(name)
    is StructureDefinition -> getProperty(name)
    is StructureMap -> getProperty(name)
    is Subscription -> getProperty(name)
    is Substance -> getProperty(name)
    is SubstanceNucleicAcid -> getProperty(name)
    is SubstancePolymer -> getProperty(name)
    is SubstanceProtein -> getProperty(name)
    is SubstanceReferenceInformation -> getProperty(name)
    is SubstanceSourceMaterial -> getProperty(name)
    is SubstanceSpecification -> getProperty(name)
    is SupplyDelivery -> getProperty(name)
    is SupplyRequest -> getProperty(name)
    is Task -> getProperty(name)
    is TerminologyCapabilities -> getProperty(name)
    is TestReport -> getProperty(name)
    is TestScript -> getProperty(name)
    is ValueSet -> getProperty(name)
    is VerificationResult -> getProperty(name)
    is VisionPrescription -> getProperty(name)
    else -> null
  }

internal fun Resource.hasProperty(name: String): Boolean =
  when (this) {
    is Account -> hasProperty(name)
    is ActivityDefinition -> hasProperty(name)
    is AdverseEvent -> hasProperty(name)
    is AllergyIntolerance -> hasProperty(name)
    is Appointment -> hasProperty(name)
    is AppointmentResponse -> hasProperty(name)
    is AuditEvent -> hasProperty(name)
    is Basic -> hasProperty(name)
    is Binary -> hasProperty(name)
    is BiologicallyDerivedProduct -> hasProperty(name)
    is BodyStructure -> hasProperty(name)
    is Bundle -> hasProperty(name)
    is CapabilityStatement -> hasProperty(name)
    is CarePlan -> hasProperty(name)
    is CareTeam -> hasProperty(name)
    is CatalogEntry -> hasProperty(name)
    is ChargeItem -> hasProperty(name)
    is ChargeItemDefinition -> hasProperty(name)
    is Claim -> hasProperty(name)
    is ClaimResponse -> hasProperty(name)
    is ClinicalImpression -> hasProperty(name)
    is CodeSystem -> hasProperty(name)
    is Communication -> hasProperty(name)
    is CommunicationRequest -> hasProperty(name)
    is CompartmentDefinition -> hasProperty(name)
    is Composition -> hasProperty(name)
    is ConceptMap -> hasProperty(name)
    is Condition -> hasProperty(name)
    is Consent -> hasProperty(name)
    is Contract -> hasProperty(name)
    is Coverage -> hasProperty(name)
    is CoverageEligibilityRequest -> hasProperty(name)
    is CoverageEligibilityResponse -> hasProperty(name)
    is DetectedIssue -> hasProperty(name)
    is Device -> hasProperty(name)
    is DeviceDefinition -> hasProperty(name)
    is DeviceMetric -> hasProperty(name)
    is DeviceRequest -> hasProperty(name)
    is DeviceUseStatement -> hasProperty(name)
    is DiagnosticReport -> hasProperty(name)
    is DocumentManifest -> hasProperty(name)
    is DocumentReference -> hasProperty(name)
    is EffectEvidenceSynthesis -> hasProperty(name)
    is Encounter -> hasProperty(name)
    is Endpoint -> hasProperty(name)
    is EnrollmentRequest -> hasProperty(name)
    is EnrollmentResponse -> hasProperty(name)
    is EpisodeOfCare -> hasProperty(name)
    is EventDefinition -> hasProperty(name)
    is Evidence -> hasProperty(name)
    is EvidenceVariable -> hasProperty(name)
    is ExampleScenario -> hasProperty(name)
    is ExplanationOfBenefit -> hasProperty(name)
    is FamilyMemberHistory -> hasProperty(name)
    is Flag -> hasProperty(name)
    is Goal -> hasProperty(name)
    is GraphDefinition -> hasProperty(name)
    is Group -> hasProperty(name)
    is GuidanceResponse -> hasProperty(name)
    is HealthcareService -> hasProperty(name)
    is ImagingStudy -> hasProperty(name)
    is Immunization -> hasProperty(name)
    is ImmunizationEvaluation -> hasProperty(name)
    is ImmunizationRecommendation -> hasProperty(name)
    is ImplementationGuide -> hasProperty(name)
    is InsurancePlan -> hasProperty(name)
    is Invoice -> hasProperty(name)
    is Library -> hasProperty(name)
    is Linkage -> hasProperty(name)
    is R4List -> hasProperty(name)
    is Location -> hasProperty(name)
    is Measure -> hasProperty(name)
    is MeasureReport -> hasProperty(name)
    is Media -> hasProperty(name)
    is Medication -> hasProperty(name)
    is MedicationAdministration -> hasProperty(name)
    is MedicationDispense -> hasProperty(name)
    is MedicationKnowledge -> hasProperty(name)
    is MedicationRequest -> hasProperty(name)
    is MedicationStatement -> hasProperty(name)
    is MedicinalProduct -> hasProperty(name)
    is MedicinalProductAuthorization -> hasProperty(name)
    is MedicinalProductContraindication -> hasProperty(name)
    is MedicinalProductIndication -> hasProperty(name)
    is MedicinalProductIngredient -> hasProperty(name)
    is MedicinalProductInteraction -> hasProperty(name)
    is MedicinalProductManufactured -> hasProperty(name)
    is MedicinalProductPackaged -> hasProperty(name)
    is MedicinalProductPharmaceutical -> hasProperty(name)
    is MedicinalProductUndesirableEffect -> hasProperty(name)
    is MessageDefinition -> hasProperty(name)
    is MessageHeader -> hasProperty(name)
    is MolecularSequence -> hasProperty(name)
    is NamingSystem -> hasProperty(name)
    is NutritionOrder -> hasProperty(name)
    is Observation -> hasProperty(name)
    is ObservationDefinition -> hasProperty(name)
    is OperationDefinition -> hasProperty(name)
    is OperationOutcome -> hasProperty(name)
    is Organization -> hasProperty(name)
    is OrganizationAffiliation -> hasProperty(name)
    is Parameters -> hasProperty(name)
    is Patient -> hasProperty(name)
    is PaymentNotice -> hasProperty(name)
    is PaymentReconciliation -> hasProperty(name)
    is Person -> hasProperty(name)
    is PlanDefinition -> hasProperty(name)
    is Practitioner -> hasProperty(name)
    is PractitionerRole -> hasProperty(name)
    is Procedure -> hasProperty(name)
    is Provenance -> hasProperty(name)
    is Questionnaire -> hasProperty(name)
    is QuestionnaireResponse -> hasProperty(name)
    is RelatedPerson -> hasProperty(name)
    is RequestGroup -> hasProperty(name)
    is ResearchDefinition -> hasProperty(name)
    is ResearchElementDefinition -> hasProperty(name)
    is ResearchStudy -> hasProperty(name)
    is ResearchSubject -> hasProperty(name)
    is RiskAssessment -> hasProperty(name)
    is RiskEvidenceSynthesis -> hasProperty(name)
    is Schedule -> hasProperty(name)
    is SearchParameter -> hasProperty(name)
    is ServiceRequest -> hasProperty(name)
    is Slot -> hasProperty(name)
    is Specimen -> hasProperty(name)
    is SpecimenDefinition -> hasProperty(name)
    is StructureDefinition -> hasProperty(name)
    is StructureMap -> hasProperty(name)
    is Subscription -> hasProperty(name)
    is Substance -> hasProperty(name)
    is SubstanceNucleicAcid -> hasProperty(name)
    is SubstancePolymer -> hasProperty(name)
    is SubstanceProtein -> hasProperty(name)
    is SubstanceReferenceInformation -> hasProperty(name)
    is SubstanceSourceMaterial -> hasProperty(name)
    is SubstanceSpecification -> hasProperty(name)
    is SupplyDelivery -> hasProperty(name)
    is SupplyRequest -> hasProperty(name)
    is Task -> hasProperty(name)
    is TerminologyCapabilities -> hasProperty(name)
    is TestReport -> hasProperty(name)
    is TestScript -> hasProperty(name)
    is ValueSet -> hasProperty(name)
    is VerificationResult -> hasProperty(name)
    is VisionPrescription -> hasProperty(name)
    else -> false
  }

internal fun Resource.getAllChildren(): CollectionsList<Any> =
  when (this) {
    is Account -> getAllChildren()
    is ActivityDefinition -> getAllChildren()
    is AdverseEvent -> getAllChildren()
    is AllergyIntolerance -> getAllChildren()
    is Appointment -> getAllChildren()
    is AppointmentResponse -> getAllChildren()
    is AuditEvent -> getAllChildren()
    is Basic -> getAllChildren()
    is Binary -> getAllChildren()
    is BiologicallyDerivedProduct -> getAllChildren()
    is BodyStructure -> getAllChildren()
    is Bundle -> getAllChildren()
    is CapabilityStatement -> getAllChildren()
    is CarePlan -> getAllChildren()
    is CareTeam -> getAllChildren()
    is CatalogEntry -> getAllChildren()
    is ChargeItem -> getAllChildren()
    is ChargeItemDefinition -> getAllChildren()
    is Claim -> getAllChildren()
    is ClaimResponse -> getAllChildren()
    is ClinicalImpression -> getAllChildren()
    is CodeSystem -> getAllChildren()
    is Communication -> getAllChildren()
    is CommunicationRequest -> getAllChildren()
    is CompartmentDefinition -> getAllChildren()
    is Composition -> getAllChildren()
    is ConceptMap -> getAllChildren()
    is Condition -> getAllChildren()
    is Consent -> getAllChildren()
    is Contract -> getAllChildren()
    is Coverage -> getAllChildren()
    is CoverageEligibilityRequest -> getAllChildren()
    is CoverageEligibilityResponse -> getAllChildren()
    is DetectedIssue -> getAllChildren()
    is Device -> getAllChildren()
    is DeviceDefinition -> getAllChildren()
    is DeviceMetric -> getAllChildren()
    is DeviceRequest -> getAllChildren()
    is DeviceUseStatement -> getAllChildren()
    is DiagnosticReport -> getAllChildren()
    is DocumentManifest -> getAllChildren()
    is DocumentReference -> getAllChildren()
    is EffectEvidenceSynthesis -> getAllChildren()
    is Encounter -> getAllChildren()
    is Endpoint -> getAllChildren()
    is EnrollmentRequest -> getAllChildren()
    is EnrollmentResponse -> getAllChildren()
    is EpisodeOfCare -> getAllChildren()
    is EventDefinition -> getAllChildren()
    is Evidence -> getAllChildren()
    is EvidenceVariable -> getAllChildren()
    is ExampleScenario -> getAllChildren()
    is ExplanationOfBenefit -> getAllChildren()
    is FamilyMemberHistory -> getAllChildren()
    is Flag -> getAllChildren()
    is Goal -> getAllChildren()
    is GraphDefinition -> getAllChildren()
    is Group -> getAllChildren()
    is GuidanceResponse -> getAllChildren()
    is HealthcareService -> getAllChildren()
    is ImagingStudy -> getAllChildren()
    is Immunization -> getAllChildren()
    is ImmunizationEvaluation -> getAllChildren()
    is ImmunizationRecommendation -> getAllChildren()
    is ImplementationGuide -> getAllChildren()
    is InsurancePlan -> getAllChildren()
    is Invoice -> getAllChildren()
    is Library -> getAllChildren()
    is Linkage -> getAllChildren()
    is R4List -> getAllChildren()
    is Location -> getAllChildren()
    is Measure -> getAllChildren()
    is MeasureReport -> getAllChildren()
    is Media -> getAllChildren()
    is Medication -> getAllChildren()
    is MedicationAdministration -> getAllChildren()
    is MedicationDispense -> getAllChildren()
    is MedicationKnowledge -> getAllChildren()
    is MedicationRequest -> getAllChildren()
    is MedicationStatement -> getAllChildren()
    is MedicinalProduct -> getAllChildren()
    is MedicinalProductAuthorization -> getAllChildren()
    is MedicinalProductContraindication -> getAllChildren()
    is MedicinalProductIndication -> getAllChildren()
    is MedicinalProductIngredient -> getAllChildren()
    is MedicinalProductInteraction -> getAllChildren()
    is MedicinalProductManufactured -> getAllChildren()
    is MedicinalProductPackaged -> getAllChildren()
    is MedicinalProductPharmaceutical -> getAllChildren()
    is MedicinalProductUndesirableEffect -> getAllChildren()
    is MessageDefinition -> getAllChildren()
    is MessageHeader -> getAllChildren()
    is MolecularSequence -> getAllChildren()
    is NamingSystem -> getAllChildren()
    is NutritionOrder -> getAllChildren()
    is Observation -> getAllChildren()
    is ObservationDefinition -> getAllChildren()
    is OperationDefinition -> getAllChildren()
    is OperationOutcome -> getAllChildren()
    is Organization -> getAllChildren()
    is OrganizationAffiliation -> getAllChildren()
    is Parameters -> getAllChildren()
    is Patient -> getAllChildren()
    is PaymentNotice -> getAllChildren()
    is PaymentReconciliation -> getAllChildren()
    is Person -> getAllChildren()
    is PlanDefinition -> getAllChildren()
    is Practitioner -> getAllChildren()
    is PractitionerRole -> getAllChildren()
    is Procedure -> getAllChildren()
    is Provenance -> getAllChildren()
    is Questionnaire -> getAllChildren()
    is QuestionnaireResponse -> getAllChildren()
    is RelatedPerson -> getAllChildren()
    is RequestGroup -> getAllChildren()
    is ResearchDefinition -> getAllChildren()
    is ResearchElementDefinition -> getAllChildren()
    is ResearchStudy -> getAllChildren()
    is ResearchSubject -> getAllChildren()
    is RiskAssessment -> getAllChildren()
    is RiskEvidenceSynthesis -> getAllChildren()
    is Schedule -> getAllChildren()
    is SearchParameter -> getAllChildren()
    is ServiceRequest -> getAllChildren()
    is Slot -> getAllChildren()
    is Specimen -> getAllChildren()
    is SpecimenDefinition -> getAllChildren()
    is StructureDefinition -> getAllChildren()
    is StructureMap -> getAllChildren()
    is Subscription -> getAllChildren()
    is Substance -> getAllChildren()
    is SubstanceNucleicAcid -> getAllChildren()
    is SubstancePolymer -> getAllChildren()
    is SubstanceProtein -> getAllChildren()
    is SubstanceReferenceInformation -> getAllChildren()
    is SubstanceSourceMaterial -> getAllChildren()
    is SubstanceSpecification -> getAllChildren()
    is SupplyDelivery -> getAllChildren()
    is SupplyRequest -> getAllChildren()
    is Task -> getAllChildren()
    is TerminologyCapabilities -> getAllChildren()
    is TestReport -> getAllChildren()
    is TestScript -> getAllChildren()
    is ValueSet -> getAllChildren()
    is VerificationResult -> getAllChildren()
    is VisionPrescription -> getAllChildren()
    else -> emptyList()
  }
