import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.plugins.signing.SigningExtension

plugins {
    id("base")
}

allprojects {
    group = "org.vaelow233.botweave"
    version = "1.0.0"
}

val releaseModules = listOf(
    "botweave-api",
    "botweave-core",
    "botweave-connector-qq-ob11",
    "botweave-connector"
)

val moduleDescriptions = mapOf(
    "botweave-api" to "Common APIs and models for the BotWeave bot framework.",
    "botweave-core" to "Bot registration, event dispatch and connector lifecycle management.",
    "botweave-connector-qq-ob11" to "QQ integration for BotWeave using the OneBot 11 protocol.",
    "botweave-connector" to "An aggregate dependency for all official BotWeave connectors."
)

val stagingDirectory = layout.buildDirectory.dir("central-staging")

subprojects {
    if (name in releaseModules) {
        val moduleName = name

        pluginManager.withPlugin("java") {
            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
                withJavadocJar()
            }

            tasks.withType<Javadoc>().configureEach {
                options.encoding = "UTF-8"
            }
        }

        pluginManager.withPlugin("maven-publish") {
            val publishingExtension = extensions.getByType<PublishingExtension>()

            publishingExtension.repositories.maven {
                name = "CentralStaging"
                url = stagingDirectory.get().asFile.toURI()
            }

            publishingExtension.publications
                .withType<MavenPublication>()
                .configureEach {
                    pom {
                        name.set(moduleName)
                        description.set(
                            moduleDescriptions.getValue(moduleName)
                        )
                        url.set(
                            "https://github.com/Vaelow233/BotWeave"
                        )

                        licenses {
                            license {
                                name.set("GNU Lesser General Public License v3.0")
                                url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                                distribution.set("repo")
                            }
                        }

                        developers {
                            developer {
                                id.set("Vaelow233")
                                name.set("Vaelow233")
                                url.set("https://github.com/Vaelow233")
                                email.set(System.getenv("BOTWEAVE_PUBLIC_EMAIL"))
                            }
                        }

                        scm {
                            url.set("https://github.com/Vaelow233/BotWeave")
                            connection.set("scm:git:https://github.com/Vaelow233/BotWeave.git")
                            developerConnection.set("scm:git:ssh://git@github.com/Vaelow233/BotWeave.git")
                        }
                    }
                }

            pluginManager.apply("signing")

            extensions.configure<SigningExtension> {
                useGpgCmd()
                sign(publishingExtension.publications)
            }
        }
    }
}

tasks.register<Zip>("centralBundle") {
    group = "publishing"
    description = "Builds a signed bundle for Central Portal upload."

    dependsOn(
        releaseModules.map {
            ":$it:publishMavenJavaPublicationToCentralStagingRepository"
        }
    )

    archiveFileName.set("botweave-${project.version}-central-bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("central-bundle"))

    from(stagingDirectory) {
        releaseModules.forEach { moduleName ->
            include(
                "org/vaelow233/botweave/$moduleName/${project.version}/**"
            )
        }

        exclude("**/maven-metadata.xml*")
        exclude("**/*.asc.md5", "**/*.asc.sha1")
        exclude("**/*.asc.sha256", "**/*.asc.sha512")
        exclude("**/*.sha256", "**/*.sha512")
    }

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}