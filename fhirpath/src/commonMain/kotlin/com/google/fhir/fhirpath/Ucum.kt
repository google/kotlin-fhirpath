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

package com.google.fhir.fhirpath

import com.google.fhir.fhirpath.ucum.BaseUnit
import com.google.fhir.fhirpath.ucum.Prefix
import com.google.fhir.fhirpath.ucum.Unit
import com.google.fhir.model.r4.Code
import com.google.fhir.model.r4.Decimal
import com.google.fhir.model.r4.Quantity
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.math.pow

fun Quantity.toEqualCanonicalized() =
  toEqualUcumDefiniteDuration().stripUcumPrefix().toCanonicalizedUcumUnit()

fun Quantity.toEquivalentCanonicalized() =
  toEquivalentUcumDefiniteDuration().stripUcumPrefix().toCanonicalizedUcumUnit()

/**
 * Converts a FHIRPath calendar duration to the equal UCUM definite unit if there is one. Returns
 * the original quantity, otherwise.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#time-valued-quantities).
 *
 * N.B. The equality relationship is maintained for up to `weeks` following discussions with Bryn
 * Rhodes. This change has not yet been made in the latest version of the specification.
 */
private fun Quantity.toEqualUcumDefiniteDuration(): Quantity {
  val calendarDurationCode = unit?.value ?: return this
  val ucumDefinitionDurationCode =
    when (calendarDurationCode) {
      "week",
      "weeks" -> "'wk'"
      "day",
      "days" -> "'d'"
      "hour",
      "hours" -> "'h'"
      "minute",
      "minutes" -> "'min'"
      "second",
      "seconds" -> "'s'"
      "millisecond",
      "milliseconds" -> "'ms'"
      else -> return this
    }
  return Quantity(value = value, unit = Code(value = ucumDefinitionDurationCode))
}

/**
 * Converts a FHIRPath calendar duration to the equivalent UCUM definite unit if there is one.
 * Returns the original quantity, otherwise.
 *
 * See [specification](https://hl7.org/fhirpath/N1/#time-valued-quantities).
 */
private fun Quantity.toEquivalentUcumDefiniteDuration(): Quantity {
  val calendarDurationCode = unit?.value ?: return this
  val ucumDefinitionDurationCode =
    when (calendarDurationCode) {
      "year",
      "years" -> "'a'"
      "month",
      "months" -> "'mo'"
      "week",
      "weeks" -> "'wk'"
      "day",
      "days" -> "'d'"
      "hour",
      "hours" -> "'h'"
      "minute",
      "minutes" -> "'min'"
      "second",
      "seconds" -> "'s'"
      "millisecond",
      "milliseconds" -> "'ms'"
      else -> return this
    }
  return Quantity(value = value, unit = Code(value = ucumDefinitionDurationCode))
}

private fun Quantity.stripUcumPrefix(): Quantity {
  // TODO: Handle more complex UCUM strings
  val code = unit?.value?.stripSingleQuotes() ?: return this
  for (prefix in Prefix.entries) {
    if (!code.startsWith(prefix.code)) continue
    val codeWithoutPrefix = code.removePrefix(prefix.code)
    if (codeWithoutPrefix in (BaseUnit.entries.map { it.code } + Unit.entries.map { it.code })) {
      return Quantity(
        value = Decimal(value = value!!.value!! * 10.0.pow(prefix.power).toBigDecimal()),
        unit = Code(value = "'$codeWithoutPrefix'"),
      )
    }
  }
  return this
}

private fun Quantity.toCanonicalizedUcumUnit(): Quantity {
  val unitCode = unit?.value?.stripSingleQuotes() ?: return this
  val unit = Unit.fromString(unitCode) ?: return this
  return Quantity(
    value = Decimal(value = value!!.value!! * unit.scalar.toBigDecimal()),
    unit = Code(value = unit.base),
  )
}

private fun String.stripSingleQuotes(): String? {
  return if (startsWith("'") && endsWith("'")) trim('\'') else null
}
