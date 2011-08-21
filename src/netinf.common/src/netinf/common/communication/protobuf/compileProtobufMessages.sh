#!/bin/bash
PROTO_FILE=netinf/common/communication/protobuf/ProtobufMessages.proto

if [ ! -f $PROTO_FILE ]; then
	echo "$PROTO_FILE does not exist. Maybe you have to cd \$REPO/src/netinf.common/src?"
	exit 1
fi

protoc --java_out=. $PROTO_FILE
protoc --version
