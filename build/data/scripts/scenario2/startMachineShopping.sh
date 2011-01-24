#!/bin/bash
cd `dirname "$0"`
./killJava.sh
./clearLogs.sh
./shoppinghans.sh &
sleep 2
./shoppingpeter.sh &
./wait.sh
