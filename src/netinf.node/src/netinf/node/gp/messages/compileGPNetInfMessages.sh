#!/bin/bash
PROTO_FILE=netinf/node/gp/GPNetInfMessages.proto

if [ ! -f $PROTO_FILE ]; then
	echo "$PROTO_FILE does not exist. Maybe you have to cd \$REPO/src/netinf.node/src?"
	exit 1
fi

protoc --java_out=. $PROTO_FILE
