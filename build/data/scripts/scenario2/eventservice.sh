#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar essiena.jar ../configs/scenario2/eventServiceSiena.properties
