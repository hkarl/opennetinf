#!/bin/bash
cd `dirname "$0"`
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
./nodeA.sh &
sleep 5
./mgmtA.sh &
./infox.sh &
./wait.sh
