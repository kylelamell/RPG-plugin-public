import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "2.2.21"
    id("org.jetbrains.intellij.platform") version "2.16.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.3"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Optional path to a locally-installed IDE for the Plugin Verifier (see
// pluginVerification below). Supply with:
//   -PverifierLocalIde="C:/Users/<you>/AppData/Local/Programs/IntelliJ IDEA"
val verifierLocalIde: java.io.File? =
    providers.gradleProperty("verifierLocalIde").orNull?.let { file(it) }

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // IntelliJ IDEA Community 2024.3 ships JBR 21, matching our JVM toolchain.
        intellijIdeaCommunity("2024.3")
        testFramework(TestFrameworkType.Platform)
        // The IntelliJ Plugin Verifier CLI, required by the `verifyPlugin` task.
        pluginVerifier()
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // We build against 243 (2024.3) — the LOWEST version we support — on
            // purpose: compiling against the floor prevents accidentally calling
            // APIs newer than sinceBuild. We use only stable platform APIs, so we
            // leave the upper bound OPEN. `untilBuild = provider { null }` removes
            // the until-build cap entirely, so the plugin loads in 2024.3 and every
            // later build (251, 261, …) without a re-release. (A hardcoded cap like
            // "243.*" is exactly what previously blocked install on newer IDEs.)
            // Forward compatibility is validated by the Plugin Verifier below, not
            // by an artificial version ceiling.
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }

    // Catch the OTHER compatibility failure mode (using an API that changed or was
    // removed in a newer IDE) at build time rather than at the user's install/runtime.
    // Run with: ./gradlew verifyPlugin
    pluginVerification {
        ides {
            // Verify the floor of our support range. IC-2024.3 is the SDK we build
            // against and is reliably resolvable from the JetBrains repositories.
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.3")
            // Verify newer IDEs against a LOCAL install rather than downloading them.
            // recommended() / create(...) for the newest releases abort the whole task here because the
            // bare-version Community binaries for the newest releases (2025.3, 2026.1)
            // are not resolvable from the JetBrains repos. Pointing at the install on
            // disk needs no download and checks the real artifact against the real IDE:
            //   ./gradlew verifyPlugin -PverifierLocalIde="C:/.../IntelliJ IDEA"
            if (verifierLocalIde != null) local(verifierLocalIde)
        }
    }

    // We ship pure Kotlin; no Java NotNull instrumentation needed.
    instrumentCode = false
}

kotlin {
    jvmToolchain(21)
}

// --- JFlex lexer generation (GrammarKit) -----------------------------------

val generatedSourcesRoot = layout.buildDirectory.dir("generated/sources/jflex")

val generateRpgLexer = tasks.register<GenerateLexerTask>("generateRpgLexer") {
    sourceFile.set(file("src/main/kotlin/com/rpgle/plugin/lexer/RpgLexer.flex"))
    targetOutputDir.set(generatedSourcesRoot.map { it.dir("com/rpgle/plugin/lexer") })
    purgeOldFiles.set(true)
}

val generateBinderLexer = tasks.register<GenerateLexerTask>("generateBinderLexer") {
    sourceFile.set(file("src/main/kotlin/com/rpgle/plugin/binder/BinderLexer.flex"))
    targetOutputDir.set(generatedSourcesRoot.map { it.dir("com/rpgle/plugin/binder") })
    purgeOldFiles.set(true)
}

val generateDdsLexer = tasks.register<GenerateLexerTask>("generateDdsLexer") {
    sourceFile.set(file("src/main/kotlin/com/rpgle/plugin/dds/DdsLexer.flex"))
    targetOutputDir.set(generatedSourcesRoot.map { it.dir("com/rpgle/plugin/dds") })
    purgeOldFiles.set(true)
}

sourceSets {
    main {
        java {
            srcDir(generatedSourcesRoot)
        }
    }
}

tasks {
    withType<KotlinCompile> {
        dependsOn(generateRpgLexer, generateBinderLexer, generateDdsLexer)
    }
    withType<JavaCompile> {
        dependsOn(generateRpgLexer, generateBinderLexer, generateDdsLexer)
    }

    // The sandbox IDE (runIde) launches on the bundled JBR with a custom system
    // class loader (com.intellij.util.lang.PathClassLoader), so the JBR's CDS
    // prints a harmless "Archived non-system classes are disabled…" notice to the
    // console on every launch. It's purely informational — boot-class CDS still
    // works — so mute just the `cds` log tag. This does NOT disable CDS; doing
    // that would need -Xshare:off and would only slow sandbox startup. (The
    // platform's SecurityManager deprecation warnings come from IntelliJ itself
    // and cannot be suppressed here.)
    withType<RunIdeTask> {
        jvmArgs("-Xlog:cds=off")
    }
}
