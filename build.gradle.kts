plugins {
	java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
	testImplementation("junit", "junit", "4.12")
}
