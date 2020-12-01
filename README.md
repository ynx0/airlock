# Airlock — Java Edition

Communicate with an Urbit ship over the eyre protocol in Java

## Example
```java
public class Main {

	private static final Gson gson = new Gson();

	public static void main(String[] args) throws Exception {
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

For the most up to date usage examples, see `src/main/java/Main.java` and `src/test/java/UrbitIntegrationTests.java`.


## Development Checklist

- [x] Minimum viable product
- [x] Basic integration tests
    - [x] Github Actions automatically tests on push/pr to `master`
- [x] Basic documentation
- [ ] Create build process (i.e. publishing artifacts to a repository)
- [ ] Examples based off of integration tests
- [ ] Create example application that uses basic functionality
- [ ] Implementation of surrounding libraries (e.g. `urbit-ob`)
- [ ] Soundness tests for `urbit-ob`
- [ ] Implementation of `scry` and `spider` request types

- [ ] `graph-store`
    - [ ] Initial functional implementation of interface to `graph-store` 
    - [ ] Graph store tests
    - [ ] Graph store documentation
    - [ ] Graph store examples 

- [ ] Other gall agent interface



## Prior Art
- Typescript 1 - https://github.com/tylershuster/urbit/
- Typescript 2 - https://github.com/liam-fitzgerald/urbit-airlock-ts
- Go - https://github.com/lukechampine/go-urbit/
- Haskell - https://github.com/bsima/urbit-airlock
- Swift - https://github.com/dclelland/UrsusAirlock/
- channel.js (part of Landscape) - https://github.com/urbit/urbit/blob/master/pkg/arvo/app/landscape/js/channel.js
