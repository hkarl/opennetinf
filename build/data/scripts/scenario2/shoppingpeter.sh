#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar shopping.jar netinf.tools.shopping.scenario2.ShoppingPeterModule
