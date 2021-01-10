# TODO

## Long Term

- move towards stable master
- up to date examples once everything stabilizes
- upload junit report test.html as github actions artifact
- cleanup Main.java. make it simpler

## Mid Term
- see if i want dataclasses to be immutable, use getters, or just completely public access to instance variables
    - for now, just to get things working, i will make the mpublic but in the future i should make getters for them or
      make them immutable copies style modifications.

- make certain dataclasses final where it makes sense (e.g. `EyreResponse`, `PokeResponse`, `SubscribeEvent`)

## Current Focus — `%graph-store`

### development roadmap


- create helper for creating link entries
- create helper for creating blog posts

- replicate all basic user flows
    - Assumption: group exists

    - [x] chat 
        - [x] create new chat
        - [x] post a message to the chat
        - [x] get newest
    - [ ] collections
        - [x] create new collection
        - [x] post a new link
        - [ ] update link
        - [ ] post comment 
        - [ ] update comment
        - [ ] delete comment
        - [ ] delete link
    - [ ] publish
        - [ ] create new notebook
        - [ ] create and add new post
        - [ ] update post
        - [ ] create comment
        - [ ] update comment
        - [ ] delete comment
        - [ ] delete post
    
    - [ ] all
        - [ ] get newer and older siblings
            - [ ] chat
            - [ ] collections
            - [ ] publish
        - [x] delete all graphs

- copy above to main
- clean up main
- integrate above into tests - also try to go for full coverage if possible but this can come later
- [optional] add experimental methods which are in hoon but not in Landscape
    - things like {add,remove}x{tags,signatures}, {archive,unarchive}graph
- finalize api and refactorings - data classes for `graph-update` payload for example
- document
- tag
- automate boot pill creation of master or updating os from master for environment

### graph-store guide outline

(should be platform-independent)

- talk about higher level basic operations
    * get graphs/get keys
    * delete graphs

- replicate all basic user flows
    - assume group exists

    * chat
        * create new chat
        * post a message to the chat
        * briefly mention how on-line content is split up between GraphContent types (use the migration guide thing
          posted by ~haddef-sigwen)
        * get newer and older siblings
    * collections
        * create new collection
        * post a new link
        * update link
        * post comment
        * update comment
        * delete comment
        * delete link
        * get newer/older links in collection (if different)
    * publish
        - caveat: in landscape there is a call here to addTags if choose to restrict notebook to certain ships. this is
          used to tell graph store to restrict ability to publish to the speciifed ships. we will not cover tihs
          functionality in the turoial because it requires a acall to
          group-push-hook (https://github.com/urbit/urbit/blob/531f406222c15116c2ff4ccc6622f1eae4f2128f/pkg/interface/src/views/apps/publish/components/Writers.js)

        * create new notebook
        * create and add new post
        * update post
        * create comment
        * update comment
        * delete comment
        * delete post
        * get newer and older siblings

- talk (briefly) about state management




## Completed Items

- [Dec 29] update to be in sync with master. current code is porting outdated `graph.ts`
- [Dec 27] make types based on sur/graph-store and whatnot
- [Dec 27] make type adapters for various new types? (sufficiently done)
- [Dec 25] deduplicate code in urbit
- [Dec 25] split up unit tests by "basic functionality" and by the agent (i.e. chat store/graph store etc.) (connect,
  auth, poke subscribe scry spider)
- [Dec 23] refactor out integration tests per-agent. keep the main one solely for the urbit client
- [Dec 23] prefer using the unit tests as examples
- [Dec 23] refactor out url stuff to util class
- [Dec 23] atom public no aura only bigint, protected access to aura, refactor later to abstract class AtomBase, atom,
  etc.

# Ideas

- [Wish] Implement the aura system in Java
    - also maybe the `++dime` and `++cord` stuff?


# Thoughts

- i need to be developing from smallest component to biggest. i.e. scry -> validate -> pokes -> validate -> create
  dataclasses -> create agent class
- new agent -> read docs -> look at landscape impl -> read json payloads created by landscape -> recreate same raw
  transaction in java -> write dataclasses, deserializers, agents, exceptions, etc. -> add tests -> add documentation ->
  merge

- maybe my exception hierarchy is too extra ?? idk we will only find out after actually using it but for now it makes
  sense theoretically
