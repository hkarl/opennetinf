#!/bin/bash
#Launch the netinf node with a fully qualified starter class name as argument
SCRIPT_PATH="dirname $0";
cd /home/netinf/on-netinf-prototype/build/dist/jar
java -jar node.jar $1
