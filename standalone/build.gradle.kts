plugins {
    id("java")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":protocol"))

    implementation(libs.netty.codec)
    implementation(libs.netty.transport)
    implementation(libs.netty.transport.epoll)
    implementation(variantOf(libs.netty.transport.epoll) { classifier("linux-x86_64") })
    implementation(variantOf(libs.netty.transport.epoll) { classifier("linux-aarch_64") })
    implementation(libs.log4j.core)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.bauhd.multi.standalone.Standalone"
            attributes["Multi-Release"] = true
            attributes["Implementation-Version"] = "${project.version}"
        }
    }

    shadowJar {
        archiveFileName.set("multi-velocity-standalone.jar")
    }
}