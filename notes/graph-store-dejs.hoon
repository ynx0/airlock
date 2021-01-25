++  dejs
  =,  dejs:format
  |%
  ++  update
    |=  jon=json
    ^-  ^update
    :-  %0
    :-  *time
    ^-  update-0
    =<  (decode jon)
    |%
    ++  decode
      %-  of
      :~  [%add-nodes add-nodes]
          [%remove-nodes remove-nodes]
          [%add-signatures add-signatures]
          [%remove-signatures remove-signatures]
        ::
          [%add-graph add-graph]
          [%remove-graph remove-graph]
        ::
          [%add-tag add-tag]
          [%remove-tag remove-tag]
        ::
          [%archive-graph archive-graph]
          [%unarchive-graph unarchive-graph]
          [%run-updates run-updates]
        ::
          [%keys keys]
          [%tags tags]
          [%tag-queries tag-queries]
      ==
    ::
    ++  add-graph
      %-  ot
      :~  [%resource dejs:res]
          [%graph graph]
          [%mark (mu so)]
          [%overwrite bo]
      ==
    ::
    ++  graph
      |=  a=json
      ^-  ^graph
      =/  or-mp  ((ordered-map atom ^node) gth)
      %+  gas:or-mp  ~
      %+  turn  ~(tap by ((om node) a))
      |*  [b=cord c=*]
      ^-  [atom ^node]
      =>  .(+< [b c]=+<)
      [(rash b dem) c]
    ::
    ++  remove-graph  (ot [%resource dejs:res]~)
    ++  archive-graph  (ot [%resource dejs:res]~)
    ++  unarchive-graph  (ot [%resource dejs:res]~)
    ::
    ++  add-nodes
      %-  ot
      :~  [%resource dejs:res]
          [%nodes nodes]
      ==
    ::
    ++  nodes  (op ;~(pfix fas (more fas dem)) node)
    ::
    ++  node
      %-  ot
      :~  [%post post]
          [%children internal-graph]
      ==
    ::
    ++  internal-graph
      ^-  $-(json ^internal-graph)
      %-  of
      :~  [%empty ul]
          [%graph graph]
      ==
    ::
    ++  post
      %-  ot
      :~  [%author (su ;~(pfix sig fed:ag))]
          [%index index]
          [%time-sent di]
          [%contents (ar content)]
          [%hash (mu nu)]
          [%signatures (as signature)]
      ==
    ::
    ++  content
      %-  of
      :~  [%mention (su ;~(pfix sig fed:ag))]
          [%text so]
          [%url so]
          [%reference uid]
          [%code eval]
      ==
    ::
    ++  eval
      |=  a=^json
      ^-  [cord (list tank)]
      =,  ^?  dejs-soft:format
      =+  exp=((ot expression+so ~) a)
      %-  need
      ?~  exp  [~ '' ~]
      :+  ~  u.exp
      ::  NOTE: when sending, if output is an empty list,
      ::  graph-store will evaluate
      (fall ((ot output+(ar dank) ~) a) ~)
    ::
    ++  remove-nodes
      %-  ot
      :~  [%resource dejs:res]
          [%indices (as index)]
      ==
    ::
    ++  add-signatures
      %-  ot
      :~  [%uid uid]
          [%signatures (as signature)]
      ==
    ::
    ++  remove-signatures
      %-  ot
      :~  [%uid uid]
          [%signatures (as signature)]
      ==
    ::
    ++  signature
      %-  ot
      :~  [%hash nu]
          [%ship (su ;~(pfix sig fed:ag))]
          [%life ni]
      ==
    ::
    ++  uid
      %-  ot
      :~  [%resource dejs:res]
          [%index index]
      ==
    ::
    ++  index  (su ;~(pfix fas (more fas dem)))
    ::
    ++  add-tag
      %-  ot
      :~  [%term so]
          [%resource dejs:res]
      ==
    ::
    ++  remove-tag
      %-  ot
      :~  [%term so]
          [%resource dejs:res]
      ==
    ::
    ++  keys
      |=  =json
      *resources
    ::
    ++  tags
      |=  =json
      *(set term)
    ::
    ++  tag-queries
      |=  =json
      *^tag-queries
    ::
    ++  run-updates
      |=  a=json
      ^-  [resource update-log]
      [*resource *update-log]
    --
  --