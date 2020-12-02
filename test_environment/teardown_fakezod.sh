#!/usr/bin/env bash

# shellcheck disable=SC2164
cd "$(dirname "$0")"
source ./setup_env_lib.sh

# this script is responsible for tearing down the environment after testing is complete

send2ship "^D"
sleep 3s
cleanup
