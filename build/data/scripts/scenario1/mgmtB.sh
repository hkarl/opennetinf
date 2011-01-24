#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar mgmt.jar -n ${SC1_IP_NODEB} -p ${SC1_PO_NODEB} --logging log4j/demoTool.xml
