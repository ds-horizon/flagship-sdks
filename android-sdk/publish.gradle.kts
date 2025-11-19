apply(plugin = "maven-publish")
apply(plugin = "signing")

// Extract version from git tag or use default
fun getReleaseVersionName(): String =
    try {
        // First try to get the latest android-v* tag
        val androidTagVersion =
            providers
                .exec {
                    commandLine("git", "tag", "--sort=-version:refname", "--list", "android-v*")
                }.standardOutput.asText
                .get()
                .trim()
                .split("\n")
                .firstOrNull()

        if (!androidTagVersion.isNullOrEmpty()) {
            // Remove 'android-v' prefix
            androidTagVersion.removePrefix("android-v")
        } else {
            // Fallback to any tag for backward compatibility
            val tagVersion =
                providers
                    .exec {
                        commandLine("git", "describe", "--tags", "--abbrev=0")
                    }.standardOutput.asText
                    .get()
                    .trim()

            // Remove 'v' prefix if present
            if (tagVersion.startsWith("v")) {
                tagVersion.substring(1)
            } else {
                tagVersion
            }
        }
    } catch (e: Exception) {
        "0.1.0-SNAPSHOT"
    }

val versionName = getReleaseVersionName()

afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.flagship.sdk"
                artifactId = "flagship-sdk"
                version = versionName

                pom {
                    name.set("Flagship SDK")
                    description.set("Android SDK for Flagship feature flags")
                    url.set("https://github.com/ds-horizon/flagship-sdks")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("dream11")
                            name.set("Dream11")
                            email.set("engineering@dream11.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/ds-horizon/flagship-sdks.git")
                        developerConnection.set("scm:git:ssh://github.com/ds-horizon/flagship-sdks.git")
                        url.set("https://github.com/ds-horizon/flagship-sdks")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/ds-horizon/flagship-sdks")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

// Optional: Configure signing for releases (uncomment if needed)

/*
configure<SigningExtension> {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(the<PublishingExtension>().publications["release"])
}
*/
