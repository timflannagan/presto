#!/bin/bash -x

set -eo pipefail

# $1=OPENSHIFT_CI=true means running in CI
if [[ "$1" == "true" ]]; then

    yum -y install --setopt=skip_missing_names_on_install=False \
      curl \
      java-1.8.0-openjdk \
      java-1.8.0-openjdk-devel

    pushd /tmp
    curl -o maven.tgz https://downloads.apache.org/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
    tar zxvf maven.tgz
    export M2_HOME=/tmp/apache-maven-3.3.9
    export PATH=${PATH}:${M2_HOME}/bin
    popd

    # build presto
    cd /build && mvn --batch-mode --errors -Dmaven.javadoc.skip=true -Dmaven.source.skip=true -DskipTests -DfailIfNoTests=false -Dtest=false clean package -pl '!presto-testing-docker' -Dmaven.repo.local=.m2/repository
    # Install prometheus-jmx agent
    mvn --batch-mode dependency:get -Dartifact=io.prometheus.jmx:jmx_prometheus_javaagent:0.3.1:jar -Ddest=/build/jmx_prometheus_javaagent.jar
else
    export PRESTO_VERSION=328.0
    export RH_PRESTO_PATCH_VERSION=00001
    export RH_PRESTO_VERSION=${PRESTO_VERSION}.0.redhat-${RH_PRESTO_PATCH_VERSION}
    export RH_PRESTO_BREW_DIR=${PRESTO_VERSION}.0.redhat_${RH_PRESTO_PATCH_VERSION}
    export PRESTO_SERVER_OUT=/build/presto-server/target/presto-server-${PRESTO_VERSION}
    export PRESTO_CLI_OUT=/build/presto-cli/target/presto-cli-${PRESTO_VERSION}-executable.jar
    export PRESTO_CLI_JAR_URL=http://download.eng.bos.redhat.com/brewroot/packages/io.prestosql-presto-root/${RH_PRESTO_BREW_DIR}/1/maven/io/prestosql/presto-cli/${RH_PRESTO_VERSION}/presto-cli-${RH_PRESTO_VERSION}-executable.jar
    export PRESTO_SERVER_URL=http://download.eng.bos.redhat.com/brewroot/packages/io.prestosql-presto-root/${RH_PRESTO_BREW_DIR}/1/maven/io/prestosql/presto-server/${RH_PRESTO_VERSION}/presto-server-${RH_PRESTO_VERSION}.tar.gz

    set -x; curl -fSLs \
        $PRESTO_SERVER_URL \
        -o /tmp/presto-server.tar.gz

    set -x; tar -xvf /tmp/presto-server.tar.gz -C /tmp \
        && mv /tmp/presto-server-${RH_PRESTO_VERSION} \
        $PRESTO_SERVER_OUT

    set -x; curl -fSLs \
        $PRESTO_CLI_JAR_URL \
        -o $PRESTO_CLI_OUT

    export PROMETHEUS_JMX_EXPORTER_VERSION=0.3.1
    export RH_PROMETHEUS_JMX_EXPORTER_PATCH_VERSION=00006
    export RH_PROMETHEUS_JMX_EXPORTER_VERSION=${PROMETHEUS_JMX_EXPORTER_VERSION}.redhat-${RH_PROMETHEUS_JMX_EXPORTER_PATCH_VERSION}
    export RH_PROMETHEUS_JMX_EXPORTER_BREW_DIR=${PROMETHEUS_JMX_EXPORTER_VERSION}.redhat_${RH_PROMETHEUS_JMX_EXPORTER_PATCH_VERSION}
    export PROMETHEUS_JMX_EXPORTER_OUT=/build/jmx_prometheus_javaagent.jar
    export PROMETHEUS_JMX_EXPORTER_URL=http://download.eng.bos.redhat.com/brewroot/packages/io.prometheus.jmx-parent/${RH_PROMETHEUS_JMX_EXPORTER_BREW_DIR}/1/maven/io/prometheus/jmx/jmx_prometheus_javaagent/${RH_PROMETHEUS_JMX_EXPORTER_VERSION}/jmx_prometheus_javaagent-${RH_PROMETHEUS_JMX_EXPORTER_VERSION}.jar

    set -x; curl -fSLs \
        $PROMETHEUS_JMX_EXPORTER_URL \
        -o $PROMETHEUS_JMX_EXPORTER_OUT
fi
