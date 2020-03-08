#!/bin/bash
set -x
set -e

ARTIFACT_DIR="${ARTIFACT_DIR:-}"

TEST_SPECIFIC_MODULES="${TEST_SPECIFIC_MODULES:-presto-tests}"
JVM_HEAPSIZE="${JVM_HEAPSIZE:-4096M}"

export MAVEN_OPTS="-Xmx$JVM_HEAPSIZE -XX:+ExitOnOutOfMemoryError"
MAVEN_SKIP_CHECKS_AND_DOCS="-Dair.check.skip-all=true -Dmaven.javadoc.skip=true"
MAVEN_FAST_INSTALL="-DskipTests $MAVEN_SKIP_CHECKS_AND_DOCS -B -q -T C1"

function cleanup() {
    exit_status=$?
    # copy artifacts to $ARTIFACT_DIR if specified
    # --no-perms/--no-group to handle running as unprivileged in prow
    if [ -n "$ARTIFACT_DIR" ]; then
        mkdir -p "$ARTIFACT_DIR"
        rsync -v -rl -m \
          --include='**/' \
          --include='**/surefire-reports/**.xml' \
          --include='**/surefire-reports/emailable-report.html' \
          --include='**/product-tests-presto-jvm-error-file.log' \
          --include='**/test-reports/junitreports/**.xml' \
          --include='**/test-reports/emailable-report.html' \
          --exclude='*' \
          . "$ARTIFACT_DIR/"
    fi
    exit "$exit_status"
}

function run_tests() {
    ./mvnw -v
    ./mvnw clean -B
    ./mvnw install $MAVEN_FAST_INSTALL -B -pl "${TEST_SPECIFIC_MODULES}" -am
    ./mvnw test $MAVEN_SKIP_CHECKS_AND_DOCS -B -pl "${TEST_SPECIFIC_MODULES}" -Dtest="!io.prestosql.execution.sessionpropertymanagers.TestDbSessionPropertyManagerIntegration"
}

function main() {
    if [ -n "$ARTIFACT_DIR" ]; then
        mkdir -p "$ARTIFACT_DIR"
        run_tests | tee "$ARTIFACT_DIR/maven-logs.txt"
    else
        run_tests
    fi
}

trap cleanup EXIT
main
