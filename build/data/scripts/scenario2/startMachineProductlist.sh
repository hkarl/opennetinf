#!/bin/bash
cd `dirname "$0"`
./productlistcheckout.sh &
./productlistgirlfriend.sh &
./wait.sh
