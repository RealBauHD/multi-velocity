plugins {
    id("java")
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":protocol"))

    compileOnly(libs.bundles.netty)
    compileOnly(libs.velocity)

    annotationProcessor(libs.velocity)
}