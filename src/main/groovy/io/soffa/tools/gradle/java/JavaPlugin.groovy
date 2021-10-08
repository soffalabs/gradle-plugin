package io.soffa.tools.gradle.java

import io.soffa.tools.gradle.LombokPlugin
import io.soffa.tools.gradle.qa.PmdPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion

class JavaPlugin {

    static void apply(Project project, int version, boolean includeLombok) {
        project.plugins.apply('java')
        project.plugins.apply('java-library')
        if (includeLombok) {
            LombokPlugin.applyPlugin(project)
        }
        project.setProperty("sourceCompatibility",  JavaLanguageVersion.of(version).toString())
        if (project.findProperty("soffa.pmd.disabled") != true) {
            new PmdPlugin().apply(project)
        }

        project.compileJava {
            options.encoding = 'UTF-8'
            options.compilerArgs << '-parameters'
            options.warnings = false
            options.deprecation = false
        }

        project.javadoc {
            options.addStringOption("Xdoclint:none", "-quiet")
        }

        project.test {
            testLogging {
                events "PASSED", "SKIPPED", "FAILED"
            }
        }

    }

}