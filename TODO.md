# TODO


- move towards stable master
- up to date examples once everything stabilizes
- upload junit report test.html as github actions artifact

# Current Focus
- clean up tests
	- deduplicate code in urbit
	- add documentation / comments
	- cleanup Main.java. make it simple, and prefer using the unit tests as examples
	- refactor out integration tests per-agent. keep the main one solely for the urbit client

- `graph-store`
	- make type adapters for various new types?
	- make types based on sur/graph-store and whatnot
	- moar integration tests. i.e. full coverage of the new methods on the agent

	- # general
	- i need to be developing from smallest component to biggest. i.e.
	scry -> validate -> pokes -> validate -> create dataclasses -> create agent class
	- new agent -> read docs -> look at landscape impl -> read json payloads created by landscape 
	  -> recreate same raw transaction in java -> write dataclasses, deserializers, agents, exceptions, etc. 
	  -> add tests -> add documentation -> merge


- misc
	- split up unit tests by "basic functionality" and by the agent (i.e. chat store/graph store etc.) (connect, auth, poke subscribe scry spider)
	- atom public no aura only bigint, protected access to aura, refactor later to abstract class AtomBase, atom, etc.
	- refactor out url stuff to util class
	- deduplicate code
	- ignore spider stuff for now
	- im try too hard with the impl of the aura type system in java. do that later if anything
	- use this if want pretty printed html errors https://github.com/dkorobtsov/LoggingInterceptor

# Ideas
