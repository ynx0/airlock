# Airlock — Java Edition

Communicate with an Urbit ship over the eyre protocol in Java.

## Prior Art
- Typescript - https://github.com/tylershuster/urbit/
- Go
- Haskell
- Swift
- Landscape channel.js


## Checklist

- [  ] Minimum viable product
- [  ] Basic integration tests
- [  ] Examples based off of integration tests
- [  ] Basic documentation
- [  ] Create build process (i.e. publishing artifacts to a repository)
- [  ] Create example application that uses basic functionality
- [  ] Implementation of surrounding libraries (e.g. `urbit-ob`)
- [  ] Soundness tests for `urbit-ob`
- [  ] Implementation of `scry` and `spider` request types
- [  ] Initial functional implementation of interface to `graph-store` 
- [  ] Graph store tests
- [  ] Graph store documentation
- [  ] Graph store examples 
- [  ] Other gall agent interface


### misc items

- possibly make custom exceptions that wrap around known failure modes
    - for example: ShipNotAvailable when first http request fails ...