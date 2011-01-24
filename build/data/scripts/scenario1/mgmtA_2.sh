#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/mgmt2/jar/ &> /dev/null
java -jar mgmt.jar -n ${SC1_IP_NODEA} -p ${SC1_PO_NODEA} --logging log4j/demoTool.xml
