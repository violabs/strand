dependencies {
    implementation(project(":webclient:shared"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(group = "io.projectreactor.netty", module = "reactor-netty-http")
    }
}