import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij.platform") version "2.1.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.2.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform("io.netty:netty-bom:4.1.115.Final"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.17.2"))
    implementation(platform("io.projectreactor:reactor-bom:2020.0.47"))
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("javax.mail:javax.mail-api:1.6.2")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("io.projectreactor.netty:reactor-netty")
    implementation("org.apache.kafka:kafka-clients:3.8.1") {
        exclude(group = "com.github.luben", module = "zstd-jni")
        exclude(group = "org.lz4", module = "lz4-java")
    }
    implementation("io.projectreactor.kafka:reactor-kafka")
    implementation("io.projectreactor.rabbitmq:reactor-rabbitmq")
    implementation("com.rabbitmq:amqp-client:5.22.0")
    implementation("io.nats:jnats:2.20.4")
    implementation("org.msgpack:jackson-dataformat-msgpack:0.9.8")
    implementation("com.github.mwiede:jsch:0.2.21")
    implementation("com.spotify:folsom:1.21.0")
    implementation("org.zeromq:jeromq:0.6.0")
    implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    implementation("redis.clients:jedis:5.2.0")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation("org.apache.rocketmq:rocketmq-client:4.9.3") {
        exclude(group = "io.netty", module = "netty-all")
        exclude(group = "com.github.luben", module = "zstd-jni")
    }
    implementation("org.apache.bookkeeper:bookkeeper-common-allocator:4.17.1")
    implementation("org.apache.bookkeeper:circe-checksum:4.17.1@jar")
    implementation("org.apache.pulsar:pulsar-client-original:3.3.2") {
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
    implementation("com.aliyun:aliyun-java-sdk-core:4.6.4")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5")
    implementation("org.json:json:20240303")
    implementation("com.aliyun.mns:aliyun-sdk-mns:1.1.9.2") {
        exclude(group = "com.aliyun", module = "aliyun-java-sdk-ecs")
    }
    implementation("com.aliyun:eventbridge-client:1.3.14")
    implementation("software.amazon.awssdk:aws-core:2.28.10")
    implementation("software.amazon.awssdk:sns:2.28.10")
    implementation("software.amazon.awssdk:sqs:2.28.10")
    implementation("software.amazon.awssdk:eventbridge:2.28.10")
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        //local("/Users/linux_china/Applications/IntelliJ IDEA Ultimate.app")

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

configurations.implementation {
    exclude(group = "org.jetbrains", module = "annotations")
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "io.netty", module = "netty-resolver-dns-native-macos")
    exclude(group = "io.netty", module = "netty-transport-native-epoll")
    exclude(group = "io.projectreactor.netty.incubator", module = "reactor-netty-incubator-quic")
    exclude(group = "io.projectreactor.netty", module = "reactor-netty-http-brave")
    exclude(group = "com.google.guava", module = "guava")
    exclude(group = "com.github.luben", module = "zstd-jni")
    exclude(group = "org.lz4", module = "lz4-java")
    exclude(group = "org.xerial.snappy", module = "snappy-java")
    exclude(group = "javax.activation", module = "activation")
    exclude(group = "com.fasterxml.jackson.core", module = "jackson-annotations")
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
    exclude(group = "javax.xml.bind", module = "jaxb-api")
    exclude(group = "com.sun.xml.bind", module = "jaxb-core")
    exclude(group = "com.sun.xml.bind", module = "jaxb-impl")
    exclude(group = "org.glassfish.jaxb", module = "jaxb-runtime")
    exclude(group = "org.apache.httpcomponents", module = "httpcore")
    exclude(group = "org.apache.httpcomponents", module = "httpclient")
    exclude(group = "org.apache.bookkeeper", module = "cpu-affinity")
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion")
            .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}


tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }
}
