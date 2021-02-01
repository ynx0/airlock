#!/usr/bin/env bash

# shellcheck disable=SC2164
cd "$(dirname "$0")"  # ensure that the current directory is where the script is located
source ./setup_env_lib.sh

########################################
# TODO write description of this script
########################################

# todo update docs

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
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  cleanup "$SHIP" # always start fresh. if we are in the setup, we'll never use a running fakezod directory

  if [[ ! -f "./$SAFE_SHIP-$TAR_SUFFIX" ]]; then
    make_fakeship "$SHIP" "$OTA"
    tar_fakeship "$SHIP"
  else
    untar_fakeship "$SHIP"
  fi

  boot_fakeship "$SHIP"
}

while read -r SHIP; do
  # set up each ship in parallel.
  {
    setup_environment "$SHIP"
    send2ship "$SHIP" "+code^M"
    getLastNLines "$SHIP" 5
    #send2ship "$SHIP" "^X"
    #send2ship "$SHIP" ";create channel /test^M"
    #send2ship "$SHIP" "^X"
  } &
done < "./ships.cfg"

wait  # wait for all ship jobs to complete

echo "Finished setting up environment"