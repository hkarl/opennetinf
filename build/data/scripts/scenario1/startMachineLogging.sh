#!/bin/bash
cd `dirname "$0"`
./killJava.sh
./clearLogs.sh
./logging.sh
./wait.sh
