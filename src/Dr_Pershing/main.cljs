(ns Dr-Pershing.main
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string]
   [clojure.pprint :as clojure.pprint]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [taoensso.timbre :as taoensso.timbre]
   #_[datahike.api :as Arthur-Dent.api]

   [Dr-Pershing.seed :refer [root op]]
   [Dr-Pershing.dates]
   [Dr-Pershing.pumpkin-seeds]
   [Dr-Pershing.grapefruit]
   [Dr-Pershing.salt]
   [Dr-Pershing.radish]
   [Dr-Pershing.rolled-oats]))

(defonce os (js/require "os"))
(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(set! (.-defaultMaxListeners (.-EventEmitter (js/require "events"))) 100)
(set! (.-AbortController js/global) (.-AbortController (js/require "node-abort-controller")))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IPFSHttpClient (js/require "ipfs-http-client"))
(defonce IPFS (js/require "ipfs"))
(defonce electron (js/require "electron"))

(taoensso.timbre/merge-config! {:level :info
                                :min-level :info})

(defmethod op :ping
  [value]
  (go
    (clojure.pprint/pprint value)
    (put! (:ui-send| root) {:op :pong
                            :from :program
                            :meatbuster :Jesus})))

(defmethod op :pong
  [value]
  (go
    (clojure.pprint/pprint value)))

(defmethod op :game
  [value]
  (go))

(defmethod op :leave
  [value]
  (go))

(defmethod op :discover
  [value]
  (go))

(defmethod op :settings
  [value]
  (go))

(defn ops-process
  [{:keys []
    :as opts}]
  (go
    (loop []
      (when-let [value (<! (:ops| root))]
        (<! (op value))
        (recur)))))

(defn -main []
  (go
    (let []

      (println "if it wansn't for me he would already be dead - please! please!")
      (println "i dont want my next job")
      (println "Kuiil has spoken")
      (println (format "i store data in %s" (:program-data-dirpath root)))

      (.ensureDirSync fs (:program-data-dirpath root) #_(clj->js {:mode 0777}))
      (.ensureDirSync fs (:indexeddb-data-dirpath root))
      (.ensureDirSync fs (:orbitdb-data-dirpath root))

      (remove-watch (:stateA root) :watch-fn)
      (add-watch (:stateA root) :watch-fn
                 (fn [ref wathc-key old-state new-state]

                   (when (not= old-state new-state))))

      (ops-process {})

      (let [done| (chan 1)]
        (.on (.-app electron) "ready"
             (fn []
               (reset! (:windowA root) (electron.BrowserWindow.
                                        (clj->js {:width 1600
                                                  :height 900
                                                  #_:title #_"the human inside was the final weakness to be solved"
                                                  :icon (.join path js/__dirname "icon.png")
                                                  :webPreferences {:nodeIntegration true
                                                                   :contextIsolation false}})))
               (.loadURL ^js/electron.BrowserWindow @(:windowA root)
                         (str "file://" (.join path js/__dirname "ui" "index.html")))
               (.on ^js/electron.BrowserWindow @(:windowA root) "closed" #(reset! (:windowA root) nil))
               (.on (.-webContents @(:windowA root)) "did-finish-load"
                    (fn []
                      (put! (:ui-send| root) {:op :ping
                                              :if :you-re-seeing-things-running-through-your-head
                                              :who :ya-gonna-call?})))
               (close! done|)))

        (.on (.-app electron) "window-all-closed" (fn []
                                                    (when-not (= js/process.platform "darwin")
                                                      (.quit (.-app electron)))))
        (.on (.-app electron) "error" (fn [ex]
                                        (js/console.log ex)
                                        (close! done|)))
        (<! done|))



      (let []
        (.on (.-ipcMain electron) "asynchronous-message" (fn [event message-string]
                                                           (put! (:ops| root) (-> message-string #_(.toString) (read-string)))))
        (go
          (loop []
            (when-let [message (<! (:ui-send| root))]
              (.send (.-webContents @(:windowA root)) "asynchronous-message" (str message))
              (recur)))))

      (let [ipfs (.create IPFSHttpClient "http://127.0.0.1:5001")
            orbitdb (<p!
                     (->
                      (.createInstance
                       OrbitDB ipfs
                       (clj->js
                        {"directory" (:orbitdb-data-dirpath root)}))
                      (.catch (fn [ex]
                                (println ex)))))]
        (println (.. orbitdb -identity -id))
        (<! (Dr-Pershing.rolled-oats/process {:orbitdb orbitdb}))))))


(comment

  (<p! (.create IPFS (clj->js
                      {:repo (:orbitdb-data-dirpath root)})))

  ;
  )