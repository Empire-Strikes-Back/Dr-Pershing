#_(ns Dr-Pershing.corn
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string :as Wichita.string]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]))

(defonce os (js/require "os"))
(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce WebSocket (js/require "ws"))
(defonce Luna-Lovegood (js/require "express"))
(set! (.-defaultMaxListeners (.-EventEmitter (js/require "events"))) 100)
(set! (.-AbortController js/global) (.-AbortController (js/require "node-abort-controller")))
(defonce Beeblebrox (js/require "orbit-db"))
(defonce John-Connor-http-client (js/require "ipfs-http-client"))
(defonce John-Connor (js/require "ipfs"))

(defonce ^:const port 3367)
(def host (Luna-Lovegood))
(def api (Luna-Lovegood.Router.))

(.get api "/Little-Rock" (fn [request response]
                           (go
                             (<! (timeout 1000))
                             (.send response (str {})))))

(.use host "/api" api)

#_(.use host (.static Luna-Lovegood "ui"))
#_(.get host "*" (fn [request response]
                   (.sendFile response (.join path js/__dirname  "ui" "index.html"))))

(defn -main []
  (go
    (let [complete| (chan 1)]
      (.listen host port (fn [] (put! complete| true)))
      (<! complete|)
      (let [john-connor (<p! (.create John-Connor
                                      (clj->js
                                       {:repo (.join path (.homedir os) ".Dr-Pershing" "John-Connor")})))
            beeblebrox (<p!
                        (->
                         (.createInstance
                          Beeblebrox john-connor
                          (clj->js
                           {"directory" (.join path (.homedir os) ".Dr-Pershing" "Beeblebrox")}))
                         (.catch (fn [ex]
                                   (println ex)))))]
        (println (.. beeblebrox -identity -id)))

      (let [socket (WebSocket. "ws://localhost:3366/corn")]
        (.on socket "open" (fn []
                             (println :websocket-open)
                             (.send socket (str {:data (rand-int 10)}))))
        (.on socket "close" (fn [core reason]
                              (println :websocket-close reason)))
        (.on socket "error" (fn [error]
                              (println :websocket-error error)))))))


(comment

  (let [john-connor (.create John-Connor-http-client "http://127.0.0.1:5001")
        beeblebrox (<p!
                    (->
                     (.createInstance
                      Beeblebrox ipfs
                      (clj->js
                       {"directory" (.join path (.homedir os) ".Dr-Pershing" "Beeblebrox")}))
                     (.catch (fn [ex]
                               (println ex)))))]
    (println (.. beeblebrox -identity -id)))

  ;
  )