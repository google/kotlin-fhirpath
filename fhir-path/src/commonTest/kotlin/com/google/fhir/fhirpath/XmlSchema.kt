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

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("tests")
data class Tests(
  val name: String,
  val description: String,
  val reference: String,
  val groups: List<Group>,
)

@Serializable
@XmlSerialName("group")
data class Group(
  val name: String,
  val reference: String? = null,
  @XmlElement(true) val notes: String? = null,
  val description: String? = null,
  val tests: List<Test>,
)

@Serializable
@XmlSerialName("test")
data class Test(
  val name: String,
  val description: String? = null,
  val inputfile: String? = null,
  val invalid: Boolean? = null,
  val ordered: Boolean? = null,
  val mode: String? = null,
  val checkOrderedFunctions: Boolean? = null,
  val isPredicate: Boolean? = null,
  val expression: Expression,
  val outputs: List<Output>,
  val predicate: Boolean? = null,
  val version: String? = null,
)

@Serializable
@XmlSerialName("expression")
data class Expression(
  val invalid: String? = null,
  val mode: String? = null,
  @XmlValue val value: String,
)

@Serializable
@XmlSerialName("output")
data class Output(val type: String? = null, @XmlValue val value: String)
