#!/bin/bash
./clearLogs.sh
./killJava.sh
./productlistcheckout.sh &
./productlistgirlfriend.sh &
./wait.sh
