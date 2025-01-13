#!/bin/bash

export ORG_GRADLE_PROJECT_mavenCentralUsername='virtue-llc-jp'
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --export-secret-keys --armor info@virtue.llc | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')"

echo -n "SonarType Central Password: "
read -s ORG_GRADLE_PROJECT_mavenCentralPassword
echo
export ORG_GRADLE_PROJECT_mavenCentralPassword

echo -n "PGP signing key PassPhrase: "
read -s ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
echo
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword

./gradlew publishAllPublicationsToMavenCentralRepository
