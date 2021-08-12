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
if [ $OTA == true ] && [ ! -d "./$OTA_PATH" ]; then
  downloadLatestOTA
fi


function setup_environment() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  cleanup "$SHIP" # always start fresh. if we are in the setup, we'll never use a running pier

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
    send2ship "$SHIP" "+code^M^M"
    getLastNLines "$SHIP" 5
  } &
done < "./ships.cfg"

wait  # wait for all ship jobs to complete

### Write your setup code below ###

# on zod: create a group called test-group, inviting ~nus
send2ship "~zod" ":contact-view &contact-view-action [%create %test-group [%invite (sy ~[~nus])] 'Test Group' 'This is a test group']^M"
# on nus: join the resource ~zod/test-group
#send2ship "~nus" ":contact-view &contact-view-action [%join ~zod %test-group]^M"

echo "Finished setting up environment"