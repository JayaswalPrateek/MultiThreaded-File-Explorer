#!/bin/bash
cd src &&
javac ./repl/Main.java -Xlint:all -Werror &&
java repl.Main &&
rm ./repl/*.class &&
rm ./core/*.class
