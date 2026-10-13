plugins {
    id("base-conventions")
}

dependencies {
    implementation(projects.engine.map)
    implementation(libs.or2.tools)
    implementation(libs.fastutil)
    implementation(libs.kotlin.inline.logger)
}
