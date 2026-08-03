/*
 * Copyright 2025-2026 Open Health Stack Foundation
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

import dev.ohs.fhir.fhirpath.model.FhirModelNavigator
import dev.ohs.fhir.fhirpath.parsers.fhirpathLexer
import dev.ohs.fhir.fhirpath.parsers.fhirpathParser
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import org.antlr.v4.kotlinruntime.BailErrorStrategy
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.Token

class FhirPathEngine(
  private val fhirPathTypeResolver: FhirPathTypeResolver,
  val fhirModelNavigator: FhirModelNavigator,
  val strictMode: Boolean = false,
) {
  var traces: Map<String, List<TraceEntry>> = emptyMap()
    private set

  // Parse trees are immutable and reused across resources, so cache them by expression string.
  // Unbounded cache, sized by the caller's distinct expressions (a fixed search-parameter set here).
  private val parsedExpressionCache = HashMap<String, fhirpathParser.ExpressionContext>()

  /**
   * Evaluates a FHIRPath expression against a single FHIR resource.
   *
   * @param expression The FHIRPath string to evaluate (e.g., "Patient.name.given").
   * @param base The initial FHIR resource or element to run the expression against.
   * @param variables Environment variables accessible via %name syntax in the expression.
   * @return A collection of elements as the result of the evaluation.
   */
  fun evaluateExpression(
    expression: String,
    base: Any?,
    variables: Map<String, Any?> = emptyMap(),
  ): Collection<Any> {
    val parsedExpression = parsedExpressionCache.getOrPut(expression) { parseExpression(expression) }

    // Create a new evaluator per invocation for thread safety.
    val evaluator =
      FhirPathEvaluator(
        fhirPathTypeResolver = fhirPathTypeResolver,
        fhirModelNavigator = fhirModelNavigator,
        strictMode = strictMode,
        context = base,
        variables = variables,
      )

    // Convert the items in the result collection from FHIR types to FHIRPath types if it has not
    // occurred in FHIRPath evaluation. Without this conversion, `Patient.name.given` would return
    // results of type FHIR.string but `Patient.name.given.select(substring(0))` would return
    // results of type FHIRPath.string. With this conversion, both expressions would return
    // FHIRPath.string. This is necessary because we lazily convert FHIR types to FHIRPath types in
    // the evaluation in order to preserve data elements such as `id` and `extension` in case they
    // are needed.
    val result = evaluator.visit(parsedExpression).map { it.toFhirPathType(fhirPathTypeResolver) }
    traces = evaluator.traces
    return result
  }

  private fun parseExpression(expression: String): fhirpathParser.ExpressionContext {
    val lexer = fhirpathLexer(CharStreams.fromString(expression))
    val tokenStream = CommonTokenStream(lexer)
    val parser =
      fhirpathParser(tokenStream).apply {
        // Make sure the parser fails for invalid expressions instead of trying to recover
        errorHandler = BailErrorStrategy()
      }

    val parsedExpression = parser.expression()
    // ANTLR may not error on incomplete parsing; verify the whole expression was consumed (EOF).
    if (tokenStream.LA(1) != Token.EOF) {
      error(
        "Expression contains extraneous input that could not be parsed: '${tokenStream[parser.currentToken!!.tokenIndex + 1].text}'"
      )
    }
    return parsedExpression
  }

  companion object
}
