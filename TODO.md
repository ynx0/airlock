# TODO

## Long Term

- move towards stable master
- up to date examples once everything stabilizes
- remove lombok dependency (?)
- write a lot more tests. also test more thoroughly
- rename stuff like GraphAgent to Api? like how landscape has BaseApi and GraphApi

## Mid Term
- see if I want dataclasses to be immutable, use getters, or just completely public access to instance variables
    - for now, just to get things working, I will make the public but in the future I should make getters for them or
      make them immutable copies style modifications.

- make certain dataclasses final where it makes sense (e.g. `EyreResponse`, `PokeResponse`, `SubscribeEvent`)
- rename the test java files
- fix the TOC in the readme
- formatting and optimize imports on the whole repo


## Development Roadmap

Current Focus — Milestone 3

### Milestone 3

To fulfill this milestone there needs to be clients for 2 gall agents.
I have chosen `%group-store` and`%invite-store`.

- [ ] `%invite-store`
    - [x] initial research and notes
    - [x] experimentation, payload analysis
    - [x] baseline port of data structures and some api functionality
    - [ ] complete port
    - [ ] unit tests
    - [ ] documentation

- [ ] `%group-store`
      - [ ] initial research and notes
      - [ ] experimentation, payload analysis
      - [ ] baseline port of data structures and some api functionality
      - [ ] complete port
      - [ ] unit tests
      - [ ] documentation



### After Milestone 3

**Graph Store Stuff**
- subscribe to `/keys`
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

### graph-store apps guide outline

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

##### graph-store guide -- imgb0rd 
TODO

## Completed Items

### Long Term
- upload junit report test.html as github actions artifact

### Milestone 2
- fix up test environment (done jan 25)
- automate boot pill creation of master or updating os from master for environment (done jan 25)
- make sure tests still work (done jan 25 yay)
- refactor graph store api if necessary (done jan 25)
- cleanup documentation (sufficiently done jan 25)
- add simple example for just graph-store to main (done jan 25)
- ~~add graph-store unit tests~~ do this later once apps is finished
- merge graph-store branch into master (done jan 25)
- tagged release to finalize milestone 2

- (optional) try using the standalone jar in another project, fix if it doesn't work
- (optional) publish test report html artifact (done jan 25)



### Dec 29
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
