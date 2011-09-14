#!/bin/bash
#Launch the netinf node with a fully qualified starter class name as argument
SCRIPT_PATH="dirname $0";
cd $SCRIPT_PATH
cd dist/jar
java -jar node.jar $1
