#!/bin/bash

if [[ $1 == 'client' ]]; then
    cd $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client
    # sed -i "s/\*\*\*\*\*.*\*\*\*\*\*/\*\*\*\*\*${2}\*\*\*\*\*/g" src/main/java/org/apache/hadoop/yarn/client/api/impl/AMRMClientImpl.java
    mvn package -DskipTests
    
    ## copy client jar from source to distribution
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn

    copy yarn jars from distribution to spark
    echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-client-2.3.0.jar
    cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-client-2.3.0.jar
    echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-api-2.3.0.jar
    cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-api-2.3.0.jar
    echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-common-2.3.0.jar
    cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-common-2.3.0.jar
    echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-common-2.3.0.jar
    cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-common-2.3.0.jar
    echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-nodemanager-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-nodemanager-2.3.0.jar
    cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-nodemanager-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-nodemanager-2.3.0.jar

    cd $SPARK_HOME
    SPARK_HADOOP_VERSION=2.3.0 SPARK_YARN=true sbt/sbt assembly

    # yrn stop
    # wait %1
    # yrn start

    # fg
    
elif [[ $1 == 'api' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-api/target/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-api/target/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    
elif [[ $1 == 'dist' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/hadoop-yarn-applications-distributedshell-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/hadoop-yarn-applications-distributedshell-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
fi
