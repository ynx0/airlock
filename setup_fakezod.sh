#!/usr/bin/env bash
# taken from https://urbit.org/using/install/
mkdir urbit
cd urbit || exit
curl -O https://bootstrap.urbit.org/urbit-v0.10.8-linux64.tgz
tar xzf urbit-v0.10.8-linux64.tgz
cd ./urbit-v0.10.8-linux64/ || exit
screen -d -m -S fakezod ./urbit -F zod
# from https://raymii.org/s/snippets/Sending_commands_or_input_to_a_screen_session.html
# https://urbit.org/using/operations/using-your-ship/#chat-management
screen -S fakezod -p 0 -X stuff "^X"
screen -S fakezod -p 0 -X stuff ";create channel /test"
# assuming eyre will be live on 8080
# todo use urbit/herb
