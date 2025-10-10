import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.gradleup.shadow")
    id("com.gradle.plugin-publish")
}

fun version(): String = version.toString()
val noRelocate = project.hasProperty("disable-relocation")
if (noRelocate) {
    if (version().contains("-SNAPSHOT")) {
        version = version().substringBefore("-SNAPSHOT") + "-NO-RELOCATE-SNAPSHOT"
    } else {
        version = version() + "-NO-RELOCATE"
    }
}

val shade: Configuration by configurations.creating
configurations.implementation {
    extendsFrom(shade)
}

configurations.shadowRuntimeElements {
    compatibilityAttributes(objects)
}

fun ShadowJar.configureStandard() {
    configurations = listOf(shade)
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
    }

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "OSGI-INF/**", "*.profile", "module-info.class", "ant_tasks/**", "OSGI-OPT/**", "META-INF/*.pro")

    mergeServiceFiles()
}

val sourcesJar by tasks.existing(AbstractArchiveTask::class) {
    from(
        zipTree(project(":paperweight-lib").tasks
            .named("sourcesJar", AbstractArchiveTask::class)
            .flatMap { it.archiveFile })
    ) {
        exclude("META-INF/**")
    }
}

gradlePlugin {
    website.set("https://github.com/MenthaMC/lightweight")
    vcsUrl.set("https://github.com/MenthaMC/lightweight")
}

val shadowJar by tasks.existing(ShadowJar::class) {
    archiveClassifier.set(null as String?)
    configureStandard()

    inputs.property("noRelocate", noRelocate)
    if (noRelocate) {
        return@existing
    }

    val prefix = "paper.libs"
    listOf(
        "codechicken.diffpatch",
        /* -> */ "codechicken.repack",
        "com.github.salomonbrys.kotson",
        "com.google.gson",
        "dev.denwav.hypo",
        /* -> */ "org.jgrapht",
        /* -> */ "org.jheaps",
        /* -> */ "com.google.errorprone.annotations",
        /* -> */ "org.objectweb.asm",
        "io.sigpipe.jbsdiff",
        /* -> */ "org.tukaani.xz",
        "net.fabricmc",
        "org.apache.http",
        /* -> */ "org.apache.commons",
        "org.cadixdev",
        /* -> */ "me.jamiemansfield",
        "org.eclipse.jgit",
        /* -> */ "com.googlecode.javaewah",
        /* -> */ "com.googlecode.javaewah32",
        "kotlinx.coroutines",
        //"org.slf4j",
        // used by multiple
        "org.intellij.lang",
        "org.jetbrains.annotations"
    ).forEach { pack ->
        relocate(pack, "$prefix.$pack")
    }
}

publishing {
    repositories {
        maven("https://repo.menthamc.org/repository/maven-snapshots/") {
            name = "MenthaMC"
            credentials(PasswordCredentials::class) {
                username = System.getenv("PRIVATE_MAVEN_REPO_USERNAME")
                password = System.getenv("PRIVATE_MAVEN_REPO_PASSWORD")
            }
        }
    }

    publications {
        withType(MavenPublication::class).configureEach {
            pom {
                pomConfig()
            }
        }
    }
}

fun MavenPom.pomConfig() {
    val repoPath = "MenthaMC/lightweight"
    val repoUrl = "https://github.com/$repoPath"

    name.set("lightweight")
    description.set("Gradle plugin for the MenthaMC project")
    url.set(repoUrl)
    inceptionYear.set("2020")

    licenses {
        license {
            name.set("LGPLv2.1")
            url.set("$repoUrl/blob/master/license/LGPLv2.1.txt")
            distribution.set("repo")
        }
    }

    issueManagement {
        system.set("GitHub")
        url.set("$repoUrl/issues")
    }

    developers {
        developer {
            id.set("DenWav")
            name.set("Kyle Wood")
            email.set("kyle@denwav.dev")
            url.set("https://github.com/DenWav")
        }
        developer {
            id.set("Bacteriawa")
            name.set("Bacteriawa")
            email.set("A3167717663@hotmail.com")
            url.set("https://github.com/Bacteriawa")
        }
        developer {
            id.set("CoderFrish")
            name.set("Frish2021")
            email.set("1573880184@qq.com")
            url.set("https://github.com/CoderFrish")
        }
    }

    scm {
        url.set(repoUrl)
        connection.set("scm:git:$repoUrl.git")
        developerConnection.set("scm:git:git@github.com:$repoPath.git")
    }
}
