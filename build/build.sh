#!/bin/bash

pushd "`dirname $0`"
if [ ! -d ~/.netinf ]; then
	mkdir ~/.netinf
fi

if [ ! -f ~/.netinf/settings ]; then
	cp settings ~/.netinf
fi

ant
cd dist/scripts
./configure.sh

source ~/.netinf/settings

if [ ! -L ~/netinf/ManagementToolIdentifiers ]; then
	ln -s ${DP_LOC}/ManagementToolIdentifiers ~/.netinf/ManagementToolIdentifiers
fi

popd
