#!/usr/bin/env bash

PLATFORM=linux64
VERSION_NUM=v1.0-rc1
URBIT_VERSION=urbit-$VERSION_NUM-$PLATFORM # example output: urbit-v1.0-rc1-linux64
TAR_SUFFIX=init.tar.gz
LOGFILE_SUFFIX=output.log
OTA_PATH=./urbit

#cd test_environment || exit

### N.B. all functions in this file should be stateless, and accept any state as parameters

die() {
  echo "$*" 1>&2
  exit 1
}

function downloadUrbitRuntime() {
  echo "Downloading Urbit Runtime $URBIT_VERSION"
  curl -o $URBIT_VERSION.tgz https://bootstrap.urbit.org/$URBIT_VERSION.tgz # force filename to be $URBIT_VERSION
  # we need to strip one directory inside and just get the binary files directly,
  # then extract them to the known folder name using the -C flag
  # this is because there is inconsistent naming of the tgz vs the internal folder so the script breaks without this
  # from https://unix.stackexchange.com/a/11019
  mkdir $URBIT_VERSION
  tar xzvf $URBIT_VERSION.tgz -C $URBIT_VERSION --strip-components 1
}

function downloadLatestOTA() {
  echo "Downloading latest OTA"
  # this function is necessary for fakeships because they are created from the latest boot pill, not the latest ota
  # however, if you are targeting the latest ota, you will not be able to get it because of the fact that you are a fakeship
  # this function clones urbit/urbit
  # # #
  #  curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash  # uncomment for latest version
  sudo apt-get install git-lfs
  git lfs install --skip-repo # need lfs to properly clone boot pill
  git clone --depth 1 --branch master https://github.com/urbit/urbit $OTA_PATH
}

function safepatp() {
  # this function removes any invalid characters from a ship's name
  # that make it unsuitable for a session name in screen/bash.
  # this ends up being only the sig character
  echo "${1//[~]/""}" # in string $1, substitute any "~" with an empty string
}

# MARK - live ship management
function start_ship() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  screen -d -m -S "$SAFE_SHIP" -L -Logfile "$SAFE_SHIP-$LOGFILE_SUFFIX" ./$URBIT_VERSION/urbit "$SAFE_SHIP"
}

function send2ship() {
  # $1 = patp of desired ship
  # $2 = input to send
  local SHIP INPUT SAFE_SHIP
  SHIP="$1"
  INPUT="$2"
  SAFE_SHIP=$(safepatp "$SHIP")

  # this command reads as:
  # with the session $SAFE_SHIP, window 0, send the stuff command, with the string $INPUT.
  # read the manpage for the input format that the `stuff` command expects
  screen -S "$SAFE_SHIP" -p 0 -X stuff "$INPUT"
}

function getLastNLines() {
  # $1 = patp of desired ship
  # $2 = number of lines to get
  local SHIP N SAFE_SHIP
  SHIP="$1"
  N="$2"
  SAFE_SHIP=$(safepatp "$SHIP")

  # shellcheck disable=SC2005
  echo "$(tail -n"$N" "$SAFE_SHIP-$LOGFILE_SUFFIX")"
}

function wait4boot() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  echo "Waiting for $SHIP to boot: "
  until [[ "$(tail -n1 "$SAFE_SHIP-$LOGFILE_SUFFIX")" =~ $SHIP":dojo>" ]]; do
    getLastNLines "$SHIP" 2
    sleep 3s
  done
}

function killShipSession() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  screen -X -S "$SAFE_SHIP" quit 2>/dev/null # ok if it doesn't exist
  # if ship is null then everything will be killed ...
}

# MARK - ship creation/deletion + boot
function make_fakeship() {
  # $1 = patp of desired ship
  # $2 = perform manual ota?
  local SHIP OTA SAFE_SHIP
  SHIP="$1"
  OTA="$2"
  SAFE_SHIP="$(safepatp "$SHIP")"

  rm -rf "./$SAFE_SHIP" # remove if existing fakeship
  echo "Creating fake $SHIP"

  # screen command adapted from https://stackoverflow.com/a/15026227

  if [[ $OTA == true ]]; then
    echo "Using latest ota"
    [[ ! -d $OTA_PATH ]] && die "Could not find folder containing urbit repo"
    screen -d -m -S "$SAFE_SHIP" -L -Logfile "$SAFE_SHIP-$LOGFILE_SUFFIX" ./$URBIT_VERSION/urbit -F "$SAFE_SHIP" -B "urbit/bin/solid.pill" -A "urbit/pkg/arvo"
  else
    screen -d -m -S "$SAFE_SHIP" -L -Logfile "$SAFE_SHIP-$LOGFILE_SUFFIX" ./$URBIT_VERSION/urbit -F "$SAFE_SHIP"
  fi

  wait4boot "$SHIP"
  sleep 3s # wait for all fakeships on local network to properly poke/ack. should prevent some errors from smudging up the event log.
  # but it didn't. weird. getting a poke-ack on nus. todo investigate this later
  echo "Fake $SHIP created"
  send2ship "^D"
  sleep 3s
}

function boot_fakeship() {
  # $1 = patp of desired ship
  local SHIP
  SHIP="$1"

  start_ship "$SHIP"
  wait4boot "$SHIP"
  echo "Booted fake $SHIP"
}

function tar_fakeship() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  echo "Saving pristine fake $SHIP state"
  if [ -d "./$SAFE_SHIP" ]; then
    # todo figure out what to do with .http.ports and .vere.lock. doesn't seem harmful to leave them right now
    # i think .http.ports could honestly be handy if you want to always have them bound to the same port.
    # you'd have to do that before saving the pristine. but .vere.lock seems useless
    #        rm "./$SAFE_SHIP/.urb/.http.ports"
    #        rm "./$SAFE_SHIP/.urb/.vere.lock"
    tar cvzf "$SAFE_SHIP-$TAR_SUFFIX" "$SAFE_SHIP"
  else
    die "Could not save ./$SHIP. Directory does not exist"
  fi
}

function untar_fakeship() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  echo "Unzipping pristine fake $SHIP"
  tar xvf "./$SAFE_SHIP-$TAR_SUFFIX"
}

function cleanup() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  killShipSession "$SHIP"
  mkdir -p ./old_logs
  mv "$SAFE_SHIP-$LOGFILE_SUFFIX" "./old_logs/$SAFE_SHIP-${LOGFILE_SUFFIX}_$(date -Iminutes).old.log" >>/dev/null 2>&1
  rm -rf "./$SAFE_SHIP" >>/dev/null 2>&1 # remove the non-compressed pier
  rm -f $URBIT_VERSION.tgz               # remove urbit runtime zipfile
}

# assuming eyre will be live on 8080 b/c port 80 is not available by default

# references
# inspired by https://github.com/asssaf/urbit-fakezod-docker/
# https://urbit.org/using/install/
# https://raymii.org/s/snippets/Sending_commands_or_input_to_a_screen_session.html
# https://urbit.org/using/operations/using-your-ship/#chat-management
