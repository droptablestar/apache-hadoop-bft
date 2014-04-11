#!/bin/bash

if [[ $1 == 'client' ]]; then
    echo cp $HADOOP_YARN_HOME/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_HOME/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    echo cp $HADOOP_YARN_HOME/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources
    cp $HADOOP_YARN_HOME/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources
fi
