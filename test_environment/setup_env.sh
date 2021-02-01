#!/usr/bin/env bash

# shellcheck disable=SC2164
cd "$(dirname "$0")"  # ensure that the current directory is where the script is located
source ./setup_env_lib.sh

########################################
# TODO write description of this script
########################################

set -x

# for now, ota is all or nothing. I can't imagine a (sane) scenario yet where you need some ships to be OTA'd but others not
# it would pretty simple to add per-ship ota setting to ships.cfg when needed
OTA=true

# download the urbit runtime if it doesn't exist
if [ ! -d "./$URBIT_VERSION" ]; then
  downloadUrbitRuntime
fi

# download the urbit runtime if it doesn't exist
if [ ! -d "./$OTA_PATH" ]; then
  downloadLatestOTA
fi


function setup_environment() {
  # $1 = patp of desired ship
  local SHIP
  SHIP="$1"

  cleanup "$SHIP" # always start fresh. if we are in the setup, we'll never use a running fakezod directory

  if [[ ! -f ./$FAKEZOD_TAR ]]; then
    make_fakezod "$SHIP" "$OTA"
    tar_fakezod_state "$SHIP"
  else
    untar_fakezod_state "$SHIP"
  fi

  boot_fakezod "$SHIP"
}

while read -r SHIP; do
  # set up each ship in parallel.
  {
    setup_environment "$SHIP"
    send2ship "$SHIP" "(add 2 2)^M"
    getLastNLines "$SHIP" 5
    #send2ship "$SHIP" "^X"
    #send2ship "$SHIP" ";create channel /test^M"
    #send2ship "$SHIP" "^X"
  } &
done < "./ships.cfg"

wait  # wait for all ship jobs to complete

echo "Finished setting up environment"