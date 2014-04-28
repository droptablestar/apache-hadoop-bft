#!/usr/bin/env python

from operator import itemgetter
import sys
import os

# ~/yarn/hadoop-3.0/logs/userlogs/application_1398627427707_0006/container_1398627427707_0006_01_000001

env = os.getenv("HADOOP_PREFIX") 
root = env+"/logs/userlogs/"

tmp = sys.argv[1].split("_")
appId = tmp[1] + "_" + tmp[2]


containers = sys.argv[2].split(",")


current_word = None
current_count = 0
word = None
dict = {}
for container in containers:

    fileStr = root+"application_"+appId+"/"+container+"/stdout"
    print(fileStr)
    file = open(fileStr, "r")
    # input comes from STDIN
    for line in file.readlines():
        # remove leading and trailing whitespace
        line = line.strip()

        # parse the input we got from mapper.py
        word, count = line.split('\t', 1)

        if word in dict:
            dict[word] += 1
        else:
            dict[word] = int(count)
file.close()
print(dict)
# do not forget to output the last word if needed!
if current_word == word:
    print ("%s\t%s" % (current_word, current_count))
