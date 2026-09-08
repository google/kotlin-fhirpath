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

package dev.ohs.fhir.fhirpath

import dev.ohs.fhir.fhirpath.model.FhirR4BModelNavigator
import dev.ohs.fhir.fhirpath.types.SimpleTypeInfo
import dev.ohs.fhir.model.r4b.Age
import dev.ohs.fhir.model.r4b.Enumeration
import dev.ohs.fhir.model.r4b.Extension
import dev.ohs.fhir.model.r4b.HumanName
import dev.ohs.fhir.model.r4b.Observation
import dev.ohs.fhir.model.r4b.Patient
import dev.ohs.fhir.model.r4b.Quantity
import dev.ohs.fhir.model.r4b.String as FhirString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class FhirR4BModelNavigatorTest {

  // --- Resources ---

  @Test
  fun `can access primitive property on resource`() {
    val patient = Patient(id = "p1")
    assertEquals("p1", FhirR4BModelNavigator.accessProperty(patient, "id")?.toString())
  }

  @Test
  fun `can access complex list property on resource`() {
    val patient = Patient(name = listOf(HumanName(family = FhirString("Smith"))))
    assertEquals(
      listOf(HumanName(family = FhirString("Smith"))),
      FhirR4BModelNavigator.accessProperty(patient, "name"),
    )
  }

  @Test
  fun `can access choice property on resource`() {
    val observation: Observation =
      Json { ignoreUnknownKeys = true }
        .decodeFromString(
          """{"resourceType": "Observation", "status": "final", "code": {"text": "w"}, "valueQuantity": {"unit": "kg"}}"""
        )
    val value = FhirR4BModelNavigator.accessProperty(observation, "value") as Quantity
    assertEquals("kg", value.unit?.value)
  }

  @Test
  fun `returns null for non existent property on resource in lenient mode`() {
    val patient = Patient(id = "p1")
    assertNull(FhirR4BModelNavigator.accessProperty(patient, "nonExistentProperty"))
  }

  @Test
  fun `throws exception for non existent property on resource in strict mode`() {
    val patient = Patient(id = "p1")
    assertFailsWith<IllegalStateException> {
      FhirR4BModelNavigator.accessProperty(patient, "nonExistentProperty", strictMode = true)
    }
  }

  @Test
  fun `canHaveChildren returns true for resource`() {
    val patient = Patient(id = "p1")
    assertTrue(FhirR4BModelNavigator.canHaveChildren(patient))
  }

  @Test
  fun `can get all children on resource`() {
    val patient = Patient(id = "p1", name = listOf(HumanName(family = FhirString("Smith"))))
    val children = FhirR4BModelNavigator.getAllChildren(patient)
    assertEquals(2, children.size)
    assertTrue(children.contains(HumanName(family = FhirString("Smith"))))
  }

  // --- Backbone elements ---

  @Test
  fun `can access property on backbone element`() {
    val contactName =
      HumanName(family = FhirString("du Marché"), given = listOf(FhirString("Bénédicte")))
    val contact = Patient.Contact(name = contactName)
    assertEquals(contactName, FhirR4BModelNavigator.accessProperty(contact, "name"))
  }

  @Test
  fun `returns null for non existent property on backbone element`() {
    val contact = Patient.Contact()
    assertNull(FhirR4BModelNavigator.accessProperty(contact, "nonExistentProperty"))
  }

  @Test
  fun `canHaveChildren returns true for backbone element`() {
    val contact = Patient.Contact()
    assertTrue(FhirR4BModelNavigator.canHaveChildren(contact))
  }

  @Test
  fun `can get all children on backbone element`() {
    val contact = Patient.Contact(name = HumanName(family = FhirString("Smith")))
    val children = FhirR4BModelNavigator.getAllChildren(contact)
    assertEquals(1, children.size)
    assertTrue(children.contains(HumanName(family = FhirString("Smith"))))
  }

  // --- Complex types ---

  @Test
  fun `can access property on complex type`() {
    val name = HumanName(family = FhirString("Chalmers"), given = listOf(FhirString("Peter")))
    assertEquals(FhirString("Chalmers"), FhirR4BModelNavigator.accessProperty(name, "family"))
    assertEquals(listOf(FhirString("Peter")), FhirR4BModelNavigator.accessProperty(name, "given"))
  }

  @Test
  fun `can access property on complex type subtype`() {
    val age = Age(unit = FhirString("years"))
    assertEquals(FhirString("years"), FhirR4BModelNavigator.accessProperty(age, "unit"))
  }

  @Test
  fun `returns null for non existent property on complex type`() {
    val name = HumanName(family = FhirString("Chalmers"))
    assertNull(FhirR4BModelNavigator.accessProperty(name, "nonExistentProperty"))
  }

  @Test
  fun `canHaveChildren returns true for complex type`() {
    val name = HumanName(family = FhirString("Chalmers"))
    assertTrue(FhirR4BModelNavigator.canHaveChildren(name))
  }

  @Test
  fun `can get all children on complex type`() {
    val name = HumanName(family = FhirString("Chalmers"), given = listOf(FhirString("Peter")))
    val children = FhirR4BModelNavigator.getAllChildren(name)

    assertTrue(children.contains(FhirString("Chalmers")))
    assertTrue(children.contains(FhirString("Peter")))
  }

  // --- Primitive types ---

  @Test
  fun `can access properties on primitive type`() {
    val fhirString =
      FhirString(
        id = "s1",
        extension =
          listOf(
            Extension(
              url = "https://example.org/x",
              value = Extension.Value.String(FhirString("e")),
            )
          ),
        value = "hello",
      )
    assertEquals("s1", FhirR4BModelNavigator.accessProperty(fhirString, "id"))
    assertEquals(1, (FhirR4BModelNavigator.accessProperty(fhirString, "extension") as List<*>).size)
    assertEquals("hello", FhirR4BModelNavigator.accessProperty(fhirString, "value"))
  }

  @Test
  fun `canHaveChildren returns true for primitive type`() {
    val fhirString = FhirString("hello")
    assertTrue(FhirR4BModelNavigator.canHaveChildren(fhirString))
  }

  @Test
  fun `can get all children on primitive type`() {
    val fhirString =
      FhirString(
        id = "s1",
        extension =
          listOf(
            Extension(
              url = "https://example.org/x",
              value = Extension.Value.String(FhirString("e")),
            )
          ),
        value = "hello",
      )
    val children = FhirR4BModelNavigator.getAllChildren(fhirString)
    assertTrue(children.contains("s1"))
    assertTrue(children.contains("hello"))
    assertEquals(3, children.size)
  }

  // --- Enumerations ---

  private val enumerationWithAllProperties =
    Enumeration(
      id = "e1",
      extension =
        listOf(
          Extension(
            url = "https://example.org/x",
            value = Extension.Value.String(FhirString(value = "e")),
          )
        ),
      value = Observation.ObservationStatus.Final,
    )

  @Test
  fun `can access id property on enumeration`() {
    assertEquals("e1", FhirR4BModelNavigator.accessProperty(enumerationWithAllProperties, "id"))
  }

  @Test
  fun `can access extension property on enumeration`() {
    assertEquals(
      1,
      (FhirR4BModelNavigator.accessProperty(enumerationWithAllProperties, "extension") as List<*>)
        .size,
    )
  }

  @Test
  fun `can access value property on enumeration`() {
    assertEquals(
      Observation.ObservationStatus.Final,
      FhirR4BModelNavigator.accessProperty(enumerationWithAllProperties, "value"),
    )
  }

  @Test
  fun `returns null for non existent property on enumeration`() {
    assertNull(FhirR4BModelNavigator.accessProperty(enumerationWithAllProperties, "nonExistent"))
  }

  @Test
  fun `canHaveChildren returns true for enumeration`() {
    assertTrue(FhirR4BModelNavigator.canHaveChildren(enumerationWithAllProperties))
  }

  @Test
  fun `can get all children on enumeration`() {
    val children = FhirR4BModelNavigator.getAllChildren(enumerationWithAllProperties)
    assertTrue(children.contains("e1"))
    assertTrue(children.contains(Observation.ObservationStatus.Final))
    assertEquals(3, children.size)
  }

  // --- Choice types ---

  @Test
  fun `can access property on choice type`() {
    val quantity = Quantity(unit = FhirString("kg"))
    val valueChoice: Observation.Value = Observation.Value.Quantity(quantity)
    assertEquals(FhirString("kg"), FhirR4BModelNavigator.accessProperty(valueChoice, "unit"))
  }

  // --- TypeInfo reflection ---

  @Test
  fun `can access reflection properties on TypeInfo`() {
    val typeInfo = SimpleTypeInfo(namespace = "System", name = "String")
    assertEquals("System", FhirR4BModelNavigator.accessProperty(typeInfo, "namespace"))
    assertEquals("String", FhirR4BModelNavigator.accessProperty(typeInfo, "name"))
    assertEquals("System.Any", FhirR4BModelNavigator.accessProperty(typeInfo, "baseType"))
    assertNull(FhirR4BModelNavigator.accessProperty(typeInfo, "nonExistent"))
  }

  // --- Non-model objects ---

  @Test
  fun `canHaveChildren returns false for non model objects`() {
    assertFalse(FhirR4BModelNavigator.canHaveChildren("not a model"))
  }

  @Test
  fun `getAllChildren returns empty list for non model objects`() {
    assertEquals(emptyList(), FhirR4BModelNavigator.getAllChildren("not a model"))
  }
}
