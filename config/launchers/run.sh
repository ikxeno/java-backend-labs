#!/bin/sh
set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
lab_java=java
if [ -n "${JAVA_HOME:-}" ]; then
    lab_java="$JAVA_HOME/bin/java"
fi
if [ -x "@cached.jdk@/bin/java" ]; then
    lab_java="@cached.jdk@/bin/java"
fi

exec "$lab_java" -jar "$script_dir/@jar.name@" "$@"
