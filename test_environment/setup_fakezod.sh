#!/usr/bin/env bash

# shellcheck disable=SC2164
cd "$(dirname "$0")" # ensure that the current directory is the location of the script

source ./setup_env.sh


# this script is responsible for setting up specific things on the ship itself
sleep 1s  # wait for any errors lmao
send2ship "^X"
send2ship ";create channel /test^M"
send2ship "^X"
getLastNLines 5
