/*
 * Copyright 2025-2026 Google LLC
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
import com.google.fhir.fhirpath.codegen.r4.schema.capitalized
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName

object ResourceExtensionFileSpecGenerator {
  fun generate(
    modelPackageName: String,
    modelExtensionPackageName: String,
    fhirPathPackageName: String,
    structureDefinitions: List<StructureDefinition>,
  ): FileSpec {
    val resourceType = ClassName(modelPackageName, "Resource")
    return FileSpec.builder(modelExtensionPackageName, "MoreResources")
      .addFunction(
        FunSpec.builder("getFhirType")
          .addModifiers(KModifier.INTERNAL)
          .receiver(resourceType)
          .returns(ClassName(fhirPathPackageName, "FhirType").copy(nullable = true))
          .beginControlFlow("return when(this)")
          .apply {
            for (structureDefinition in structureDefinitions) {
              val typeName = structureDefinition.name.capitalized()
              when (structureDefinition.kind) {
                Kind.RESOURCE -> {
                  addStatement(
                    "is %T -> %T(%T.%N)",
                    ClassName(modelPackageName, typeName),
                    ClassName("com.google.fhir.fhirpath", "FhirResourceType"),
                    ClassName("$modelPackageName.terminologies", "ResourceType"),
                    typeName,
                  )
                }
                else -> error(": ${structureDefinition.kind} for ${structureDefinition.name}")
              }
            }
            addStatement("else -> null")
          }
          .endControlFlow()
          .build()
      )
      .addFunction(
        FunSpec.builder("getProperty")
          .addModifiers(KModifier.INTERNAL)
          .receiver(resourceType)
          .returns(Any::class.asTypeName().copy(nullable = true))
          .addParameter(name = "name", type = String::class)
          .beginControlFlow("return when(this)")
          .apply {
            for (structureDefinition in structureDefinitions) {
              addStatement(
                "is %T -> getProperty(name)",
                ClassName(modelPackageName, structureDefinition.name.capitalized()),
              )
            }
            addStatement("else -> null")
          }
          .endControlFlow()
          .build()
      )
      .addFunction(
        FunSpec.builder("hasProperty")
          .addModifiers(KModifier.INTERNAL)
          .receiver(resourceType)
          .returns(Boolean::class)
          .addParameter(name = "name", type = String::class)
          .beginControlFlow("return when(this)")
          .apply {
            for (structureDefinition in structureDefinitions) {
              addStatement(
                "is %T -> hasProperty(name)",
                ClassName(modelPackageName, structureDefinition.name.capitalized()),
              )
            }
            addStatement("else -> false")
          }
          .endControlFlow()
          .build()
      )
      .addFunction(
        FunSpec.builder("getAllChildren")
          .addModifiers(KModifier.INTERNAL)
          .receiver(resourceType)
          .returns(LIST.parameterizedBy(Any::class.asTypeName()))
          .beginControlFlow("return when(this)")
          .apply {
            for (structureDefinition in structureDefinitions) {
              addStatement(
                "is %T -> getAllChildren()",
                ClassName(modelPackageName, structureDefinition.name.capitalized()),
              )
            }
            addStatement("else -> emptyList()")
          }
          .endControlFlow()
          .build()
      )
      .build()
  }
}
