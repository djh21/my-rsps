plugins {
    id("base-conventions")
}

dependencies {
    implementation(projects.engine.map)
    implementation(libs.or2.tools)
    implementation(libs.kotlin.inline.logger)
}
