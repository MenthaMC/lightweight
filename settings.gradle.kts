plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "lightweight"

include("lightweight-core", "lightweight-lib", "lightweight-userdev")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
