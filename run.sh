#!/bin/bash
  
DIR="$( cd "$( dirname "$0" )" && pwd )"
LOC="$DIR/build/classes/java/main"
COMPILE="$( ps -ef | grep [c]s455.overlay.node.Registry )"

HOST=phoenix
PORT=5001

SCRIPT="cd $LOC; java -cp . cs455.overlay.node.MessagingNode $HOST $PORT"

if [ -z "$COMPILE" ]
then
LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
    echo Project has "$LINES" lines
    gradle clean; gradle build
    gnome-terminal --geometry=132x43 -- bash -c "pushd $LOC; java -cp . cs455.overlay.node.Registry $PORT; popd; bash;"
else
    for ((j=0; j<${1:-1}; j++))
    do
        COMMAND='gnome-terminal'
        for i in `cat machine_list`
        do
            echo 'logging into '$i
            OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
            COMMAND+=" $OPTION"
        done
        eval $COMMAND &
    done
fi
