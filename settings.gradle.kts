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

include("threads")
include("integrationTests")