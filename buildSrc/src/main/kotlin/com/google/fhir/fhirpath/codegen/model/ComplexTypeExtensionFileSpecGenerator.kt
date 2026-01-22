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

package com.google.fhir.fhirpath.codegen.model

import com.google.fhir.fhirpath.codegen.model.schema.StructureDefinition
import com.google.fhir.fhirpath.codegen.model.schema.capitalized
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName

object ComplexTypeExtensionFileSpecGenerator {
  fun generate(
    modelPackageName: String,
    modelExtensionPackageName: String,
    structureDefinitions: List<StructureDefinition>,
  ): FileSpec {
    return FileSpec.builder(modelExtensionPackageName, "MoreComplexTypes")
      .addFunction(
        FunSpec.builder("getProperty")
          .addModifiers(KModifier.INTERNAL)
          .receiver(ClassName(modelPackageName, "Element"))
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
          .receiver(ClassName(modelPackageName, "Element"))
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
          .receiver(ClassName(modelPackageName, "Element"))
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
