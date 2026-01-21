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
import com.google.fhir.fhirpath.codegen.model.schema.StructureDefinition.Kind
import com.google.fhir.fhirpath.codegen.model.schema.capitalized
import com.google.fhir.model.r4.terminologies.ResourceType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object TypeExtensionFileSpecGenerator {
  fun generate(
    modelPackageName: String,
    fhirPathPackageName: String,
    structureDefinitions: List<StructureDefinition>,
  ): FileSpec {
    return FileSpec.builder(fhirPathPackageName, "MoreTypes")
      .addFunction(
        FunSpec.builder("getFhirType")
          .addModifiers(KModifier.INTERNAL)
          .receiver(Any::class)
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
                    ResourceType::class,
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
      .build()
  }
}
