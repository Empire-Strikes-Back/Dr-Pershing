(ns Dr-Pershing.radish
  (:require
   [clojure.core.async
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]
   [clojure.pprint]
   [relative.trueskill]
   [relative.elo]
   [relative.rating]
   [glicko2.core]
   [Dr-Pershing.seed]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(comment

  (do
    (def matches (read-string (slurp (clojure.java.io/resource "matches.edn"))))
    (def competitors (read-string (slurp (clojure.java.io/resource "competitors.edn"))))


    (def engine #_(relative.trueskill/trueskill-engine) (relative.elo/elo-engine))

    (def competitors-map (into {}
                               (reduce (fn [result competitor]
                                         (conj result [(:competitor/id competitor) competitor]))
                                       [] competitors)))

    (def competitors-engine (into {}
                                  (reduce (fn [result [id competitor]]
                                            (conj result [id (relative.rating/player engine (merge competitor
                                                                                               {:id id}))]))
                                          [] competitors-map)))

    (def competitors-engineA (atom competitors-engine))

    (->>
     matches
     (map (fn [match]
            (let [competitor-a-engine (get @competitors-engineA (:match/competitor-a-id match))
                  competitor-b-engine (get @competitors-engineA (:match/competitor-b-id match))]
              (if (> (:match/competitor-a-score match) (:match/competitor-b-score match))
                (let [players (relative.rating/match engine competitor-a-engine competitor-b-engine)]
                  (swap! competitors-engineA update-in [(:id (first players))] merge (first players))
                  (swap! competitors-engineA update-in [(:id (second players))] merge (second players)))
                (let [players (relative.rating/match engine competitor-b-engine competitor-a-engine)]
                  (swap! competitors-engineA update-in [(:id (first players))] merge (first players))
                  (swap! competitors-engineA update-in [(:id (second players))] merge (second players))))))))

    )
  (let [rankings (->>
                  @competitors-engineA
                  (mapv (fn [[id competitor]]
                          competitor))
                  (sort-by :rating)
                  (reverse)
                  (mapv (fn [competitor]
                          [(:competitor/name competitor) (:competitor/surname competitor) (Float/parseFloat (format "%.1f" (:rating competitor)))])))]
    (spit "data/rating-elo.edn" (with-out-str (clojure.pprint/pprint rankings))))

  

  


  (doseq [competitor competitors-engine]
    (println competitor)
    (println (relative.rating/rating competitor)))




  ;
  )


(comment

  (def player1 (relative.rating/player elo-engine {:id "relative"}))
  (def player2 (relative.rating/player elo-engine {:id "glicko2"}))

  (relative.rating/rating player1)

  (relative.rating/match elo-engine player1 player2)
  
  (relative.rating/rating player1)

  ;
  )


(comment

  glicko2.core/get-rating

  glicko2.core/POINTS_FOR_WIN
  glicko2.core/POINTS_FOR_LOSS

  (do
    (def matches (read-string (slurp (clojure.java.io/resource "matches.edn"))))
    (def competitors (read-string (slurp (clojure.java.io/resource "competitors.edn"))))


    (def competitors-map (into {}
                               (reduce (fn [result competitor]
                                         (conj result [(:competitor/id competitor) competitor]))
                                       [] competitors))))



  (let [ratings (->>
                 (let [players (into {}
                                     (map (fn [[id competitor]]
                                            [id (merge competitor
                                                       {:rating 1500
                                                        :rd 200
                                                        :vol 0.06})]) competitors-map))
                       results (map (fn [match]
                                      {:player1 (:match/competitor-a-id match)
                                       :player2 (:match/competitor-b-id match)
                                       :result (if (> (:match/competitor-a-score match) (:match/competitor-b-score match))
                                                 glicko2.core/POINTS_FOR_WIN
                                                 glicko2.core/POINTS_FOR_LOSS)}) matches)

                       tau 0.5

                       new-ratings (glicko2.core/compute-ratings players results tau)]
                   new-ratings)
                 (mapv (fn [[id result]]
                         (let [competitor (get competitors-map id)]
                           (merge competitor result))))
                 (sort-by :rating)
                 (reverse)
                 (mapv (fn [competitor]
                         [(:competitor/name competitor)
                          (:competitor/surname competitor)
                          (:rating competitor) #_(Float/parseFloat (format "%.1f" (:rating competitor)))])))]

    (spit "data/rating-glicko2.edn" (with-out-str (clojure.pprint/pprint ratings))))


  ;
  )