#!/bin/bash
source ~/.netinf/settings
sed -i "s|^cc.tcp.port = .*$|cc.tcp.port = ${SC2_PO_RS}|g" "../../configs/createIOs.properties"
cd ${DP_LOC}/jar
java -jar createdefaultios.jar
