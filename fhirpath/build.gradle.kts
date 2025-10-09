import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.google.fhir.fhirpath.codegen.r4.R4HelperGenerationTask
import com.google.fhir.fhirpath.codegen.ucum.UcumHelperGenerationTask
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.antlr.kotlin)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotest)
    `maven-publish`
}

group = "com.google.fhir"
version = "1.0.0-alpha01"

// Run `./gradlew generateR4Helpers` to generate helper functions for R4 in `fhirpath/build/generated`
val generateR4Helpers = tasks.register<R4HelperGenerationTask>("generateR4Helpers") {
    description = "Generate FHIR model extensions for R4"
    this.corePackageFiles.from(
        File(project.rootDir, "third_party/hl7.fhir.r4.core/package").listFiles()
    )
    this.modelPackageName.set("com.google.fhir.model.r4")
    this.fhirPathPackageName.set("com.google.fhir.fhirpath")
    outputDirectory.set(layout.buildDirectory.dir("generated/kotlin"))
}

// Run `./gradlew generateUcumHelpers` to generate helper functions for UCUM in `fhirpath/build/generated`
val generateUcumHelpers = tasks.register<UcumHelperGenerationTask>("generateUcumHelpers") {
    description = "Generate FHIR model extensions for R4"
    this.ucumFile.set(
        File(project.rootDir, "third_party/ucum/ucum-essence.xml")
    )
    this.packageName.set("com.google.fhir.fhirpath.ucum")
    outputDirectory.set(layout.buildDirectory.dir("generated/kotlin"))
}

// Run `./gradlew generateKotlinGrammarSource` to generate parser in `fhirpath/build/generatedAntlr`
val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")
    source = fileTree(rootProject.file("third_party/fhirpath-2.0.0")) {
        include("**/*.g4")
    }
    packageName = "com.google.fhir.fhirpath.parsers"
    arguments = listOf("-visitor")  // Generate visitors alongside listeners

    val outDir = "generated/kotlin/${packageName!!.replace(".", "/")}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
}


tasks.withType<Test>().configureEach {
    // Provide root directory reference for test code to access third_party
    systemProperty("projectRootDir", project.rootDir.absolutePath)
}

kotlin {
    jvmToolchain(21)

    jvm()
    @OptIn(ExperimentalWasmDsl::class) wasmJs {
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
    @OptIn(ExperimentalWasmDsl::class) wasmWasi {
        nodejs()
        binaries.library()
    }
    js {
        browser()
        binaries.library()
    }
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "KotlinFhirPath"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin {
                srcDir(generateR4Helpers)
                srcDir(generateUcumHelpers)
                srcDir(generateKotlinGrammarSource)
            }
            dependencies {
                api(libs.kotlin.fhir)
                implementation(libs.antlr.kotlin.runtime)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.framework.datatest)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.xmlutil.serialization)
            implementation(libs.xmlutil.core)
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

android {
    namespace = "com.google.fhir.fhirpath"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateR4Helpers)
    dependsOn(generateUcumHelpers)
    dependsOn(generateKotlinGrammarSource)
}

// publishing prep
val localRepo: Directory = project.layout.buildDirectory.get().dir("repo")

publishing {
    repositories {
        maven {
            url = localRepo.asFile.toURI()
        }
    }
    publications {
        withType<MavenPublication> {
            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}
val deleteRepoTask = tasks.register<Delete>("deleteLocalRepo") {
    description =
        "Deletes the local repository to get rid of stale artifacts before local publishing"
    this.delete(localRepo)
}
tasks.named("publishAllPublicationsToMavenRepository").configure {
    dependsOn(deleteRepoTask)
}
tasks.register("zipRepo", Zip::class) {
    description = "Create a zip of the maven repository"
    this.destinationDirectory.set(project.layout.buildDirectory.dir("repoZip"))
    archiveBaseName.set("kotlin-fhirpath")

    // Hint to gradle that the repo files are produced by the publish task. This establishes a
    // dependency from the zipRepo task to the publish task.
    this.from(tasks.named("publish").map { _ ->
        localRepo
    })
}
