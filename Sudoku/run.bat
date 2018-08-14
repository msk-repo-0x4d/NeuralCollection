@echo off
REM Created - 21-Aug-2017
REM Copyright (c) 2017 M.S.Khan (Apache License 2.0)

REM Batch file for running Sudoku on Windows-x64


REM add required dlls to path
set PATH=%PATH%;lib/opencv-3.2.0;lib/swipl-7.4.2/libs;lib/swipl-7.4.2;lib/neuroph2.93

REM run jar file
java -Xms100m -Xmx400m -jar Sudoku.jar
