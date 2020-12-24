# TODO

## Long Term
- move towards stable master
- up to date examples once everything stabilizes
- upload junit report test.html as github actions artifact
- clean up tests
- cleanup Main.java. make it simpler

## Current Focus
- deduplicate code in urbit

- `graph-store`
	- make type adapters for various new types?
	- make types based on sur/graph-store and whatnot
	- moar integration tests. i.e. full coverage of the new methods on the agent
	

- add documentation / comments for graph-store updates (will happen last after everything is stabilized)

## Completed Items
- [Dec 23] refactor out integration tests per-agent. keep the main one solely for the urbit client
  - split up unit tests by "basic functionality" and by the agent (i.e. chat store/graph store etc.) (connect, auth, poke subscribe scry spider)
- [Dec 23] prefer using the unit tests as examples
- [Dec 23] refactor out url stuff to util class
- [Dec 23] atom public no aura only bigint, protected access to aura, refactor later to abstract class AtomBase, atom, etc.

# Ideas


- [Wish] Implement the aura system in Java
	- also maybe the `++dime` and `++cord` stuff?


# Thoughts

- i need to be developing from smallest component to biggest. i.e.
  scry -> validate -> pokes -> validate -> create dataclasses -> create agent class
- new agent -> read docs -> look at landscape impl -> read json payloads created by landscape
  -> recreate same raw transaction in java -> write dataclasses, deserializers, agents, exceptions, etc.
  -> add tests -> add documentation -> merge
  
- maybe my exception hierarchy is too extra ?? 
  idk we will only find out after actually using it but for now it makes sense theoretically
