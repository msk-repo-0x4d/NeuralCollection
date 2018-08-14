#!/bin/sh
#
# Created - 21-Aug-2017
# Copyright (c) 2017 M.S.Khan (Apache License 2.0)
#
#
# Script file for running Sudoku on Linux-x86_64



# add shared libraries to path
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$PWD/lib/opencv-3.2.0"\
":$PWD/lib/swipl-7.4.2:$PWD/lib/neuroph2.93"


## SWI-Prolog related changes to get Prolog running ##
# make sure swipl binary is executable
chmod u+x lib/swipl-7.4.2/bin/swipl

# add path to SWI Prolog binary
export PATH="$PATH:lib/swipl-7.4.2/bin"


## Run Sudoku ##

# run jar file
java -Xms100m -Xmx400m -jar Sudoku.jar

