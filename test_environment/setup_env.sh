#!/usr/bin/env bash


# shellcheck disable=SC2164
cd "$(dirname "$0")"


REBUILD=false
source ./setup_env_lib.sh

# this script sets up a pristine fakezod
if [ ! -d "./$URBIT_VERSION/" ]; then
  downloadUrbitRuntime
fi

if [[ $REBUILD == true || ! -f ./$FAKEZOD_TAR ]]; then
  echo "REBUILD: $REBUILD"
  make_fakezod
  tar_fakezod_state
else
  untar_fakezod_state
fi

boot_fakezod
