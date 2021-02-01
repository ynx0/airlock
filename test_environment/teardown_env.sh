#!/usr/bin/env bash

# shellcheck disable=SC2164
cd "$(dirname "$0")"
source ./setup_env_lib.sh

#############################################
# this script is responsible for tearing down
# the environment after testing is complete
#############################################

while read -r SHIP; do
  {
    send2ship "^D"
    sleep 3s  # give it time to exit
    cleanup "$SHIP"
  } &

done < "./ships.cfg"

wait  # wait for each job to complete

echo "Finished tearing down environment"