plugins {
    id("com.github.ElytraServers.elytra-conventions") version "v1.1.1"
    id("com.gtnewhorizons.gtnhconvention")
}

// Generates docs/RULES.md from the rules declared in CurtainRules.
tasks.register<JavaExec>("generateRulesReadme") {
    group = "documentation"
    description = "Generates docs/RULES.md for the Curtain rules"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.Lilith.Curtain.utils.CurtainRulesGenerator")
}
