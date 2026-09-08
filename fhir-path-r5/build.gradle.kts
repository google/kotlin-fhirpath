import dev.ohs.fhir.fhirpath.codegen.model.FhirModelHelperGenerationTask

plugins {
    id("fhir-path-version-conventions")
}

val generateR5Helpers = tasks.register<FhirModelHelperGenerationTask>("generateR5Helpers") {
    description = "Generate FHIR model extensions for R5"
    this.corePackageFiles.from(
        layout.projectDirectory.dir("../third_party/hl7.fhir.r5.core/package")
    )
    this.fhirVersion.set("r5")
    outputDirectory.set(layout.projectDirectory.dir("src/commonMain/kotlin"))
    finalizedBy(rootProject.tasks.named("spotlessApply"))
}


