#!/bin/bash

tmpdir=~/.
jar=$2
jarsize=$(wc -c $jar | xargs | cut -d' ' -f 1)
secs=$(date +"%s")
extractedJar="/tmp/tmpcli-$secs.jar"
echo "Creating script, jar file size: $jarsize"
SCRIPT=$'#!/bin/bash

# Copyright (c) 2000-2014 hybris AG
# All rights reserved.
#
# This software is the confidential and proprietary information of hybris
# ("Confidential Information"). You shall not disclose such Confidential
# Information and shall use it only in accordance with the terms of the
# license agreement you entered into with hybris.

currentScript=${BASH_SOURCE[0]}
tail -c '
SCRIPT+=$jarsize
SCRIPT+=$' $currentScript > '
SCRIPT+=$extractedJar
SCRIPT+=$' 2>/dev/null
'

SCRIPT+=$'CMD=$@
'

SCRIPT+='java -jar '
SCRIPT+="$extractedJar "
SCRIPT+=$'${CMD[@]:0}
'
SCRIPT+='ex=$?
'
SCRIPT+="rm -f $extractedJar"

SCRIPT+=$'\nexit $ex
#PAYLOAD
'

echo "$SCRIPT" > $1
chmod 755 $1

cat $jar >> $1 


