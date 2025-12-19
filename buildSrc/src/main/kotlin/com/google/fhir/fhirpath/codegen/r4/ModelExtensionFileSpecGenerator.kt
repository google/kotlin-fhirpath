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
import com.google.fhir.fhirpath.codegen.r4.schema.backboneElements
import com.google.fhir.fhirpath.codegen.r4.schema.capitalized
import com.google.fhir.fhirpath.codegen.r4.schema.getElementName
import com.google.fhir.fhirpath.codegen.r4.schema.getNestedClassName
import com.google.fhir.fhirpath.codegen.r4.schema.rootElements
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import kotlin.collections.iterator

object ModelExtensionFileSpecGenerator {
  fun generate(
    modelPackageName: String,
    fhirPathExtPackageName: String,
    structureDefinition: StructureDefinition,
  ): FileSpec {
    val modelClassName = ClassName(modelPackageName, structureDefinition.name.capitalized())

    return FileSpec.builder(
        fhirPathExtPackageName,
        "More${structureDefinition.name.capitalized()}s",
      )
      .addFunction(
        FunSpec.builder("getProperty")
          .addModifiers(KModifier.INTERNAL)
          .receiver(modelClassName)
          .addParameter(name = "name", type = String::class)
          .returns(Any::class.asTypeName().copy(nullable = true))
          .beginControlFlow("return when(name)")
          .apply {
            for (element in structureDefinition.rootElements) {
              addStatement("%S -> this.%N", element.getElementName(), element.getElementName())
            }
            addStatement("else -> error(\"\$name is not a valid property name\")")
          }
          .endControlFlow()
          .build()
      )
      .addFunction(
        FunSpec.builder("hasProperty")
          .addModifiers(KModifier.INTERNAL)
          .receiver(modelClassName)
          .addParameter(name = "name", type = String::class)
          .returns(Boolean::class)
          .beginControlFlow("return when(name)")
          .apply {
            for (element in structureDefinition.rootElements) {
              addStatement("%S -> true", element.getElementName())
            }
            addStatement("else -> false")
          }
          .endControlFlow()
          .build()
      )
      .addFunction(
        FunSpec.builder("getAllChildren")
          .addModifiers(KModifier.INTERNAL)
          .receiver(modelClassName)
          .returns(LIST.parameterizedBy(Any::class.asTypeName()))
          .addCode("return buildList {\n")
          .apply {
            for (element in structureDefinition.rootElements) {
              val elementName = element.getElementName()
              addCode(
                "  this@getAllChildren.%N?.let { if (it is kotlin.collections.List<*>) addAll(it as Collection<Any>) else add(it) }\n",
                elementName,
              )
            }
          }
          .addCode("}\n")
          .build()
      )
      .apply {
        for (backboneElement in structureDefinition.backboneElements) {
          addFunction(
            FunSpec.builder("getProperty")
              .addModifiers(KModifier.INTERNAL)
              .receiver(backboneElement.key.getNestedClassName(modelClassName))
              .addParameter(name = "name", type = String::class)
              .returns(Any::class.asTypeName().copy(nullable = true))
              .beginControlFlow("return when(name)")
              .apply {
                for (element in backboneElement.value) {
                  addStatement("%S -> %N", element.getElementName(), element.getElementName())
                }
                addStatement("else -> error(\"\$name is not a valid property name\")")
              }
              .endControlFlow()
              .build()
          )
          addFunction(
            FunSpec.builder("hasProperty")
              .addModifiers(KModifier.INTERNAL)
              .receiver(backboneElement.key.getNestedClassName(modelClassName))
              .addParameter(name = "name", type = String::class)
              .returns(Boolean::class)
              .beginControlFlow("return when(name)")
              .apply {
                for (element in backboneElement.value) {
                  addStatement("%S -> true", element.getElementName())
                }
                addStatement("else -> false")
              }
              .endControlFlow()
              .build()
          )
          addFunction(
            FunSpec.builder("getAllChildren")
              .addModifiers(KModifier.INTERNAL)
              .receiver(backboneElement.key.getNestedClassName(modelClassName))
              .returns(LIST.parameterizedBy(Any::class.asTypeName()))
              .addCode("return buildList {\n")
              .apply {
                for (element in backboneElement.value) {
                  val elementName = element.getElementName()
                  addCode(
                    "  this@getAllChildren.%N?.let { if (it is kotlin.collections.List<*>) addAll(it as Collection<Any>) else add(it) }\n",
                    elementName,
                  )
                }
              }
              .addCode("}\n")
              .build()
          )
        }
      }
      .build()
  }
}
