#!/bin/bash

#first arg: amount of clients
#second arg: amount of rounds before quiting

rounds=$2

function boot {
	fname="../log/client_$1.log"
	java impl.Client2 $rounds > $fname &
	echo "Starting client $1, with output to $fname"
}

cd bin
for i in $(seq 1 $1); do boot $i; done
wait
