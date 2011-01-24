#!/bin/bash
source ~/.netinf/settings
cd ${DP_LOC}/jar/ &> /dev/null
java -jar productlist.jar CHECKOUT netinf.tools.productlist.scenario2.ProductListCheckoutModule
