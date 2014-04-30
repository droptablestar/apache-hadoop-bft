#!/usr/bin/env python

from operator import itemgetter
import sys
import os
import subprocess

# ~/yarn/hadoop-3.0/logs/userlogs/application_1398627427707_0006/container_1398627427707_0006_01_000001

env = os.getenv("HADOOP_PREFIX") 
root = env+"/logs/userlogs/"
hdfs_path = env+"/bin/hdfs"


tmp = sys.argv[1].split("_")
appId = tmp[1] + "_" + tmp[2]


containers = sys.argv[2].split(",")


current_word = None
current_count = 0
word = None
myDict = {}
for container in containers:

    filePath = root+"application_"+appId+"/"+container+"/stdout.dat"
    #output = subprocess.check_output([hdfs_path, "dfs","-cat", filePath], stderr=None) 
    output = subprocess.Popen([hdfs_path, "dfs", "-cat", filePath], stdout=subprocess.PIPE)

    for line in output.stdout:
       
        # remove leading and trailing whitespace
        line = line.strip()

        if( line == ""):
            continue
 
        # parse the input we got from mapper.py
        word, count = line.split('\t', 1)

        word = word.lower().strip(' .,!')

        if word in myDict:
            myDict[word] += int(count)
        else:
            myDict[word] = int(count)

#print(sorted(myDict, key=lambda key: myDict[key]))
print(sorted(myDict.items(), key=lambda(k,v): (k,v)))

