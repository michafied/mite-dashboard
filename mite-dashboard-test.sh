#!/usr/bin/env bash

NAME=mite-dashboard-1.4
DEPLOYABLE_HOME=$(pwd)
JAVA=/usr/bin/java
RUNNABLE="${DEPLOYABLE_HOME}/build/libs/${NAME}.jar"

function info {
  logger "${NAME}: $*"
}

function start {
  info "starting ${NAME}"
  ${JAVA} -jar ${RUNNABLE} start \
    --java-opts="-Dlogback.configurationFile=${DEPLOYABLE_HOME}/logback.xml" \
    -conf "${DEPLOYABLE_HOME}/src/test/resources/test-config.json"
}

function stop {
  info "stopping ${NAME}"
  ${JAVA} -jar ${RUNNABLE} stop "$1"
}

function status {
  ${JAVA} -jar ${RUNNABLE} list
}

function is_running {
  RUNNING_UUID=$(status | grep "${RUNNABLE}" | awk '{print $1}')
  if [[ "x${RUNNING_UUID}" = "x" ]]; then
    info "${NAME} is not running"
  else
    info "${NAME} is running"
  fi
  echo "${RUNNING_UUID}"
}

case $1 in
  start)
    ID=$(is_running)
    [[ "x$ID" = "x" ]] && start
    ;;
  stop)
    ID=$(is_running)
    [[ "x$ID" != "x" ]] && stop "$ID"
    ;;
  status)
    status
    ;;
  restart)
    ID=$(is_running)
    [[ "x$ID" != "x" ]] && stop "$ID"
    sleep 3
    start
    ;;
  *)
    echo "usage: $0 [start/stop/restart/status]"
    ;;
esac
