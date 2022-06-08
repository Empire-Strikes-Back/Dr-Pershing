(ns Dr-Pershing.query
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as clojure.java.io]
   [clojure.string :as clojure.string]

   [datahike.api :as datahike.api]
   [Dr-Pershing.seed :refer [root]]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defmulti q :q)

(defmethod q :all-attributes
  [{:keys [conn]
    :as opts}]
  (datahike.api/q '[:find [?ident ...]
                        :where [_ :db/ident ?ident]]
                      @conn))