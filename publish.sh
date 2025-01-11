#!/bin/bash

export SONATYPE_CENTRAL_USERNAME='virtue-llc-jp'
export PGP_SIGNING_KEY=$(gpg --armor --export-secret-keys info@virtue.llc)

echo -n "SonarType Central Password: "
read -s SONATYPE_CENTRAL_PASSWORD
echo
export SONATYPE_CENTRAL_PASSWORD

echo -n "PGP signing key PassPhrase: "
read -s PGP_SIGNING_KEY_PASSPHRASE
echo
export PGP_SIGNING_KEY_PASSPHRASE

./gradlew sonatypeCentralUpload
