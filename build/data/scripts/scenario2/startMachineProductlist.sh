#!/bin/bash
cd `dirname "$0"`
./clearLogs.sh
./killJava.sh
./productlistcheckout.sh &
./productlistgirlfriend.sh &
./wait.sh
