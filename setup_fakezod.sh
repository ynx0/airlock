#!/usr/bin/env bash
# taken from https://urbit.org/using/install/
mkdir urbit
cd urbit || exit
curl -O https://bootstrap.urbit.org/urbit-v0.10.8-linux64.tgz
tar xzf urbit-v0.10.8-linux64.tgz
cd ./urbit-v0.10.8-linux64/ || exit

#PATP=${1:-"zod"}
#PATP="zod"
screen -v # sanity check
screen -d -m -S fakeship -L -Logfile "./fakeship_output.log" ./urbit -F zod  # https://stackoverflow.com/a/15026227
# shellcheck disable=SC2076
until [[ "$(tail -n1 fakeship_output.log)" =~ "~zod:dojo>" ]]; do
  echo "Waiting for zod to boot"
  sleep 10s
done

# from https://raymii.org/s/snippets/Sending_commands_or_input_to_a_screen_session.html
# https://urbit.org/using/operations/using-your-ship/#chat-management
screen -S fakeship -p 0 -X stuff "^X"
screen -S fakeship -p 0 -X stuff ";create channel /test"
# assuming eyre will be live on 8080
# todo use urbit/herb
