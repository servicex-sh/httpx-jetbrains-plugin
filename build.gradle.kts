import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.4.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

dependencies {
    implementation(platform("io.netty:netty-bom:4.1.75.Final"))
    implementation(platform("io.projectreactor:reactor-bom:2020.0.17"))
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("javax.mail:javax.mail-api:1.6.2")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("io.projectreactor.netty:reactor-netty")
    implementation("org.apache.kafka:kafka-clients:3.1.0") {
        exclude(group = "com.github.luben", module = "zstd-jni")
        exclude(group = "org.lz4", module = "lz4-java")
    }
    implementation("io.projectreactor.kafka:reactor-kafka")
    implementation("io.projectreactor.rabbitmq:reactor-rabbitmq")
    implementation("com.rabbitmq:amqp-client:5.14.2")
    implementation("io.nats:jnats:2.13.2")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("com.spotify:folsom:1.13.1")
    implementation("org.zeromq:jeromq:0.5.2")
    implementation("io.lettuce:lettuce-core:6.1.8.RELEASE")
    implementation("org.apache.rocketmq:rocketmq-client:4.9.3") {
        exclude(group = "io.netty", module = "netty-all")
    }
    implementation("org.apache.bookkeeper:bookkeeper-common-allocator:4.14.4")
    implementation("org.apache.bookkeeper:circe-checksum:4.14.4@jar")
    implementation("org.apache.pulsar:pulsar-client-original:2.9.1") {
        exclude(group = "com.beust", module = "jcommander")
        exclude(group = "org.asynchttpclient", module = "async-http-client")
        exclude(group = "org.apache.avro", module = "avro")
        exclude(group = "org.apache.avro", module = "avro-protobuf")
        exclude(group = "io.netty", module = "netty-tcnative-boringssl-static")
        exclude(group = "javax.ws.rs", module = "javax.ws.rs-api")
        exclude(group = "org.apache.pulsar", module = "bouncy-castle-bc")
        exclude(group = "org.eclipse.jetty", module = "jetty-util")
    }
    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("com.aliyun:aliyun-java-sdk-core:4.6.0")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5")
    implementation("com.aliyun.mns:aliyun-sdk-mns:1.1.9.1") {
        exclude(group = "com.aliyun", module = "aliyun-java-sdk-ecs")
    }
    implementation("com.aliyun:eventbridge-client:1.3.2")
    implementation("software.amazon.awssdk:aws-core:2.17.150")
    implementation("software.amazon.awssdk:sns:2.17.150")
    implementation("software.amazon.awssdk:sqs:2.17.150")
    implementation("software.amazon.awssdk:eventbridge:2.17.150")
}

configurations.implementation {
    exclude(group = "org.jetbrains", module = "annotations")
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "io.netty", module = "netty-resolver-dns-native-macos")
    exclude(group = "io.netty", module = "netty-transport-native-epoll")
    exclude(group = "io.projectreactor.netty.incubator", module = "reactor-netty-incubator-quic")
    exclude(group = "io.projectreactor.netty", module = "reactor-netty-http-brave")
    exclude(group = "com.google.guava", module = "guava")
    exclude(group = "com.google.code.gson", module = "gson")
    exclude(group = "com.github.luben", module = "zstd-jni")
    exclude(group = "org.lz4", module = "lz4-java")
    exclude(group = "org.xerial.snappy", module = "snappy-java")
    exclude(group = "javax.activation", module = "activation")
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    exclude(group = "com.fasterxml.jackson.dataformat", module = "jackson-dataformat-yaml")
    exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-jsonSchema")
    exclude(group = "commons-lang", module = "commons-lang")
    exclude(group = "commons-io", module = "commons-io")
    exclude(group = "commons-codec", module = "commons-codec")
    exclude(group = "commons-logging", module = "commons-logging")
    exclude(group = "commons-collections", module = "commons-collections")
    exclude(group = "commons-beanutils", module = "commons-beanutils")
    exclude(group = "org.apache.commons", module = "commons-lang3")
    exclude(group = "org.ini4j", module = "ini4j")
    exclude(group = "net.jcip", module = "jcip-annotations")
    exclude(group = "org.bouncycastle", module = "bcpkix-jdk15on")
    exclude(group = "org.bouncycastle", module = "bcutil-jdk15on")
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
    exclude(group = "org.bouncycastle", module = "bcprov-ext-jdk15on")
    exclude(group = "org.jacoco", module = "org.jacoco.agent")
    exclude(group = "io.opentracing", module = "opentracing-api")
    exclude(group = "io.opentracing", module = "opentracing-util")
    exclude(group = "javax.xml.bind", module = "jaxb-api")
    exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    exclude(group = "com.sun.xml.bind", module = "jaxb-impl")
    exclude(group = "org.glassfish.jaxb", module = "jaxb-runtime")
    exclude(group = "org.apache.httpcomponents", module = "httpcore")
    exclude(group = "org.apache.httpcomponents", module = "httpclient")
    exclude(group = "org.apache.bookkeeper", module = "cpu-affinity")
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
            kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
