(ns Dr-Pershing.main
  (:require
   [clojure.core.async
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.java.io]
   [clojure.string]
   [clojure.pprint]
   [clojure.repl]

   [cheshire.core]

   [datahike.api]
   [taoensso.timbre]

   [relative.trueskill]
   [relative.elo]
   [relative.rating]
   [glicko2.core]
   
   [Dr-Pershing.libp2p]

   [Dr-Pershing.seed]
   [Dr-Pershing.dates]
   [Dr-Pershing.pumpkin-seeds]
   [Dr-Pershing.grapefruit]
   [Dr-Pershing.salt]
   [Dr-Pershing.radish]
   [Dr-Pershing.rolled-oats])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel JTextPane JTextField JTable JTabbedPane)
   (javax.swing DefaultListSelectionModel JCheckBox UIManager)
   (javax.swing.border EmptyBorder)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent ComponentListener ComponentEvent)
   (java.awt.event  WindowListener WindowAdapter WindowEvent)
   (java.awt.geom Ellipse2D Ellipse2D$Double Point2D$Double)
   (com.formdev.flatlaf FlatLaf FlatLightLaf)
   (com.formdev.flatlaf.extras FlatUIDefaultsInspector FlatDesktop FlatDesktop$QuitResponse FlatSVGIcon)
   (com.formdev.flatlaf.util SystemInfo UIScale)
   (java.util.function Consumer)
   (java.util ServiceLoader)
   (net.miginfocom.swing MigLayout)
   (net.miginfocom.layout ConstraintParser LC UnitValue)
   (java.io File)
   (java.lang Runnable)
   (java.nio.charset StandardCharsets))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(taoensso.timbre/merge-config! {:min-level :warn})

(defonce program-data-dirpath (or
                        (some-> (System/getenv "DR_PERSHING_PATH")
                                (.replaceFirst "^~" (System/getProperty "user.home")))
                        (.getCanonicalPath ^File (clojure.java.io/file (System/getProperty "user.home") "Dr-Pershing"))))

(defonce state-file-filepath (.getCanonicalPath ^File (clojure.java.io/file program-data-dirpath "Dr-Pershing.edn")))

(defonce db-data-dirpath (.getCanonicalPath ^File (clojure.java.io/file program-data-dirpath "db")))

(defonce stateA (atom nil))
(defonce settingsA (atom nil))
(defonce resize| (chan (sliding-buffer 1)))
(defonce ops| (chan 10))
(def ^:dynamic ^JFrame jframe nil)
(def ^:dynamic ^JPanel jroot-panel nil)
(def ^:const jframe-title "the human inside was the final weakness to be solved")

(defn reload
  []
  (require
   '[Dr-Pershing.seed]
   '[Dr-Pershing.dates]
   '[Dr-Pershing.pumpkin-seeds]
   '[Dr-Pershing.grapefruit]
   '[Dr-Pershing.salt]
   '[Dr-Pershing.radish]
   '[Dr-Pershing.rolled-oats]
   '[Dr-Pershing.main]
   :reload))

(defn menubar-process
  [{:keys [^JMenuBar jmenubar
           ^JFrame jframe
           menubar|]
    :as opts}]
  (let [on-menubar-item (fn [f]
                          (reify ActionListener
                            (actionPerformed [_ event]
                              (SwingUtilities/invokeLater
                               (reify Runnable
                                 (run [_]
                                   (f _ event)))))))

        on-menu-item-show-dialog (on-menubar-item (fn [_ event] (JOptionPane/showMessageDialog jframe (.getActionCommand ^ActionEvent event) "menu bar item" JOptionPane/PLAIN_MESSAGE)))]
    (doto jmenubar
      (.add (doto (JMenu.)
              (.setText "program")
              (.setMnemonic \F)
              (.add (doto (JMenuItem.)
                      (.setText "settings")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_S (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \S)
                      (.addActionListener
                       (on-menubar-item (fn [_ event]
                                          (put! menubar| {:op :settings}))))))
              (.add (doto (JMenuItem.)
                      (.setText "exit")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Q (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \Q)
                      (.addActionListener (on-menubar-item (fn [_ event]
                                                             (.dispose jframe))))))))

      #_(.add (doto (JMenu.)
                (.setText "edit")
                (.setMnemonic \E)
                (.add (doto (JMenuItem.)
                        (.setText "undo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Z (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \U)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "redo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Y (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \R)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "cut")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_X (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \C)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "copy")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_C (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \O)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "paste")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_V (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \P)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "delete")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_DELETE 0))
                        (.setMnemonic \D)
                        (.addActionListener on-menu-item-show-dialog)))))))
  nil)

