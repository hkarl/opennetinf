#!/bin/bash
#Launch the netinf node with a fully qualified starter class name as argument
cd /home/netinf/builds/checkout/CurrentRepository/build/dist/jar
java -jar node.jar $1
