#!/usr/bin/env bash

PLATFORM=linux64
VERSION_NUM=v1.0-rc1
URBIT_VERSION=urbit-$VERSION_NUM-$PLATFORM  # example output: urbit-v1.0-rc1-linux64
FAKEZOD_TAR=fakezod-init.tar.gz
LOGFILE=fakeship_output.log
OTA_PATH=./urbit

#cd test_environment || exit

die() { echo "$*" 1>&2 ; exit 1; }

function downloadUrbitRuntime() {
  echo "Downloading Urbit Runtime"
  curl -o $URBIT_VERSION.tgz https://bootstrap.urbit.org/$URBIT_VERSION.tgz  # force filename to be $URBIT_VERSION
  # we need to strip one directory inside and just get the binary files directly,
  # then extract them to the known folder name using the -C flag
  # this is because there is inconsistent naming of the tgz vs the internal folder so the script breaks without this
  # from https://unix.stackexchange.com/a/11019
  mkdir $URBIT_VERSION
  tar xzvf $URBIT_VERSION.tgz -C $URBIT_VERSION --strip-components 1
}

function downloadLatestOTA() {
  # this function is necessary for fakezods because they are created from the latest boot pill, not the latest ota
  # however, if you are targeting the latest ota, you will not be able to get it because of the fact that you are a fake ship
  # this function clones urbit/urbit
  git clone --depth 1 --branch master https://github.com/urbit/urbit $OTA_PATH
}


# MARK - live ship management
function start_ship() {
  screen -d -m -S fakeship -L -Logfile "$LOGFILE" ./$URBIT_VERSION/urbit zod
}

function send2ship() {
  # read the manpage for the input format that screen expects
  screen -S fakeship -p 0 -X stuff "$1"
}

function getLastNLines() {
  # shellcheck disable=SC2005
  echo "$(tail -n"$1" fakeship_output.log)"
}

function wait4boot() {
  until [[ "$(tail -n1 fakeship_output.log)" =~ "~zod:dojo>" ]]; do
    echo "Waiting for zod to boot: "
    getLastNLines 2
    sleep 10s
  done
}

function killShip() {
  screen -S fakeship -X quit 2> /dev/null # ok if it doesn't exist
}

# MARK - ship creation/deletion + boot
function make_fakezod() {
  rm -rf ./zod  # remove if existing fakezod
  echo "Creating fakezod"
  # screen command adapted from https://stackoverflow.com/a/15026227
  if [[ "$OTA" == true ]]; then
    [[ ! -f $OTA_PATH ]] && die "Could not find folder containing urbit repo"
    echo "Using latest ota"
    screen -d -m -S fakeship -L -Logfile "$LOGFILE" ./$URBIT_VERSION/urbit -F zod -B "urbit/bin/solid.pill" -A "urbit/pkg/arvo"
  else
    screen -d -m -S fakeship -L -Logfile "$LOGFILE" ./$URBIT_VERSION/urbit -F zod
  fi
  wait4boot
  echo "Fakezod created"
  send2ship "^D"
  sleep 5s
}

function boot_fakezod() {
  start_ship
  wait4boot
  echo "Booted fakezod"
}

function tar_fakezod_state() {
  echo "Saving pristine fakezod state"
  if [ -d ./zod ]; then
#    rm ./zod/.urb/.http.ports
#    rm ./zod/.urb/.vere.lock
    tar cvzf $FAKEZOD_TAR zod
  else
    echo "Could not save ./zod. Does not exist"
  fi
}

function untar_fakezod_state() {
  echo "Unzipping existing fakezod"
  tar xvf ./$FAKEZOD_TAR
}


function cleanup() {
  killShip
  mkdir -p ./old_logs
  mv "$LOGFILE" "./old_logs/${LOGFILE}_$(date -Iminutes).old.log"
  rm -rf ./zod >> /dev/null 2>&1
  rm -f $URBIT_VERSION.tgz
}

# assuming eyre will be live on 8080 b/c port 80 is not available by default

# references
# inspired by https://github.com/asssaf/urbit-fakezod-docker/
# https://urbit.org/using/install/
# https://raymii.org/s/snippets/Sending_commands_or_input_to_a_screen_session.html
# https://urbit.org/using/operations/using-your-ship/#chat-management
