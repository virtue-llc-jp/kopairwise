#!/bin/bash
# ~/.gradle/gradle.properties:
# mavenCentralUsername=[publisher token username(server.username)]
# mavenCentralPassword=[publisher token password(server.password)]

export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --export-secret-keys --armor info@virtue.llc | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"

echo -n "PGP signing key PassPhrase: "
read -s ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
echo
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword

./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
