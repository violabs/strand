pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/violabs/public-libs")
            credentials {
                username = ""
                password = ""
            }
        }
        mavenCentral()
    }
}

rootProject.name = "strand"

include(
    "threads",
    "integrationTests",
    "webclient",
    "webclient:blocking-client",
    "webclient:nonblocking-client",
    "webclient:server",
    "webclient:shared",
)