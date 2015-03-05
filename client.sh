#!/bin/bash

function boot {
	fname="../log/client_$1.log"
	java org.da.impl.ClientImpl > $fname &
	echo "Starting client $1, with output to $fname"
}

cd bin
for i in $(seq 1 $1); do boot $i; done
wait

