package io.soffa.tools.gradle.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class MavenPublishPlugin implements Plugin<Project> {

    static final String  NEXUS_PUBLISH_PLUGIN = "io.github.gradle-nexus.publish-plugin"

    void apply(Project project) {

        project.plugins.apply('maven-publish')

        String projectVersion = project.version

        if (project.hasProperty("snapshot") && !projectVersion.endsWith("-SNAPSHOT")) {
            projectVersion += "-SNAPSHOT"
        } else if (project.hasProperty("release") && projectVersion.endsWith("-SNAPSHOT")) {
            projectVersion = projectVersion.replace("-SNAPSHOT", "")
        }


        if (project.findProperty("sonatype")) {

            project.plugins.apply('signing')
            Project root = project.getRootProject()

            if (!root.plugins.hasPlugin(NEXUS_PUBLISH_PLUGIN)) {
                root.plugins.apply(NEXUS_PUBLISH_PLUGIN)
                root.nexusPublishing {
                    repositories {
                        sonatype {
                            // stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID"))
                            username = root.property("ossrhUsername")
                            password = root.property("ossrhPassword")
                        }
                    }
                }
            }

            project.publishing {
                publications {
                    maven(MavenPublication) {
                        from project.components.java
                        groupId = project.property("group")
                        artifactId = project.name
                        version = projectVersion
                        pom {
                            name.set(project.displayName ?: project.name)
                            description.set(project.description)
                            url.set(project.property("url"))
                            licenses {
                                license {
                                    name.set("Apache License 2.0")
                                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                                }
                            }
                            developers {
                                developer {
                                    id.set("soffa.io")
                                    name.set("SOFFA")
                                    email.set("studio@soffa.io")
                                }
                            }
                            scm {
                                connection = "scm:git:git://soffa-io/${project.property("github.project")}.git"
                                developerConnection = "scm:git:ssh://soffa-io/${project.property("github.project")}.git"
                                url = "https://github.com/soffa-io/${project.property("github.project")}"
                            }
                        }
                    }
                }
            }

            project.ext["ossrhUsername"] = project.property("ossrhUsername")
            project.ext["ossrhPassword"] = project.property("ossrhPassword")
            project.ext["sonatypeStagingProfileId"] = ""
            project.ext["signing.keyId"] = project.property("signing.keyId")
            project.ext["signing.password"] = project.property("signing.password")
            project.ext["signing.secretKeyRingFile"] = project.property("signing.secretKeyRingFile")

        } else {
            project.publishing.publications {
                maven(MavenPublication) {
                    from project.components.java
                    groupId = project.property("group")
                    artifactId = project.name
                    version = projectVersion
                }
            }

            String mvnPublishingUrl = project.findProperty("mavenPublishingUrl") ?: System.getenv("MAVEN_PUBLISHING_URL")
            String mvnPublishingUser = project.findProperty("mavenPublishingUser") ?: System.getenv("MAVEN_PUBLISHING_USER")
            String mvnPublishingPwd = project.findProperty("mavenPublishingPassword") ?: System.getenv("MAVEN_PUBLISHING_PASSWORD")
            if (mvnPublishingUrl != null) {
                project.publishing.repositories {
                    maven {
                        url = mvnPublishingUrl
                        credentials {
                            username = mvnPublishingUser
                            password = mvnPublishingPwd
                        }
                    }
                }
            }
        }


        project.java {
            withJavadocJar()
            withSourcesJar()
        }

        if (project.plugins.hasPlugin("signing")) {
            project.signing {
                sign(project.publishing.publications["maven"])
            }
        }
    }

}