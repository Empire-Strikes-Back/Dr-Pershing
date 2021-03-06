(ns Dr-Pershing.pumpkin-seeds
  (:require
   [clojure.core.async
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]

   [Dr-Pershing.seed]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))