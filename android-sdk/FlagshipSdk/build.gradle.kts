plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    kotlin("kapt")
    id("maven-publish")
    id("signing")
}

val sdkVersion = "0.0.67"

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = mutableMapOf<String, String>()

if (localPropertiesFile.exists()) {
    localPropertiesFile.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val index = trimmed.indexOf('=')
            if (index > 0) {
                val key = trimmed.substring(0, index).trim()
                val value = trimmed.substring(index + 1).trim()
                localProperties[key] = value
            }
        }
    }
}

fun getLocalProperty(key: String): String? = localProperties[key]

val signingKeyId = getLocalProperty("signing.keyId") ?: ""
val signingKey = getLocalProperty("signing.key") ?: ""
val signingPassword = getLocalProperty("signing.password") ?: ""

android {
    namespace = "com.flagship.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    api(libs.open.feature.ktx)

    // Semver version comparison
    implementation(libs.java.semver)

    // Use JUnit Platform BOM to ensure all JUnit 5 dependencies are aligned
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.14.6")
    
    // Exclude JUnit 4 from transitive dependencies
    configurations.testImplementation.get().exclude(group = "junit", module = "junit")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("bar") {
            groupId = "com.dream11"
            artifactId = "flagship-sdk"
            version = sdkVersion
            artifact("${layout.buildDirectory.get()}/outputs/aar/FlagshipSdk-release.aar")

            pom {
                name.set("flagship-sdk")
                description.set("Flagship Android SDK")
                url.set("https://github.com/ds-horizon/flagship-sdks")
                licenses {
                    license {
                        name.set("Flagship License")
                        url.set("https://github.com/ds-horizon/flagship-sdks/blob/main/flagship-rn-sdk/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("dream11-atharva")
                        name.set("Atharva Kothawade")
                        email.set("atharva.kothawade@dream11.com")
                    }
                    developer {
                        id.set("arinjay-d11")
                        name.set("Arinjay Patni")
                        email.set("arinjay.patni@dream11.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/ds-horizon/flagship-sdks.git")
                    developerConnection.set("scm:git:ssh://github.com/ds-horizon/flagship-sdks.git")
                    url.set("https://github.com/ds-horizon/flagship-sdks")
                }

                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    val addDependency = addDependency@ { dep: org.gradle.api.artifacts.Dependency, scope: String ->
                        if (dep.group == null || dep.version == null || dep.name == "unspecified") {
                            return@addDependency
                        }
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", dep.group)
                        dependencyNode.appendNode("artifactId", dep.name)
                        dependencyNode.appendNode("version", dep.version)
                        dependencyNode.appendNode("scope", scope)

                        if (dep is org.gradle.api.artifacts.ModuleDependency) {
                            if (!dep.isTransitive) {
                                val exclusionNode = dependencyNode.appendNode("exclusions").appendNode("exclusion")
                                exclusionNode.appendNode("groupId", "*")
                                exclusionNode.appendNode("artifactId", "*")
                            } else if (dep.excludeRules.isNotEmpty()) {
                                val exclusionsNode = dependencyNode.appendNode("exclusions")
                                dep.excludeRules.forEach { rule ->
                                    val exclusionNode = exclusionsNode.appendNode("exclusion")
                                    exclusionNode.appendNode("groupId", rule.group ?: "*")
                                    exclusionNode.appendNode("artifactId", rule.module ?: "*")
                                }
                            }
                        }
                    }
                    configurations["api"].dependencies.forEach { dep -> addDependency(dep, "compile") }
                    configurations["implementation"].dependencies.forEach { dep -> addDependency(dep, "runtime") }
                }
            }
        }
    }
}
tasks.register("cleanBuildPublishFlagship") {
    dependsOn("clean")
    dependsOn("bundleReleaseAar")
    dependsOn("publishBarPublicationToSonatypeRepository")
    // Only add root project tasks if they exist
    rootProject.tasks.findByName("publishToSonatype")?.let {
        dependsOn(it)
    }
    rootProject.tasks.findByName("closeSonatypeStagingRepository")?.let {
        dependsOn(it)
    }
}

afterEvaluate {
    tasks.named("publishBarPublicationToSonatypeRepository") {
        mustRunAfter("bundleReleaseAar")
    }
    // Only configure root project tasks if they exist
    rootProject.tasks.findByName("publishToSonatype")?.let { publishTask ->
        publishTask.mustRunAfter("publishBarPublicationToSonatypeRepository")
    }
    rootProject.tasks.findByName("closeSonatypeStagingRepository")?.let { closeTask ->
        rootProject.tasks.findByName("publishToSonatype")?.let { publishTask ->
            closeTask.mustRunAfter(publishTask)
        }
    }
    // Only configure signing task if it exists (only created when signing is configured)
    tasks.findByName("signBarPublication")?.let {
        it.dependsOn("bundleReleaseAar")
    }
    tasks.named("bundleReleaseAar") {
        mustRunAfter("clean")
    }
}

signing {
    if (signingKeyId.isNotEmpty() && signingKey.isNotEmpty() && signingPassword.isNotEmpty()) {
        useInMemoryPgpKeys(
            signingKeyId,
            signingKey,
            signingPassword
        )
        sign(publishing.publications["bar"])
    }
}




