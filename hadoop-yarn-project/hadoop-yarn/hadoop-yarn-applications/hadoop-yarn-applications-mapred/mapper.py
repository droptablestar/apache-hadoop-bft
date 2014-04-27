#!/usr/bin/env python

import sys

filename = sys.argv[1]
sliceNum = int(float(sys.argv[2]))
numSlices = int(float(sys.argv[3]))

file = open(filename, "r")
lines = file.readlines()
file.close()


startLine = (sliceNum-1)*(len(lines)//numSlices)
endLine = (sliceNum)*(len(lines)//numSlices)



# input comes from STDIN (standard input)
for i in range(startLine, endLine):
    line = lines[i]
    # remove leading and trailing whitespace
    line = line.strip()
    # split the line into words
    words = line.split()
    # increase counters
    for word in words:
        # write the results to STDOUT (standard output);
        # what we output here will be the input for the
        # Reduce step, i.e. the input for reducer.py
        #
        # tab-delimited; the trivial word count is 1
        print ("%s\t%s" % (word, 1))
