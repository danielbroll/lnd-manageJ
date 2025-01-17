plugins {
    `java-platform`
}

group = "de.c-otto.lndmanagej"

javaPlatform {
    allowDependencies()
}

dependencies {
    val springBootVersion = "3.1.1"
    val grpcVersion = "1.56.0"

    api(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.3"))
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    constraints {
        api("com.google.ortools:ortools-java:9.6.2534")
        api("com.google.protobuf:protobuf-gradle-plugin:0.9.3")
        api("io.grpc:grpc-netty:$grpcVersion")
        api("io.grpc:grpc-protobuf:$grpcVersion")
        api("io.grpc:grpc-stub:$grpcVersion")
        api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        api("io.vavr:vavr:0.10.4")
    }
}
