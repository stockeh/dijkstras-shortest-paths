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
JAR_PATH="$DIR/build/libs/dijkstras-shortest-paths.jar"
COMPILE="$( ps -ef | grep [c]s455.overlay.node.Registry )"
MACHINE_LIST="$DIR/machine_list"

HOST=atlanta
PORT=5001

# Launch Registry

LINES=`find . -name "*.java" -print | xargs wc -l | grep "total" | awk '{$1=$1};1'`
echo Project has "$LINES" lines
gradle clean; gradle build
gnome-terminal --geometry=132x43 -e "ssh -t $HOST 'cd $DIR; java -cp $JAR_PATH cs455.overlay.node.Registry $PORT; bash;'"

sleep 1

# Launch Messaging Nodes

SCRIPT="java -cp $JAR_PATH cs455.overlay.node.MessagingNode $HOST $PORT"
COMMAND='gnome-terminal --geometry=200x40'

for machine in `cat $MACHINE_LIST`
do
   OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
   COMMAND+=" $OPTION"
done
eval $COMMAND &