(defn toolbar-process
  [{:keys [^JToolBar jtoolbar]
    :as opts}]
  (let []
    (doto jtoolbar
      #_(.setMargin (Insets. 3 3 3 3))
      (.add (doto (JButton.)
              (.setToolTipText "new file")))
      (.add (doto (JButton.)
              (.setToolTipText "open file")))
      (.add (doto (JButton.)
              (.setToolTipText "save")))
      (.add (doto (JButton.)
              (.setToolTipText "undo")))
      (.add (doto (JButton.)
              (.setToolTipText "redo")))
      #_(.addSeparator)))
  nil)

(defn settings-process
  [{:keys [^JFrame root-jframe
           ^JFrame jframe
           ops|
           settingsA]
    :or {}
    :as opts}]
  (let [root-panel (JPanel.)
        jscroll-pane (JScrollPane.)

        jcheckbox-editor (JCheckBox.)]

    (doto jscroll-pane
      (.setViewportView root-panel)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

    (doto jframe
      (.add root-panel))

    (doto root-panel
      (.setLayout (MigLayout. "insets 10"))
      (.add (JLabel. ":editor?") "cell 0 0")
      (.add jcheckbox-editor "cell 0 0"))

    (.setPreferredSize jframe (Dimension. (* 0.8 (.getWidth root-jframe))
                                          (* 0.8 (.getHeight root-jframe))))

    (.addActionListener jcheckbox-editor
                        (reify ActionListener
                          (actionPerformed [_ event]
                            (SwingUtilities/invokeLater
                             (reify Runnable
                               (run [_]
                                 (put! ops| {:op :settings-value
                                             :editor? (.isSelected jcheckbox-editor)})))))))

    (remove-watch settingsA :settings-process)
    (add-watch settingsA :settings-process
               (fn [ref wathc-key old-state new-state]
                 (SwingUtilities/invokeLater
                  (reify Runnable
                    (run [_]
                      (.setSelected jcheckbox-editor (:editor? new-state)))))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
      (.pack)
      (.setLocationRelativeTo root-jframe)
      (.setVisible true)))
  nil)

(defn -main
  [& args]
  (println "if it wansn't for me he would already be dead - please! please!")
  (println "i dont want my next job")
  (println "Kuiil has spoken")

  #_(alter-var-root #'*ns* (constantly (find-ns 'Dr-Pershing.main)))

  (when SystemInfo/isMacOS
    (System/setProperty "apple.laf.useScreenMenuBar" "true")
    (System/setProperty "apple.awt.application.name" jframe-title)
    (System/setProperty "apple.awt.application.appearance" "system"))

  (when SystemInfo/isLinux
    (JFrame/setDefaultLookAndFeelDecorated true)
    (JDialog/setDefaultLookAndFeelDecorated true))

  (when (and
         (not SystemInfo/isJava_9_orLater)
         (= (System/getProperty "flatlaf.uiScale") nil))
    (System/setProperty "flatlaf.uiScale" "2x"))

  (FlatLaf/setGlobalExtraDefaults (java.util.Collections/singletonMap "@background" "#ffffff"))
  (FlatLightLaf/setup)

  #_(UIManager/put "background" Color/WHITE)
  (FlatLaf/updateUI)

  (FlatDesktop/setQuitHandler (reify Consumer
                                (accept [_ response]
                                  (.performQuit ^FlatDesktop$QuitResponse response))
                                (andThen [_ after] after)))

  (let [screenshotsMode? (Boolean/parseBoolean (System/getProperty "flatlaf.demo.screenshotsMode"))

        jframe (JFrame. jframe-title)
        jroot-panel (JPanel.)
        jmenubar (JMenuBar.)]

    (let []
      (clojure.java.io/make-parents program-data-dirpath)
      (reset! stateA {})
      (reset! settingsA {:editor? true})



      (let [jtabbed-pane (JTabbedPane.)
            jpanel-grapefruit (JPanel.)
            jpanel-radish (JPanel.)
            jpanel-rolled-oats (JPanel.)]

        (doto jtabbed-pane
          (.setTabLayoutPolicy JTabbedPane/SCROLL_TAB_LAYOUT)
          (.addTab "rating" jpanel-rolled-oats)
          (.addTab "brackets" jpanel-grapefruit)
          (.setSelectedIndex 0))

        (Dr-Pershing.rolled-oats/process {:jpanel-tab jpanel-rolled-oats
                                          :db-data-dirpath db-data-dirpath})

        (.add jroot-panel jtabbed-pane))

      (clojure.java.io/make-parents db-data-dirpath)
      (let [config {:store {:backend :file :path db-data-dirpath}
                    :keep-history? true
                    :name "main"}
            _ (when-not (datahike.api/database-exists? config)
                (datahike.api/create-database config))
            conn (datahike.api/connect config)]

        (datahike.api/transact
         conn
         [{:db/cardinality :db.cardinality/one
           :db/ident :id
           :db/unique :db.unique/identity
           :db/valueType :db.type/uuid}
          {:db/ident :name
           :db/valueType :db.type/string
           :db/cardinality :db.cardinality/one}])

        (datahike.api/transact
         conn
         [{:id #uuid "3e7c14ce-5f00-4ac2-9822-68f7d5a60952"
           :name  "datahike"}
          {:id #uuid "f82dc4f3-59c1-492a-8578-6f01986cc4c2"
           :name  "clojure"}
          {:id #uuid "5358b384-3568-47f9-9a40-a9a306d75b12"
           :name  "Little-Rock"}])

        (->>
         (datahike.api/q '[:find ?e ?n
                           :where
                           [?e :name ?n]]
                         @conn)
         (println))

        (->>
         (datahike.api/q '[:find [?ident ...]
                           :where [_ :db/ident ?ident]]
                         @conn)
         (sort)
         (println))))

    (SwingUtilities/invokeLater
     (reify Runnable
       (run [_]

         (doto jframe
           (.add jroot-panel)
           (.addComponentListener (let []
                                    (reify ComponentListener
                                      (componentHidden [_ event])
                                      (componentMoved [_ event])
                                      (componentResized [_ event] (put! resize| (.getTime (java.util.Date.))))
                                      (componentShown [_ event]))))
           (.addWindowListener (proxy [WindowAdapter] []
                                 (windowClosing [event]
                                   (let [event ^WindowEvent event]
                                     #_(println :window-closing)
                                     #_(put! host| true)
                                     (-> event (.getWindow) (.dispose)))))))

         (doto jroot-panel
           #_(.setLayout (BoxLayout. jroot-panel BoxLayout/Y_AXIS))
           (.setLayout (MigLayout. "insets 10"
                                   "[grow,shrink,fill]"
                                   "[grow,shrink,fill]")))

         (when-let [url (clojure.java.io/resource "icon.png")]
           (.setIconImage jframe (.getImage (ImageIcon. url))))

         (menubar-process
          {:jmenubar jmenubar
           :jframe jframe
           :menubar| ops|})
         (.setJMenuBar jframe jmenubar)

         #_(Dr-Pershing.grapefruit/toolbar-process
            {:jtoolbar jtoolbar})
         #_(.add jroot-panel jtoolbar "dock north")


         (.setPreferredSize jframe
                            (let [size (-> (Toolkit/getDefaultToolkit) (.getScreenSize))]
                              (Dimension. (* 0.7 (.getWidth size)) (* 0.7 (.getHeight size)))
                              #_(Dimension. (UIScale/scale 1024) (UIScale/scale 576)))
                            #_(if SystemInfo/isJava_9_orLater
                                (Dimension. 830 440)
                                (Dimension. 1660 880)))

         #_(doto jframe
             (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
             (.setSize 2400 1600)
             (.setLocation 1300 200)
             #_(.add panel)
             (.setVisible true))

         #_(println :before (.getGraphics canvas))
         (doto jframe
           (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
           (.pack)
           (.setLocationRelativeTo nil)
           (.setVisible true))
         #_(println :after (.getGraphics canvas))

         (alter-var-root #'Dr-Pershing.main/jframe (constantly jframe))

         (remove-watch stateA :watch-fn)
         (add-watch stateA :watch-fn
                    (fn [ref wathc-key old-state new-state]

                      (when (not= old-state new-state))))

         (remove-watch settingsA :main)
         (add-watch settingsA :main
                    (fn [ref wathc-key old-state new-state]
                      (SwingUtilities/invokeLater
                       (reify Runnable
                         (run [_])))))
         (reset! settingsA @settingsA))))


    (go
      (loop []
        (when-let [value (<! ops|)]
          (condp = (:op value)

            :settings
            (let [settings-jframe (JFrame. "settings")]
              (settings-process
               {:jframe settings-jframe
                :root-jframe jframe
                :ops| ops|
                :settingsA settingsA})
              (reset! settingsA @settingsA))

            :settings-value
            (let []
              (swap! settingsA merge value)))

          (recur)))))
  (println "Kuiil has spoken"))


(comment

  (.getName (class (make-array Object 1 1)))

  (.getName (class (make-array String 1)))

  ;
  )