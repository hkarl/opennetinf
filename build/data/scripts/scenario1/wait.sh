#!/bin/bash
A=0
#echo "waiting for (X)"
while read A; do
	if [ "x$A" == "xX" ]; then
		exit
	fi
done
