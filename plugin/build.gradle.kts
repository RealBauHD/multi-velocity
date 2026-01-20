plugins {
    id("java")
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":protocol"))

    compileOnly(libs.netty.codec)
    compileOnly(libs.netty.transport)
    compileOnly(libs.netty.transport.epoll)
    compileOnly(libs.velocity)

    annotationProcessor(libs.velocity)
}