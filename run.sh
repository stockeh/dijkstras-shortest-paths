#!/bin/bash

#########################################################################
#                                                                       #
# Using Dijkstra’s Shortest Paths to Route Packets in a Network Overlay #
#                                                                       #
#          Jason D Stock - stock - 830635765 - Feb 14, 2019             #
#                                                                       #
#########################################################################

# Configurations

DIR="$( cd "$( dirname "$0" )" && pwd )"
BUILD="$DIR/build/classes/java/main"
COMPILE="$( ps -ef | grep [c]s455.overlay.node.Registry )"

HOST=atlanta
PORT=5001

# Launch Registry

LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
echo Project has "$LINES" lines
gradle clean; gradle build
gnome-terminal --geometry=132x43 -e "ssh -t $HOST 'cd $BUILD; java -cp . cs455.overlay.node.Registry $PORT; bash;'"

sleep 1

# Launch Messaging Nodes

SCRIPT="cd $BUILD; java -cp . cs455.overlay.node.MessagingNode $HOST $PORT"

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
