#!/usr/bin/env bash

source ./setup_env.sh

# this script is responsible for setting up specific things on the ship itself

send2ship "^X"
send2ship ";create channel /test^M"
echo "$(getLastNLines 5)"
send2ship "^X"
send2ship "^D"
sleep 3s

cleanup
