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

package com.google.fhir.fhirpath

import com.google.fhir.fhirpath.model.FhirModelNavigator
import com.google.fhir.fhirpath.model.FhirR4BModelNavigator
import com.google.fhir.fhirpath.model.FhirR4ModelNavigator
import com.google.fhir.fhirpath.model.FhirR5ModelNavigator
import com.google.fhir.fhirpath.parsers.fhirpathLexer
import com.google.fhir.fhirpath.parsers.fhirpathParser
import com.google.fhir.fhirpath.types.FhirPathTypeResolver
import com.google.fhir.fhirpath.types.FhirR4BTypeResolver
import com.google.fhir.fhirpath.types.FhirR4TypeResolver
import com.google.fhir.fhirpath.types.FhirR5TypeResolver
import com.google.fhir.model.r4.Resource
import org.antlr.v4.kotlinruntime.BailErrorStrategy
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.Token

class FhirPathEngine
internal constructor(
  private val fhirPathTypeResolver: FhirPathTypeResolver,
  fhirModelNavigator: FhirModelNavigator,
) {
  private val evaluator = FhirPathEvaluator(fhirPathTypeResolver, fhirModelNavigator)

  /**
   * Evaluates a FHIRPath expression against a single FHIR resource.
   *
   * @param expression The FHIRPath string to evaluate (e.g., "Patient.name.given").
   * @param resource The initial FHIR resource to run the expression against.
   * @param variables Environment variables accessible via %name syntax in the expression.
   * @return @return A collection of elements as the result of the evaluation.
   */
  fun evaluateExpression(
    expression: String,
    resource: Resource?,
    variables: Map<String, Any?> = emptyMap(),
  ): Collection<Any> {
    val lexer = fhirpathLexer(CharStreams.fromString(expression))
    val tokenStream = CommonTokenStream(lexer)
    val parser =
      fhirpathParser(tokenStream).apply {
        // Make sure the parser fails for invalid expressions instead of trying to recover
        errorHandler = BailErrorStrategy()
      }

    val parsedExpression = parser.expression()
    // ANTLR attempts to parse the entire expression but does not throw an error when it cannot. In
    // such cases, explicitly check that the entire expression has been consumed to ensure that the
    // expression is valid.
    if (tokenStream.LA(1) != Token.EOF) {
      error(
        "Expression contains extraneous input that could not be parsed: '${tokenStream[parser.currentToken!!.tokenIndex + 1].text}'"
      )
    }

    evaluator.initialize(context = resource, variables = variables)

    // Convert the items in the result collection from FHIR types to FHIRPath types if it has not
    // occurred in FHIRPath evaluation. Without this conversion, `Patient.name.given` would return
    // results of type FHIR.string but `Patient.name.given.select(substring(0))` would return
    // results of type FHIRPath.string. With this conversion, both expressions would return
    // FHIRPath.string. This is necessary because we lazily convert FHIR types to FHIRPath types in
    // the evaluation in order to preserve data elements such as `id` and `extension` in case they
    // are needed.
    val result = evaluator.visit(parsedExpression).map { it.toFhirPathType(fhirPathTypeResolver) }
    return result
  }

  companion object {
    fun forR4(): FhirPathEngine {
      return FhirPathEngine(
        fhirPathTypeResolver = FhirR4TypeResolver,
        fhirModelNavigator = FhirR4ModelNavigator,
      )
    }

    fun forR4B(): FhirPathEngine {
      return FhirPathEngine(
        fhirPathTypeResolver = FhirR4BTypeResolver,
        fhirModelNavigator = FhirR4BModelNavigator,
      )
    }

    fun forR5(): FhirPathEngine {
      return FhirPathEngine(
        fhirPathTypeResolver = FhirR5TypeResolver,
        fhirModelNavigator = FhirR5ModelNavigator,
      )
    }
  }
}
