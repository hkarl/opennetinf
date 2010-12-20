#!/bin/bash
source ~/.netinf/settings
mv ~/.netinf/settings ~/.settingsTMP
rm -rf ${HM_LOC}/.netinf &> /dev/null
cp -r ${DP_LOC}/netinf ${HM_LOC}/.netinf &> /dev/null
mv ~/.settingsTMP ~/.netinf/settings
