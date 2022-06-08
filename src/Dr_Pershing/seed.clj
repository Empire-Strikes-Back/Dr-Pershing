(ns Dr-Pershing.seed
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]
   [clojure.repl])
  (:import
   (java.io File)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defmulti op :op)

(defonce root (let [program-data-dirpath (or
                                          (some-> (System/getenv "DR_PERSHING_PATH")
                                                  (.replaceFirst "^~" (System/getProperty "user.home")))
                                          (.getCanonicalPath ^File (clojure.java.io/file (System/getProperty "user.home") ".Dr-Pershing")))]
                {:program-data-dirpath program-data-dirpath
                 :state-file-filepath (.getCanonicalPath ^File (clojure.java.io/file program-data-dirpath "Dr-Pershing.edn"))
                 :db-data-dirpath (.getCanonicalPath ^File (clojure.java.io/file program-data-dirpath "db"))
                 :port (or (try (Integer/parseInt (System/getenv "PORT"))
                                (catch Exception e nil))
                           3366)
                 :stateA (atom nil)
                 :host| (chan 1)
                 :ops| (chan 10)
                 :ui-send| (chan 10)}))