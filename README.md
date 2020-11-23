# Urbit Interface — Java Edition

This repository will hold the code that provides interoperability with an Urbit ship.
More details can be found here: https://grants.urbit.org/proposals/288224550-urbit-http-interface-java-edition

## Initial Plan

First, I'll try to do a port of the typescript implementation, which can be found here: https://github.com/tylershuster/urbit/blob/master/src/index.ts

## Misc thoughts

- Maybe shed okhttp library if I end up not needing it/too bloated
- possibly make custom exceptions that wrap around known failure modes
    - for example: ShipNotAvailable when first http request fails ...