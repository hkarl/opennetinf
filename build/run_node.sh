#!/bin/bash

#Launch the netinf node with a fully qualified starter class name as argument
SCRIPT_PATH=`dirname $0`
MODULE=$1

cd $SCRIPT_PATH/dist/jar
java -jar node.jar $MODULE
