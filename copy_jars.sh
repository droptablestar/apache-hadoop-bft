#!/bin/bash

if [[ $1 == 'client' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn

    echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-client-2.3.0.jar
    cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-client-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-client-2.3.0.jar

    ## THIS IS ALL LIKELY NOT NEEDED.... ##
    # echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-api-2.3.0.jar
    # cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-api-2.3.0.jar
    # echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-common-2.3.0.jar
    # cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-common-2.3.0.jar
    # echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-common-2.3.0.jar
    # cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-common-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-common-2.3.0.jar
    # echo cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-nodemanager-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-nodemanager-2.3.0.jar
    # cp $HADOOP_PREFIX/share/hadoop/yarn/hadoop-yarn-server-nodemanager-3.0.0-SNAPSHOT.jar $SPARK_HOME/lib_managed/jars/hadoop-yarn-server-nodemanager-2.3.0.jar
        
    # for i in `find $HADOOP_COMMON_CLASSES -name AMRMClient*`; do
    # 	for j in `find $SPARK_HOME -name $(basename $i)`; do
    # 	    echo "cp $i $j"
    # 	    cp $i $j
    # 	done
    # done 
    
elif [[ $1 == 'api' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-api/target/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-api/target/hadoop-yarn-api-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    # echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources
    # cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-client/target/hadoop-yarn-client-*-SNAPSHOT-sources.jar $HADOOP_PREFIX/share/hadoop/yarn/sources

elif [[ $1 == 'dist' ]]; then
    echo cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/hadoop-yarn-applications-distributedshell-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
    cp $HADOOP_YARN_SRC/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/hadoop-yarn-applications-distributedshell-3.0.0-SNAPSHOT.jar $HADOOP_PREFIX/share/hadoop/yarn
fi
