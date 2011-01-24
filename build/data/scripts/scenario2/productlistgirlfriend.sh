#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar productlist.jar GIRLFRIEND netinf.tools.productlist.scenario2.ProductListGirlfriendPeterModule
