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

if [ "$#" -eq 0 ]; then
    set -- "$script_dir/characters.csv" "$script_dir/locations.txt"
elif [ "$#" -eq 1 ] && [ "$1" = "--crud-demo" ]; then
    set -- "$script_dir/characters.csv" "$script_dir/locations.txt" "$1"
fi

exec "$lab_java" -jar "$script_dir/@jar.name@" "$@"
