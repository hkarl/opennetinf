#!/bin/bash
source ~/.netinf/settings
killall lt-formux-netinf-entity &> /dev/null
sleep 2 &> /dev/null
killall -9 lt-formux-netinf-entity &> /dev/null
