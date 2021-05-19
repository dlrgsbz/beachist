#!/bin/bash

DIR="$(pwd)"
GRADLE_PROPERTIES=${DIR}"/project_resources/keystore.properties"
export GRADLE_PROPERTIES
echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

if [[ ! -f "$GRADLE_PROPERTIES" ]]; then
    echo "Gradle Properties does not exist"

    echo "Creating Gradle Properties file..."
    touch $GRADLE_PROPERTIES
fi

echo "Writing keys to gradle.properties..."
echo "KEYSTORE_FILE=../project_resources/release.keystore" >> ${GRADLE_PROPERTIES}
echo "KEYSTORE_PASSWORD=$CLIENT_KEYSTORE_PASSWORD" >> ${GRADLE_PROPERTIES}
echo "KEY_ALIAS=$KEY_ALIAS" >> ${GRADLE_PROPERTIES}
echo "KEY_PASSWORD=$KEY_PASSWORD" >> ${GRADLE_PROPERTIES}
