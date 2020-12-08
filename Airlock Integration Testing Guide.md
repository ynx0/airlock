# Airlock Integration Testing Guide

This guide will show you how to set up a minimal testing environment in an automated fashion (i.e. in a CI/CD pipeline) 
using a set of utility scripts which require no outside dependencies, with quick setup process and simple to use interface.

Although tools such as [herb](https://github.com/urbit/urbit/tree/master/pkg/herb) exist, they usually require many extra steps and dependencies which can make the the process cumbersome.
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
* Create a **pristine fakezod state** (a freshly created fakezod which has not been touched since boot)
* Allow you to send arbitrary input to the dojo in order to set up the ship

* Method 1 - **Boot From Scratch** has the following properties:
	* Always downloads the urbit runtime
	* Always boots fakezod from scratch (**on every run**)
	* Made up of a single shell script



* Method 2 - **Boot From Cache** has the following properties:
	* Only downloads the urbit runtime if it doesn't exist
	* Uses an archive of a pristine fakezod state to avoid booting from scratch
	* Made up of multiple shell scripts


Method 1 doesn't really make much sense running as a local script, and is only really useful as a CI script.
Method 2, on the other hand works equally well both as a local script and as a script running in a CI environment. 


## Implementation
### Core

The core of the script is really these three lines:

```bash
screen -d -m -S fakeship -L -Logfile "./fakeship_output.log" ./urbit -F zod	# 1
screen -S fakeship -p 0 -X stuff "(add 2 2)^M"					# 2

until [[ "$(tail -n1 fakeship_output.log)" =~ "~zod:dojo>" ]]; do		# 3
  sleep 10s # wait for fakezod to boot
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

The reference for format that the `stuff` command takes should be [in the screen manual](https://www.gnu.org/software/screen/manual/screen.html).
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

send2ship "^X"
send2ship ";create channel /test^M"
```


This is all that's necessary to boot a fakezod and perform various setup tasks like creating chats it in an automated fashion. 
However, it makes no promises in regards to speed—necessarily, it boots up a full zod ship every time it runs, 
which means that every time the script runs on a CI/CD platform, it will take roughly 5 minutes just to set up the tests. 
Not the end of the world, but quite problematic for quickly evaluating and merging pull requests, for example.

Let's move on to Method 2, which brings this time down to the order of seconds.


### Method 2 - Boot From Cache

Method 2 works off of the same basic concepts as Method 1, and adds in caching functionality.

#### Helper Functions

Since the setup of this method is quite complex, it is split into regular scripts, and a library script, which has a bunch of helper functions. 
It can be found at:
https://github.com/ynx0/urbit/blob/master/test_environment/setup_env_lib.sh

Here is a list of each function and what it does:

* `downloadUrbitRuntime` - downloads the urbit runtime binary from bootstrap.urbit.org
* `start_ship` - starts a fakezod from an existing pier (`./urbit zod`)
* `send2ship` - sends arbitrary input to the dojo of the fakezod
* `getLastNLines` - gets the last `n` lines from the fakezod's output (from "fakeship_output.log")
* `wait4boot` - waits for the fakezod to boot by periodically checking the "fakeship_output.log"
* `killShip` - kills the **screen session** for the ship, killing the ship as well
* `make_fakezod` - boots up a fresh fakezod (`./urbit -F zod`), then kills it when booted
* `tar_fakezod_state` - archives the current fakezod pier. (assumes it will be pristine)
* `untar_fakezod_state` - unarchives a pristine fakezod state
* `cleanup` - calls `killShip`, moves the current log into `./old_logs`, removes the tainted pier, and removes the urbit runtime zipfile.



#### Environment Setup

Here is the logic to set up the environment according to Method 2, found at: 
https://github.com/ynx0/urbit/blob/master/test_environment/setup_env.sh


```bash
# test_environment/setup_env.sh

REBUILD=false
source ./setup_env_lib.sh  # import the functions from the library file

								# 1: Download Urbit Runtime
if [ ! -d "./$URBIT_VERSION/" ]; then
  downloadUrbitRuntime
fi
								# 2: Caching Logic
if [[ $REBUILD == true || ! -f ./$FAKEZOD_TAR ]]; then
								# 2a: build fakezod
  echo "REBUILD: $REBUILD"
  make_fakezod
  tar_fakezod_state          
else
  untar_fakezod_state						# 2b. unarchive the existing pristine fakezod state
fi

boot_fakezod							# 3.  boot from the pristine fakezod state
```

The steps are as follows:

<ol>
	<li>Download the urbit runtime if it doesn't exist</li>
	<li>Caching logic
		<ol type="a">
			<li>Rebuild or non-existent cache
				<ul>
					<li>Build fakezod from scratch</li>
					<li>Archive the pristine fakezod state</li>
				</ul>
			</li>
			<li>Otherwise, use the existing pristine fakezod and unarchive it</li>
		</ol>
	</li>
	<li>Boot from the pristine fakezod</li>
</ol>




#### Ship Setup

Now that our pristine fakezod is ready, we move on to the actual setup of the ship.
To do this, we edit the contents of `setup_fakezod.sh` and send commands to the dojo. 
This will be our entry point for the CI/CD pipeline as it imports and runs the environment setup script.

```bash
# test_environment/setup_fakezod.sh
source ./setup_env.sh               # 1

sleep 1s
send2ship "^X"                      # 2
send2ship ";create channel /test^M" # 3
send2ship "^X"                      # 4
getLastNLines 5                     # 5
```

The script:
1. Imports and runs the environment setup script, booting up a fakezod
2. Sends `Control-X` to the ship to enter `chat-cli`
3. Sends a command to create a channel called test, followed by an enter `^M`
4. Switches back to the dojo
5. Prints the last 5 lines of output from the ship

Again, this is where you should put all of your setup code that you want to entered in the dojo.


#### Teardown

One final thing to note is that Method 2 comes with a teardown script, which simply sends `Ctrl-D` to the ship, 
kills the screen process in case it hangs, and does some cleanup on the log files. Feel free to change this to your liking.
 
The contents of `teardown_fakezod.sh` are:

```bash
send2ship "^D"	# 1
sleep 3s	# 2
cleanup		# 3
```

The script:
1. Sends `Control-D` to the dojo, safely stopping the urbit process
2. Waits for the previous action to take place
3. Executes the cleanup function


This script is handy for when you run the `setup_fakezod.sh` script in a local environment and don't want to leave your fakezod running.


## Using the Scripts - Integrating with GitHub Actions

Once you have chosen a method, you will want to actually use it with a CI/CD runner. 
In this section, we will demonstrate the steps required to set up the scripts using GitHub Actions, 
which is a CI/CD platform that is free for public repositories.


Steps:
1. Create the directory `.github/workflows` in the root of your project
2. In that directory, create a new file called `integration-tests.yml`
3. Populate the file based off of the following example and adjust to taste

When writing your workflow, you should:
1. Checkout the repository
2. Setup the language runtime
3. Setup the test runner for your language
4. Run the setup script
5. Run your tests
6. [Optional] Run the teardown script
	* This is not strictly necessary with GitHub Actions because the platform tears down the whole machine anyway.


Here is an example of a fully filled out workflow:

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
      run: ./test_environment/setup_fakezod.sh  # or setup_fakezod_basic.sh

    # 5. Run the tests
    - name: Test with Gradle
      run: ./gradlew test --stacktrace # --info

    # 6. Run the teardown script if you are using method 2
    - name: Teardown test environment
      run: ./test_environment/teardown_fakezod.sh

```

A general reference for the syntax of this file can be found [here](https://docs.github.com/en/free-pro-team@latest/actions/reference/workflow-syntax-for-github-actions)



### Notes
* I keep the scripts in a separate directory called `test_environment` to keep the rest of my repository clean.
* To force a rebuild of the cached fakezod state, simply delete the cached file or set the `REBUILD` variable in the file `setup_env.sh` to true.
* **IMPORTANT:** When running the `setup_fakezod` script, the cached version is saved as a `.tar.gz` file in the same directory. 
    -  You **must** add and commit this file so that it is uploaded to the repository as well, 
       otherwise the script will simply start from scratch when running in the CI runner (and will take forever).


## Wrapping up

In summary, here are the steps:
1. In your repository, create a directory dedicated to your test environment.
2. Copy the scripts that you want to use to that directory
	* Method 1: https://github.com/ynx0/urbit/blob/master/extras/setup_fakezod_basic.sh
	* Method 2: https://github.com/ynx0/urbit/blob/master/test_environment
3. Call the scripts in your CI/CD pipeline
4. If using Method 2, make sure to generate and commit the pristine fakezod to your repository

Happy Hooning!
