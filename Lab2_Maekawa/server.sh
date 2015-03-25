#!/bin/sh

# pass the amount of ms to wait as the first argument. E.g.:
# ./server.sh 8000
# 
cd bin
java org.da.impl.ServerImpl $1
