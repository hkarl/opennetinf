#!/bin/bash
cd `dirname "$0"`
./shoppinghans.sh &
sleep 2
./shoppingpeter.sh &
./wait.sh
