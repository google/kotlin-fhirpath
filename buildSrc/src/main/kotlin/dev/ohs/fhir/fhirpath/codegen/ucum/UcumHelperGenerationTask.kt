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

package dev.ohs.fhir.fhirpath.codegen.ucum

import dev.ohs.fhir.fhirpath.codegen.ucum.schema.Root
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import nl.adaptivity.xmlutil.serialization.XML
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Regular expression used to remove invalid characters in the generated enum name. */
val ENUM_NAME_REGEX = Regex("[^a-zA-Z0-9]")

@CacheableTask
abstract class UcumHelperGenerationTask : DefaultTask() {
  @get:InputFile @get:PathSensitive(PathSensitivity.NONE) abstract val ucumFile: RegularFileProperty

  @get:Input abstract val packageName: Property<String>

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun generateCode() {
    val packageNameString = packageName.get()
    val ucumEssenceXmlContent =
      ucumFile
        .get()
        .asFile
        .readText()
        .replace(Regex("<printSymbol>[\\s\\S]*?</printSymbol>"), "") // Remove printSymbol tags
        .replace("<printSymbol/>", "") // Remove empty printSymbol tags
        .replace(Regex("<(sup|sub|i|r)>(.*?)</\\1>"), "$2") // Remove nested tags
        .replace(Regex("<function[\\s\\S]*?/>"), "") // Remove functions
    val root = XML.decodeFromString<Root>(ucumEssenceXmlContent)

    FileSpec.builder(packageName.get(), "Ucum")
      .addPrefixEnum(packageNameString, root)
      .addBaseUnitEnum(packageNameString, root)
      .addUnitEnum(packageNameString, root)
      .build()
      .writeTo(outputDirectory.get().asFile)
  }
}

private fun FileSpec.Builder.addPrefixEnum(packageName: String, root: Root): FileSpec.Builder =
  apply {
    val prefixClassName = ClassName(packageName, "Prefix")
    addType(
      TypeSpec.enumBuilder(prefixClassName)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("code", String::class)
            .addParameter("power", Int::class)
            .build()
        )
        .addProperty(PropertySpec.builder("code", String::class).initializer("code").build())
        .addProperty(PropertySpec.builder("power", Int::class).initializer("power").build())
        .apply {
          // Ignore prefixes used in computer science. They are not defined as powers of 10.
          // See https://ucum.org/ucum#section-Prefixes-and-Units-Used-in-Information-Technology.
          for (prefix in root.prefixes.filter { it.power != null }) {
            addEnumConstant(
              prefix.name.uppercase(),
              TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%S", prefix.Code)
                .addSuperclassConstructorParameter("%L", prefix.power!!)
                .build(),
            )
          }
        }
        .addType(
          TypeSpec.companionObjectBuilder()
            .addFunction(
              FunSpec.builder("fromString")
                .addParameter("value", String::class)
                .returns(prefixClassName.copy(nullable = true))
                .addStatement("return entries.find { it.code == value }")
                .build()
            )
            .build()
        )
        .build()
    )
  }

private fun FileSpec.Builder.addBaseUnitEnum(packageName: String, root: Root): FileSpec.Builder =
  apply {
    val baseUnitClassName = ClassName(packageName, "BaseUnit")
    addType(
      TypeSpec.enumBuilder(baseUnitClassName)
        .primaryConstructor(
          FunSpec.constructorBuilder().addParameter("code", String::class).build()
        )
        .addProperty(PropertySpec.builder("code", String::class).initializer("code").build())
        .apply {
          for (unit in root.baseUnits) {
            addEnumConstant(
              unit.name.uppercase(),
              TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%S", unit.Code)
                .build(),
            )
          }
        }
        .addType(
          TypeSpec.companionObjectBuilder()
            .addFunction(
              FunSpec.builder("fromString")
                .addParameter("value", String::class)
                .returns(baseUnitClassName.copy(nullable = true))
                .addStatement("return entries.find { it.code == value }")
                .build()
            )
            .build()
        )
        .build()
    )
  }

private fun FileSpec.Builder.addUnitEnum(packageName: String, root: Root): FileSpec.Builder =
  apply {
    val unitClassName = ClassName(packageName, "Unit")
    val ucumUnitCanonicalizer = UcumUnitCanonicalizer(root)

    addType(
      TypeSpec.enumBuilder(unitClassName)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("code", String::class)
            .addParameter("base", String::class)
            .addParameter("scalar", Double::class)
            .build()
        )
        .addProperty(PropertySpec.builder("code", String::class).initializer("code").build())
        .addProperty(PropertySpec.builder("base", String::class).initializer("base").build())
        .addProperty(PropertySpec.builder("scalar", Double::class).initializer("scalar").build())
        .apply {
          // Generate unique enum names for units
          val nameToUnitMap =
            root.units
              .groupBy { it.name.first().normalizeUnitName() }
              .flatMap {
                if (it.value.size == 1) {
                  listOf(it.key to it.value.single())
                } else {
                  // Append a number in the enum name to differentiate units with the same name
                  it.value.mapIndexed { i, unit -> "${it.key}_${i+1}" to unit }
                }
              }
              .toMap()
          for ((name, unit) in nameToUnitMap) {
            val code = unit.Code
            val canonicalRepresentation = ucumUnitCanonicalizer.canonicalize(code)
            addEnumConstant(
              name,
              TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%S", code)
                .addSuperclassConstructorParameter(
                  "%S",
                  canonicalRepresentation?.getBaseUnitString() ?: "",
                )
                .addSuperclassConstructorParameter("%L", canonicalRepresentation?.scalar ?: 1)
                .build(),
            )
          }
        }
        .addType(
          TypeSpec.companionObjectBuilder()
            .addFunction(
              FunSpec.builder("fromString")
                .addParameter("value", String::class)
                .returns(unitClassName.copy(nullable = true))
                .addStatement("return entries.find { it.code == value }")
                .build()
            )
            .build()
        )
        .build()
    )
  }

/**
 * Sanitizes the unit name for enum generation:
 * - Replaces `/` with `per` to make the generated enum name more readable
 * - Removes invalid characters
 * - Converts to uppercase
 * - Joins each part with `_`
 */
private fun String.normalizeUnitName(): String {
  return replace("/", "Per")
    .replace(ENUM_NAME_REGEX, " ")
    .split(" ")
    .filter { it.isNotBlank() }
    .joinToString("_") { it.uppercase() }
}
