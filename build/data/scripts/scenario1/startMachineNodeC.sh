#!/bin/bash
./killJava.sh
./killGP.sh
./clearPastry.sh
./clearLogs.sh
./startGP.sh &
sleep 5
./nodeC.sh &
sleep 7 
./createdefaultios.sh
./wait.sh
