#!/bin/bash

pushd "`dirname $0`"
if [ ! -f ~/.netinf/settings ]; then
	mkdir ~/.netinf
	cp settings ~/.netinf
fi
ant
cd dist/scripts
./configure.sh
popd
