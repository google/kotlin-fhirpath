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

package dev.ohs.fhir.fhirpath.functions

import dev.ohs.fhir.fhirpath.toFhirPathType
import dev.ohs.fhir.fhirpath.types.FhirPathTypeResolver
import kotlin.math.min

/**
 * Makes the dot match all characters, including line breaks.
 *
 * See https://www.regular-expressions.info/modifiers.html.
 */
const val SINGLE_LINE_MODE_REGEX_PREFIX = "(?s)"

/** See [specification](https://hl7.org/fhirpath/STU3/en/#indexofsubstring--string--integer). */
internal fun Collection<Any>.indexOf(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "indexOf() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val substring = params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.indexOf(substring))
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#substringstart--integer--length--integer--string).
 */
internal fun Collection<Any>.substring(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "substring() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val start = params.firstOrNull() as? Int ?: return emptyList()
  val length = params.getOrNull(1)?.let { it as Int }

  if (start >= input.length || start < 0) {
    return emptyList()
  }

  if (length != null) {
    val endIndex = min(start + length, input.length)
    return listOf(input.substring(start, endIndex))
  }

  return listOf(input.substring(start))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#startswithprefix--string--boolean). */
internal fun Collection<Any>.startsWith(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "startsWith() cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()
  val input =
    item.unwrapString(fhirPathTypeResolver)
      ?: error("startsWith() cannot be called on non-string item: $item")
  val prefix =
    params.single().unwrapString(fhirPathTypeResolver)
      ?: error("startsWith() prefix must be a string")
  return listOf(input.startsWith(prefix))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#endswithsuffix--string--boolean). */
internal fun Collection<Any>.endsWith(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "endsWith() cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()
  val input =
    item.unwrapString(fhirPathTypeResolver)
      ?: error("endsWith() cannot be called on non-string item: $item")
  val suffix =
    params.single().unwrapString(fhirPathTypeResolver)
      ?: error("endsWith() suffix must be a string")
  return listOf(input.endsWith(suffix))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#containssubstring--string--boolean). */
internal fun Collection<Any>.strContains(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "contains() cannot be called on a collection with more than 1 item" }
  val item = singleOrNull() ?: return emptyList()
  val input =
    item.unwrapString(fhirPathTypeResolver)
      ?: error("contains() cannot be called on non-string item: $item")
  val substring =
    params.single().unwrapString(fhirPathTypeResolver)
      ?: error("contains() substring must be a string")
  return listOf(input.contains(substring))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#upper--string). */
internal fun Collection<Any>.upper(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  check(size <= 1) { "upper() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.uppercase())
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#lower--string). */
internal fun Collection<Any>.lower(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  check(size <= 1) { "lower() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.lowercase())
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#replacepattern--string-substitution--string--string).
 */
internal fun Collection<Any>.replace(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "replace() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val pattern = params.getOrNull(0)?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val substitution = params.getOrNull(1)?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.replace(pattern, substitution))
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#matchesregex--string-flags--string--boolean).
 */
internal fun Collection<Any>.matches(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "matches() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val regexText = params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(regexText.toSingleLineModeRegex().containsMatchIn(input))
}

/**
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#matchesfullregex--string--boolean).
 */
internal fun Collection<Any>.matchesFull(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "matches() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val regex = params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.matches(regex.toSingleLineModeRegex()))
}

/**
 * See
 * [specification](https://hl7.org/fhirpath/STU3/en/#replacematchesregex--string-substitution-string-flags--string--string).
 */
internal fun Collection<Any>.replaceMatches(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "replaceMatches() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()

  if (params.size < 2) {
    return emptyList()
  }
  val regex = params[0].unwrapString(fhirPathTypeResolver)!!
  val substitution = params[1].unwrapString(fhirPathTypeResolver)!!

  if (regex.isEmpty()) {
    return this
  }
  return listOf(input.replace(regex.toSingleLineModeRegex(), substitution))
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#length--integer). */
internal fun Collection<Any>.length(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  check(size <= 1) { "length() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.length)
}

/** See [specification](https://hl7.org/fhirpath/STU3/en/#tochars--collection). */
internal fun Collection<Any>.toChars(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  check(size <= 1) { "toChars() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return input.toCharArray().map { it.toString() }
}

// Standard for Trial Use (STU)

/**
 * Encodes the single string item in the input collection using the specified format. Supported
 * formats: `'base64'`, `'urlbase64'`, `'hex'`, and `'url'`.
 *
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#encodeformat--string--string).
 */
@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
internal fun Collection<Any>.encode(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<String> {
  check(size <= 1) { "encode() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val format = params.firstOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()

  val result =
    try {
      when (format.lowercase()) {
        "base64" -> kotlin.io.encoding.Base64.encode(input.encodeToByteArray())
        "urlbase64" -> kotlin.io.encoding.Base64.UrlSafe.encode(input.encodeToByteArray())
        "hex" -> hexEncode(input)
        "url" -> urlEncode(input)
        else -> return emptyList()
      }
    } catch (_: Exception) {
      return emptyList()
    }

  return listOf(result)
}

/**
 * Decodes the single string item in the input collection encoded in the specified format. Supported
 * formats: `'base64'`, `'urlbase64'`, `'hex'`, and `'url'`.
 *
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#decodeformat--string--string).
 */
@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
internal fun Collection<Any>.decode(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<String> {
  check(size <= 1) { "decode() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val format = params.firstOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()

  val result =
    try {
      when (format.lowercase()) {
        "base64" -> kotlin.io.encoding.Base64.decode(input).decodeToString()
        "urlbase64" -> kotlin.io.encoding.Base64.UrlSafe.decode(input).decodeToString()
        "hex" -> hexDecode(input)
        "url" -> urlDecode(input)
        else -> return emptyList()
      }
    } catch (_: Exception) {
      return emptyList()
    }

  return listOf(result)
}

/** See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#trim--string). */
internal fun Collection<Any>.trim(fhirPathTypeResolver: FhirPathTypeResolver): Collection<Any> {
  check(size <= 1) { "trim() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return listOf(input.trim())
}

/**
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#splitseparator-string--collection).
 */
internal fun Collection<Any>.split(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  check(size <= 1) { "split() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  return input.split(
    params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  )
}

/** See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#joinseparator-string--string). */
internal fun Collection<Any>.join(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<Any> {
  if (isEmpty()) return emptyList()
  return listOf(
    joinToString(params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: "") {
      it.unwrapString(fhirPathTypeResolver)!!
    }
  )
}

private fun Any.unwrapString(fhirPathTypeResolver: FhirPathTypeResolver): String? {
  return when (this) {
    is String -> this
    else -> toFhirPathType(fhirPathTypeResolver) as? String
  }
}

private fun String.toSingleLineModeRegex(): Regex = "$SINGLE_LINE_MODE_REGEX_PREFIX$this".toRegex()

/** Converts a UTF-8 string to a lowercase hexadecimal representation (2 hex digits per byte). */
private fun hexEncode(input: String): String =
  input.encodeToByteArray().joinToString("") { byte ->
    // Mask signed Kotlin Byte to unsigned 0..255 integer, convert to base-16 string,
    // and left-pad with '0' to ensure every byte produces 2 hex digits.
    (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
  }

/** Decodes a hexadecimal string (case-insensitive) back to a UTF-8 string. */
private fun hexDecode(hex: String): String {
  require(hex.length % 2 == 0) { "Hexadecimal string must have an even length" }
  val bytes = ByteArray(hex.length / 2) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
  return bytes.decodeToString()
}

/** Encodes non-unreserved characters in a UTF-8 string using URI percent-encoding (%XX). */
private fun urlEncode(input: String): String {
  val sb = StringBuilder()
  for (byte in input.encodeToByteArray()) {
    val unsignedByte = byte.toInt() and 0xFF
    val char = unsignedByte.toChar()
    if (isUnreservedUrlChar(char)) {
      sb.append(char)
    } else {
      sb.append('%')
      sb.append(unsignedByte.toString(16).uppercase().padStart(2, '0'))
    }
  }
  return sb.toString()
}

/** Decodes URI percent-encoded (%XX) sequences back to a UTF-8 string. */
private fun urlDecode(input: String): String {
  val bytes = mutableListOf<Byte>()
  var i = 0
  while (i < input.length) {
    when (val c = input[i]) {
      '%' -> {
        require(i + 2 < input.length)
        val hexByte = input.substring(i + 1, i + 3).toInt(16).toByte()
        bytes.add(hexByte)
        i += 3
      }
      '+' -> {
        bytes.add(' '.code.toByte())
        i++
      }
      else -> {
        bytes.add(c.code.toByte())
        i++
      }
    }
  }
  return bytes.toByteArray().decodeToString()
}

/**
 * Checks whether a character is an unreserved URI character per RFC 3986 Section 2.3
 * (alphanumerics, hyphen, underscore, period, and tilde). Unreserved characters do not require
 * percent-encoding.
 */
private fun isUnreservedUrlChar(ch: Char): Boolean =
  ch in 'a'..'z' ||
    ch in 'A'..'Z' ||
    ch in '0'..'9' ||
    ch == '-' ||
    ch == '_' ||
    ch == '.' ||
    ch == '~'

/**
 * Escapes the single string item in the input collection for the specified target. Supported
 * targets: `'html'` and `'json'`.
 *
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#escapetarget--string--string).
 */
internal fun Collection<Any>.escape(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<String> {
  check(size <= 1) { "escape() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val target = params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()

  return when (target) {
    "html" -> listOf(htmlEscape(input))
    "json" -> listOf(jsonEscape(input))
    else -> emptyList()
  }
}

/**
 * Unescapes the single string item in the input collection for the specified target. Supported
 * targets: `'html'` and `'json'`. Malformed input, such as a truncated `\uXXXX` escape or an entity
 * with an invalid code point, returns empty rather than throwing.
 *
 * See [specification](https://build.fhir.org/ig/HL7/FHIRPath/#unescapetarget--string--string).
 */
internal fun Collection<Any>.unescape(
  params: List<Any>,
  fhirPathTypeResolver: FhirPathTypeResolver,
): Collection<String> {
  check(size <= 1) { "unescape() cannot be called on a collection with more than 1 item" }
  val input = singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()
  val target = params.singleOrNull()?.unwrapString(fhirPathTypeResolver) ?: return emptyList()

  return try {
    when (target) {
      "html" -> listOf(htmlUnescape(input))
      "json" -> listOf(jsonUnescape(input))
      else -> emptyList()
    }
  } catch (_: IllegalArgumentException) {
    emptyList()
  }
}

/**
 * Escapes the HTML special characters `&`, `<`, `>`, `"` and `'` as named or numeric entities, and
 * every character above 127 as a numeric entity. A surrogate pair is escaped as a single entity for
 * the full code point.
 *
 * These five are the only characters with markup meaning in HTML, matching the XML predefined
 * entities (https://www.w3.org/TR/xml/#sec-predefined-ent) and OWASP's HTML encoding rule
 * (https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html).
 */
private fun htmlEscape(input: String): String = buildString {
  var i = 0
  while (i < input.length) {
    val char = input[i]
    val next = input.getOrNull(i + 1)
    when {
      char == '&' -> append("&amp;")
      char == '<' -> append("&lt;")
      char == '>' -> append("&gt;")
      char == '"' -> append("&quot;")
      // Numeric because HTML4 never defined `&apos;`.
      char == '\'' -> append("&#39;")
      char.isHighSurrogate() && next != null && next.isLowSurrogate() -> {
        append("&#").append(surrogatePairToCodePoint(char, next)).append(';')
        i += 2
        continue
      }
      char.code > 127 -> append("&#").append(char.code).append(';')
      else -> append(char)
    }
    i++
  }
}

/**
 * Decodes HTML character entities, both numeric (`&#65;`, `&#x41;`) and named. Only the five
 * predefined names (`amp`, `lt`, `gt`, `quot`, `apos`) are decoded, which covers everything
 * [htmlEscape] can produce. HTML defines many more names (`&nbsp;`, `&eacute;` and so on, see
 * https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references); those are left
 * in the string unchanged.
 */
private fun htmlUnescape(input: String): String = buildString {
  var i = 0
  while (i < input.length) {
    val char = input[i]
    if (char == '&') {
      // The longest decodable entity is `&#1114111;`, the last Unicode code point
      // (https://www.unicode.org/glossary/#code_point), whose `;` is 9 characters from the `&`.
      val end = input.indexOf(';', i)
      val decoded =
        if (end in (i + 1)..(i + 9)) decodeHtmlEntity(input.substring(i + 1, end)) else null
      if (decoded != null) {
        append(decoded)
        i = end + 1
        continue
      }
    }
    append(char)
    i++
  }
}

/** Decodes the text between `&` and `;`, or returns null if it is not a recognized entity. */
private fun decodeHtmlEntity(entity: String): String? =
  when {
    entity == "amp" -> "&"
    entity == "lt" -> "<"
    entity == "gt" -> ">"
    entity == "quot" -> "\""
    entity == "apos" -> "'"
    entity.startsWith("#x") || entity.startsWith("#X") ->
      codePointToString(entity.drop(2).toInt(16))
    entity.startsWith("#") -> codePointToString(entity.drop(1).toInt())
    else -> null
  }

/**
 * Escapes `\`, `"` and control characters as in a JSON string literal (RFC 8259,
 * https://datatracker.ietf.org/doc/html/rfc8259#section-7).
 */
private fun jsonEscape(input: String): String = buildString {
  for (char in input) {
    when (char) {
      '\\' -> append("\\\\")
      '"' -> append("\\\"")
      '\n' -> append("\\n")
      '\r' -> append("\\r")
      '\t' -> append("\\t")
      '\b' -> append("\\b")
      '\u000C' -> append("\\f")
      // The remaining control characters have no dedicated escape.
      in '\u0000'..'\u001F' -> append("\\u").append(char.code.toString(16).padStart(4, '0'))
      else -> append(char)
    }
  }
}

/** Decodes JSON string literal escape sequences, including `\uXXXX`. */
private fun jsonUnescape(input: String): String = buildString {
  var i = 0
  while (i < input.length) {
    val char = input[i]
    val next = input.getOrNull(i + 1)
    if (char == '\\' && next != null) {
      when (next) {
        '"' -> append('"')
        '\\' -> append('\\')
        '/' -> append('/')
        'n' -> append('\n')
        'r' -> append('\r')
        't' -> append('\t')
        'b' -> append('\b')
        'f' -> append('\u000C')
        'u' -> {
          // The 6 character `\uXXXX` escape carries one UTF-16 code unit, so appending each
          // unit as a Char lets a surrogate pair, arriving as two escapes, combine naturally.
          // The bound is checked up front because on Kotlin/JS substring clamps instead of
          // throwing, and a truncated escape like `a\u12` would silently decode garbage.
          val escapeEnd = i + 6
          require(escapeEnd <= input.length) { "Truncated unicode escape" }
          append(Char(input.substring(i + 2, escapeEnd).toInt(16)))
          i = escapeEnd
          continue
        }
        // An escape JSON does not define, like FHIRPath's `\'`, keeps the escaped character.
        else -> append(next)
      }
      i += 2
    } else {
      append(char)
      i++
    }
  }
}

/**
 * Reverses the encoding in [codePointToString]: strip the 0xD800 and 0xDC00 markers, rejoin the two
 * 10 bit halves, add back the 0x10000.
 */
private fun surrogatePairToCodePoint(high: Char, low: Char): Int =
  0x10000 + ((high.code - 0xD800) shl 10) + (low.code - 0xDC00)

/**
 * Converts a Unicode code point to a string: a single `Char` for code points up to `0xFFFF`, and a
 * surrogate pair for those above, which do not fit in one 16 bit `Char`.
 */
private fun codePointToString(codePoint: Int): String {
  // No character exists above 0x10FFFF, and 0xD800..0xDFFF is set aside for the two char
  // encoding below, so neither is a real character.
  require(codePoint in 0..0x10FFFF && codePoint !in 0xD800..0xDFFF) {
    "Invalid code point: $codePoint"
  }
  return if (codePoint <= 0xFFFF) {
    Char(codePoint).toString()
  } else {
    // Too big for one 16 bit char, so it is split in two. Subtracting 0x10000 makes it fit in
    // 20 bits, and the 0xD800 and 0xDC00 markers show which char is which half.
    val offset = codePoint - 0x10000
    charArrayOf(Char(0xD800 + (offset shr 10)), Char(0xDC00 + (offset and 0x3FF))).concatToString()
  }
}
