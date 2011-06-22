#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar node.jar netinf.node.module.scenario2.SearchRdfModule
