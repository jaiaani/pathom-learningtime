(ns learning-time.core
  (:require [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [learning-time.utils :as utils]
            ))

(pc/defresolver answer [_ _]
                {::pc/output [:answer-to-everything]}
                {:answer-to-everything 42})

(pc/defresolver answer-plus-one [_ {:keys [answer-to-everything]}]
                {::pc/input  #{:answer-to-everything}
                 ::pc/output [:answer-plus-one]}
                {:answer-plus-one (inc answer-to-everything)})

(pc/defresolver person-resolver
                [_ __]
                {::pc/output [:person/first-name :person/age :person/last-name]}
                {:person/age        28
                 :person/first-name "Sara"
                 :person/last-name  "Last"})

(pc/defresolver person-full-name-resolver
                [_ {:person/keys [first-name last-name] }]
                {::pc/input #{:person/first-name :person/last-name}
                 ::pc/output [:person/full-name]}
                {:person/full-name (str first-name " " last-name)})

(pc/defresolver pokemon-resolver
                [_ {:pokemon/keys [name]}]
                { ::pc/input #{:pokemon/name}
                 ::pc/output [:pokemon/id]}
                {:pokemon/id (:id (utils/http-get (str "https://pokeapi.co/api/v2/pokemon/" name)))})

(pc/defresolver pokemon-id-resolver
                [_ {:pokemon/keys [id]}]
                { ::pc/input #{:pokemon/id}
                 ::pc/output [:pokemon/name]}
                {:pokemon/name (:name (utils/http-get (str "https://pokeapi.co/api/v2/pokemon/" id)))})

(def registry
  [answer answer-plus-one person-resolver person-full-name-resolver pokemon-resolver pokemon-id-resolver])

(def parser
  (p/parser
    {::p/env     {::p/reader               [p/map-reader
                                            pc/reader2
                                            pc/open-ident-reader
                                            p/env-placeholder-reader]
                  ::p/placeholder-prefixes #{">"}}
     ::p/mutate  pc/mutate
     ::p/plugins [(pc/connect-plugin {::pc/register registry})
                  p/error-handler-plugin
                  p/trace-plugin]}))



(comment
  ; to call the parser and get some data out of it, run:
  (parser {} [{[:pokemon/id 25] [:pokemon/name]} {[:pokemon/name "charmander"] [:pokemon/id]}]))
