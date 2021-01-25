graph stuff
```json
{
  "create": {
    "resource": {
      "ship": "~zod",
      "name": "mkjhkjhkjh"
    },
    "title": "mkjhkjhkjh",
    "description": "hkjhkjh",
    "associated": {
      "group": {
        "ship": "~zod",
        "name": "test-group-dos"
      }
    },
    "module": "link"
  }
}
```

```json
{
  "graph-update": {
    "keys": [
      {
        "name": "collapse-open-blog",
        "ship": "timluc-miptev"
      },
      {
        "name": "announcements",
        "ship": "littel-wolfur"
      },
      {
        "name": "best-new-technology",
        "ship": "tacryt-socryp"
      },
      {
        "name": "test-notebook-3605",
        "ship": "sipfyn-pidmex"
      },
      {
        "name": "airlocks-apis-2746",
        "ship": "littel-wolfur"
      }
    ]
  }
}
```

,============ScryRequest============,
Request: http://localhost:80/~/scry/graph-store/graph/~littel-wolfur/announcements.json
.============ScryRequest============.

```json
{
  "graph-update": {
    "add-graph": {
      "graph": [
        [
          "/170141184504779319006408998471364272717",
          {
            "post": {
              "index": "/170141184504779319006408998471364272717",
              "author": "littel-wolfur",
              "time-sent": 1606091076589,
              "signatures": [],
              "contents": []
            },
            "children": [
              [
                "/2",
                {
                  "post": {
                    "index": "/170141184504779319006408998471364272717/2",
                    "author": "littel-wolfur",
                    "time-sent": 1606091076589,
                    "signatures": [],
                    "contents": []
                  }
                }
              ],
              [
                "/1",
                {
                  "post": {
                    "index": "/170141184504779319006408998471364272717/1",
                    "author": "littel-wolfur",
                    "time-sent": 1606091076589,
                    "signatures": [],
                    "contents": []
                  },
                  "children": [
                    [
                      "/4",
                      {
                        "post": {
                          "index": "/170141184504779319006408998471364272717/1/4",
                          "author": "littel-wolfur",
                          "time-sent": 1606189212052,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates - November"
                            },
                            {
                              "text": "## BTC Gall Agent\n\n* Authors:~timluc-miptev\n* Link:https://github.com/timlucmiptev/btc-agents\n\nCheck the project channel in the forge for updates\n\n## ed\n\n* Link:https://github.com/crides/ed.hoon\n* Authors:~hosbud-socbur\n\nNo longer actively maintained for the moment, but outside contributions are welcomed Main roadblock right now is regex\n\n## duiker\n\n* Link:https://github.com/Fang-/suite\n* Authors:~palfun-foslup\n\nTorrent discovery with a robust CLI app\n\n## mapsmithing\n\n* Authors:~lomped-firser\n* Link:\n\n## ursus\n\n* Authors:~lanrus-rinfep\n* Link:https://github.com/dclelland/UrsusChat\n\nWorking on graph-store implementation\n\n## srrs\n\n* Authors:~littel-wolfur\n* Link:https://github.com/ryjm/srrs\n  * still in maintenance mode\n  * indigo-tlon migration is on going\n  * uses the new pkg/interface build with webpack\n* Future Work\n  * refactor into store/hook/view pattern.\n  * Review in srrs-cli\n  * Use one key mode in shoe\n  * https://github.com/ryjm/srrs/issues\n\n\n## odyssey\n\n* Authors:~littel-wolfur\n* Link:soon!\n\n* did a very small test with a few people that exposed some issues with graph-store subscriptions being randomly closed, so delaying the public test until i get that worked out \n* for any nix users out there - i have a full nix build for the creation of a personal ragnarok game server which i will distribute, so hoping to see a few competing servers soon ;) will send to interested parties by request\n\n# Airlocks\n\n#### Ruby Airlock\n\n* Authors:~winter-paches\n\n#### Haskell Airlock\n\n* Authors:bsima\n* Link:https://github.com/bsima/urbit-airlock\n\n#### Go Airlock​\n\n* Authors:cmarcelo\n* Link:https://github.com/cmarcelo/go-urbit\n\n#### Typescript Airlock\n\n* Authors: tylershuster\n* Link: https://github.com/tylershuster/urbit/tree/refactor\n\n#### ​Swift Airlock\n\n* Authors: dcelland\n* Link: https://github.com/dclelland/UrsusAirlock\n"
                            }
                          ]
                        }
                      }
                    ],
                    [
                      "/3",
                      {
                        "post": {
                          "index": "/170141184504779319006408998471364272717/1/3",
                          "author": "littel-wolfur",
                          "time-sent": 1606188503649,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates - November"
                            },
                            {
                              "text": "## BTC Gall Agent\n\n* Authors:~timluc-miptev\n* Link:https://github.com/timlucmiptev/btc-agents\n\nCheck the project channel in the forge for updates\n\n## ed\n\n* Link:https://github.com/crides/ed.hoon\n* Authors:~hosbud-socbur\n\nNo longer actively maintained for the moment, but outside contributions are welcomed Main roadblock right now is regex\n\n## duiker\n\n* Link:https://github.com/Fang-/suite\n* Authors:~palfun-foslup\n\nTorrent discovery with a robust CLI app\n\n## mapsmithing\n\n* Authors:~lomped-firser\n* Link:\n\n## ursus\n\n* Authors:~lanrus-rinfep\n* Link:https://github.com/dclelland/UrsusChat\n\nWorking on graph-store implementation\n\n## srrs\n\n* Authors:~littel-wolfur\n* Link:https://github.com/ryjm/srrs\n* Future Work\n  * refactor into store/hook/view pattern.\n  * Review in srrs-cli\n  * Use one key mode in shoe\n  * https://github.com/ryjm/srrs/issues\n  * still in maintenance mode\n  * indigo-tlon migration is on going\n  * uses the new pkg/interface build with webpack\n\n## odyssey\n\n* Authors:~littel-wolfur\n* Link:soon!\n\n* did a very small test with a few people that exposed some issues with graph-store subscriptions being randomly closed, so delaying the public test until i get that worked out \n* for any nix users out there - i have a full nix build for the creation of a personal ragnarok game server which i will distribute, so hoping to see a few competing servers soon ;) will send to interested parties by request\n\n# Airlocks\n\n#### Ruby Airlock\n\n* Authors:~winter-paches\n\n#### Haskell Airlock\n\n* Authors:bsima\n* Link:https://github.com/bsima/urbit-airlock\n\n#### Go Airlock​\n\n* Authors:cmarcelo\n* Link:https://github.com/cmarcelo/go-urbit\n\n#### Typescript Airlock\n\n* Authors: tylershuster\n* Link: https://github.com/tylershuster/urbit/tree/refactor\n\n#### ​Swift Airlock\n\n* Authors: dcelland\n* Link: https://github.com/dclelland/UrsusAirlock\n"
                            }
                          ]
                        }
                      }
                    ],
                    [
                      "/2",
                      {
                        "post": {
                          "index": "/170141184504779319006408998471364272717/1/2",
                          "author": "littel-wolfur",
                          "time-sent": 1606091420966,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates - November"
                            },
                            {
                              "text": "## BTC Gall Agent\n\n* Authors:~timluc-miptev\n* Link:https://github.com/timlucmiptev/btc-agents\n\nCheck the project channel in the forge for updates\n\n## ed\n\n* Link:https://github.com/crides/ed.hoon\n* Authors:~hosbud-socbur\n\nNo longer actively maintained for the moment, but outside contributions are welcomed Main roadblock right now is regex\n\n## duiker\n\n* Link:https://github.com/Fang-/suite\n* Authors:~palfun-foslup\n\nTorrent discovery with a robust CLI app\n\n## mapsmithing\n\n* Authors:~lomped-firser\n* Link:\n\n## ursus\n\n* Authors:~lanrus-rinfep\n* Link:https://github.com/dclelland/UrsusChat\n\nWorking on graph-store implementation\n\n## srrs\n\n* Authors:~littel-wolfur\n* Link:https://github.com/ryjm/srrs\n* Future Work\n  * refactor into store/hook/view pattern.\n  * Review in srrs-cli\n  * Use one key mode in shoe\n  * https://github.com/ryjm/srrs/issues\n  * still in maintenance mode\n  * indigo-tlon migration is on going\n  * uses the new pkg/interface build with webpack\n\n## odyssey\n\n* Authors:~littel-wolfur\n* Link:soon!\n\ndid a very small test with a few people that exposed some issues with graph-store subscriptions being randomly closed, so delaying the public test until i get that worked out for any nix users out there - i have a full nix build for the creation of a personal ragnarok game server which i will distribute, so hoping to see a few competing servers soon ;) will send to interested parties by request\n\n# Airlocks\n\n#### Ruby Airlock\n\n* Authors:~winter-paches\n\n#### Haskell Airlock\n\n* Authors:bsima\n* Link:https://github.com/bsima/urbit-airlock\n\n#### Go Airlock​\n\n* Authors:cmarcelo\n* Link:https://github.com/cmarcelo/go-urbit\n\n#### Typescript Airlock\n\n* Authors:tylershuster\n* Link:https://github.com/tylershuster/urbit/tree/refactor\n\n#### ​Swift Airlock\n\n* Authors:dcelland\n* Link:https://github.com/dclelland/UrsusAirlock\n"
                            }
                          ]
                        }
                      }
                    ],
                    [
                      "/1",
                      {
                        "post": {
                          "index": "/170141184504779319006408998471364272717/1/1",
                          "author": "littel-wolfur",
                          "time-sent": 1606091076589,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates - November"
                            },
                            {
                              "text": "\u003d BTC Gall Agent \u003d\n\n* Authors:~timluc-miptev\n* Link:https://github.com/timlucmiptev/btc-agents\n\nCheck the project channel in the forge for updates\n\n\u003d ed \u003d\n\n* Link:https://github.com/crides/ed.hoon\n* Authors:~hosbud-socbur\n\nNo longer actively maintained for the moment, but outside contributions are welcomed Main roadblock right now is regex\n\n\u003d duiker \u003d\n\n* Link:https://github.com/Fang-/suite\n* Authors:~palfun-foslup\n\nTorrent discovery with a robust CLI app\n\n\u003d mapsmithing \u003d\n\n* Authors:~lomped-firser\n* Link:\n\n\u003d ursus \u003d\n\n* Authors:~lanrus-rinfep\n* Link:https://github.com/dclelland/UrsusChat\n\nWorking on graph-store implementation\n\n\u003d srrs \u003d\n\n* Authors:~littel-wolfur\n* Link:https://github.com/ryjm/srrs\n* Future Work\n** refactor into store/hook/view pattern.\n** Review in srrs-cli\n** Use one key mode in shoe\n** https://github.com/ryjm/srrs/issues\n** still in maintenance mode\n** indigo-tlon migration is on going\n** uses the new pkg/interface build with webpack\n\n\u003d odyssey \u003d\n\n* Authors:~littel-wolfur\n* Link:soon!\n\ndid a very small test with a few people that exposed some issues with graph-store subscriptions being randomly closed, so delaying the public test until i get that worked out for any nix users out there - i have a full nix build for the creation of a personal ragnarok game server which i will distribute, so hoping to see a few competing servers soon ;) will send to interested parties by request\n\n\u003d Airlocks \u003d\n\n\u003d\u003d Ruby Airlock \u003d\u003d\n\n* Authors:~winter-paches\n\n\u003d\u003d Haskell Airlock \u003d\u003d\n\n* Authors:bsima\n* Link:https://github.com/bsima/urbit-airlock\n\n\u003d\u003d Go Airlock​ \u003d\u003d\n\n* Authors:cmarcelo\n* Link:https://github.com/cmarcelo/go-urbit\n\n\u003d\u003d Typescript Airlock \u003d\u003d\n\n* Authors:tylershuster\n* Link:https://github.com/tylershuster/urbit/tree/refactor\n\n\u003d\u003d ​Swift Airlock \u003d\u003d\n\n* Authors:dcelland\n* Link:https://github.com/dclelland/UrsusAirlock\n"
                            }
                          ]
                        }
                      }
                    ]
                  ]
                }
              ]
            ]
          }
        ],
        [
          "/170141184504727714766775591470358331392",
          {
            "post": {
              "index": "/170141184504727714766775591470358331392",
              "author": "littel-wolfur",
              "time-sent": 1603293605153,
              "signatures": [],
              "contents": []
            },
            "children": [
              [
                "/2",
                {
                  "post": {
                    "index": "/170141184504727714766775591470358331392/2",
                    "author": "littel-wolfur",
                    "time-sent": 1603293605153,
                    "signatures": [],
                    "contents": []
                  },
                  "children": [
                    [
                      "/170141184504728676735872733540371136512",
                      {
                        "post": {
                          "index": "/170141184504727714766775591470358331392/2/170141184504728676735872733540371136512",
                          "author": "timluc-miptev",
                          "time-sent": 1603345753602,
                          "signatures": [],
                          "contents": []
                        },
                        "children": [
                          [
                            "/1",
                            {
                              "post": {
                                "index": "/170141184504727714766775591470358331392/2/170141184504728676735872733540371136512/170141184504728676735872733540371136512",
                                "author": "timluc-miptev",
                                "time-sent": 1603345753602,
                                "signatures": [],
                                "contents": [
                                  {
                                    "text": "## BTC Urbit\n* RPC calls working great\n* Got native signing for segwit addresses working fully in Urbit. Legacy still need to figure out. I put this on hold since Urbit doesn\u0027t have secure private key storage capabilities yet.\n* Started work on integrating the provider agent with a wallet agent."
                                  }
                                ]
                              }
                            }
                          ]
                        ]
                      }
                    ]
                  ]
                }
              ],
              [
                "/1",
                {
                  "post": {
                    "index": "/170141184504727714766775591470358331392/1",
                    "author": "littel-wolfur",
                    "time-sent": 1603293605153,
                    "signatures": [],
                    "contents": []
                  },
                  "children": [
                    [
                      "/2",
                      {
                        "post": {
                          "index": "/170141184504727714766775591470358331392/1/2",
                          "author": "littel-wolfur",
                          "time-sent": 1606091055906,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates September/October"
                            },
                            {
                              "text": "\n\n\n\n\n\n\n\n\n---\n`~timluc`\n---\n- _BTC Urbit Gall Agents_\n    - RPC calls working great\n    - Got native signing for segwit addresses working fully in Urbit. Legacy still need to figure out. I put this on hold since Urbit doesn\u0027t have secure private key storage capabilities yet.\n    - Started work on integrating the provider agent with a wallet agent.\n---\n`~lomped-firser`\n---\n\n- _Mapsmithing_\n\n---\n`~littel-wolfur`\n---\n\n- _odyssey_\n\t- (delayed a bit for IRL stuff, shoot me a dm if you want to help out)\n- _srrs_\n  \t- on hiatus (though still making incremental improvements)\n    - recent update borked it, still investigating\n    \n---\n`~lanrus-rinfep`\n---\n\n- _ursus_ work ongoing\n\n---\nrss reader (not sure of author)\n---\n-  needs an update to support new links\n- `https://github.com/clonex10100/hoon-rss"
                            }
                          ]
                        }
                      }
                    ],
                    [
                      "/1",
                      {
                        "post": {
                          "index": "/170141184504727714766775591470358331392/1/1",
                          "author": "littel-wolfur",
                          "time-sent": 1603293605153,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates #2"
                            },
                            {
                              "text": "\n\n\n\n\n\n\n\n\n---\n`~timluc`\n---\n- _BTC Urbit Gall Agents_\n    - RPC calls working great\n    - Got native signing for segwit addresses working fully in Urbit. Legacy still need to figure out. I put this on hold since Urbit doesn\u0027t have secure private key storage capabilities yet.\n    - Started work on integrating the provider agent with a wallet agent.\n---\n`~lomped-firser`\n---\n\n- _Mapsmithing_\n\n---\n`~littel-wolfur`\n---\n\n- _odyssey_\n\t- (delayed a bit for IRL stuff, shoot me a dm if you want to help out)\n- _srrs_\n  \t- on hiatus (though still making incremental improvements)\n    - recent update borked it, still investigating\n    \n---\n`~lanrus-rinfep`\n---\n\n- _ursus_ work ongoing\n\n---\nrss reader (not sure of author)\n---\n-  needs an update to support new links\n- `https://github.com/clonex10100/hoon-rss"
                            }
                          ]
                        }
                      }
                    ]
                  ]
                }
              ]
            ]
          }
        ],
        [
          "/170141184504624283413921098053476941824",
          {
            "post": {
              "index": "/170141184504624283413921098053476941824",
              "author": "littel-wolfur",
              "time-sent": 1597686580280,
              "signatures": [],
              "contents": []
            },
            "children": [
              [
                "/2",
                {
                  "post": {
                    "index": "/170141184504624283413921098053476941824/2",
                    "author": "littel-wolfur",
                    "time-sent": 1597686580280,
                    "signatures": [],
                    "contents": []
                  }
                }
              ],
              [
                "/1",
                {
                  "post": {
                    "index": "/170141184504624283413921098053476941824/1",
                    "author": "littel-wolfur",
                    "time-sent": 1597686580280,
                    "signatures": [],
                    "contents": []
                  },
                  "children": [
                    [
                      "/2",
                      {
                        "post": {
                          "index": "/170141184504624283413921098053476941824/1/2",
                          "author": "littel-wolfur",
                          "time-sent": 1606091023537,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates - August"
                            },
                            {
                              "text": "\n# New Projects\n---\n## picky\n_~timluc-miptev_\n[github](https://github.com/timlucmiptev/picky)\n\u003e See who is active or not in your chats. Keep control of your groups as Urbit expands.\n\n## vuvuzela\n_~raltem-lartem_\n[github](https://github.com/Electr0phile/vuvuzela-urbit/tree/master)\n\u003e [Vuvuzela messenger](https://github.com/vuvuzela/vuvuzela) implementation on [Urbit](https://urbit.org/).\n\n## Urbit RO\n_~littel-wolfur_\n\u003e Ragnarok Online private server supporting account creation with an Urbit ID. Character state management in urbit. \n\n# Project Updates\n---\n  - [srrs](https://github.com/ryjm/srrs) contributions\n    - Review All button (_~fostyr-solfyr_)\n    - Frontend cleanup (_~radbur-sivmus_)\n  - [ucal](https://github.com/taalhavras/ucal) development has been ongoing\n"
                            }
                          ]
                        }
                      }
                    ],
                    [
                      "/1",
                      {
                        "post": {
                          "index": "/170141184504624283413921098053476941824/1/1",
                          "author": "littel-wolfur",
                          "time-sent": 1597686580280,
                          "signatures": [],
                          "contents": [
                            {
                              "text": "Project Updates - Week of 8/17"
                            },
                            {
                              "text": "\n# New Projects\n---\n## picky\n_~timluc-miptev_\n[github](https://github.com/timlucmiptev/picky)\n\u003e See who is active or not in your chats. Keep control of your groups as Urbit expands.\n\n## vuvuzela\n_~raltem-lartem_\n[github](https://github.com/Electr0phile/vuvuzela-urbit/tree/master)\n\u003e [Vuvuzela messenger](https://github.com/vuvuzela/vuvuzela) implementation on [Urbit](https://urbit.org/).\n\n## Urbit RO\n_~littel-wolfur_\n\u003e Ragnarok Online private server supporting account creation with an Urbit ID. Character state management in urbit. \n\n# Project Updates\n---\n  - [srrs](https://github.com/ryjm/srrs) contributions\n    - Review All button (_~fostyr-solfyr_)\n    - Frontend cleanup (_~radbur-sivmus_)\n  - [ucal](https://github.com/taalhavras/ucal) development has been ongoing\n"
                            }
                          ]
                        }
                      }
                    ]
                  ]
                }
              ]
            ]
          }
        ]
      ],
      "resource": {
        "name": "announcements",
        "ship": "littel-wolfur"
      },
      "mark": "graph-validator-publish",
      "overwrite": true
    }
  }
}
```


