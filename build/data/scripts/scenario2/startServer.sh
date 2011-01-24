#!/bin/bash
./killJava.sh
./clearLogs.sh
./mysqlServer.sh
./logging.sh &
sleep 5
./eventservice.sh &
sleep 5 
./nodeglobalrs.sh &
sleep 5
./searchservice.sh &
sleep 5
./createdefaultiosServer.sh
./wait.sh
