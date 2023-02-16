#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`
script=`basename $0`
user=$USER
epoch=`date +%s`

function usage() {
  echo "Application lifecycle management tool for TrafficAnalytics app."
  echo "Usage: $script --action <deploy|start|stop|status> [--gateway <hostname>]"
  echo ""
  echo "  Options"
  echo "    --action    Specifies the action to be taken on the TrafficAnalytics application."
  echo "    --gateway   Specifies the hostname the gateway is running on.(Default: localhost)"
  echo "    --help      This help message"
  echo ""
}

function deploy_action() {
  local app=$1; shift;
  local jar=$1; shift;
  local gateway=$1; shift;

  echo "Deploying application $app..."
  status=`curl -o /dev/null -sL -w "%{http_code}\\n" -H "X-Archive-Name: $app" -X POST http://$gateway:10000/v2/apps --data-binary @"$jar"`
  if [ $status -ne 200 ]; then
    echo "Failed to deploy app"
    exit 1;
  fi

  echo "Deployed."
}

function program_action() {
  local app=$1; shift;
  local program=$1; shift;
  local type=$1; shift;
  local action=$1; shift;
  local gateway=$1; shift

  http="-X POST"
  if [ "x$action" == "xstatus" ]; then
    http=""
  fi

  maction="$(tr '[:lower:]' '[:upper:]' <<< ${action:0:1})${action:1}"
  echo " - ${maction/Stop/Stopp}ing $type $program... "

  status=$(curl -s $http http://$gateway:10000/v2/apps/$app/${type}s/$program/$action 2>/dev/null)

  if [ $? -ne 0 ]; then
   echo "Action '$action' failed."
  else
    if [ "x$action" == "xstatus" ]; then
      echo $status
    fi
  fi

}

if [ $# -lt 1 ]; then
  usage
  exit 1
fi

gateway="localhost"
action=
while [ $# -gt 0 ]
do
  case "$1" in
    --action) shift; action="$1"; shift;;
    --gateway) shift; gateway="$1"; shift;;
    *)  usage; exit 1
  esac
done


if [ "x$action" == "x" ]; then
  usage
  echo
  echo "Action not specified."
fi

app="TrafficAnalytics"
app_jar="TrafficAnalytics-1.0.jar"

if [ "x$action" == "xdeploy" ]; then
  jar_path="target/$app_jar"
  deploy_action $app $jar_path $gateway
else
  program_action $app "RequestCountFlow" "flow" $action $gateway
  program_action $app "LogCountProcedure" "procedure" $action $gateway
fi
