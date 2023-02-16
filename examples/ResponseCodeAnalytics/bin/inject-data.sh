#!/usr/bin/env bash

#
# Copyright 2012-2014 Continuuity, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`
script=`basename $0`

function usage() {
  echo "Tool for sending data to the ResponseCodeAnalytics"
  echo "Usage: $script [--host <hostname>]"
  echo ""
  echo "  Options"
  echo "    --host      Specifies the host that Reactor is running on. (Default: localhost)"
  echo "    --help      This help message"
  echo ""
}

gateway="localhost"
stream="logEventStream"
  while [ $# -gt 0 ]
  do
    case "$1" in
      --host) shift; gateway="$1"; shift;;
      *)  usage; exit 1
     esac
  done
OLD_IFS=IFS
IFS=$'\n'
lines=`cat "$bin"/../resources/apache.accesslog`
for line in $lines
do
  curl -X POST -d "$line" http://$gateway:10000/v2/streams/$stream;
done
IFS=$OLD_IFS
