import com.android.build.api.dsl.androidLibrary
import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import dev.ohs.fhir.fhirpath.codegen.ucum.UcumHelperGenerationTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.antlr.kotlin)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.vanniktech.maven.publish")
}

val mavenGroupId: String by project
val mavenVersion: String by project
val androidNamespace: String by project

// Run `./gradlew generateUcumHelpers` to generate helper functions for UCUM in `fhirpath/src/commonMain/kotlin`
val generateUcumHelpers = tasks.register<UcumHelperGenerationTask>("generateUcumHelpers") {
    description = "Generate FHIR model extensions for R4"
    this.ucumFile.set(
        File(project.rootDir, "third_party/ucum/ucum-essence.xml")
    )
    this.packageName.set("dev.ohs.fhir.fhirpath.ucum")
    outputDirectory.set(layout.projectDirectory.dir("src/commonMain/kotlin"))
    finalizedBy(rootProject.tasks.named("spotlessApply"))
}

// Run `./gradlew generateKotlinGrammarSource` to generate parser in `fhirpath/src/commonMain/kotlin`
val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    source = fileTree(rootProject.file("third_party/fhirpath-2.0.0")) {
        include("**/*.g4")
    }
    packageName = "dev.ohs.fhir.fhirpath.parsers"
    arguments = listOf("-visitor")  // Generate visitors alongside listeners

    val outDir = "src/commonMain/kotlin/${packageName!!.replace(".", "/")}"
    outputDirectory = layout.projectDirectory.dir(outDir).asFile
    finalizedBy(rootProject.tasks.named("spotlessApply"))
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    jvmToolchain(21)

    jvm()
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }
    wasmWasi {
        nodejs()
        binaries.library()
    }
    js {
        browser()
        binaries.library()
    }
    androidLibrary {
        namespace = "$androidNamespace.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    listOf(
        iosSimulatorArm64(),
        iosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "KotlinFhirPathCore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.bignum)
                api(libs.kotlinx.datetime)
                implementation(libs.antlr.kotlin.runtime)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}



mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(mavenGroupId, "fhir-path-core", mavenVersion)

    pom {
        name = "Kotlin FHIRPath Core"
        description = "Core engine for Kotlin FHIRPath"
        inceptionYear = "2025"
        url = "https://github.com/ohs-foundation/kotlin-fhirpath"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "ohs-foundation"
                name = "Open Heath Stack Foundation"
                url = "https://ohs.dev/"
            }
        }
        scm {
            url = "https://github.com/ohs-foundation/kotlin-fhirpath/"
            connection = "scm:git:git://github.com/ohs-foundation/kotlin-fhirpath.git"
            developerConnection = "scm:git:ssh://git@github.com/ohs-foundation/kotlin-fhirpath.git"
        }
    }
}
