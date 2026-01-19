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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec

object PrimitiveTypeEnumFileSpecGenerator {
  fun generate(
    modelPackageName: String,
    fhirPathPackageName: String,
    fhirVersion: String,
    structureDefinitions: List<StructureDefinition>,
  ): FileSpec {
    val className = ClassName(fhirPathPackageName, "Fhir${fhirVersion.uppercase()}PrimitiveType")
    return FileSpec.builder(className)
      .addType(
        TypeSpec.enumBuilder(className)
          .addSuperinterface(ClassName(fhirPathPackageName, "FhirType"))
          .primaryConstructor(
            FunSpec.constructorBuilder().addParameter("typeName", String::class).build()
          )
          .addProperty(
            PropertySpec.builder("typeName", String::class, KModifier.OVERRIDE)
              .initializer("typeName")
              .build()
          )
          .apply {
            for (structureDefinition in structureDefinitions) {
              when (structureDefinition.kind) {
                Kind.PRIMITIVE_TYPE -> {
                  val typeName = structureDefinition.name
                  addEnumConstant(
                    typeName.capitalized(),
                    TypeSpec.anonymousClassBuilder()
                      .addSuperclassConstructorParameter("%S", typeName)
                      .build(),
                  )
                }
                else ->
                  error(
                    "Unexpected kind: ${structureDefinition.kind} for ${structureDefinition.name}"
                  )
              }
            }
          }
          .addType(
            TypeSpec.companionObjectBuilder()
              .addFunction(
                FunSpec.builder("fromString")
                  .addParameter("value", String::class)
                  .returns(className.copy(nullable = true))
                  .addStatement("return entries.find { it.typeName == value }")
                  .build()
              )
              .addFunction(
                FunSpec.builder("fromObject")
                  .addParameter("value", Any::class)
                  .returns(className.copy(nullable = true))
                  .addStatement(
                    "return %L",
                    CodeBlock.builder()
                      .beginControlFlow("when (value)")
                      .apply {
                        for (structureDefinition in structureDefinitions) {
                          when (structureDefinition.kind) {
                            Kind.PRIMITIVE_TYPE -> {
                              val typeName = structureDefinition.name
                              addStatement(
                                "is %T -> %N",
                                ClassName(modelPackageName, typeName.capitalized()),
                                typeName.capitalized(),
                              )
                            }
                            else ->
                              error(
                                "Unexpected kind: ${structureDefinition.kind} for ${structureDefinition.name}"
                              )
                          }
                        }

                        // Enumerations are a special case that should be mapped to Code
                        addStatement(
                          "is %T -> %N",
                          ClassName(modelPackageName, "Enumeration").parameterizedBy(STAR),
                          "Code",
                        )
                      }
                      .addStatement("else -> null")
                      .endControlFlow()
                      .build(),
                  )
                  .build()
              )
              .build()
          )
          .build()
      )
      .build()
  }
}
