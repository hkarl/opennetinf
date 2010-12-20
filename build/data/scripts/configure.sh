#!/bin/bash
source ~/.netinf/settings


echo "This script configures the compiled distribution in ${DP_LOC}"

if [ "x$1" == "x" ]; then
	echo "Usage: $0 <mode> where <mode> is one of"
	echo "  sc1a, sc1b, sc1c - for three nodes of scenario 1"
	echo "  sc2 - for scenario 2 which runs on one node"
	echo "  sci - for the integration scenario"
	exit 1
fi
