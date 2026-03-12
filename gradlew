#!/bin/sh
DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed and Gradle wrapper JAR is not included in this environment." >&2
echo "Install Gradle 9.4+ or generate wrapper locally with: gradle wrapper --gradle-version 9.4.0" >&2
exit 1
