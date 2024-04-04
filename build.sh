#!/bin/bash
cd src &&
javac ./repl/repl.java -Xlint:all -Werror &&
java repl.repl &&
rm ./repl/*.class &&
rm ./core/*.class
