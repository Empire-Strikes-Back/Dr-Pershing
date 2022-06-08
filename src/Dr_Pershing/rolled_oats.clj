(ns Dr-Pershing.rolled-oats
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as clojure.java.io]
   [clojure.string :as clojure.string]

   [relative.trueskill :as Chip.trueskill]
   [relative.elo :as Chip.elo]
   [relative.rating :as Chip.rating]
   [glicko2.core :as Dale.core]

   [datahike.api :as datahike.api]

   [Dr-Pershing.seed :refer [root op]]
   [Dr-Pershing.query]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn column-names
  [conn]
  (->>
   (Dr-Pershing.query/q {:q :all-attributes :conn conn})
   (sort)
   (into []
         (comp
          (keep (fn [attr] (if (#{:id :name} attr) nil attr)))
          (map name)))))

(defmethod op :beans/create-database
  [value]
  (go
    (println value)
    #_(put! (:ui-send| root) {:op :beans/database-created})
    #_(let [settings-jframe (JFrame. "settings")]
        (Dr-Pershing.grapefruit/settings-process
         {:jframe settings-jframe
          :root-jframe jframe
          :ops| ops|
          :settingsA settingsA})
        (reset! settingsA @settingsA))))

(defn process
  [{:keys []
    :as opts}]
  (let [ops| (chan 10)
        config-databases {:store {:backend :file :path (:db-data-dirpath root)}
                          :keep-history? true
                          :name ":database"}
        _ (when-not (datahike.api/database-exists? config-databases)
            (datahike.api/create-database config-databases))
        conn-databases (datahike.api/connect config-databases)
        schema-databases (read-string (slurp (clojure.java.io/resource "Dr_Pershing/schema.edn")))]
    (let []
      (datahike.api/transact conn-databases schema-databases)
      (->>
       (Dr-Pershing.query/q {:q :all-attributes :conn conn-databases})
       (sort)
       (println)))))