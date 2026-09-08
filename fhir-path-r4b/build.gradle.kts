import dev.ohs.fhir.fhirpath.codegen.model.FhirModelHelperGenerationTask

plugins {
    id("fhir-path-version-conventions")
}

val generateR4BHelpers = tasks.register<FhirModelHelperGenerationTask>("generateR4BHelpers") {
    description = "Generate FHIR model extensions for R4B"
    this.corePackageFiles.from(
        layout.projectDirectory.dir("../third_party/hl7.fhir.r4b.core/package")
    )
    this.fhirVersion.set("r4b")
    outputDirectory.set(layout.projectDirectory.dir("src/commonMain/kotlin"))
    finalizedBy(rootProject.tasks.named("spotlessApply"))
}


