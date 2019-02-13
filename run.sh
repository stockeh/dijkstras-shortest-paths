#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
registry=phoenix
registry_port=5001

gradle clean; gradle build;

dbus-launch gnome-terminal --geometry=105x72 -x bash -c "cd ${DIR}/build/classes/java/main; java cs455.overlay.node.Registry ${registry_port}"

sleep .5

for i in `cat machine_list`;
do
	echo 'logging into '${i}
	dbus-launch gnome-terminal -x bash -c "ssh -t ${i} 'cd ${DIR}/build/classes/java/main; java cs455.overlay.node.MessagingNode ${registry} ${registry_port};bash;'" &
done
