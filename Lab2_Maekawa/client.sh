#!/bin/bash

#first arg: amount of clients
#second arg: amount of messages
#third arg: amount of rounds before quiting

msgs=$2
rounds=$3

function boot {
	fname="../log/client_$1.log"
	java org.da.impl.ClientImpl $msgs $rounds &> $fname &
	echo "Starting client $1, with output to $fname"
}

cd bin
for i in $(seq 1 $1); do boot $i; done
wait

