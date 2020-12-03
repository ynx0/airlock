#!/usr/bin/env bash

# download and install the urbit runtime
mkdir urbit
cd urbit
curl -O https://bootstrap.urbit.org/urbit-v0.10.8-linux64.tgz
tar xzf urbit-v0.10.8-linux64.tgz
cd ./urbit-v0.10.8-linux64/

# start a new screen session while writing the output of the process to a log file
screen -d -m -S fakeship -L -Logfile "./fakeship_output.log" ./urbit -F zod

# wait until zod boots
until [[ "$(tail -n1 fakeship_output.log)" =~ "~zod:dojo>" ]]; do
  echo "Waiting for zod to boot: "
  echo tail -n3 fakeship_output.log
  sleep 10s
done


function send2ship() {
  screen -S fakeship -p 0 -X stuff "$1"
}

# send input to the dojo
send2ship "^X"
send2ship ";create channel /test^M"

# references
# https://stackoverflow.com/a/15026227
# https://urbit.org/using/install/
# https://raymii.org/s/snippets/Sending_commands_or_input_to_a_screen_session.html
# https://urbit.org/using/operations/using-your-ship/#chat-management
# assuming eyre will be live on 8080 b/c port 80 is not available by default
