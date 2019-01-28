#!/bin/bash

PORT=5001
HOST=localhost

DIR="$( cd "$( dirname "$0" )" && pwd )"
COMPILE="$( ps -ef | grep [c]s455.overlay.node.Registry )"

if [ -z "$COMPILE" ]
then
  make
  open -a Terminal .
  cd "$DIR"/src
  java cs455.overlay.node.Registry "$PORT"
else
  cd "$DIR"/src
  java cs455.overlay.node.MessagingNode "$HOST" "$PORT"
fi
