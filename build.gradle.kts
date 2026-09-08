plugins {
    alias(libs.plugins.spotless)
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    ratchetFrom = "origin/main"
    kotlin {
        target("**/*.kt")
        targetExclude(
            "fhir-path-core/src/commonMain/kotlin/dev/ohs/fhir/fhirpath/ucum/**/*.kt",
            "fhir-path-core/src/commonMain/kotlin/dev/ohs/fhir/fhirpath/parsers/**/*.kt",
            "fhir-path-r4/src/commonMain/kotlin/**/*.kt",
            "fhir-path-r4b/src/commonMain/kotlin/**/*.kt",
            "fhir-path-r5/src/commonMain/kotlin/**/*.kt",
        )
        ktfmt().googleStyle()
        licenseHeaderFile(
            "license-header.txt",
        )
    }

    val currentYear = java.time.LocalDate.now().year.toString()
    val licenseHeaderTemplate = file("license-header.txt").readText()
    val generatedLicenseHeader = licenseHeaderTemplate.replace("\$YEAR", currentYear)
    format("generatedKotlin", com.diffplug.gradle.spotless.KotlinExtension::class.java) {
        target(
            "fhir-path-core/src/commonMain/kotlin/dev/ohs/fhir/fhirpath/ucum/**/*.kt",
            "fhir-path-core/src/commonMain/kotlin/dev/ohs/fhir/fhirpath/parsers/**/*.kt",
            "fhir-path-r4/src/commonMain/kotlin/**/*.kt",
            "fhir-path-r4b/src/commonMain/kotlin/**/*.kt",
            "fhir-path-r5/src/commonMain/kotlin/**/*.kt",
        )
        ktfmt().googleStyle()
        licenseHeader(generatedLicenseHeader)
    }
    flexmark {
        target("**/*.md")
        flexmark()
    }
}

val codegenTasks = listOf(
    ":fhir-path-core:generateUcumHelpers",
    ":fhir-path-core:generateKotlinGrammarSource",
    ":fhir-path-r4:generateR4Helpers",
    ":fhir-path-r4b:generateR4BHelpers",
    ":fhir-path-r5:generateR5Helpers",
)

tasks.named("spotlessKotlin") {
    dependsOn(codegenTasks)
}

tasks.named("spotlessGeneratedKotlin") {
    dependsOn(codegenTasks)
}

tasks.named("spotlessFlexmark") {
    dependsOn(codegenTasks)
}

tasks.register("generateSources") {
    group = "generation"
    description = "Regenerate all ANTLR grammar, UCUM helpers, and FHIR model helpers across all modules."
    dependsOn(codegenTasks)
}

tasks.register("verifyCodegen") {
    description = "Verify committed generated code matches the codegen output."
    group = "verification"
    dependsOn(codegenTasks, "spotlessGeneratedKotlinApply")

    val rootDir = project.rootDir

    doLast {
        val generatedDirs = listOf(
            "fhir-path-core/src/commonMain/kotlin/dev/ohs/fhir/fhirpath/ucum",
            "fhir-path-core/src/commonMain/kotlin/dev/ohs/fhir/fhirpath/parsers",
            "fhir-path-r4/src/commonMain/kotlin",
            "fhir-path-r4b/src/commonMain/kotlin",
            "fhir-path-r5/src/commonMain/kotlin",
        )

        val diffProcess = ProcessBuilder(
            listOf("git", "diff", "--exit-code", "--") + generatedDirs
        ).directory(rootDir).redirectErrorStream(true).start()

        val diffOutput = diffProcess.inputStream.bufferedReader().readText()
        val diffExitCode = diffProcess.waitFor()

        if (diffExitCode != 0) {
            throw GradleException(
                buildString {
                    appendLine("Codegen verification FAILED – the committed generated sources differ from the codegen output.")
                    appendLine()
                    appendLine("Changed files:")
                    appendLine(diffOutput)
                    appendLine()
                    appendLine("Run `./gradlew generateSources spotlessApply` locally and commit the result.")
                }
            )
        }

        val untrackedProcess = ProcessBuilder(
            listOf("git", "ls-files", "--others", "--exclude-standard", "--") + generatedDirs
        ).directory(rootDir).redirectErrorStream(true).start()

        val untrackedFiles = untrackedProcess.inputStream.bufferedReader().readText().trim()
        untrackedProcess.waitFor()

        if (untrackedFiles.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Codegen verification FAILED – codegen produced new files not present in the repository.")
                    appendLine()
                    appendLine("Untracked files:")
                    appendLine(untrackedFiles)
                    appendLine()
                    appendLine("Run `./gradlew generateSources spotlessApply` locally and commit the result.")
                }
            )
        }

        logger.lifecycle("✅ Codegen verification passed – all generated sources are up to date.")
    }
}
