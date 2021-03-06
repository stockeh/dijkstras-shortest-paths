#!/bin/bash
#
# run script for MacOSX.  Run in the top level directory of project.
# Registry will start in the terminal, and a new window will be opened.
# This new window will spawn MULTI + 1 MessagingNodes.
#

HOST=localhost
PORT=5001
MULTI="1 2 3 4 5 6 7 8 9"

DIR="$( cd "$( dirname "$0" )" && pwd )"
BUILD="$DIR/build/classes/java/main"
COMPILE="$( ps -ef | grep [c]s455.overlay.node.Registry )"

SCRIPT="cd $BUILD; java -cp . cs455.overlay.node.MessagingNode $HOST $PORT;"

function new_tab() {
    osascript \
        -e "tell application \"Terminal\"" \
        -e "tell application \"System Events\" to keystroke \"t\" using {command down}" \
        -e "do script \"$SCRIPT\" in front window" \
        -e "end tell" > /dev/null
}

if [ -z "$COMPILE" ]
then
LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
    echo Project has "$LINES" lines
    gradle clean
    gradle build
    open -a Terminal .
    pushd $BUILD; java -cp . cs455.overlay.node.Registry $PORT; popd;
else
    if [ -n "$MULTI" ]
    then
        for tab in `echo $MULTI`
        do
            new_tab
        done
    fi
    eval $SCRIPT
fi
