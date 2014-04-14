#!/bin/bash

if [[ $1 == 'client' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    # echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources
    # cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources

elif [[ $1 == 'api' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-api/target/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-api/target/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    # echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources
    # cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources

elif [[ $1 == 'dist' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/hadoop-yarn-applications-distributedshell-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/hadoop-yarn-applications-distributedshell-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
fi
