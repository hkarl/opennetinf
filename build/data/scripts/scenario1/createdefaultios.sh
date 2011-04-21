#!/bin/bash
source ~/.netinf/settings
sed -i "s|^cc.tcp.port = .*$|cc.tcp.port = ${SC1_PORT_NODEC}|g" "${DP_LOC}/configs/createIOs.properties" &> /dev/null
cd ${DP_LOC}/jar &> /dev/null
java -jar createdefaultios.jar &> /dev/null
