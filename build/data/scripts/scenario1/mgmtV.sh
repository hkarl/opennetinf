#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar mgmt.jar -p ${SC1_PO_NODEC} --logging log4j/demoTool.xml
