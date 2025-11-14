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

package com.google.fhir.fhirpath.codegen.r4

import com.google.fhir.fhirpath.codegen.r4.schema.StructureDefinition
import com.google.fhir.fhirpath.codegen.r4.schema.StructureDefinition.Kind
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class R4HelperGenerationTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.NONE)
  // These are files retrieved from third_party/hl7.fhir.<R4|R4B|R5>.core directory
  abstract val corePackageFiles: ConfigurableFileCollection

  @get:Input abstract val modelPackageName: Property<String>
  @get:Input abstract val fhirPathPackageName: Property<String>

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
  }

  @TaskAction
  fun generateCode() {
    // Prepare the output folder
    val outputDir = outputDirectory.get().asFile

    // Prepare the input files
    val structureDefinitionInputFiles =
      corePackageFiles.files.flatMap { file ->
        // Use structure definitions files
        // NB filtering by file name is only an approximation.
        file.walkTopDown().filter {
          it.isFile && it.name.matches("StructureDefinition-[A-Za-z0-9]*\\.json".toRegex())
        }
      }

    // Only use structure definition files for resource types.
    val structureDefinitions =
      structureDefinitionInputFiles
        .asSequence()
        .filter { it.name.startsWith("StructureDefinition") }
        .map { json.decodeFromString<StructureDefinition>(it.readText(Charsets.UTF_8)) }
        .filterNot {
          // Do not generate classes for logical types e.g. `Definition`, `Request`, `Event`, etc.
          it.kind == Kind.LOGICAL
        }
        .filterNot {
          // Do not generate metadata resource or canonical resource types as interface inheritance
          // has not been implemented.
          it.name == "MetadataResource" || it.name == "CanonicalResource"
        }
        .filterNot {
          // ???
          it.kind == Kind.RESOURCE && it.name != it.id
        }
        .filterNot {
          // Filter out files like StructureDefinition-hdlcholesterol.json
          it.baseDefinition?.endsWith(it.type) == true
        }
        .toList()

    val modelPackageName = this.modelPackageName.get()
    val fhirPathPackageName = this.fhirPathPackageName.get()
    val fhirPathExtPackageName = "${fhirPathPackageName}.ext"

    // Generate resource extensions for accessing elements by name (e.g. `MorePatients.kt`)

    for (structureDefinition in structureDefinitions.filterNot { it.name == "Element" }) {
      ModelExtensionFileSpecGenerator.generate(
          modelPackageName = modelPackageName,
          fhirPathExtPackageName = fhirPathExtPackageName,
          structureDefinition = structureDefinition,
        )
        .writeTo(outputDir)
    }

    // Generate "routers" for accessing elements by name (e.g. `MoreResources.kt`)

    ResourceExtensionFileSpecGenerator.generate(
        modelPackageName = modelPackageName,
        fhriPathPackageName = fhirPathPackageName,
        fhirPathExtPackageName = fhirPathExtPackageName,
        structureDefinitions =
          structureDefinitions
            .filter { it.kind == Kind.RESOURCE }
            .filterNot { it.name == "Resource" }
            .filterNot { it.name == "DomainResource" },
      )
      .writeTo(outputDir)

    BackboneElementExtensionFileSpecGenerator.generate(
        modelPackageName = modelPackageName,
        fhirPathExtPackageName = fhirPathExtPackageName,
        structureDefinitions = structureDefinitions.filter { it.kind == Kind.RESOURCE },
      )
      .writeTo(outputDir)

    SealedInterfaceExtensionFileSpecGenerator.generate(
        modelPackageName = modelPackageName,
        fhirPathExtPackageName = fhirPathExtPackageName,
        structureDefinitions =
          structureDefinitions
            .filter { it.kind == Kind.RESOURCE }
            .filterNot { it.name == "Resource" }
            .filterNot { it.name == "DomainResource" },
      )
      .writeTo(outputDir)

    ComplexTypeExtensionFileSpecGenerator.generate(
        modelPackageName = modelPackageName,
        fhirPathExtPackageName = fhirPathExtPackageName,
        structureDefinitions =
          structureDefinitions
            .filter { it.kind == Kind.COMPLEX_TYPE }
            .filterNot { it.name == "Element" },
      )
      .writeTo(outputDir)

    // Generate primitive and complex type enums

    PrimitiveTypeEnumFileSpecGenerator.generate(
        modelPackageName = modelPackageName,
        fhirPathPackageName = fhirPathPackageName,
        structureDefinitions = structureDefinitions.filter { it.kind == Kind.PRIMITIVE_TYPE },
      )
      .writeTo(outputDir)

    ComplexTypeEnumFileSpecGenerator.generate(
        modelPackageName = modelPackageName,
        fhirPathPackageName = fhirPathPackageName,
        structureDefinitions =
          structureDefinitions.filter { it.kind == Kind.COMPLEX_TYPE && it.name != "Element" },
      )
      .writeTo(outputDir)
  }
}
