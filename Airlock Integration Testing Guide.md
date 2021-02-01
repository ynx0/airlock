# Airlock Integration Testing Guide

This guide will show you how to set up a minimal testing environment in an automated fashion (i.e. in a CI/CD pipeline) 
using a set of utility scripts which require no outside dependencies, with quick setup process and simple to use interface.

Although tools such as [herb](https://github.com/urbit/urbit/tree/master/pkg/herb) exist, they usually require many extra steps and dependencies which can make the process cumbersome.
Herb requires cloning in the urbit repository and building the tool from nix, which makes the build process heavier if the goal is to run it a CI platform.
The following process exists as a lightweight alternative.


## Table of Contents
1. [Rationale](#rationale)
2. [Methods](#overview-of-methods)
3. [Implementation](#implementation)
    - [Core](#core)
    - [Boot From Scratch](#method-1---boot-from-scratch)
    - [Boot From Cache](#method-2---boot-from-cache)
4. [Usage](#using-the-scripts---integrating-with-github-actions)
5. [Wrapping Up](#wrapping-up)


## Rationale 

(Feel free to skip this section if you simply want to get started)

Recently, I started work on an Airlock implementation in Java.
I've been lurking around on Urbit for a while, but I still don't have a great grasp of 
the Hoon language and the internals of the operating system.
 
As a result, trying to write software that interfaces with a ship becomes quite tricky. 
I'm essentially working blindly and treating the ship as a black box.

Gaining complete understanding of something as big as Urbit is a monstrous task in and of itself, so instead I chose a different tactic: writing integration tests. Even though I didn't have knowledge about the internals of the system, as long as I could construct inputs with known outputs, 
I would be able to verify that any code that I wrote was valid. Using this approach yielded good results, and allowed me to quite productive while developing the library.

The main benefits of writing integration tests are pretty much the same regardless of the project, but specifically, they allowed me to:
- **Stay Organized**: A single `Main.java` file gets messy fast. Using a testing framework helped me separate 
  the functionality into isolated components at the code level.
 
- **Ensure Correct Behavior Throughout the Codebase**: I never had to guess whether any of the functionality that my library provided would work. My test harness allowed me to avoid leaving major parts of my library in a vague, untested state.

- **Develop Incrementally**: Because running the tests (locally) takes less than a few seconds,
it is cheap to run them every time even a small change is made to the code. 
In this way, I was able to gradually introduce a new feature in chunks and find failures quicker.  

- **Develop Courageously**: Once I had some basic tests set up, I did not feel scared to make major changes because I knew that 
    I would be able to immediately see the diff between the working and failing piece of code and pinpoint exactly what change caused the failure.


Now, to be able to write integration tests, there are two key components:
* Mechanically writing the tests in the language
* Setting up the testing environment

The mechanical act of writing the tests is largely language dependent, so I can't really speak about it here,
but generally speaking, the two general ideas I had in mind were:

* **Principle of Coverage**: making sure any major new api or functionality that is introduced is used at least once in a test 
* **Real World Use**: thinking along the lines of how a consumer of the library would use it can be a pattern to coming up with test cases. This is probably less important, though. 

In this guide, I'll focus instead on the common aspect, which is setting up the testing environment. 

## Overview of Methods

There are two methods to setting up the environment.
 **Boot From Scratch** and **Boot From Cache**

Both methods:
* Download the urbit runtime
* Create a **pristine pier** (a freshly created fakeship which has not been touched since boot)
* Allow you to send arbitrary input to the dojo in order to set up the ship



* Method 1 - **Boot From Scratch** has the following properties:
	* Always downloads the urbit runtime
	* Always boots fakezod from scratch (**on every run**)
	* Made up of a single shell script



* Method 2 - **Boot From Cache** has the following properties:
	* Uses an archive of pristine state to avoid booting from scratch
	* Can create any number of fakeships
	* Can perform a manual OTA
	* Only downloads certain dependencies (i.e. the urbit runtime, OTA source) if they don't exist
	* Made up of multiple shell scripts


Method 1 doesn't really make much sense running as a local script, and is only really useful as a quick and dirty CI script.
Method 2, on the other hand works equally well both as a local script and as a script running in a CI environment. 


## Implementation
### Core

The core of the script is really these three lines:

```bash
screen -d -m -S fakeship -L -Logfile "./fakeship_output.log" ./urbit -F zod	# 1
screen -S fakeship -p 0 -X stuff "(add 2 2)^M"					# 2

until [[ "$(tail -n1 fakeship_output.log)" =~ "~zod:dojo>" ]]; do		# 3
  sleep 10s # wait for fakeship to boot
done
```

Let's dive in.

`screen(1)` is a command that essentially acts as a scriptable terminal emulator.

In line 1, we create a new "screen" (terminal) that starts detached `-d -m` and name the session fakeship `-S fakeship`.
We also enable dumping to a log `-L` and specify the `-Logfile` to be `"./fakeship_output.log"`. 

`./urbit -F zod` is the command we would like to run in our detached screen.

<br/>

In line 2, we send input to the dojo by first specifying the session `-S fakeship`, 
choosing the default "screen window" `-p 0`, sending the "stuff" command `-X stuff` 
and specifying the input that we want to send `"(add 2 2)^M"`.

The reference for the format that the `stuff` command takes should be [in the screen manual](https://www.gnu.org/software/screen/manual/screen.html).
In general, it uses the caret notation to represent control characters, and you can find a reference for that [on Wikipedia](https://en.wikipedia.org/wiki/ASCII#ASCII_control_code_chart).

In particular, take good note of the `^M`. This part of the string is necessary to send an "enter" key press.
It is the escape sequence that represents carriage return on linux. Likewise, the string `"^X"`, would be equivalent to sending `Control-x`.

<br/>

Line 3 and onward is how we consume the output log, which is what the ship prints to stdout. 
The tail command gets the last `n` lines from `fakeship_output.log`, `n` being `1` in this case, and is compared to a known value, `"~zod:dojo>"`, which confirms that we've booted successfully.

<br/>

### Method 1 - Boot From Scratch

With that in mind, here is the full implementation of Method 1, found at: https://github.com/ynx0/urbit/blob/master/extras/setup_fakezod_basic.sh

```bash
# setup_fakezod_basic.sh

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

# send input to the dojo
function send2ship() {
  screen -S fakeship -p 0 -X stuff "$1"
}

send2ship "(add 2 2)"
```


This is all that's necessary to boot a fakezod and perform various setup tasks like creating chats it in an automated fashion. 
However, it makes no promises in regard to speed—necessarily, it boots up a fakezod from scratch, *every time it runs*. 
This means that every time the script runs on a CI/CD platform, it will take roughly **5 minutes** just to set up the tests. 
Not the end of the world, but quite problematic for quickly evaluating and merging pull requests, for example.

Let's move on to Method 2, which brings this time down to the order of seconds.


### Method 2 - Boot From Cache

Method 2 works off of the same basic concepts as Method 1, and adds in caching functionality to the booting process.

#### Helper Functions

Since the setup of this method is quite complex, it is split into 2 scripts; a setup script a library script. 

The library script [(`setup_env_lib.sh`)](https://github.com/ynx0/urbit/blob/master/test_environment/setup_env_lib.sh) is stateless, and simply exposes helper functions.

The setup script [(`setup_env.sh`)](https://github.com/ynx0/urbit/blob/master/test_environment/setup_env.sh) is stateful, and uses the helper functions to perform the necessary tasks.




Here is a list of each function and what it does:

* `downloadUrbitRuntime` - downloads the urbit runtime binary from bootstrap.urbit.org
* `downloadLatestOTA` - downloads the `urbit/urbit` repo, which contains the arvo kernel and boot pill
* `safepatp(ship)` - strips characters from a `@p` that would otherwise cause problems in filenames/commands
* `start_ship(ship)` - starts the desired fakeship from an existing pier (`./urbit <ship>`)
* `send2ship(ship, input)` - sends arbitrary input to the dojo of the desired fakeship
* `getLastNLines(ship, n)` - gets the last `n` lines from a given fakeship's output
* `wait4boot(ship)` - waits for the fakeship to boot by periodically checking the "fakeship_output.log"
* `killShipSession(ship)` - kills the **screen session** for the ship, killing the ship as well
* `make_fakeship(ship, ota)` - boots up a fresh fakeship, then kills it when booted. performs ota if `ota` is `true`
* `tar_fakeship(ship)` - archives the pier of the desired fakeship. (assumes a pristine pier)
* `untar_fakeship(ship)` - unarchives the pristine pier of a given fakeship
* `cleanup(ship)` - calls `killShipSession`, moves the current log into `./old_logs`, removes the tainted pier, and removes the urbit runtime zipfile.



#### Environment Setup

Here is the logic to set up the environment according to Method 2, found at: 
https://github.com/ynx0/urbit/blob/master/test_environment/setup_env.sh


- The `@p` of all desired fakeships are stored and read from a file called `ships.cfg`.
- After this script has run, a `screen` session for each ship (named after the ship) will be available.

```bash
# test_environment/setup_env.sh

OTA=true

# 1. download the urbit runtime if it doesn't exist
if [ ! -d "./$URBIT_VERSION" ]; then
  downloadUrbitRuntime
fi

# 2. download the urbit runtime if it doesn't exist
if [ $OTA == true ] && [ ! -d "./$OTA_PATH" ]; then
  downloadLatestOTA
fi


function setup_environment() {
  # $1 = patp of desired ship
  local SHIP SAFE_SHIP
  SHIP="$1"
  SAFE_SHIP=$(safepatp "$SHIP")

  cleanup "$SHIP"			# a

  if [[ ! -f "./$SAFE_SHIP-$TAR_SUFFIX" ]]; then
    make_fakeship "$SHIP" "$OTA"	# b
    tar_fakeship "$SHIP"
  else
    untar_fakeship "$SHIP"		# c
  fi

  boot_fakeship "$SHIP"		# d
}


while read -r SHIP; do			
  # set up each ship in parallel.
  {
    setup_environment "$SHIP"		# i
    send2ship "$SHIP" "+code^M"	# ii
    getLastNLines "$SHIP" 5		# iii
  } &
done < "./ships.cfg"

wait  # wait for all ship jobs to complete

### Add your configuration here ###

echo "Finished setting up environment"
```

The steps are as follows:


<ol>
	<li>Download the urbit runtime if not present</li>
	<li>Download the OTA files if necessary and not present</li>
	<li> For each `@p` in `ships.cfg`
	<ol>
		<li>Set up the environment
			<ol>
				<li>Run the `cleanup` function for the given ship</li>	
				<li>If a pristine doesn't exist for the ship
					<ul>
						<li>Boot a fakeship from scratch</li>
						<li>Archive the pristine fakeship's pier</li>
					</ul>
				</li>
				<li>Otherwise, use the existing pristine fakeship and unarchive it</li>
				<li>Boot from the pristine pier of the target ship</li>
			</ol>
		</li>
		<li>Send the command `+code`</li>
		<li>Print the last 5 lines of the ship's output</li>
	</ol>
	</li>
</ol>


#### Manual OTAs

It may be desirable to be able to target the latest OTA and build tests around it.
However, this is not possible through the default fakeship creation process.
Instead, one must manually perform the ota by manually cloning the `urbit/urbit` repository,
then running the urbit binary with the appropriate flags, while also ensuring that `git-lfs` is installed in order to properly clone the boot pills.

Method 2 provides this functionality.
Simply set the `OTA` variable to true in the `setup_env.sh` script, and the script will handle the rest.

Please be aware that when enabled, 
the script will likely ask for your password because it requires `sudo` to install `git-lfs`. 


#### Ship Setup

Now that our pristine fakeship(s) are ready, we move on to the actual setup of the ship(s).
To do this, we add any commands we want to send to the dojo in the `setup_env.sh` script. 
This script will be our entry point for the CI/CD pipeline.


Here is an example of what could go in `setup_env.sh` 
```bash
### Add your configuration here ###

send2ship "~zod" "(add 2 2)^M"
getLastNLines "~zod" 2
```



#### Teardown

Method 2 also comes with a teardown script, which restores the `test_environment` directory to a clean state, 
which is especially handy when working locally.
Keep in mind that **this script will delete all piers**, so if you are still in the middle of experimenting don't run this. 

Feel free to change this to your liking.
 
The contents of `teardown_env.sh` are:

```bash

while read -r SHIP; do
  # tear down each ship in parallel
  {
    send2ship "$SHIP" "^D"		# 1
    sleep 3s				# 2
    cleanup "$SHIP"			# 3
  } &

done < "./ships.cfg"

wait  # wait for each job to complete

```

The script:
1. Sends `Control-D` to the dojo, safely stopping the urbit process
2. Waits for the previous action to take place
3. Executes the cleanup function



## Using the Scripts - Integrating with GitHub Actions

Once you have chosen a method, you will then want to use it with a CI/CD runner. 
In this section, we will demonstrate the steps required to set up the scripts using GitHub Actions, 
which is a CI/CD platform that is free for public repositories.


Steps:
1. Create the directory `.github/workflows` in the root of your project
2. In that directory, create a new file called `integration-tests.yml`
3. Populate the file based off of the following example and adjust to taste

When writing your workflow, you should:
1. Checkout the repository
2. Set up the language runtime
3. Set up the test runner for your language
4. Run the setup script
5. Run your tests
6. [Optional] Run the teardown script
	* This is not strictly necessary with GitHub Actions because the platform tears down the whole machine anyway.
	

Here is an example of a fully filled out workflow for Java:

```yml
name: Run Integration Tests


on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]


jobs:
  intergration-tests:

    runs-on: ubuntu-latest

    steps:
    # 1. Checkout the repository
    - uses: actions/checkout@v2
   
    # 2. Setup the language runtime
    - name: Set up JDK 11              
      uses: actions/setup-java@v1
      with:
        java-version: 11

    # 3. Setup the test runner for java
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    # 4. Run the setup script
    - name: Setup test environment
      run: ./test_environment/setup_env.sh  # or setup_fakezod_basic.sh

    # 5. Run the tests
    - name: Test with Gradle
      run: ./gradlew test --stacktrace # --info

    # 6. Run the teardown script if you are using method 2
    - name: Teardown test environment
      run: ./test_environment/teardown_env.sh

```

A general reference for the syntax of this file can be found [here](https://docs.github.com/en/free-pro-team@latest/actions/reference/workflow-syntax-for-github-actions)



### Notes
* Everything is kept in one directory, `test_environment`, which keeps the rest of the repository clean.
* **IMPORTANT:** When running the `setup_env.sh` script, the cached version is saved as a `.tar.gz` file in the same directory. 
    -  You **must** add and commit this file so that it is uploaded to the repository as well, 
       otherwise the script will simply start from scratch when running in the CI runner (and will take forever).


## Wrapping up

In summary, here are the steps:
1. In your repository, create a directory dedicated to your test environment.
2. Copy the scripts that you want to use to that directory
	* Method 1: https://github.com/ynx0/urbit/blob/master/extras/setup_fakezod_basic.sh
	* Method 2: https://github.com/ynx0/urbit/blob/master/test_environment
3. Call the scripts in your CI/CD pipeline
4. If using Method 2, make sure to generate and commit the pristine(s) to your repository

Happy Hooning!
