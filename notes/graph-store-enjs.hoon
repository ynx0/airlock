++  enjs
  =,  enjs:format
  |%
  ::
  ++  signatures
    |=  s=^signatures
    ^-  json
    [%a (turn ~(tap in s) signature)]
  ::
  ++  signature
    |=  s=^signature
    ^-  json
    %-  pairs
    :~  [%signature s+(scot %ux p.s)]
        [%ship (ship q.s)]
        [%life (numb r.s)]
    ==
  ::
  ++  index
    |=  i=^index
    ^-  json
    ?:  =(~ i)  s+'/'
    =/  j=^tape  ""
    |-
    ?~  i  [%s (crip j)]
    =/  k=json  (numb i.i)
    ?>  ?=(%n -.k)
    %_  $
        i  t.i
        j  (weld j (weld "/" (trip +.k)))
    ==
  ::
  ++  uid
    |=  u=^uid
    ^-  json
    %-  pairs
    :~  [%resource (enjs:res resource.u)]
        [%index (index index.u)]
    ==
  ::
  ++  content
    |=  c=^content
    ^-  json
    ?-  -.c
        %mention    (frond %mention (ship ship.c))
        %text       (frond %text s+text.c)
        %url        (frond %url s+url.c)
        %reference  (frond %reference (uid uid.c))
        %code
      %+  frond  %code
      %-  pairs
      :-  [%expression s+expression.c]
      :_  ~
      :-  %output
      ::  virtualize output rendering, +tank:enjs:format might crash
      ::
      =/  result=(each (list json) tang)
        (mule |.((turn output.c tank)))
      ?-  -.result
        %&  a+p.result
        %|  a+[a+[%s '[[output rendering error]]']~]~
      ==
    ==
  ::
  ++  post
    |=  p=^post
    ^-  json
    %-  pairs
    :~  [%author (ship author.p)]
        [%index (index index.p)]
        [%time-sent (time time-sent.p)]
        [%contents [%a (turn contents.p content)]]
        [%hash ?~(hash.p ~ s+(scot %ux u.hash.p))]
        [%signatures (signatures signatures.p)]
    ==
  ::
  ++  update
    |=  upd=^update
    ^-  json
    ?>  ?=(%0 -.upd)
    |^  (frond %graph-update (pairs ~[(encode q.upd)]))
    ::
    ++  encode
      |=  upd=update-0
      ^-  [cord json]
      ?-  -.upd
          %add-graph
        :-  %add-graph
        %-  pairs
        :~  [%resource (enjs:res resource.upd)]
            [%graph (graph graph.upd)]
            [%mark ?~(mark.upd ~ s+u.mark.upd)]
            [%overwrite b+overwrite.upd]
        ==
      ::
          %remove-graph
        [%remove-graph (enjs:res resource.upd)]
      ::
          %add-nodes
        :-  %add-nodes
        %-  pairs
        :~  [%resource (enjs:res resource.upd)]
            [%nodes (nodes nodes.upd)]
        ==
      ::
          %remove-nodes
        :-  %remove-nodes
        %-  pairs
        :~  [%resource (enjs:res resource.upd)]
            [%indices (indices indices.upd)]
        ==
      ::
          %add-signatures
        :-  %add-signatures
        %-  pairs
        :~  [%uid (uid uid.upd)]
            [%signatures (signatures signatures.upd)]
        ==
      ::
          %remove-signatures
        :-  %remove-signatures
        %-  pairs
        :~  [%uid (uid uid.upd)]
            [%signatures (signatures signatures.upd)]
        ==
      ::
          %add-tag
        :-  %add-tag
        %-  pairs
        :~  [%term s+term.upd]
            [%resource (enjs:res resource.upd)]
        ==
      ::
          %remove-tag
        :-  %remove-tag
        %-  pairs
        :~  [%term s+term.upd]
            [%resource (enjs:res resource.upd)]
        ==
      ::
          %archive-graph
        [%archive-graph (enjs:res resource.upd)]
      ::
          %unarchive-graph
        [%unarchive-graph (enjs:res resource.upd)]
      ::
          %keys
        [%keys [%a (turn ~(tap in resources.upd) enjs:res)]]
      ::
          %tags
        [%tags [%a (turn ~(tap in tags.upd) |=(=term s+term))]]
      ::
          %run-updates
        [%run-updates ~]
      ::
          %tag-queries
        :-  %tag-queries
        %-  pairs
        %+  turn  ~(tap by tag-queries.upd)
        |=  [=term =resources]
        ^-  [cord json]
        [term [%a (turn ~(tap in resources) enjs:res)]]
      ==
    ::
    ++  graph
      |=  g=^graph
      ^-  json
      :-  %a
      %+  turn  (tap:orm g)
      |=  [a=atom n=^node]
      ^-  json
      :-  %a
      :~  (index [a]~)
          (node n)
      ==
    ++  node
      |=  n=^node
      ^-  json
      %-  pairs
      :~  [%post (post post.n)]
          :-  %children
          ?-  -.children.n
              %empty  ~
              %graph  (graph +.children.n)
          ==
      ==
    ::
            ::
    ++  nodes
      |=  m=(map ^index ^node)
      ^-  json
      :-  %a
      %+  turn  ~(tap by m)
      |=  [n=^index o=^node]
      ^-  json
      :-  %a
      :~  (index n)
          (node o)
      ==
    ::
    ++  indices
      |=  i=(set ^index)
      ^-  json
      [%a (turn ~(tap in i) index)]
    ::
    --
  --
::