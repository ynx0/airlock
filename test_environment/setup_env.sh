#!/usr/bin/env bash

# shellcheck disable=SC2164
cd "$(dirname "$0")"  # ensure that the current directory is where the script is located
source ./setup_env_lib.sh

########################################
# this script sets up a pristine fakezod
########################################

REBUILD=false
OTA=false

cleanup  # always start fresh. if we are in the setup, we'll never use a running fakezod directory

# download the urbit runtime if it doesn't exist
if [ ! -d "./$URBIT_VERSION/" ]; then
  downloadUrbitRuntime
fi

if [[ $OTA == true ]]; then
  downloadLatestOTA
fi


if [[ $REBUILD == true || ! -f ./$FAKEZOD_TAR ]]; then
  echo "REBUILD: $REBUILD"
  make_fakezod
  tar_fakezod_state
else
  untar_fakezod_state
fi

boot_fakezod
