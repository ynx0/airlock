# Airlock — Java Edition

Communicate with an Urbit ship over the eyre protocol in Java

## Example
```java
public class Main {

	private static final Gson gson = new Gson();

	public static void main(String[] args) {
		String url = "http://localhost:80";
		String shipName = "zod";
		String code = "lidlut-tabwed-pillex-ridrup";

		// SETUP
        // The following example assumes you have:
        // - a ship named 'zod' running
        // - a chat channel called 'test' (you must manually create this)


        // MARK - connect to the ship
		Urbit ship = new Urbit(url, shipName, code);
        ship.authenticate();
		ship.connect();
        
        // MARK - create a mailbox subscription on a channel named 'test' 
		int subscriptionID = ship.subscribe(ship.getShipName(), "chat-store", "/mailbox/~zod/test", subscribeEvent -> {
			System.out.println("[Subscribe Event]");
			System.out.println(subscribeEvent);
		});
	}
}

```

For the most up to date usage examples, see `src/test/java/UrbitIntegrationTests.java` or `src/main/java/Main.java`.




## Using

**WARNING: The library is currently in alpha and does not really have a stable api yet. Expect things to break and change**

Currently, the simplest way to play around with the library is to download the source and edit the `Main.java` file yourself.

### Running From Source
1. Clone the repository: `git clone https://github.com/ynx0/urbit`
2. In the cloned repository, run `./gradlew run`. This will run the `Main.java` class.
3. Edit `Main.java` to experiment

### Jar
If the code is alpha, using the jar is the greek letter before that. It most likely doesn't work.
To generate a jar from the source, all you have to do is run `./gradlew jar`.

However, if you want to try it out, you can download the jar without actually downloading the repository:
1. Go to the [Github Actions](https://github.com/ynx0/urbit/actions) workflows page
2. Find a commit with `Java CI with gradle` underneath that has is passing (has green checkmark).
3. Under the **Artifacts** heading, click on `all-jars`. This will download the jar that was generated from the aforementioned task.





## Roadmap

- [x] Minimum viable product
- [x] Basic integration tests
    - [x] Github Actions automatically tests on push/pr to `master`
- [x] Basic documentation
    - [x] Build javadocs
- [x] Implementation of `scry` and `spider` request types
- [ ] Integration for `scry` and `spider`
- [ ] Implementation of surrounding libraries
    - [ ] atom manipulation
    - [ ] related urbit types

- [ ] Implementation of `chat-store`/`chat-view`
    - [ ] Initial functional implementation of interface to agents
    - [ ] Graph store tests
    - [ ] Graph store documentation
    - [ ] Graph store examples

- [ ] `graph-store`
    - [ ] Initial functional implementation of interface 
    - [ ] Graph store tests
    - [ ] Graph store documentation
    - [ ] Graph store examples

- [ ] Other gall agent interfaces

### After Stabilization
- [ ] Create example application that uses basic functionality
- [ ] Examples based off of integration tests
- [ ] Create build process (i.e. publishing artifacts to a repository)
- [ ] Soundness tests for various parts of the library (i.e. unit tests)
    - [ ] Urbit uid and hexString
    - [ ] atom manipulation
    - [ ] related urbit types





## Prior Art
- Typescript 1 - https://github.com/tylershuster/urbit/
- Typescript 2 - https://github.com/liam-fitzgerald/urbit-airlock-ts
- Go - https://github.com/lukechampine/go-urbit/
- Haskell - https://github.com/bsima/urbit-airlock
- Swift - https://github.com/dclelland/UrsusAirlock/
- channel.js (part of Landscape) - https://github.com/urbit/urbit/blob/master/pkg/arvo/app/landscape/js/channel.js
