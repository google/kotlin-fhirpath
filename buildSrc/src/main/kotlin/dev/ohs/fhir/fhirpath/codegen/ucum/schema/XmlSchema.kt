/*
 * Copyright 2025-2026 Open Health Stack Foundation
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

package dev.ohs.fhir.fhirpath.codegen.ucum.schema

import dev.ohs.fhir.fhirpath.codegen.ucum.UnitComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

private const val UCUM_NAMESPACE = "http://unitsofmeasure.org/ucum-essence"

/** Matches the number after `1e`. */
private val prefixValueRegex = Regex("(?<=e)[-]?\\d+")

@Serializable
@XmlSerialName("root", UCUM_NAMESPACE, "")
data class Root(
  val version: String,
  val revision: String,
  @XmlSerialName("revision-date", "", "") val revisionDate: String,
  @XmlSerialName("prefix", UCUM_NAMESPACE, "") val prefixes: List<Prefix>,
  @XmlSerialName("base-unit", UCUM_NAMESPACE, "") val baseUnits: List<BaseUnit>,
  @XmlSerialName("unit", UCUM_NAMESPACE, "") val units: List<Unit>,
)

@Serializable
@XmlSerialName("prefix", UCUM_NAMESPACE, "")
data class Prefix(
  val Code: String,
  val CODE: String,
  @XmlElement(true) val name: String,
  val value: PrefixValue,
  @Transient val power: Int? = prefixValueRegex.find(value.value)?.groupValues?.single()?.toInt(),
)

@Serializable
@XmlSerialName("value", UCUM_NAMESPACE, "")
data class PrefixValue(val value: String, @XmlValue val content: String = "")

@Serializable
@XmlSerialName("base-unit", UCUM_NAMESPACE, "")
data class BaseUnit(
  val Code: String,
  val CODE: String,
  val dim: String,
  @XmlElement(true) val name: String,
  @XmlElement(true) val property: String,
)

@Serializable
@XmlSerialName("unit", UCUM_NAMESPACE, "")
data class Unit(
  val Code: String,
  val CODE: String,
  val isMetric: String? = null,
  @XmlSerialName("class", "", "") val `class`: String? = null,
  val isSpecial: String? = null,
  val isArbitrary: String? = null,
  @XmlElement(true) val name: List<String>,
  @XmlElement(true) val property: String,
  val value: UnitValue,
  @Transient var components: List<UnitComponent> = emptyList(),
)

@Serializable
@XmlSerialName("value", UCUM_NAMESPACE, "")
data class UnitValue(
  val value: String? = null,
  val Unit: String? = null,
  val UNIT: String? = null,
  @XmlValue(true) val content: String = "",
)
