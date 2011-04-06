#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar mgmt.jar --logging scenario2/log4j/managementTool.xml
