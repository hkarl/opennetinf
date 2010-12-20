#!/bin/bash
./killInfox.sh
./killInbird.sh
./killJava.sh
./killGP.sh
./clearSDB.sh
./clearCache.sh
./clearMgmt.sh
./clearLogs.sh
./startGP.sh &
sleep 5
./nodeB.sh &
sleep 5
./mgmtA_2.sh &
./infox.sh &
./inbird.sh
./wait.sh
