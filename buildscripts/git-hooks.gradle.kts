fun isLinuxOrMacOs(): Boolean {
    val osName = System.getProperty("os.name").lowercase()
    return osName.contains("linux") || osName.contains("mac os") || osName.contains("macos")
}

tasks.register<Copy>("copyGitHooks") {
    description = "Copies the git hooks from /scripts/git-hooks to the .git folder."
    group = "git hooks"
    from("${rootDir}/scripts/git-hooks/") {
        include("**/*.sh")
        rename("(.+)\\.sh", "$1")
    }
    into("${rootDir}/.git/hooks")
    onlyIf { isLinuxOrMacOs() }
}

tasks.register<Exec>("installGitHooks") {
    description = "Installs the git hooks from /scripts/git-hooks."
    group = "git hooks"
    dependsOn(tasks.named("copyGitHooks"))
    onlyIf { isLinuxOrMacOs() }

    workingDir = rootDir
    commandLine = listOf("chmod", "-R", "+x", ".git/hooks/")

    doLast {
        println("✅ Git hooks installed successfully.")
    }
}

tasks.register("uninstallGitHooks") {
    description = "Uninstalls the project's git hooks."
    group = "git hooks"
    onlyIf { isLinuxOrMacOs() }

    doLast {
        val hooksDir = rootDir.resolve(".git/hooks")
        val sourceHooksDir = rootDir.resolve("scripts/git-hooks")
        if (hooksDir.exists() && sourceHooksDir.exists()) {
            sourceHooksDir.walk().filter { it.isFile && it.extension == "sh" }.forEach { sourceFile ->
                val destFile = hooksDir.resolve(sourceFile.nameWithoutExtension)
                if (destFile.exists()) {
                    destFile.delete()
                    println("🗑️ Deleted hook: ${destFile.name}")
                }
            }
            println("✅ Git hooks uninstalled.")
        } else {
            println("Could not find git hooks to uninstall.")
        }
    }
}
