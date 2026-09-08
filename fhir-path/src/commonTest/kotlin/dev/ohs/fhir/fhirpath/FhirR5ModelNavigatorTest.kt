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

import dev.ohs.fhir.fhirpath.model.FhirR5ModelNavigator
import dev.ohs.fhir.fhirpath.types.SimpleTypeInfo
import dev.ohs.fhir.model.r5.Age
import dev.ohs.fhir.model.r5.Enumeration
import dev.ohs.fhir.model.r5.Extension
import dev.ohs.fhir.model.r5.HumanName
import dev.ohs.fhir.model.r5.Observation
import dev.ohs.fhir.model.r5.Patient
import dev.ohs.fhir.model.r5.Quantity
import dev.ohs.fhir.model.r5.String as FhirString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class FhirR5ModelNavigatorTest {

  // --- Resources ---

  @Test
  fun `can access primitive property on resource`() {
    val patient = Patient(id = "p1")
    assertEquals("p1", FhirR5ModelNavigator.accessProperty(patient, "id")?.toString())
  }

  @Test
  fun `can access complex list property on resource`() {
    val patient = Patient(name = listOf(HumanName(family = FhirString("Smith"))))
    assertEquals(
      listOf(HumanName(family = FhirString("Smith"))),
      FhirR5ModelNavigator.accessProperty(patient, "name"),
    )
  }

  @Test
  fun `can access choice property on resource`() {
    val observation: Observation =
      Json { ignoreUnknownKeys = true }
        .decodeFromString(
          """{"resourceType": "Observation", "status": "final", "code": {"text": "w"}, "valueQuantity": {"unit": "kg"}}"""
        )
    val value = FhirR5ModelNavigator.accessProperty(observation, "value") as Quantity
    assertEquals("kg", value.unit?.value)
  }

  @Test
  fun `returns null for non existent property on resource in lenient mode`() {
    val patient = Patient(id = "p1")
    assertNull(FhirR5ModelNavigator.accessProperty(patient, "nonExistentProperty"))
  }

  @Test
  fun `throws exception for non existent property on resource in strict mode`() {
    val patient = Patient(id = "p1")
    assertFailsWith<IllegalStateException> {
      FhirR5ModelNavigator.accessProperty(patient, "nonExistentProperty", strictMode = true)
    }
  }

  @Test
  fun `canHaveChildren returns true for resource`() {
    val patient = Patient(id = "p1")
    assertTrue(FhirR5ModelNavigator.canHaveChildren(patient))
  }

  @Test
  fun `can get all children on resource`() {
    val patient = Patient(id = "p1", name = listOf(HumanName(family = FhirString("Smith"))))
    val children = FhirR5ModelNavigator.getAllChildren(patient)
    assertEquals(2, children.size)
    assertTrue(children.contains(HumanName(family = FhirString("Smith"))))
  }

  // --- Backbone elements ---

  @Test
  fun `can access property on backbone element`() {
    val contactName =
      HumanName(family = FhirString("du Marché"), given = listOf(FhirString("Bénédicte")))
    val contact = Patient.Contact(name = contactName)
    assertEquals(contactName, FhirR5ModelNavigator.accessProperty(contact, "name"))
  }

  @Test
  fun `returns null for non existent property on backbone element`() {
    val contact = Patient.Contact()
    assertNull(FhirR5ModelNavigator.accessProperty(contact, "nonExistentProperty"))
  }

  @Test
  fun `canHaveChildren returns true for backbone element`() {
    val contact = Patient.Contact()
    assertTrue(FhirR5ModelNavigator.canHaveChildren(contact))
  }

  @Test
  fun `can get all children on backbone element`() {
    val contact = Patient.Contact(name = HumanName(family = FhirString("Smith")))
    val children = FhirR5ModelNavigator.getAllChildren(contact)
    assertEquals(1, children.size)
    assertTrue(children.contains(HumanName(family = FhirString("Smith"))))
  }

  // --- Complex types ---

  @Test
  fun `can access property on complex type`() {
    val name = HumanName(family = FhirString("Chalmers"), given = listOf(FhirString("Peter")))
    assertEquals(FhirString("Chalmers"), FhirR5ModelNavigator.accessProperty(name, "family"))
    assertEquals(listOf(FhirString("Peter")), FhirR5ModelNavigator.accessProperty(name, "given"))
  }

  @Test
  fun `can access property on complex type subtype`() {
    val age = Age(unit = FhirString("years"))
    assertEquals(FhirString("years"), FhirR5ModelNavigator.accessProperty(age, "unit"))
  }

  @Test
  fun `returns null for non existent property on complex type`() {
    val name = HumanName(family = FhirString("Chalmers"))
    assertNull(FhirR5ModelNavigator.accessProperty(name, "nonExistentProperty"))
  }

  @Test
  fun `canHaveChildren returns true for complex type`() {
    val name = HumanName(family = FhirString("Chalmers"))
    assertTrue(FhirR5ModelNavigator.canHaveChildren(name))
  }

  @Test
  fun `can get all children on complex type`() {
    val name = HumanName(family = FhirString("Chalmers"), given = listOf(FhirString("Peter")))
    val children = FhirR5ModelNavigator.getAllChildren(name)

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
    assertEquals("s1", FhirR5ModelNavigator.accessProperty(fhirString, "id"))
    assertEquals(1, (FhirR5ModelNavigator.accessProperty(fhirString, "extension") as List<*>).size)
    assertEquals("hello", FhirR5ModelNavigator.accessProperty(fhirString, "value"))
  }

  @Test
  fun `canHaveChildren returns true for primitive type`() {
    val fhirString = FhirString("hello")
    assertTrue(FhirR5ModelNavigator.canHaveChildren(fhirString))
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
    val children = FhirR5ModelNavigator.getAllChildren(fhirString)
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
    assertEquals("e1", FhirR5ModelNavigator.accessProperty(enumerationWithAllProperties, "id"))
  }

  @Test
  fun `can access extension property on enumeration`() {
    assertEquals(
      1,
      (FhirR5ModelNavigator.accessProperty(enumerationWithAllProperties, "extension") as List<*>)
        .size,
    )
  }

  @Test
  fun `can access value property on enumeration`() {
    assertEquals(
      Observation.ObservationStatus.Final,
      FhirR5ModelNavigator.accessProperty(enumerationWithAllProperties, "value"),
    )
  }

  @Test
  fun `returns null for non existent property on enumeration`() {
    assertNull(FhirR5ModelNavigator.accessProperty(enumerationWithAllProperties, "nonExistent"))
  }

  @Test
  fun `canHaveChildren returns true for enumeration`() {
    assertTrue(FhirR5ModelNavigator.canHaveChildren(enumerationWithAllProperties))
  }

  @Test
  fun `can get all children on enumeration`() {
    val children = FhirR5ModelNavigator.getAllChildren(enumerationWithAllProperties)
    assertTrue(children.contains("e1"))
    assertTrue(children.contains(Observation.ObservationStatus.Final))
    assertEquals(3, children.size)
  }

  // --- Choice types ---

  @Test
  fun `can access property on choice type`() {
    val quantity = Quantity(unit = FhirString("kg"))
    val valueChoice: Observation.Value = Observation.Value.Quantity(quantity)
    assertEquals(FhirString("kg"), FhirR5ModelNavigator.accessProperty(valueChoice, "unit"))
  }

  // --- TypeInfo reflection ---

  @Test
  fun `can access reflection properties on TypeInfo`() {
    val typeInfo = SimpleTypeInfo(namespace = "System", name = "String")
    assertEquals("System", FhirR5ModelNavigator.accessProperty(typeInfo, "namespace"))
    assertEquals("String", FhirR5ModelNavigator.accessProperty(typeInfo, "name"))
    assertEquals("System.Any", FhirR5ModelNavigator.accessProperty(typeInfo, "baseType"))
    assertNull(FhirR5ModelNavigator.accessProperty(typeInfo, "nonExistent"))
  }

  // --- Non-model objects ---

  @Test
  fun `canHaveChildren returns false for non model objects`() {
    assertFalse(FhirR5ModelNavigator.canHaveChildren("not a model"))
  }

  @Test
  fun `getAllChildren returns empty list for non model objects`() {
    assertEquals(emptyList(), FhirR5ModelNavigator.getAllChildren("not a model"))
  }
}
