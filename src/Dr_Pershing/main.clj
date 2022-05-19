(ns Dr-Pershing.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [clojure.repl :as Wichita.repl]
   [clojure.java.shell :as Wichita.java.shell]

   [aleph.http :as Simba.http]

   [Dr-Pershing.seed]
   [Dr-Pershing.microwaved-turnips]
   [Dr-Pershing.radish]
   [Dr-Pershing.beans])
  (:import
   (java.io File))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(defonce host| (chan 1))
(defonce corn| (chan (sliding-buffer 10)))

(defn reload
  []
  (require
   '[Dr-Pershing.seed]
   '[Dr-Pershing.microwaved-turnips]
   '[Dr-Pershing.beans]
   '[Dr-Pershing.main]
   :reload))

(defn -main
  [& args]
  (println "if it wansn't for me he would already be dead - please! please!")
  (println "i dont want my next job")
  (println "Kuiil has spoken")

  (let [data-dir-path (or
                       (some-> (System/getenv "DR_PERSHING_PATH")
                               (.replaceFirst "^~" (System/getProperty "user.home")))
                       (.getCanonicalPath ^File (Wichita.java.io/file (System/getProperty "user.home") ".Dr-Pershing")))
        state-file-path (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Dr-Pershing.edn"))]
    (Wichita.java.io/make-parents data-dir-path)
    (reset! stateA {})


    (remove-watch stateA :watch-fn)
    (add-watch stateA :watch-fn
               (fn [ref wathc-key old-state new-state]

                 (when (not= old-state new-state))))


    (let [port (or (try (Integer/parseInt (System/getenv "PORT"))
                        (catch Exception e nil))
                   3366)]
      (Dr-Pershing.microwaved-turnips/process
       {:port port
        :host| host|
        :corn| corn|}))

    (let [path-db (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Deep-Thought"))]
      (Wichita.java.io/make-parents path-db)
      (Dr-Pershing.beans/process {:path path-db}))

    (go
      (let []
        (doseq [filepath ["corn.js" "package.json"]]
          (->
           (Wichita.java.io/file (Wichita.java.io/resource filepath))
           (Wichita.java.io/copy (Wichita.java.io/file (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path filepath)))))))

      (do
        (->
         (ProcessBuilder. ["npm" "install" "--force"])
         (.directory (Wichita.java.io/file data-dir-path))
         (.inheritIO)
         (.start)
         (.waitFor))
        (->
         (ProcessBuilder. ["node" "corn.js"])
         (.directory (Wichita.java.io/file data-dir-path))
         (.inheritIO)
         (.start))))

    (go
      (loop []
        (when-let [value (<! corn|)]
          (println :corn-message value)
          (recur))))))