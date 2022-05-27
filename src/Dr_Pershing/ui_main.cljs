(ns Dr-Pershing.ui-main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [goog.events]

   ["react" :as react]
   ["react-dom/client" :as react-dom.client]

   [reagent.core]
   [reagent.dom]

   [reitit.frontend]
   [reitit.frontend.easy]
   [reitit.coercion.spec]
   [reitit.frontend.controllers]
   [reitit.frontend.history]
   [spec-tools.data-spec]

   ["antd/lib/layout" :default AntdLayout]
   ["antd/lib/menu" :default AntdMenu]
   ["antd/lib/button" :default AntdButton]
   ["antd/lib/row" :default AntdRow]
   ["antd/lib/col" :default AntdCol]
   ["antd/lib/input" :default AntdInput]
   ["antd/lib/table" :default AntdTable]


   [clojure.test.check.generators]
   [clojure.spec.alpha :as s]

   [Dr-Pershing.ui-seed :refer [root]]
   [Dr-Pershing.ui-beans]
   #_[Dr-Pershing.Ritchi]))


(defn rc-main-page
  []
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:div {}
    [:div "at the rist of getting my mouth shut - you are wrong"]]])

(defn rc-settings-page
  []
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:> AntdRow
    "settings"
    #_(str "settings" (:rand-int @stateA))]])

(defn rc-brackets-page
  []
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:> AntdRow
    "brackets"
    #_(str "settings" (:rand-int @stateA))]])

(defn rc-query-page
  []
  [:> (.-Content AntdLayout)
   {:style {:background-color "white"}}
   [:> AntdRow
    "query"
    #_(str "settings" (:rand-int @stateA))]])

(defn rc-current-page
  []
  (reagent.core/with-let
    [route-keyA (reagent.core/cursor (:matchA root) [:data :name])]
    (let [route-key @route-keyA]
      [:> AntdLayout
       [:> AntdMenu
        {:mode "horizontal"
         :size "large"
         :selectedKeys [route-key]
         :items [{:type "item"
                  :label
                  (reagent.core/as-element [:a {:href (reitit.frontend.easy/href :rc-main-page)} "raiting"])
                  :key :rc-rating-page
                  :icon nil}
                 {:type "item"
                  :label
                  (reagent.core/as-element [:a {:href (reitit.frontend.easy/href :rc-brackets-page)} "brackets"])
                  :key :rc-brackets-page
                  :icon nil}
                 {:type "item"
                  :label
                  (reagent.core/as-element [:a {:href (reitit.frontend.easy/href :rc-query-page)} "query"])
                  :key :rc-query-page
                  :icon nil}
                 #_{:type "item"
                    :label
                    (reagent.core/as-element [:a {:href (reitit.frontend.easy/href :rc-settings-page)} "settings"])
                    :key :rc-settings-page
                    :icon nil}]}]
       (when-let [match @(:matchA root)]
         [(-> match :data :view)])])
    #_[:<>

       [:ul
        [:li [:a {:href (reitit.frontend.easy/href :rc-main-page)} "game"]]
        [:li [:a {:href (reitit.frontend.easy/href :rc-settings-page)} "settings"]]]
       (when-let [match @matchA]
         [(-> match :data :view) match stateA])]))

(defn websocket-process
  [{:keys [send| recv|]
    :as opts}]
  (let [socket (js/WebSocket. "ws://localhost:3366/ui")]
    (.addEventListener socket "open" (fn [event]
                                       (println :websocket-open)
                                       (put! send| {:op :ping
                                                    :from :ui
                                                    :if :there-is-sompn-strage-in-your-neighbourhood
                                                    :who :ya-gonna-call?})))
    (.addEventListener socket "message" (fn [event]
                                          (put! recv| (read-string (.-data event)))))
    (.addEventListener socket "close" (fn [event]
                                        (println :websocket-close event)))
    (.addEventListener socket "error" (fn [event]
                                        (println :websocket-error event)))
    (go
      (loop []
        (when-let [value (<! send|)]
          (.send socket (str value))
          (recur))))))

(defn router-process
  [{:keys []
    :as opts}]
  (let [history (reitit.frontend.easy/start!
                 (reitit.frontend/router
                  ["/"
                   [""
                    {:name :rc-main-page
                     :view Dr-Pershing.ui-beans/rc-page
                     :controllers [{:start (fn [_]
                                             (js/console.log "start rc-main-page"))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-main-page"))}]}]

                   ["rating"
                    {:name :rc-rating-page
                     :view Dr-Pershing.ui-beans/rc-page
                     :controllers [{:start (fn [_]
                                             #_(js/console.log "start rc-rating-page")
                                             (Dr-Pershing.ui-beans/process {}))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-rating-page"))}]}]

                   ["brackets"
                    {:name :rc-brackets-page
                     :view rc-brackets-page
                     :controllers [{:start (fn [_]
                                             (js/console.log "start rc-brackets-page"))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-brackets-page"))}]}]

                   ["query"
                    {:name :rc-query-page
                     :view rc-query-page
                     :controllers [{:start (fn [_]
                                             (js/console.log "start rc-query-page"))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-query-page"))}]}]

                   #_["settings"
                      {:name :rc-settings-page
                       :view rc-settings-page
                       :controllers [{:start (fn [_]
                                               (js/console.log "start rc-settings-page"))
                                      :stop (fn [_]
                                              (js/console.log "stop rc-settings-page"))}]}]]
                  {:data {:controllers [{:start (fn [_]
                                                  (js/console.log "start program")
                                                  (websocket-process {:send| (:program-send| root)
                                                                      :recv| (:ops| root)}))
                                         :stop (fn [_]
                                                 (js/console.log "stop program"))}]
                          :coercion reitit.coercion.spec/coercion}})
                 (fn [new-match]
                   (swap! (:matchA root) (fn [old-match]
                                           (if new-match
                                             (assoc new-match :controllers (reitit.frontend.controllers/apply-controllers (:controllers old-match) new-match))))))
                 {:use-fragment false})]
    (goog.events/unlistenByKey (:click-listen-key history))
    (goog.events/listen js/document goog.events.EventType.CLICK
                        (fn [event]
                          (when-let [element (reitit.frontend.history/closest-by-tag
                                              (reitit.frontend.history/event-target event) "a")]
                            (let [uri (.parse goog.Uri (.-href element))]
                              (when (reitit.frontend.history/ignore-anchor-click? (.-router history) event element uri)
                                (.preventDefault event)
                                (let [path (str (.getPath uri)
                                                (when (.hasQuery uri)
                                                  (str "?" (.getQuery uri)))
                                                (when (.hasFragment uri)
                                                  (str "#" (.getFragment uri))))]
                                  (.pushState js/window.history nil "" path)
                                  (reitit.frontend.history/-on-navigate history path)))))) true)))

(def colors
  {:sands "#edd3af" #_"#D2B48Cff"
   :Korvus "lightgrey"
   :signal-tower "brown"
   :recharge "#30ad23"
   :Inaros "blue"})


(defmulti op :op)

(defmethod op :ping
  [value]
  (go
    (Wichita.pprint/pprint value)
    (put! (:program-send| root) {:op :pong
                                 :from :ui
                                 :moneybuster :Jesus})))

(defmethod op :pong
  [value]
  (go
    (Wichita.pprint/pprint value)))

(defn ops-process
  [{:keys []
    :as opts}]
  (go
    (loop []
      (when-let [value (<! (:ops| root))]
        (<! (op value))
        (recur)))))

(defn -main
  []
  (go
    (<! (timeout 1000))
    (println "twelve is the new twony")
    (println ":Madison you though i was a zombie?")
    (println ":Columbus yeah, of course - a zombie")
    (println ":Madison oh my God, no - i dont even eat meat - i'm a vegatarian - vegan actually")
    #_(set! (.-innerHTML (.getElementById js/document "ui"))
            ":Co-Pilot i saw your planet destroyed - i was on the Death Star :_ which one?")
    (router-process {})
    (ops-process {})
    (.render @(:dom-rootA root) (reagent.core/as-element [rc-current-page]))
    #_(reitit.frontend.easy/push-state :rc-main-page)))

(defn reload
  []
  (when-let [dom-root @(:dom-rootA root)]
    (.unmount dom-root)
    (let [new-dom-root (react-dom.client/createRoot (.getElementById js/document "ui"))]
      (reset! (:dom-rootA root) new-dom-root)
      (.render @(:dom-rootA root) (reagent.core/as-element [rc-current-page])))))

#_(-main)