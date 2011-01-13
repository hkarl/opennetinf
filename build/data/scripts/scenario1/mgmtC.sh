#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar mgmt.jar -n ${SC1_IP_NODEC} -p ${SC_PO_NODEC} --logging log4j/demoTool.xml
