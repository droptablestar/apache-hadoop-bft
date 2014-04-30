#!/usr/bin/env python

import sys


try:
    filename = sys.argv[1]
    sliceNum = int(float(sys.argv[2]))
    numSlices = int(float(sys.argv[3]))

    myfile = open(filename, "r")



    #get number of lines... this is horribly slow but 
    #it reduces memory footprint which is necessary

    num_lines = 0
    with open(filename) as fileobject:
        for line in fileobject:
            num_lines+= 1

    startLine = (sliceNum-1)*(num_lines//numSlices)
    endLine = (sliceNum)*(num_lines//numSlices)



    line_count = 0
    with open(filename) as fileobject:
        for line in fileobject:
            
            if(line_count==endLine):
                break
            
            if(line_count>=startLine):
                # remove leading and trailing whitespace
                line = line.strip()
                # split the line into words
                words = line.split()
                # increase counters
                for word in words:
                    print ("%s\t%s" % (word, 1))      
                    continue

            line_count += 1
except:
    f = open("/tmp/error.txt", 'a')
    f.write("Unexpected error:"+sys.exc_info()[0]+ "\n")
    f.close()
