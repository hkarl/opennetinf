#!/bin/bash
cd `dirname "$0"`
./killInfox.sh
./killInbird.sh
./killJava.sh
./killGP.sh
./clearSDB.sh
./clearCache.sh
./clearLogs.sh
./startGP.sh &
sleep 5
./nodeB.sh &
sleep 5
./mgmtA_2.sh &
./infox.sh &
./inbird.sh
./wait.sh
