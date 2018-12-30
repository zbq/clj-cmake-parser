
set ANTLRPATH=C:\Tools\antlr-4.7.2-complete.jar

java -cp %ANTLRPATH% org.antlr.v4.Tool -package cmake_parser CMake.g4
rem javac -cp %ANTLRPATH% *.java
rem java -cp .;%ANTLRPATH% org.antlr.v4.gui.TestRig CMake file -gui ..\..\test\cmake_parser\CMakeLists.txt
