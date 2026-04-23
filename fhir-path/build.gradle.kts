import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import dev.ohs.fhir.fhirpath.codegen.model.FhirModelHelperGenerationTask
import dev.ohs.fhir.fhirpath.codegen.ucum.UcumHelperGenerationTask
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.antlr.kotlin)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotest)
    alias(libs.plugins.maven.publish)
}

val fhirVersions = mapOf(
    "r4" to "third_party/hl7.fhir.r4.core/package",
    "r4b" to "third_party/hl7.fhir.r4b.core/package",
    "r5" to "third_party/hl7.fhir.r5.core/package"
)

// Run `./gradlew generate{R4,R4B,R5}Helpers` to generate helper functions in `fhirpath/build/generated`
fhirVersions.forEach { (version, path) ->
    val taskName = "generate${version.uppercase()}Helpers"
    tasks.register<FhirModelHelperGenerationTask>(taskName) {
        description = "Generate FHIR model extensions for ${version.uppercase()}"
        this.corePackageFiles.from(
            File(project.rootDir, path).listFiles()
        )
        this.fhirVersion.set(version)
        outputDirectory.set(layout.buildDirectory.dir("generated/kotlin"))
    }
}

// Run `./gradlew generateUcumHelpers` to generate helper functions for UCUM in `fhirpath/build/generated`
val generateUcumHelpers = tasks.register<UcumHelperGenerationTask>("generateUcumHelpers") {
    description = "Generate FHIR model extensions for R4"
    this.ucumFile.set(
        File(project.rootDir, "third_party/ucum/ucum-essence.xml")
    )
    this.packageName.set("dev.ohs.fhir.fhirpath.ucum")
    outputDirectory.set(layout.buildDirectory.dir("generated/kotlin"))
}

// Run `./gradlew generateKotlinGrammarSource` to generate parser in `fhirpath/build/generatedAntlr`
val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")
    source = fileTree(rootProject.file("third_party/fhirpath-2.0.0")) {
        include("**/*.g4")
    }
    packageName = "dev.ohs.fhir.fhirpath.parsers"
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
        publishLibraryVariants("release")
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
                fhirVersions.keys.forEach { version ->
                    srcDir(tasks.named("generate${version.uppercase()}Helpers"))
                }
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
            
            implementation(libs.kotest.framework.engine)
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
    namespace = project.property("androidNamespace") as String
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
    dependsOn(tasks.withType<FhirModelHelperGenerationTask>())
    dependsOn(generateUcumHelpers)
    dependsOn(generateKotlinGrammarSource)
}

version = "1.0.0-beta01"
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(project.property("mavenGroupId") as String, project.property("mavenArtifactId") as String, version.toString())

    pom {
        name = "Kotlin FHIRPath"
        description = "A Kotlin Multiplatform library for FHIRPath"
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

