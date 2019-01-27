#!/bin/bash
#
# run script for MacOSX.  Run in the top level directory of project.
# Registry will start in the terminal, and a new window will be opened.
# This new window will spawn MULTI + 1 MessagingNodes.
#

PORT=5001
HOST=localhost
MULTI="1 2"

DIR="$( cd "$( dirname "$0" )" && pwd )"
COMPILE="$( ps -ef | grep [c]s455.overlay.node.Registry )"

function new_tab() {
    COMMAND="cd $DIR/src; java cs455.overlay.node.MessagingNode $HOST $PORT;"
    osascript \
        -e "tell application \"Terminal\"" \
        -e "tell application \"System Events\" to keystroke \"t\" using {command down}" \
        -e "do script \"$COMMAND\" in front window" \
        -e "end tell" > /dev/null
}

if [ -z "$COMPILE" ]
then
    make
    open -a Terminal .
    cd $DIR/src; java cs455.overlay.node.Registry $PORT
else
    if [ -n "$MULTI" ]
    then
        for tab in `echo $MULTI`
        do
            new_tab
        done
    fi
    cd $DIR/src; java cs455.overlay.node.MessagingNode $HOST $PORT;
fi
