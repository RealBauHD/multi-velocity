plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":protocol"))

    implementation(libs.bundles.netty)
    implementation(variantOf(libs.netty.transport.epoll) { classifier("linux-x86_64") })
    implementation(variantOf(libs.netty.transport.epoll) { classifier("linux-aarch_64") })
    implementation(libs.log4j.core)
    implementation(libs.fastutil)
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

        exclude(
            "it/unimi/dsi/fastutil/*Big*",
            "it/unimi/dsi/fastutil/*Pair*",
            "it/unimi/dsi/fastutil/*Queue*",
            "it/unimi/dsi/fastutil/booleans/**",
            "it/unimi/dsi/fastutil/bytes/**",
            "it/unimi/dsi/fastutil/chars/**",
            "it/unimi/dsi/fastutil/doubles/**",
            "it/unimi/dsi/fastutil/floats/**",
            "it/unimi/dsi/fastutil/io/**",
            "it/unimi/dsi/fastutil/shorts/**",
            "it/unimi/dsi/fastutil/ints/*Byte*",
            "it/unimi/dsi/fastutil/ints/*Char*",
            "it/unimi/dsi/fastutil/ints/*Double*",
            "it/unimi/dsi/fastutil/ints/*Long*",
            "it/unimi/dsi/fastutil/ints/*Float*",
            "it/unimi/dsi/fastutil/ints/*Big*",
            "it/unimi/dsi/fastutil/ints/*Tree*",
            "it/unimi/dsi/fastutil/ints/*Pair*",
            "it/unimi/dsi/fastutil/ints/*Immutable*",
            "it/unimi/dsi/fastutil/ints/*Array*",
            "it/unimi/dsi/fastutil/ints/*Linked*",
            "it/unimi/dsi/fastutil/longs/**",
            "it/unimi/dsi/fastutil/objects/*Byte*",
            "it/unimi/dsi/fastutil/objects/*Char*",
            "it/unimi/dsi/fastutil/objects/*Double*",
            "it/unimi/dsi/fastutil/objects/*Long*",
            "it/unimi/dsi/fastutil/objects/*Big*",
            "it/unimi/dsi/fastutil/objects/*Tree*",
            "it/unimi/dsi/fastutil/objects/*Pair*",
            "it/unimi/dsi/fastutil/objects/*2Object*",
            "it/unimi/dsi/fastutil/objects/*Float*",
            "it/unimi/dsi/fastutil/objects/*Boolean*",
            "it/unimi/dsi/fastutil/objects/*Immutable*",
            "it/unimi/dsi/fastutil/objects/*Array*",
            "it/unimi/dsi/fastutil/objects/*Linked*"
        )
    }
}