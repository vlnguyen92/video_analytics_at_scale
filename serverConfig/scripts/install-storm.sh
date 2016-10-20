#! /usr/bin/env bash

# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

source "$__dir/helpers.sh"

## Zookeeper
subinfo "Installing Zookeeper 3.4.9"
cd $TOOL_DIR
### download release package
download http://www-eu.apache.org/dist/zookeeper/current/zookeeper-3.4.9.tar.gz
tar xvf $DOWNLOAD_DIR/zookeeper-3.4.9.tar.gz
mv zookeeper* zookeeper
### config
cd zookeeper/conf
cp zoo_sample.cfg zoo.cfg

## Storm
subinfo "Installing Apache Storm 1.0.2"
cd $TOOL_DIR
### download release package
download http://www-us.apache.org/dist/storm/apache-storm-1.0.2/apache-storm-1.0.2.tar.gz
tar xvf $DOWNLOAD_DIR/apache-storm-1.0.2.tar.gz
mv apache-storm* storm
### config
cd storm/conf
cat >> storm.yaml <<EOF
storm.local.dir: "/tmp/storm"
java.library.path: "$JAVA_HOME:$LD_LIBRARY_PATH"

storm.zookeeper.port: 2181
storm.zookeeper.servers:
    - "$(hostname)"

nimbus.seeds:
    - "$(hostname)"
nimbus.thrift.port: 6627
nimbus.cleanup.inbox.freq.secs: 60
nimbus.inbox.jar.expiration.secs: 20

ui.port: 8772

supervisor.slots.ports:
    - 6700
    - 6701
    - 6702
    - 6703
    - 6704
    - 6705
EOF
